package org.trotiletre.client;

import org.trotiletre.client.stubs.AuthenticationManagerStub;
import org.trotiletre.client.stubs.ScooterManagerStub;
import org.trotiletre.client.workers.NotificationListener;
import org.trotiletre.common.communication.Demultiplexer;
import org.trotiletre.common.communication.TaggedConnection;
import org.trotiletre.models.utils.GenericPair;
import org.trotiletre.models.utils.Location;

import java.io.IOException;
import java.net.Socket;


public class ScooterManagerRCPClient {

    private final String serverAddress;
    private final int port;
    private final Socket clientSocket;
    private final TaggedConnection connection;

    public ScooterManagerRCPClient(String serverAddress, int port) throws IOException {

        this.serverAddress = serverAddress;
        this.port = port;

        this.clientSocket = new Socket(serverAddress, port);
        this.connection = new TaggedConnection(clientSocket);
    }

    public void run() throws IOException, InterruptedException {

        Demultiplexer demultiplexer = new Demultiplexer(connection);

        ScooterManagerStub scooterManager = new ScooterManagerStub(connection, demultiplexer);
        AuthenticationManagerStub authManager = new AuthenticationManagerStub(connection, demultiplexer);

        demultiplexer.start();

        // Running the notification listener.
        new Thread(new NotificationListener(demultiplexer)).start();

        /* Tests */
        String username = "Guilherme";
        String password = "emrehliuG";

        boolean registerStatus = authManager.registerUser(username, password);
        if (registerStatus) System.out.println("Successfully registered.");
        else System.out.printf("User already exists.");
        System.in.read();

        boolean loginStatus = authManager.loginUser(username, password);
        if (loginStatus) System.out.println("Successfully logged in.");
        else System.out.printf("Invalid password or username.");
        System.in.read();

        String freeScooters = scooterManager.listFreeScooters(
                2, new Location(0,0));
        System.out.println("Available scooters: " + freeScooters);
        System.in.read();

        GenericPair<String, Location> reservationCode =
                scooterManager.reserveScooter(2, new Location(0, 0), username);

        System.out.println("Reservation code: " + reservationCode.getFirst());
        System.out.println("Scooter at: " + reservationCode.getSecond());
        System.in.read();

        GenericPair<Double, Double> parkPrice =
                scooterManager.parkScooter(reservationCode.getFirst(), new Location(3, 3), username);

        double price = parkPrice.getFirst();

        if (price == -1) System.out.println("Invalid reservation code!");
        else if (price == -2) System.out.println("You need to log in before trying any action.");
        else System.out.println("You have been charged " + parkPrice.getFirst() + "€");

        System.in.read();
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        ScooterManagerRCPClient client = new ScooterManagerRCPClient(
                "localhost",
                20022);

        client.run();
    }
}
