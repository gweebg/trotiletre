package org.trotiletre.server.skeletons;

import org.trotiletre.common.communication.Skeleton;
import org.trotiletre.common.communication.TaggedConnection;
import org.trotiletre.server.services.AuthenticationManager;

import java.io.*;

/**
 * A class that implements the {@link Skeleton} interface for the {@link AuthenticationManager} class.
 * <p>
 * This class delegates the handling of input and output streams to the auth instance.
 */
public class AuthenticationManagerSkeleton implements Skeleton {

    private final AuthenticationManager auth; // The auth instance to delegate to.

    /**
     * Constructs a new skeleton implementation for the given auth instance.
     *
     * @param auth The auth service instance.
     */
    public AuthenticationManagerSkeleton(AuthenticationManager auth) { this.auth = auth; }

    @Override
    public void handle(byte[] data, TaggedConnection connection) throws Exception {

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
            connection.send(0, output.toByteArray());
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
                System.out.println("server> User '" + username + "' has logged in.");
                dataOutput.writeBoolean(true);

            } else {
                System.out.println("server> Failed to log in user '" + username + "'.");
                dataOutput.writeBoolean(false);
            }

            // Sending to the client.
            connection.send(0, output.toByteArray());
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
                System.out.println("server> User '" + username + "' has logged out.");
                dataOutput.writeBoolean(true);

            } else {
                System.out.println("server> Failed to log out user '" + username + "'.");
                dataOutput.writeBoolean(false);
            }

            // Sending to the client.
            connection.send(0, output.toByteArray());
        }

        if (operation == 3) {

            /*
             * This section handles the requests for changing the notication status of the user.
             * The message we are expecting to receive will have:
             *  + user's username: Integer.
             *  + new state: Boolean.
             *
             *  This operation may fail and if so, returns false.
             */

            String username = payload.readUTF(); // Reading the user username.
            boolean newState = payload.readBoolean(); // Reading the new state for the configuration.

            // Attempting to log in the user.
            boolean notifStatus = auth.changeNotificationStatus(username, newState);

            // New byte array stream to put our results.
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            DataOutput dataOutput = new DataOutputStream(output);

            if (notifStatus) {
                System.out.println("server> User '" + username + "' changed notification");
                dataOutput.writeBoolean(true);

            } else {
                System.out.println("server> Failed to change notification setting for user '" + username + "'.");
                dataOutput.writeBoolean(false);
            }

            // Sending to the client.
            connection.send(0, output.toByteArray());
        }

    }

    /**
     * Getter for the authentication manager.
     * @return The {@link AuthenticationManager} object.
     */
    public AuthenticationManager getAuth() {
        return auth;
    }
}
