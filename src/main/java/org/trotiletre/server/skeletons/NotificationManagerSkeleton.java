package org.trotiletre.server.skeletons;

import org.trotiletre.common.communication.Skeleton;
import org.trotiletre.common.communication.TaggedConnection;
import org.trotiletre.server.services.NotificationManager;
import org.trotiletre.server.services.ResponseManager;

import java.net.SocketAddress;

public class NotificationManagerSkeleton implements Skeleton {
    private NotificationManager notificationManager;

    public NotificationManagerSkeleton(NotificationManager notificationManager){
        this.notificationManager = notificationManager;
    }

    @Override
    public void handle(byte[] receivedData, SocketAddress socketAddress) throws Exception {
        // TODO
    }
}
