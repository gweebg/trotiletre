package org.trotiletre.server;

import org.trotiletre.common.communication.Skeleton;
import org.trotiletre.common.communication.TaggedConnection;

import java.io.*;
import java.net.Socket;
import java.util.Map;

public class Worker implements Runnable {

    private Socket socket;
    private TaggedConnection connection;
    private Map<Integer, Skeleton> services;

    public Worker(Socket socket, Map<Integer, Skeleton> services) throws IOException {
        this.socket = socket;
        this.services = services;
        this.connection = new TaggedConnection(socket);
    }

    @Override
    public void run() {

        try {

            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            while (true) {

                TaggedConnection.Frame receivedMessage = connection.receive();

                Skeleton service = services.get(receivedMessage.tag);
                service.handle(receivedMessage.data);
            }

        } catch (Exception e) {

            System.out.println("Closed connection with the client.");

        } finally {

            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    }
}
