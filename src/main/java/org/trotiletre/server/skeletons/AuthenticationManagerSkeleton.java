package org.trotiletre.server.skeletons;

import org.trotiletre.common.communication.Skeleton;
import org.trotiletre.common.communication.TaggedConnection;
import org.trotiletre.server.services.AuthenticationManager;
import org.trotiletre.server.services.ScooterManager;

import javax.swing.text.html.HTML;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

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
    public void handle(byte[] data) throws Exception {

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

}
