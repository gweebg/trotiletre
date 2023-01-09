//package org.trotiletre.ui.controller;
//
//import org.trotiletre.client.stubs.AuthenticationManagerStub;
//import org.trotiletre.client.stubs.ScooterManagerStub;
//import org.trotiletre.client.workers.NotificationListener;
//import org.trotiletre.common.communication.Demultiplexer;
//import org.trotiletre.common.communication.TaggedConnection;
//
//import java.io.IOException;
//import java.net.Socket;
//import java.util.Scanner;
//
//public class MainController {
//    private final String serverAddress;
//    private final int port;
//    private final Socket clientSocket;
//    private final TaggedConnection connection;
//    private final Demultiplexer demultiplexer;
//    private final ScooterManagerStub scooterManager;
//    private final AuthenticationManagerStub authenticationManager;
//
//    public MainController(String serverAddress, int port) throws IOException {
//
//        this.serverAddress = serverAddress;
//        this.port = port;
//
//        this.clientSocket = new Socket(serverAddress, port);
//        this.connection = new TaggedConnection(clientSocket);
//        this.demultiplexer = new Demultiplexer(connection);
//
//        this.scooterManager = new ScooterManagerStub(connection, demultiplexer);
//        this.authenticationManager = new AuthenticationManagerStub(connection);
//
//        demultiplexer.start();
//        new Thread(new NotificationListener(demultiplexer)).start();
//    }
//
//    private void loginUser() {}
//    private void registerUser() {}
//
//    public void run() throws IOException, InterruptedException {
//
//        Scanner userInput = new Scanner(System.in);
//
//        // Application greeter.
//        View.greetings();
//        View.mainMenu();
//        int action = userInput.nextInt();
//
//        if (action == 0)
//
//        // Prompting for username.
//        View.promptUsername();
//        String username = userInput.nextLine();
//
//        // Prompting for password.
//        View.promptPassword();
//        String password = userInput.toString();
//
//        authenticationManager.loginUser(username, password);
//
//
//        while (true) {
//
//
//
//        }
//
//    }
//}
