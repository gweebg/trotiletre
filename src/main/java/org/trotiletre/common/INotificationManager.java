package org.trotiletre.common;

import org.trotiletre.models.utils.Location;

import java.io.IOException;

public interface INotificationManager {
    boolean register(String user) throws IOException, InterruptedException;
    boolean isRegistered(String user) throws IOException, InterruptedException;
    boolean addLocation(String user, Location location, int radius) throws IOException, InterruptedException;
    boolean remove(String user) throws IOException, InterruptedException;
}
