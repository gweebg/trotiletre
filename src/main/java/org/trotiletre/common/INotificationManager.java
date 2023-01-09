package org.trotiletre.common;

import org.trotiletre.models.utils.Location;

import java.io.IOException;

public interface INotificationManager {
    void register(String user) throws IOException;
    boolean isRegistered(String user) throws IOException;
    void addLocation(String user, Location location, int radius) throws IOException;
    void remove(String user) throws IOException;
}
