package org.trotiletre.server.skeletons;

import org.trotiletre.common.communication.Skeleton;
import org.trotiletre.common.communication.TaggedConnection;
import org.trotiletre.server.services.AuthenticationManager;

import java.io.*;
import java.net.SocketAddress;

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
    public void handle(byte[] data, SocketAddress socketAddress) throws Exception {

        ByteArrayInputStream dataStream = new ByteArrayInputStream(data);
        DataInput payload = new DataInputStream(dataStream);

        int operation = payload.readInt();

        if (operation == 0) {

            var username = payload.readUTF();
            var passwordHash = payload.readUTF();

            System.out.println("Received username: " + username);
            System.out.println("Received password: " + passwordHash);

            auth.registerUser(username, passwordHash);

            System.out.println("Registered user: " + username);
        }

        if (operation == 1) {

            var username = payload.readUTF();
            var passwordHash = payload.readUTF();

            auth.loginUser(username, passwordHash);
            System.out.println("Logged in user: " + username);
        }

    }

    public AuthenticationManager getAuth() {
        return auth;
    }
}
