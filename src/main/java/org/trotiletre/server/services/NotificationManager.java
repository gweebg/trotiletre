package org.trotiletre.server.services;

import org.trotiletre.common.ManagerSkeletonTags;

import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NotificationManager {
    private final Map<String, SocketAddress> userConnMap = new HashMap<>();
    private final Lock lock = new ReentrantLock();
    private final ResponseManager responseManager;

    public NotificationManager(ResponseManager responseManager){
        this.responseManager = responseManager;
    }

    public void register(String user, Socket socket){
        lock.lock();
        try{
            this.userConnMap.put(user, socket.getRemoteSocketAddress());
        }finally {
            lock.unlock();
        }
        this.responseManager.register(socket);
    }

    public boolean isRegistered(String user){
        lock.lock();
        try {
            return this.userConnMap.containsKey(user);
        }finally {
            lock.unlock();
        }
    }

    public void send(String user, byte[] data){
        SocketAddress socketAddress;
        lock.lock();
        try {
            socketAddress = this.userConnMap.get(user);
            if(socketAddress==null)
                return;
        } finally {
            lock.unlock();
        }
        responseManager.send(socketAddress, data, ManagerSkeletonTags.NOTIFICATION.tag);
    }

    public void remove(String user){
        lock.lock();
        try{
            if(!this.userConnMap.containsKey(user))
                return;

            this.responseManager.remove(this.userConnMap.remove(user));

        } finally {
            lock.unlock();
        }
    }

    public Set<String> getUserSet(){
        lock.lock();
        try {
            return this.userConnMap.keySet();
        }finally {
            lock.unlock();
        }
    }

}
