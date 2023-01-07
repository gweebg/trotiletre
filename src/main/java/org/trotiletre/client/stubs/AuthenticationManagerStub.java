package org.trotiletre.client.stubs;

import org.trotiletre.common.IAuthenticationManager;
import org.trotiletre.models.User;

import java.io.*;
import java.net.Socket;

public class AuthenticationManagerStub implements IAuthenticationManager {

    private Socket socket = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;

    public AuthenticationManagerStub() {

        try {

            this.socket = new Socket("localhost", 20022);
            this.in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            this.out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

        } catch (IOException e) {

            System.out.println("(SooterManagerStub.java) Could not create socket.");
            System.exit(1);
        }
    }

    /**
     * Registers a new user with the given username and password.
     * <p>
     * This method sends a request to the authentication manager service to register the user.
     * The provided password comes in plain text, only when sent to the server it is encrypted.
     *
     * @param username The username of the new user.
     * @param password The password of the new user.
     * @throws IOException If an error occurs while sending the request.
     */
    @Override
    public void registerUser(String username, String password) throws IOException {

        User user = new User(username);
        user.setPassword(password);

        out.writeInt(1); // Service 1: AuthenticationManager
        out.writeInt(0); // Operation 0: Register
        out.writeUTF(username); // Send the username.
        out.writeUTF(user.getPasswordHash()); // Send the password hash.

        out.flush();

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
    public void loginUser(String username, String password) throws IOException {

        out.writeInt(1);
        out.writeInt(1);
        out.writeUTF(username);
        out.writeUTF(password);

        out.flush();

    }


}
