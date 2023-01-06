package org.trotiletre.server.services;

import org.trotiletre.common.communication.Skeleton;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * A class that implements the {@link Skeleton} interface for the {@link Server} class.
 * <p>
 * This class delegates the handling of input and output streams to the server instance.
 */
public class ServerSkeletonImplementation implements Skeleton {

    private final Server server; // The server instance to delegate to.

    /**
     * Constructs a new skeleton implementation for the given server instance.
     *
     * @param server The server instance.
     */
    public ServerSkeletonImplementation(Server server) { this.server = server; }

    @Override
    public void handle(DataInputStream in, DataOutputStream out) throws Exception {

    }

}
