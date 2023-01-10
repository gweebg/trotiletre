package org.trotiletre.server;

import org.trotiletre.common.communication.Skeleton;
import org.trotiletre.server.services.*;
import org.trotiletre.server.skeletons.AuthenticationManagerSkeleton;
import org.trotiletre.common.CommunicationTags;
import org.trotiletre.server.skeletons.NotificationManagerSkeleton;
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

        ScooterMap scooterMap = new ScooterMap(4,4);

        // Map of service skeletons keyed by service ID.
        Map<Integer, Skeleton> services = new HashMap<>();

        // Register service skeletons.
        AuthenticationManager authenticationManager = new AuthenticationManager();
        ResponseManager responseManager = new ResponseManager();
        NotificationManager notificationManager = new NotificationManager();
        RewardManager rewardManager = new RewardManager(responseManager, notificationManager, scooterMap,
                authenticationManager, 1);
        ScooterManager scooterManager = new ScooterManager(scooterMap, authenticationManager, rewardManager);


        services.put(CommunicationTags.AUTHENTICATION_MAN.tag, new AuthenticationManagerSkeleton(authenticationManager,
                responseManager, notificationManager));
        services.put(CommunicationTags.SCOOTER_MAN.tag, new ScooterManagerSkeleton(scooterManager,
                responseManager, rewardManager));
        services.put(CommunicationTags.NOTIFICATION_MAN.tag, new NotificationManagerSkeleton(notificationManager,
                responseManager));


        // Listen for incoming connections.
        while (true) {

            try {

                Socket s = socket.accept();
                Worker worker = new Worker(s, services, responseManager);
                new Thread(worker).start();

            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static void main(String[] args) throws Exception {

        RMIServer server = new RMIServer(12345);
        server.runServer();

    }

}
