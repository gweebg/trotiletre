package org.trotiletre.client;

import org.trotiletre.client.stubs.AuthenticationManagerStub;
import org.trotiletre.client.stubs.ScooterManagerStub;
import org.trotiletre.common.IScooterManager;
import org.trotiletre.common.communication.TaggedConnection;
import org.trotiletre.models.utils.GenericPair;
import org.trotiletre.models.utils.Location;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;


public class ScooterManagerRCPClient {


    public static void main(String[] args) throws IOException {

        Socket clientSocket = new Socket("localhost", 20022);
        TaggedConnection connection = new TaggedConnection(clientSocket);

        ScooterManagerStub scooterManager = new ScooterManagerStub(connection);
        AuthenticationManagerStub authManager = new AuthenticationManagerStub(connection);

        String username = "babi";
        String password = "password_da_babi";

        authManager.registerUser(username, password);
        authManager.loginUser(username, password);

        String res = scooterManager.listFreeScooters(2, new Location(0,0));
        System.out.println("Available scooters: " + res);

        System.in.read();

        GenericPair<String, Location> reservationCode =
                scooterManager.reserveScooter(2, new Location(0, 0), "babi");

        System.out.println("Reservation code: " + reservationCode.getFirst());
        System.out.println("Scooter at: " + reservationCode.getSecond());

        System.in.read();

        GenericPair<Double, Double> parkPrice =
                scooterManager.parkScooter(reservationCode.getFirst(), new Location(3, 3), "babi");

        double price = parkPrice.getFirst();

        if (price == -1) System.out.println("Invalid reservation code!");

        else if (price == -2) System.out.println("You need to log in before trying any action.");

        else System.out.println("You have been charged " + parkPrice.getFirst() + "€");

        System.in.read();

    }
}
