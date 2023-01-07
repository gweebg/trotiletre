package org.trotiletre.server;

import org.trotiletre.common.communication.Skeleton;
import org.trotiletre.common.communication.TaggedConnection;
import org.trotiletre.server.services.AuthenticationManager;
import org.trotiletre.server.services.ScooterManager;
import org.trotiletre.server.skeletons.AuthenticationManagerSkeleton;
import org.trotiletre.server.skeletons.ScooterManagerSkeleton;

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

        System.out.println("Running server...");

        // Map of service skeletons keyed by service ID.
        Map<Integer, Skeleton> services = new HashMap<>();

        // Register service skeletons.
        services.put(0, new ScooterManagerSkeleton(new ScooterManager()));
        services.put(1, new AuthenticationManagerSkeleton(new AuthenticationManager()));
        //services.put(2, new NotificationManagerSkeleton(new NotificationManager()));

        // Listen for incoming connections.
        while (true) {

            try {

                Socket s = socket.accept();
                Worker worker = new Worker(s, services);
                new Thread(worker).start();

            } catch (IOException e) {
                System.out.println(e.getMessage());
            }

        }

    }

    public static void main(String[] args) throws Exception {

        RMIServer server = new RMIServer(20022);
        server.runServer();

    }

}
