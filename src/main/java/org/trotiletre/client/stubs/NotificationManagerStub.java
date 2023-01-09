package org.trotiletre.client.stubs;

import org.trotiletre.common.INotificationManager;
import org.trotiletre.common.ManagerTags;
import org.trotiletre.common.NotificationOperations;
import org.trotiletre.common.communication.Demultiplexer;
import org.trotiletre.common.communication.TaggedConnection;
import org.trotiletre.models.utils.Location;

import java.io.*;

public class NotificationManagerStub implements INotificationManager {
    private final TaggedConnection connection;
    private final Demultiplexer demultiplexer;

    public NotificationManagerStub(TaggedConnection connection, Demultiplexer demultiplexer){
        this.connection = connection;
        this.demultiplexer = demultiplexer;
    }

    public boolean register(String user) throws IOException, InterruptedException {
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        DataOutput dataOutput = new DataOutputStream(dataStream);


        dataOutput.writeInt(NotificationOperations.REGISTER.operationTag);
        dataOutput.writeUTF(user);
        connection.send(ManagerTags.NOTIFICATION.tag, dataStream.toByteArray());

        DataInput dataInput = new DataInputStream(new ByteArrayInputStream(demultiplexer.receive(ManagerTags.NOTIFICATION.tag)));
        return dataInput.readBoolean();
    }

    public boolean isRegistered(String user) throws IOException, InterruptedException {
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        DataOutput dataOutput = new DataOutputStream(dataStream);

        dataOutput.writeInt(NotificationOperations.IS_REGISTERED.operationTag);
        dataOutput.writeUTF(user);
        connection.send(ManagerTags.NOTIFICATION.tag, dataStream.toByteArray());

        DataInput dataInput = new DataInputStream(new ByteArrayInputStream(demultiplexer.receive(ManagerTags.NOTIFICATION.tag)));
        return dataInput.readBoolean();
    }

    public boolean addLocation(String user, Location location, int radius) throws IOException, InterruptedException {
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        DataOutput dataOutput = new DataOutputStream(dataStream);

        dataOutput.writeInt(NotificationOperations.ADD_LOCATION.operationTag);
        dataOutput.writeUTF(user);
        dataOutput.writeInt(location.x());
        dataOutput.writeInt(location.y());
        dataOutput.writeInt(radius);

        connection.send(ManagerTags.NOTIFICATION.tag, dataStream.toByteArray());

        DataInput dataInput = new DataInputStream(new ByteArrayInputStream(demultiplexer.receive(ManagerTags.NOTIFICATION.tag)));
        return dataInput.readBoolean();
    }

    public boolean remove(String user) throws IOException, InterruptedException {
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        DataOutput dataOutput = new DataOutputStream(dataStream);

        dataOutput.writeInt(NotificationOperations.REMOVE.operationTag);
        dataOutput.writeUTF(user);
        connection.send(ManagerTags.NOTIFICATION.tag, dataStream.toByteArray());

        DataInput dataInput = new DataInputStream(new ByteArrayInputStream(demultiplexer.receive(ManagerTags.NOTIFICATION.tag)));
        return dataInput.readBoolean();
    }
}
