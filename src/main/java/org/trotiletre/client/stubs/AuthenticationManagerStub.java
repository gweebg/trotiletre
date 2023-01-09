package org.trotiletre.client.stubs;

import org.trotiletre.common.IAuthenticationManager;
import org.trotiletre.common.communication.Demultiplexer;
import org.trotiletre.common.communication.TaggedConnection;
import org.trotiletre.models.User;

import java.io.*;

public class AuthenticationManagerStub implements IAuthenticationManager {

    private TaggedConnection connection; // The connection to the client.
    private Demultiplexer demultiplexer; // Application demultiplexer, used to delegate different tags to different threads.


    public AuthenticationManagerStub(TaggedConnection connection, Demultiplexer demultiplexer) {
        this.connection = connection;
        this.demultiplexer = demultiplexer;
    }

    /**
     * Registers a new user with the given username and password.
     * <p>
     * This method sends a request to the server to register a new user with the
     * provided username and password.
     *
     * @param username The username of the new user.
     * @param password The password of the new user.
     * @throws IOException If an error occurs while sending the request.
     */
    @Override
    public boolean registerUser(String username, String password) throws IOException, InterruptedException {

        // Creating the user object and hashing the password to be stored.
        User user = new User(username);
        user.setPassword(password);

        // Since the tagged connection takes a byte[] a parameter, we write to a stream
        // and then convert it into the byte[].
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        DataOutput dataOutput = new DataOutputStream(dataStream);

        dataOutput.writeInt(0); // Writing the operation we want to use.
        dataOutput.writeUTF(username); // Writing the username.
        dataOutput.writeUTF(user.getPasswordHash()); // Writing the password hash.

        // Converting the ByteArrayOutputStream into a primitive byte[].
        byte[] data = dataStream.toByteArray();

        connection.send(1, data); // Sending the message to the server.

        // Waiting for a response from the server.
        byte[] receivedData = demultiplexer.receive(0);

        // Unwrapping the bytes received in data into a stream of bytes.
        ByteArrayInputStream responseStream = new ByteArrayInputStream(receivedData);
        DataInput response = new DataInputStream(responseStream);

        return response.readBoolean();
    }

    /**
     * Login an existing user with the given username and password.
     * <p>
     * This method sends a request to the authentication manager service to log in the user.
     * The provided password comes in plain text, only when sent to the server it is encrypted.
     *
     * @param username The username of the new user.
     * @param password The password of the new user.
     * @throws IOException If an error occurs while sending the request.
     */
    @Override
    public boolean loginUser(String username, String password) throws IOException, InterruptedException {

        // Since the tagged connection takes a byte[] a parameter, we write to a stream
        // and then convert it into the byte[].
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        DataOutput dataOutput = new DataOutputStream(dataStream);

        dataOutput.writeInt(1); // Writing the operation we want to use.
        dataOutput.writeUTF(username); // Writing the username.
        dataOutput.writeUTF(password); // Writing the password hash.

        // Converting the ByteArrayOutputStream into a primitive byte[].
        byte[] data = dataStream.toByteArray();

        connection.send(1, data); // Sending the message to the server.

        // Waiting for a response from the server.
        byte[] receivedData = demultiplexer.receive(0);

        // Unwrapping the bytes received in data into a stream of bytes.
        ByteArrayInputStream responseStream = new ByteArrayInputStream(receivedData);
        DataInput response = new DataInputStream(responseStream);

        return response.readBoolean();
    }
}
