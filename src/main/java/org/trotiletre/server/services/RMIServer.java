package org.trotiletre.server.services;

import org.trotiletre.common.communication.Skeleton;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


/**
 * A class that represents a remote method invocation (RMI) server.
 * <p>
 * This class creates a server socket and listens
 * for incoming connections on the specified port.
 */
public class RMIServer {

    private int serverPort;
    private ServerSocket socket;

    /**
     * Constructs a new RMI server with the given port.
     *
     * @param serverPort The port on which the server will listen for incoming connections.
     */
    public RMIServer(int serverPort) {

        this.serverPort = serverPort;

        try {

            // Create server socket on the specified port.
            this.socket = new ServerSocket(serverPort);

        } catch (IOException e) {

            // Throw a runtime exception if an error occurs while creating the socket.
            throw new RuntimeException(e);
        }

    }

    /**
     * Runs the server and listens for incoming connections.
     * <p>
     * This method listens for incoming connections and delegates the handling of the
     * input and output streams to the appropriate service skeleton.
     *
     * @throws Exception If an error occurs while running the server.
     */
    public void runServer() throws Exception {

        // Map of service skeletons keyed by service ID.
        Map<Integer, Skeleton> services = new HashMap<>();

        // Register service skeletons.
        services.put(0, new ServerSkeletonImplementation(new Server()));

        // Listen for incoming connections.
        while (true) {

            try (Socket s = socket.accept()) {

                // Create input and output streams.
                var in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
                var out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));

                // Get the appropriate service skeleton.
                Skeleton service = services.get(in.readInt());
                service.handle(in, out); // Delegate the handling of the streams to the service skeleton.

            }

        }

    }

}
