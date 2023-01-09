package org.trotiletre.server.services;

import org.trotiletre.common.ManagerTags;
import org.trotiletre.models.utils.Location;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RewardManager {
    public record RewardPath(Location start, Location finish, double reward){}
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
                            dataOutput.writeDouble(rewardPath.reward);
                        }
                    } catch (IOException ignored) {

                    }

                    responseManager.send(entry.getKey(), byteStream.toByteArray(), ManagerTags.NOTIFICATION.tag);
                }

            }
        }
    }

    private final WorkSignaller workSignaller = new WorkSignaller();
    private final NotificationManager notificationManager;
    private final ScooterMap scooterMap;
    private final AuthenticationManager authenticationManager;
    private final ResponseManager responseManager;
    private final int defaultRadius;
    private final double defaultReward = 10;
    private final Map<Location, List<RewardPath>> rewardPathMap = new HashMap<>();
    private final Lock rewardPathLock = new ReentrantLock();

    public RewardManager(ResponseManager responseManager, NotificationManager notificationManager, ScooterMap scooterMap,
                         AuthenticationManager authenticationManager, int defaultRadius){
        this.notificationManager = notificationManager;
        this.authenticationManager = authenticationManager;
        this.defaultRadius = defaultRadius;
        this.responseManager = responseManager;
        this.scooterMap = scooterMap;
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

    public Optional<Double> getReward(Location start, Location finish){
        rewardPathLock.lock();
        try {
            Optional<Double> optReward = Optional.empty();

            List<RewardPath> rewardPathList = this.rewardPathMap.get(start);
            if(rewardPathList==null)
                return optReward;

            for(var iter = rewardPathList.iterator(); iter.hasNext();){
                RewardPath rewardPath = iter.next();
                if(rewardPath.finish.equals(finish)){
                    optReward = Optional.of(rewardPath.reward);
                    iter.remove();
                    break;
                }
            }

            if(rewardPathList.size()==0){
                this.rewardPathMap.remove(start);
            }

            return optReward;
        } finally {
            rewardPathLock.unlock();
        }
    }
}
