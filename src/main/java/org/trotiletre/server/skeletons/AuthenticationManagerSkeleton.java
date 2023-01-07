package org.trotiletre.server.skeletons;

import org.trotiletre.common.communication.Skeleton;
import org.trotiletre.server.services.AuthenticationManager;
import org.trotiletre.server.services.ScooterManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOError;
import java.io.IOException;
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
    public void handle(DataInputStream in, DataOutputStream out) throws Exception {

        int operation = in.readInt();

        if (operation == 0) {

            var username = in.readUTF();
            var passowrdHash = in.readUTF();

            auth.registerUser(username, passowrdHash);
        }

        if (operation == 1) {

            var username = in.readUTF();
            var passowrdHash = in.readUTF();

            auth.loginUser(username, passowrdHash);
        }

    }

}
