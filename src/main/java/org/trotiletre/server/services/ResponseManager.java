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

    static class SenderInfo{
        public final SocketAddress address;
        public int users;
        public final BlockingDeque<SenderData> dataQueue;

        public SenderInfo(SocketAddress address, BlockingDeque<SenderData> dataQueue){
            this.address = address;
            this.dataQueue = dataQueue;
            this.users = 1;
        }
    }
    record SenderData(byte[] data, int tag, boolean stop){}
    private final Map<SocketAddress, SenderInfo> senderMap = new HashMap<>();
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
                sdata.users+=1;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
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

    public void remove(SocketAddress socketAddress){
        mapLock.lock();
        try{
            if(!this.senderMap.containsKey(socketAddress))
                return;

            SenderInfo senderInfo = this.senderMap.get(socketAddress);
            senderInfo.users--;
            if(senderInfo.users==0){
                senderInfo.dataQueue.add(new SenderData(null, -1, true));
                this.senderMap.remove(socketAddress);
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
