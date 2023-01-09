package org.trotiletre.client.stubs;

import org.jetbrains.annotations.NotNull;
import org.trotiletre.common.IScooterManager;
import org.trotiletre.common.ManagerTags;
import org.trotiletre.common.communication.Demultiplexer;
import org.trotiletre.common.communication.TaggedConnection;
import org.trotiletre.models.utils.GenericPair;
import org.trotiletre.models.utils.Location;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * A stub implementation of the {@link IScooterManager} interface.
 */
public class ScooterManagerStub implements IScooterManager {

    private TaggedConnection connection; // The connection to the client.
    private Demultiplexer demultiplexer; // Application demultiplexer, used to delegate different tags to different threads.

    /**
     * Constructs a new {@code ScooterManagerStub} object.
     *
     * @param connection a {@link TaggedConnection} object representing the connection to the client.
     */
    public ScooterManagerStub(TaggedConnection connection, Demultiplexer demultiplexer) {
            this.connection = connection;
            this.demultiplexer = demultiplexer;
    }

    @Override
    public @NotNull String listFreeScooters(int range, @NotNull Location lookupPosition) throws IOException, InterruptedException {

        /*
         * This section handles the requests for listing the free scooters.
         * The message we are expecting to receive will have:
         *  + x coordinate of the user's position: Integer.
         *  + y coordinate of the user's position: Integer.
         *  + The range of the search for scooters: Integer.
         */

        // Creating our own byte stream, so we can send it via tagged connection.
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        DataOutput dataOutput = new DataOutputStream(dataStream);

        dataOutput.writeInt(0); // Writing the operation we want to use.

        dataOutput.writeInt(lookupPosition.x()); // Writing the x inital position.
        dataOutput.writeInt(lookupPosition.y()); // Writing the y inital position.
        dataOutput.writeInt(range); // Writing the range of search.

        // Converting the ByteArrayOutputStream into a primitive byte[].
        byte[] data = dataStream.toByteArray();
        connection.send(ManagerTags.SCOOTER.tag, data); // Sending the message to the server.

        // The response is a byte encoded string with all the locations. This means we just need to convert it back.
        // TaggedConnection.Frame frame = connection.receive();
        //return new String(frame.data, StandardCharsets.UTF_8); // Return the converted string from the bytes received.

        byte[] receivedData = demultiplexer.receive(ManagerTags.SCOOTER.tag);
        return new String(receivedData, StandardCharsets.UTF_8);
    }

    @Override
    public GenericPair<String, Location> reserveScooter(int range, @NotNull Location local, String username) throws IOException, InterruptedException {

        /*
         * This section handles the requests for renting a free scooters.
         * The message we are expecting to receive will have:
         *  + The range of the search for scooters: Integer.
         *  + x coordinate of the user's position: Integer.
         *  + y coordinate of the user's position: Integer.
         *  + The user's username, used to check whether he is authenticated.
         *
         *  This operation requires authentication.
         */

        // Creating our own byte stream, so we can send it via tagged connection.
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        DataOutput dataOutput = new DataOutputStream(dataStream);

        dataOutput.writeInt(1); // Writing the operation we want to use.

        dataOutput.writeInt(range); // Writing the range of search.
        dataOutput.writeInt(local.x()); // Writing the x inital position.
        dataOutput.writeInt(local.y()); // Writing the y inital position.
        dataOutput.writeUTF(username); // Writing the username for authentication.

        connection.send(ManagerTags.SCOOTER.tag, dataStream.toByteArray()); // Sending the message to the server.

        // Receiving the response from the server as a frame.
        // TaggedConnection.Frame frame = connection.receive();
        byte[] receivedData = demultiplexer.receive(ManagerTags.SCOOTER.tag);

        // Unwrapping the bytes received in data into a stream of bytes.
        ByteArrayInputStream responseStream = new ByteArrayInputStream(receivedData);
        DataInput response = new DataInputStream(responseStream);

        /*
         * The response code determines the output of the request made.
         * The response code can be:
         *   0 - Normal behaviour;
         *   1 - There are no scooters available;
         *   2 - User is not logged in.
         */
        int responseCode = response.readInt();

        if (responseCode == 1) return new GenericPair<>("There were no scooters found in the area.", null);

        else if (responseCode == 2) return new GenericPair<>("You need to log in before renting a scooter!", null);

        else {

            String reservationCode = response.readUTF();
            int locationX = response.readInt();
            int locationY = response.readInt();

            return new GenericPair<>(reservationCode, new Location(locationX, locationY));
        }
    }

    @Override
    public GenericPair<Double, Double> parkScooter(String reservationCode, Location newScooterLocation, String username) throws IOException, InterruptedException {

        /*
         * This section handles the requests for parking a scooter.
         * The message we are expecting to receive will have:
         *  + The reservation identification of the scooter: UTF.
         *  + x coordinate of the new position: Integer.
         *  + y coordinate of the new position: Integer.
         *  + The user's username, used to check whether he is authenticated.
         *
         *  This operation requires authentication.
         */

        // Creating our own byte stream, so we can send it via tagged connection.
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        DataOutput dataOutput = new DataOutputStream(dataStream);

        dataOutput.writeInt(2); // Writing the operation we want to use.

        dataOutput.writeUTF(reservationCode); // Writing the range of search.
        dataOutput.writeInt(newScooterLocation.x()); // Writing the x of the new position.
        dataOutput.writeInt(newScooterLocation.y()); // Writing the y of the new position.
        dataOutput.writeUTF(username); // Writing the username for authentication.

        connection.send(ManagerTags.SCOOTER.tag, dataStream.toByteArray()); // Sending the message to the server.

        // Receiving the response from the server as a frame.
        // TaggedConnection.Frame frame = connection.receive();
        byte[] receivedData = demultiplexer.receive(ManagerTags.SCOOTER.tag);

        // Unwrapping the bytes received in data into a stream of bytes.
        ByteArrayInputStream responseStream = new ByteArrayInputStream(receivedData);
        DataInput response = new DataInputStream(responseStream);

        /*
         * The response code can be:
         *   0 - Normal behaviour;
         *   1 - Reservation ID is not valid;
         *   2 - User is not logged in.
         *   3 - Packet includes a bounty price.
         */
        int responseCode = response.readInt();

        if (responseCode == 1) return new GenericPair<>(-1d, null);

        else if (responseCode == 2) return new GenericPair<>(-2d, null);

        else {

            double priceToPay = response.readDouble();
            Double bounty = null;

            if (responseCode == 3) bounty = response.readDouble();
            return new GenericPair<>(priceToPay, bounty);
        }
    }
}
