package org.trotiletre.server.skeletons;

import org.trotiletre.common.communication.Skeleton;
import org.trotiletre.server.services.ResponseManager;

public class NotificationManagerSkeleton implements Skeleton {
    private ResponseManager responseManager;

    public NotificationManagerSkeleton(ResponseManager responseManager){
        this.responseManager = responseManager;
    }

    @Override
    public void handle(byte[] receivedData) throws Exception {

    }
}
