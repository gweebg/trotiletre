package org.trotiletre.server;

import org.trotiletre.models.utils.Location;
import org.trotiletre.server.services.AuthenticationManager;
import org.trotiletre.server.services.NotificationManager;
import org.trotiletre.server.services.ResponseManager;
import org.trotiletre.server.services.ScooterManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RewardThread implements Runnable{
    public record RewardPath(Location start, Location finish, int reward){}
    private final Lock workLock = new ReentrantLock();
    private final Condition workCond = workLock.newCondition();
    private boolean hasWork = false;
    private final NotificationManager notificationManager;
    private final ScooterManager scooterManager;
    private final AuthenticationManager authenticationManager;
    private final ResponseManager responseManager;
    private final int defaultRadius;
    private final Map<Location, List<RewardPath>> rewardPathMap = new HashMap<>();
    private final Lock rewardPathLock = new ReentrantLock();

    public RewardThread(ResponseManager responseManager, NotificationManager notificationManager, ScooterManager scooterManager,
                        AuthenticationManager authenticationManager, int defaultRadius){
        this.notificationManager = notificationManager;
        this.scooterManager = scooterManager;
        this.authenticationManager = authenticationManager;
        this.defaultRadius = defaultRadius;
        this.responseManager = responseManager;
    }

    public void run(){
        while (true){
            try{
                workLock.lock();
                while (!hasWork)
                    workCond.await();
            }catch (InterruptedException e){
                e.printStackTrace();
                return;
            }
            finally {
                workLock.unlock();
            }

            // TODO gerar recompensas e enviar notifs


        }
    }

    public void signal(){
        workLock.lock();
        hasWork = true;
        workCond.signal();
        workLock.unlock();
    }

    // TODO

    public void consumeReward(RewardPath rewardPath){

    }

    public List<RewardPath> getRewardPaths(Location start){
        return null;
    }

    public List<RewardPath> getRewardPaths(Location start, int radius){
        return null;
    }
}
