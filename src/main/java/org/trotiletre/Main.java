package org.trotiletre;

import org.trotiletre.server.RMIServer;

public class Main {
    public static void main(String[] args) throws Exception {
        new RMIServer(12345).runServer();
    }
}