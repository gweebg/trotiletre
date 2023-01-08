package org.trotiletre.client.stubs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.trotiletre.common.IScooterManager;
import org.trotiletre.common.communication.TaggedConnection;
import org.trotiletre.models.User;
import org.trotiletre.models.utils.Location;

import javax.swing.text.html.HTML;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class ScooterManagerStub implements IScooterManager {

    private TaggedConnection connection;

    public ScooterManagerStub(TaggedConnection connection) {
            this.connection = connection;
    }

    @Override
    public @NotNull String listFreeScooters(int range, @NotNull Location lookupPosition) throws IOException {

        // Since the tagged connection takes a byte[] a parameter, we write to a stream
        // and then convert it into the byte[].
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        DataOutput dataOutput = new DataOutputStream(dataStream);

        dataOutput.writeInt(0); // Writing the operation we want to use.
        dataOutput.writeInt(range); // Writing the range of search.
        dataOutput.writeInt(lookupPosition.x()); // Writing the x inital position.
        dataOutput.writeInt(lookupPosition.y()); // Writing the y inital position.

        // Converting the ByteArrayOutputStream into a primitive byte[].
        byte[] data = dataStream.toByteArray();

        connection.send(0, data); // Sending the message to the server.
        TaggedConnection.Frame frame = connection.receive();

        System.out.println("Tag: " + frame.tag);
        System.out.println("Data: " + Arrays.toString(frame.data));

        return Arrays.toString(frame.data);
    }

    @Override
    public @Nullable String reserveScooter(int range, @NotNull Location local, String username) {
        return null;
    }

    @Override
    public double parkScooter(String reservationCode, Location newScooterLocation, String username) {
        return 0;
    }

    /* Implement methods, move scooter, park, bounties, login, etc. */
}
