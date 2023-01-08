package org.trotiletre.server;

import org.trotiletre.server.services.ResponseManager;
import org.trotiletre.server.services.ScooterManager;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RewardThread implements Runnable{
    private final Lock workLock = new ReentrantLock();
    private final Condition workCond = workLock.newCondition();
    private boolean hasWork = false;
    private final ResponseManager responseManager;
    private final ScooterManager scooterManager;

    public RewardThread(ResponseManager responseManager, ScooterManager scooterManager){
        this.responseManager = responseManager;
        this.scooterManager = scooterManager;
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
}
