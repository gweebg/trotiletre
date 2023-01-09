package org.trotiletre.server.skeletons;

import org.trotiletre.common.ManagerSkeletonTags;
import org.trotiletre.common.NotificationOperations;
import org.trotiletre.common.communication.Skeleton;
import org.trotiletre.common.communication.TaggedConnection;
import org.trotiletre.models.utils.Location;
import org.trotiletre.server.services.NotificationManager;
import org.trotiletre.server.services.ResponseManager;

import java.io.*;
import java.net.SocketAddress;

public class NotificationManagerSkeleton implements Skeleton {
    private final NotificationManager notificationManager;
    private final ResponseManager responseManager;

    public NotificationManagerSkeleton(NotificationManager notificationManager, ResponseManager responseManager){
        this.notificationManager = notificationManager;
        this.responseManager = responseManager;
    }

    @Override
    public void handle(byte[] receivedData, SocketAddress socketAddress) throws Exception {
        DataInput dataInput = new DataInputStream(new ByteArrayInputStream(receivedData));

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutput dataOutput = new DataOutputStream(byteStream);

        NotificationOperations op = NotificationOperations.fromInt(dataInput.readInt());

        switch (op){
            case REGISTER -> {
                String user = dataInput.readUTF();
                this.notificationManager.register(user);
            }
            case IS_REGISTERED -> {
                String user = dataInput.readUTF();
                dataOutput.writeBoolean(this.notificationManager.isRegistered(user));
                this.responseManager.send(socketAddress, byteStream.toByteArray(), ManagerSkeletonTags.NOTIFICATION.tag);
            }
            case ADD_LOCATION -> {
                String user = dataInput.readUTF();
                Location location = new Location(
                        dataInput.readInt(),
                        dataInput.readInt()
                );
                int radius = dataInput.readInt();
                this.notificationManager.addLocation(user, location, radius);
            }
            case REMOVE -> {
                String user = dataInput.readUTF();
                this.notificationManager.remove(user);
            }
        }
    }
}
