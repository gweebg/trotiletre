package org.trotiletre.server;

import org.trotiletre.common.communication.Skeleton;
import org.trotiletre.common.communication.TaggedConnection;
import org.trotiletre.server.services.ResponseManager;

import java.io.*;
import java.net.Socket;
import java.util.Map;

/**
 * Class representing a worker that processes incoming requests from a client.
 * This worker is created for each new connection and runs in a separate thread.
 * The worker processes requests until the connection is closed.
 */
public class Worker implements Runnable {

    private Socket socket;
    private TaggedConnection connection;
    private Map<Integer, Skeleton> services;
    private final ResponseManager responseManager;

    public Worker(Socket socket, Map<Integer, Skeleton> services, ResponseManager responseManager) throws IOException {
        this.socket = socket;
        this.services = services;
        this.connection = new TaggedConnection(socket);
        this.responseManager = responseManager;
        this.responseManager.register(socket);
    }

    @Override
    public void run() {

        try {

            while (true) {
                TaggedConnection.Frame receivedMessage = connection.receive();
                Skeleton service = services.get(receivedMessage.tag);
                service.handle(receivedMessage.data, socket.getRemoteSocketAddress());
            }

        } catch (Exception e) {
            this.responseManager.remove(socket.getRemoteSocketAddress());

            System.out.println("server> Closed connection a client.");

        } finally {

            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    }
}
