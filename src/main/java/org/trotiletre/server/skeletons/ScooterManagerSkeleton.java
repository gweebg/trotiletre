package org.trotiletre.server.skeletons;

import org.trotiletre.common.communication.Skeleton;
import org.trotiletre.server.services.AuthenticationManager;
import org.trotiletre.server.services.ScooterManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * A class that implements the {@link Skeleton} interface for the {@link ScooterManager} class.
 * <p>
 * This class delegates the handling of input and output streams to the server instance.
 */
public class ScooterManagerSkeleton implements Skeleton {

    private final ScooterManager server; // The server instance to delegate to.

    /**
     * Constructs a new skeleton implementation for the given server instance.
     *
     * @param server The server instance.
     */
    public ScooterManagerSkeleton(ScooterManager server) { this.server = server; }

    @Override
    public void handle(DataInputStream in, DataOutputStream out) throws Exception {
        System.out.println("Message to Manager!");
    }

}
