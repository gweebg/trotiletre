package org.trotiletre.client.stubs;

import org.trotiletre.common.INotificationManager;
import org.trotiletre.common.ManagerSkeletonTags;
import org.trotiletre.common.NotificationOperations;
import org.trotiletre.common.communication.TaggedConnection;
import org.trotiletre.models.utils.Location;

import java.io.*;

public class NotificationManagerStub implements INotificationManager {
    private final TaggedConnection connection;

    public NotificationManagerStub(TaggedConnection connection){
        this.connection = connection;
    }

    public void register(String user) throws IOException {
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        DataOutput dataOutput = new DataOutputStream(dataStream);

        dataOutput.writeInt(NotificationOperations.REGISTER.operationTag);
        dataOutput.writeUTF(user);
        connection.send(ManagerSkeletonTags.NOTIFICATION.tag, dataStream.toByteArray());
    }

    public boolean isRegistered(String user) throws IOException {
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        DataOutput dataOutput = new DataOutputStream(dataStream);

        dataOutput.writeInt(NotificationOperations.IS_REGISTERED.operationTag);
        dataOutput.writeUTF(user);
        connection.send(ManagerSkeletonTags.NOTIFICATION.tag, dataStream.toByteArray());

        TaggedConnection.Frame frame = connection.receive();

        if(frame.tag!=ManagerSkeletonTags.NOTIFICATION.tag)
            return false;

        DataInput dataInput = new DataInputStream(new ByteArrayInputStream(frame.data));

        return dataInput.readBoolean();
    }

    public void addLocation(String user, Location location, int radius) throws IOException {
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        DataOutput dataOutput = new DataOutputStream(dataStream);

        dataOutput.writeInt(NotificationOperations.ADD_LOCATION.operationTag);
        dataOutput.writeUTF(user);
        dataOutput.writeInt(location.x());
        dataOutput.writeInt(location.y());
        dataOutput.writeInt(radius);
    }

    public void remove(String user) throws IOException {
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        DataOutput dataOutput = new DataOutputStream(dataStream);

        dataOutput.writeInt(NotificationOperations.REMOVE.operationTag);
        dataOutput.writeUTF(user);
        connection.send(ManagerSkeletonTags.NOTIFICATION.tag, dataStream.toByteArray());
    }
}
