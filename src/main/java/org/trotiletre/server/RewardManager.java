package org.trotiletre.server;

import org.trotiletre.common.ManagerSkeletonTags;
import org.trotiletre.models.utils.Location;
import org.trotiletre.server.services.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RewardManager {
    public record RewardPath(Location start, Location finish, int reward){}
    private static class WorkSignaller{
        private final Lock workLock = new ReentrantLock();
        private final Condition workCond = workLock.newCondition();
        private boolean hasWork = false;

        public void signal(){
            this.workLock.lock();
            this.hasWork = true;
            this.workCond.signal();
            this.workLock.unlock();
        }

        public void await() throws InterruptedException {
            this.workLock.lock();
            while(!this.hasWork)
                this.workCond.await();
            this.hasWork = false;
            this.workLock.unlock();
        }
    }

    private class RewardThread implements Runnable{
        public void run(){
            ScooterMap scooterMap = scooterManager.getScooterMap();
            while (true){
                try{
                    workSignaller.await();
                }catch (InterruptedException e){
                    e.printStackTrace();
                    return;
                }
                rewardPathLock.lock();

                rewardPathMap.clear();


                for(Map.Entry<Location, Set<Location>> entry : scooterMap.getRewardPaths(defaultRadius).entrySet()){
                    List<RewardPath> rewardPathList = new ArrayList<>();
                    for(Location finish : entry.getValue()){
                        rewardPathList.add(new RewardPath(entry.getKey(), finish, defaultReward));
                    }
                    rewardPathMap.put(entry.getKey(), rewardPathList);
                }

                Map<String, List<RewardPath>> notifUsers = new HashMap<>();

                for(String user : notificationManager.getUserSet()){
                    List<RewardPath> rewardPathList = new ArrayList<>();

                    for(NotificationManager.LocationData locationData : notificationManager.getUserLocationSet(user)){
                        for(RewardPath rewardPath : rewardPathMap.get(locationData.location())){
                            if(rewardPath.start.manhattanDistance(rewardPath.finish)<=locationData.radius()){
                                rewardPathList.add(rewardPath);
                            }
                        }
                    }
                    notifUsers.put(user, rewardPathList);
                }

                for(Map.Entry<String, List<RewardPath>> entry : notifUsers.entrySet()){
                    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                    DataOutput dataOutput = new DataOutputStream(byteStream);
                    List<RewardPath> rewardPathList = entry.getValue();
                    try{
                        dataOutput.writeInt(rewardPathList.size());
                        for(RewardPath rewardPath : rewardPathList){
                            dataOutput.writeInt(rewardPath.start.x());
                            dataOutput.writeInt(rewardPath.start.y());
                            dataOutput.writeInt(rewardPath.finish.x());
                            dataOutput.writeInt(rewardPath.finish.y());
                            dataOutput.writeInt(rewardPath.reward);
                        }
                    } catch (IOException ignored) {

                    }

                    responseManager.send(entry.getKey(), byteStream.toByteArray(), ManagerSkeletonTags.NOTIFICATION.tag);
                }

            }
        }
    }

    private final WorkSignaller workSignaller = new WorkSignaller();
    private final NotificationManager notificationManager;
    private final ScooterManager scooterManager;
    private final AuthenticationManager authenticationManager;
    private final ResponseManager responseManager;
    private final int defaultRadius;
    private final int defaultReward = 10;
    private final Map<Location, List<RewardPath>> rewardPathMap = new HashMap<>();
    private final Lock rewardPathLock = new ReentrantLock();

    public RewardManager(ResponseManager responseManager, NotificationManager notificationManager, ScooterManager scooterManager,
                         AuthenticationManager authenticationManager, int defaultRadius){
        this.notificationManager = notificationManager;
        this.scooterManager = scooterManager;
        this.authenticationManager = authenticationManager;
        this.defaultRadius = defaultRadius;
        this.responseManager = responseManager;
        new Thread(new RewardThread()).start();
    }


    public void signal(){
        this.workSignaller.signal();
    }

    public List<RewardPath> getRewardPaths(Location start, int radius){
        rewardPathLock.lock();
        try {
            List<RewardPath> rewardPathList = new ArrayList<>();
            List<RewardPath> mapRewardPathList = this.rewardPathMap.get(start);
            if(mapRewardPathList==null)
                return rewardPathList;

            for(RewardPath rewardPath : mapRewardPathList){
                if(start.manhattanDistance(rewardPath.finish)<=radius)
                    rewardPathList.add(rewardPath);
            }

            return rewardPathList;
        } finally {
            rewardPathLock.unlock();
        }
    }
}
