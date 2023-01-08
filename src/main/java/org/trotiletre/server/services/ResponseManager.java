package org.trotiletre.server.services;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ResponseManager {

    record SenderInfo(String user, Socket socket, BlockingDeque<SenderData> dataQueue){}
    record SenderData(byte[] data, boolean stop){}
    private final Map<String, SenderInfo> senderMap = new HashMap<>();
    private final Lock mapLock = new ReentrantLock();
    private final ExecutorService executorService = Executors.newCachedThreadPool();


    public void registerUser(String user, Socket socket){
        mapLock.lock();
        try{
            if(!this.senderMap.containsKey(user)){
                SenderInfo sdata = new SenderInfo(user, socket, new LinkedBlockingDeque<>());
                this.senderMap.put(user, sdata);
                this.executorService.execute(new RespondingThread(sdata));
            }
        }
        finally {
            mapLock.unlock();
        }
    }

    public void send(String user, byte[] data){
        mapLock.lock();
        try {
            SenderInfo senderInfo = this.senderMap.get(user);
            if(senderInfo==null)
                return;

            senderInfo.dataQueue.add(new SenderData(data, false));
        }
        finally {
            mapLock.unlock();
        }
    }

    public void remove(String user){
        mapLock.lock();
        try{
            if(!this.senderMap.containsKey(user))
                return;

            this.senderMap.get(user).dataQueue.add(new SenderData(null, true));
            this.senderMap.remove(user);
        }
        finally {
            mapLock.unlock();
        }
    }
}

class RespondingThread implements Runnable{

    private final ResponseManager.SenderInfo senderInfo;
    public RespondingThread(ResponseManager.SenderInfo senderInfo){
        this.senderInfo = senderInfo;
    }

    @Override
    public void run() { // TODO

    }
}
