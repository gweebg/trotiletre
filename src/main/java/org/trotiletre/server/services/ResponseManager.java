package org.trotiletre.server.services;

import org.trotiletre.common.communication.TaggedConnection;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ResponseManager {

    private static class SenderInfo{
        public final SocketAddress address;
        public int resourceUsers;
        public final BlockingDeque<SenderData> dataQueue;

        public SenderInfo(SocketAddress address, BlockingDeque<SenderData> dataQueue){
            this.address = address;
            this.dataQueue = dataQueue;
            this.resourceUsers = 1;
        }
    }
    protected record SenderData(byte[] data, int tag, boolean stop){}
    private final Map<SocketAddress, SenderInfo> senderMap = new HashMap<>();
    private final Map<String, SocketAddress> userMap = new HashMap<>();
    private final Map<SocketAddress, String> socketMap = new HashMap<>();
    private final Lock mapLock = new ReentrantLock();
    private final ExecutorService executorService = Executors.newCachedThreadPool();


    public void register(Socket socket){
        mapLock.lock();
        try{
            SocketAddress socketAddress = socket.getRemoteSocketAddress();
            if(!this.senderMap.containsKey(socketAddress)){
                SenderInfo sdata = new SenderInfo(socketAddress, new LinkedBlockingDeque<>());
                this.senderMap.put(socketAddress, sdata);
                this.executorService.execute(new RespondingThread(sdata.dataQueue, socket));
            }
            else{
                SenderInfo sdata = this.senderMap.get(socketAddress);
                sdata.resourceUsers +=1;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            mapLock.unlock();
        }
    }

    public void registerUser(String user, SocketAddress socketAddress){
        mapLock.lock();
        try {
            if(!this.senderMap.containsKey(socketAddress) || this.userMap.containsKey(user))
                return;

            this.userMap.put(user, socketAddress);
            this.socketMap.put(socketAddress, user);

        } finally {
            mapLock.unlock();
        }
    }

    public void send(SocketAddress socketAddress, byte[] data, int tag){
        mapLock.lock();
        try {
            SenderInfo senderInfo = this.senderMap.get(socketAddress);
            if(senderInfo==null)
                return;

            senderInfo.dataQueue.add(new SenderData(data, tag, false));
        }
        finally {
            mapLock.unlock();
        }
    }

    public void send(String user, byte[] data, int tag){
        mapLock.lock();
        try {
            SocketAddress socketAddress = this.userMap.get(user);
            if(socketAddress==null)
                return;

            this.send(socketAddress, data, tag);
        } finally {
            mapLock.unlock();
        }
    }

    public void remove(SocketAddress socketAddress){
        mapLock.lock();
        try{
            if(!this.senderMap.containsKey(socketAddress))
                return;

            SenderInfo senderInfo = this.senderMap.get(socketAddress);
            senderInfo.resourceUsers--;
            if(senderInfo.resourceUsers==0){
                senderInfo.dataQueue.add(new SenderData(null, -1, true));
                this.senderMap.remove(socketAddress);
                String user = this.socketMap.remove(socketAddress);
                if(user!=null){
                    this.userMap.remove(user);
                }
            }
        }
        finally {
            mapLock.unlock();
        }
    }

}

class RespondingThread implements Runnable{

    private final BlockingDeque<ResponseManager.SenderData> dataQueue;
    private final TaggedConnection taggedConnection;
    public RespondingThread(BlockingDeque<ResponseManager.SenderData> dataQueue, Socket socket) throws IOException {
        this.dataQueue = dataQueue;
        this.taggedConnection = new TaggedConnection(socket);
    }

    @Override
    public void run() {
        while(true){
            ResponseManager.SenderData senderData;
            try{
                senderData = this.dataQueue.take();
            } catch (InterruptedException e){
                e.printStackTrace();
                return;
            }
            if(senderData.stop())
                return;

            try {
                this.taggedConnection.send(senderData.tag(), senderData.data());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
