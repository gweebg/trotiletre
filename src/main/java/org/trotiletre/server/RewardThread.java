package org.trotiletre.server;

import org.trotiletre.server.services.AuthenticationManager;
import org.trotiletre.server.services.NotificationManager;
import org.trotiletre.server.services.ResponseManager;
import org.trotiletre.server.services.ScooterManager;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RewardThread implements Runnable{
    private final Lock workLock = new ReentrantLock();
    private final Condition workCond = workLock.newCondition();
    private boolean hasWork = false;
    private final NotificationManager notificationManager;
    private final ScooterManager scooterManager;
    private final AuthenticationManager authenticationManager;

    public RewardThread(NotificationManager notificationManager, ScooterManager scooterManager,
                        AuthenticationManager authenticationManager){
        this.notificationManager = notificationManager;
        this.scooterManager = scooterManager;
        this.authenticationManager = authenticationManager;
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
