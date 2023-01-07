package org.trotiletre.server;

import org.bouncycastle.mime.encoding.Base64OutputStream;
import org.trotiletre.common.communication.Skeleton;

import java.io.*;
import java.net.Socket;
import java.util.Map;

public class Worker implements Runnable {

    private Socket socket;
    private Map<Integer, Skeleton> services;

    public Worker(Socket socket, Map<Integer, Skeleton> services) {
        this.socket = socket;
        this.services = services;
    }

    @Override
    public void run() {

        Skeleton service = null;
        try {
            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            while (true) {
                service = services.get(in.readInt());
                service.handle(in, out);
                System.out.println("Handler");
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
