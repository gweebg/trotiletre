package org.trotiletre.client;

import org.trotiletre.client.stubs.AuthenticationManagerStub;
import org.trotiletre.client.stubs.ScooterManagerStub;
import org.trotiletre.common.IScooterManager;
import org.trotiletre.common.communication.TaggedConnection;
import org.trotiletre.models.utils.Location;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


public class ScooterManagerRCPClient {


    public static void main(String[] args) throws IOException {

        Socket clientSocket = new Socket("localhost", 12345);
        TaggedConnection connection = new TaggedConnection(clientSocket);

        ScooterManagerStub scooterManager = new ScooterManagerStub(connection);
        AuthenticationManagerStub authManager = new AuthenticationManagerStub(connection);

        String username = "babi";
        String password = "password_da_babi";

        authManager.registerUser(username, password);
        authManager.loginUser(username, password);

        String res = scooterManager.listFreeScooters(10, new Location(4,4));
        System.out.println(res);

        System.in.read();
    }
}
