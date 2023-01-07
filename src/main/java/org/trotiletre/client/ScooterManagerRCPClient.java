package org.trotiletre.client;

import org.trotiletre.client.stubs.AuthenticationManagerStub;
import org.trotiletre.client.stubs.ScooterManagerStub;
import org.trotiletre.common.IScooterManager;

import java.io.IOException;


public class ScooterManagerRCPClient {

    private ScooterManagerStub scooterManager = new ScooterManagerStub();

    public static void main(String[] args) throws IOException {

        AuthenticationManagerStub authManager = new AuthenticationManagerStub();

        String username = "gweebg";
        String password = "password";

        authManager.registerUser(username, password);

        authManager.loginUser(username, password);

        System.in.read();

    }

    /*
    Example of possible usage:
    scooterManager.login()
    scooterManager.moveScooter()
    ...
    */

}
