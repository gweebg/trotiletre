package org.trotiletre.client.stubs;

import org.trotiletre.common.IScooterManager;
import org.trotiletre.models.User;

import java.io.*;
import java.net.Socket;

public class ScooterManagerStub implements IScooterManager {

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public ScooterManagerStub(Socket socket) {

        try {

            this.socket = socket;
            this.in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            this.out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

        } catch (IOException e) {

            System.out.println("(SooterManagerStub.java) Could not create socket.");
            System.exit(1);
        }
    }

    /* Implement methods, move scooter, park, bounties, login, etc. */
}
