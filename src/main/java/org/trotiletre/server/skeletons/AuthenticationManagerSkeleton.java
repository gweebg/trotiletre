package org.trotiletre.server.skeletons;

import org.trotiletre.common.AnswerTag;
import org.trotiletre.common.communication.Skeleton;
import org.trotiletre.server.services.AuthenticationManager;
import org.trotiletre.server.services.NotificationManager;
import org.trotiletre.server.services.ResponseManager;

import java.io.*;
import java.net.SocketAddress;

/**
 * A class that implements the {@link Skeleton} interface for the {@link AuthenticationManager} class.
 * <p>
 * This class delegates the handling of input and output streams to the auth instance.
 */
public class AuthenticationManagerSkeleton implements Skeleton {

    private final AuthenticationManager auth; // The auth instance to delegate to.
    private final ResponseManager responseManager;
    private final NotificationManager notificationManager;

    /**
     * Constructs a new skeleton implementation for the given auth instance.
     *
     * @param auth The auth service instance.
     */
    public AuthenticationManagerSkeleton(AuthenticationManager auth, ResponseManager responseManager,
                                         NotificationManager notificationManager) {
        this.auth = auth;
        this.responseManager = responseManager;
        this.notificationManager = notificationManager;
    }

    @Override
    public void handle(byte[] data, SocketAddress socketAddress) throws Exception {

        // Unwrapping the data obtained in 'data' argument.
        ByteArrayInputStream dataStream = new ByteArrayInputStream(data);
        DataInput payload = new DataInputStream(dataStream);

        /*
         * There are 3 operations on this skeleton, login, register and logout.
         * Operations:
         *   0: Register - String, String
         *   1: LogIn - String, String
         *   2: LogOut - String
         */
        int operation = payload.readInt(); // Reading the operation we want to use.

        if (operation == 0) {

            /*
             * This section handles the requests for registering a new user.
             * The message we are expecting to receive will have:
             *  + user's username: Integer.
             *  + user's password hash: Integer.
             *
             *  This operation may fail and if so, returns -1.
             */

            var username = payload.readUTF(); // Reading the username.
            var passwordHash = payload.readUTF(); // Reading the password hash.

            // Registering the user using the 'API'.
            boolean registerStatus = auth.registerUser(username, passwordHash);

            // New byte array stream to put our results.
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            DataOutput dataOutput = new DataOutputStream(output);

            if (registerStatus) {
                System.out.println("server> Registered new user '" + username + "'.");
                dataOutput.writeBoolean(true);

            } else {
                System.out.println("server> Cannot register user '" + username + "' because it already exists.");
                dataOutput.writeBoolean(false);
            }

            // Sending to the client.
            responseManager.send(socketAddress, output.toByteArray(), AnswerTag.ANSWER.tag);
        }

        if (operation == 1) {

            /*
             * This section handles the requests for logging in a user.
             * The message we are expecting to receive will have:
             *  + user's username: Integer.
             *  + user's password: Integer.
             *
             *  This operation may fail and if so, returns false.
             */

            var username = payload.readUTF(); // Reading the user username.
            var passwordHash = payload.readUTF(); // Reading the user's password.

            // Attempting to log in the user.
            boolean loginStatus = auth.loginUser(username, passwordHash);

            // New byte array stream to put our results.
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            DataOutput dataOutput = new DataOutputStream(output);

            if (loginStatus) {
                responseManager.registerUser(username, socketAddress);
                System.out.println("server> User '" + username + "' has logged in.");
                dataOutput.writeBoolean(true);

            } else {
                System.out.println("server> Failed to log in user '" + username + "'.");
                dataOutput.writeBoolean(false);
            }

            // Sending to the client.
            responseManager.send(socketAddress, output.toByteArray(), AnswerTag.ANSWER.tag);
        }

        if (operation == 2) {

            /*
             * This section handles the requests for logging out the user.
             * The message we are expecting to receive will have:
             *  + user's username: Integer.
             *
             *  This operation may fail and if so, returns false.
             */

            var username = payload.readUTF(); // Reading the user username.

            // Attempting to log in the user.
            boolean logoutStatus = auth.logoutUser(username);

            // New byte array stream to put our results.
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            DataOutput dataOutput = new DataOutputStream(output);

            if (logoutStatus) {
                responseManager.removeUser(username);
                notificationManager.remove(username);
                System.out.println("server> User '" + username + "' has logged out.");
                dataOutput.writeBoolean(true);

            } else {
                System.out.println("server> Failed to log out user '" + username + "'.");
                dataOutput.writeBoolean(false);
            }
            responseManager.send(socketAddress, output.toByteArray(), AnswerTag.ANSWER.tag);
        }

    }

    /**
     * Getter for the authentication manager.
     *
     * @return The {@link AuthenticationManager} object.
     */
    public AuthenticationManager getAuth() {
        return auth;
    }
}
