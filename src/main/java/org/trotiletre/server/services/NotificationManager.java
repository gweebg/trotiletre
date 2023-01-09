package org.trotiletre.server.services;

import org.trotiletre.common.INotificationManager;
import org.trotiletre.models.utils.Location;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NotificationManager implements INotificationManager {
    public record LocationData(Location location, int radius){}
    private final Map<String, Set<LocationData>> userMap = new HashMap<>();
    private final Lock lock = new ReentrantLock();

    public void register(String user){
        lock.lock();
        try{
            if(this.userMap.containsKey(user))
                return;
            this.userMap.put(user, new HashSet<>());

        }finally {
            lock.unlock();
        }
    }

    public boolean isRegistered(String user){
        lock.lock();
        try {
            return this.userMap.containsKey(user);
        }finally {
            lock.unlock();
        }
    }

    public void addLocation(String user, Location location, int radius){
        lock.lock();
        try {
            Set<LocationData> locationDataSet = this.userMap.get(user);
            if(locationDataSet==null)
                return;

            locationDataSet.add(new LocationData(location, radius));
        } finally {
            lock.unlock();
        }
    }

    public void remove(String user){
        lock.lock();
        try{
            if(!this.userMap.containsKey(user))
                return;

            this.userMap.remove(user);

        } finally {
            lock.unlock();
        }
    }

    public Set<String> getUserSet(){
        lock.lock();
        try {
            return new HashSet<>(this.userMap.keySet());
        }finally {
            lock.unlock();
        }
    }

    public Set<NotificationManager.LocationData> getUserLocationSet(String user){
        lock.lock();
        try {
            Set<LocationData> userLocationDataSet = this.userMap.get(user);
            if(userLocationDataSet==null)
                return new HashSet<>();

            return new HashSet<>(userLocationDataSet);
        } finally {
            lock.unlock();
        }
    }

}
