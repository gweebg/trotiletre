package org.trotiletre.ui;

import org.trotiletre.client.stubs.AuthenticationManagerStub;
import org.trotiletre.client.stubs.ScooterManagerStub;
import org.trotiletre.models.utils.GenericPair;
import org.trotiletre.models.utils.Location;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Processor {

    private ScooterManagerStub scooterManager;
    private AuthenticationManagerStub authManager;

    private Map<String, Pattern> operationPatterns = new HashMap<>();
    private Map<String, Method> operations = new HashMap<>();

    private int range = 0;
    private Location currentLocation = new Location(0, 0);
    private String loggedInAs = "";

    public Processor(ScooterManagerStub scooterManager, AuthenticationManagerStub authManager) throws NoSuchMethodException {

        this.scooterManager = scooterManager;
        this.authManager = authManager;

        operations.put("help", this.getClass().getMethod("processHelp", String.class));

        operationPatterns.put("login", Pattern.compile("login\\s+(\\w+)\\s+(\\w+)"));
        operations.put("login", this.getClass().getMethod("processLogin", String.class));

        operationPatterns.put("register", Pattern.compile("register\\s+(\\w+)\\s+(\\w+)"));
        operations.put("register", this.getClass().getMethod("processRegister", String.class));

        operationPatterns.put("setlocation", Pattern.compile("setlocation\\s+(\\d+)\\s+(\\d+)"));
        operations.put("setlocation", this.getClass().getMethod("processSetLocation", String.class));

        operationPatterns.put("setrange", Pattern.compile("setrange\\s+(\\d+)+"));
        operations.put("setrange", this.getClass().getMethod("processSetRange", String.class));

        operations.put("rent", this.getClass().getMethod("processRent", String.class));

        operations.put("list", this.getClass().getMethod("processList", String.class));

        operationPatterns.put("park", Pattern.compile("^park\\s([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})\\s(\\d+)\\s(\\d+)$"));
        operations.put("park", this.getClass().getMethod("processParking", String.class));

        operationPatterns.put("logout", Pattern.compile("logout\\s+(\\w+)"));
        operations.put("logout", this.getClass().getMethod("processLogout", String.class));
    }

    public void processLogout(String userCommand) throws IOException, InterruptedException {

        Pattern parkPattern = operationPatterns.get("logout");
        Matcher m = parkPattern.matcher(userCommand);

        if (m.find()) {

            String username = m.group(1);

            if (Objects.equals(username, loggedInAs)) {

                boolean statusLogout = authManager.logoutUser(username);
                if (!statusLogout) System.out.println("trotiletre.error> You cannot logout if you are not logged in.");
                else System.out.println("trotiletre.info> Successfully logged out!");

            } else System.out.println("trotiletre.error> You cannot sign out user '" + username + "' when you are using another account.");

        }
        else System.out.println("trotiletre.error> Invalid usage of 'logout' command, check the help menu.");
    }

    public void processList(String userCommand) throws IOException, InterruptedException {

        String listStatus = scooterManager.listFreeScooters(range, currentLocation);
        System.out.println("trotiletre.info> Available scooters in your area: " + listStatus);
    }

    public void processRent(String userCommand) throws IOException, InterruptedException {

        GenericPair<String, Location> rentingStatus = scooterManager.reserveScooter(
                range, currentLocation, loggedInAs);

        if (rentingStatus.getSecond() == null) System.out.println("trotiletre.error> " + rentingStatus.getFirst());
        else {

            System.out.println("trotiletre.info> Reservation code for scooter at "
                    + rentingStatus.getSecond() + ": "
                    + rentingStatus.getFirst());
        }
    }

    public void processHelp(String userCommand) {

        StringBuilder helpMenu = new StringBuilder();

        helpMenu.append("trotiletre.help> 'help' displays this message\n");

        helpMenu.append("trotiletre.help> 'register [username] [password]' register a new user with the provided parameters\n");
        helpMenu.append("trotiletre.help> 'login [username] [password]' login the user with the provided parameters\n");

        helpMenu.append("trotiletre.help> 'setlocation [x] [y]' set the user location to (x,y) coordinates\n");
        helpMenu.append("trotiletre.help> 'setrange [range]' set range to search scooters for\n");

        helpMenu.append("trotiletre.help> 'status' displays information about the user\n");
        helpMenu.append("trotiletre.help> 'rent' make a reservation for a scooter within the provided range\n");
        helpMenu.append("trotiletre.help> 'park [reservation_code] [x] [y]' park the scooter indicated by the provided reservation code at (x,y) coordinates.\n");
        helpMenu.append("trotiletre.help> 'list' list available scooters within the range\n");
        helpMenu.append("trotiletre.help> 'listr' list available rewards within the range");

        System.out.println(helpMenu);
    }

    public void processParking(String userCommand) throws IOException, InterruptedException {

        Pattern parkPattern = operationPatterns.get("park");
        Matcher m = parkPattern.matcher(userCommand);

        if (m.find()) {

            String reservationCode = m.group(1);
            int xLocation = Integer.parseInt(m.group(2));
            int yLocation = Integer.parseInt(m.group(3));

            GenericPair<Double, Double> parkPrice =
                    scooterManager.parkScooter(reservationCode, new Location(xLocation, yLocation), loggedInAs);

            double price = parkPrice.getFirst();

            if (price == -1) System.out.println("trotiletre.error> Invalid reservation code!");
            else if (price == -2) System.out.println("trotiletre.error> You need to log in before trying any action.");
            else System.out.println("trotiletre.info> You have been charged " + parkPrice.getFirst() + "€");

        }
        else System.out.println("trotiletre.error> Invalid usage of 'park' command, check the help menu.");
    }

    public void processSetRange(String userCommand) {

        Pattern rangePattern = operationPatterns.get("setrange");
        Matcher m = rangePattern.matcher(userCommand);

        if (m.find()) {

            this.range = Integer.parseInt(m.group(1));

            System.out.println("trotiletre.info> Range is set to " + range + ".");
        }
        else System.out.println("trotiletre.error> Invalid usage of 'setrange' command, check the help menu.");
    }

    public void processSetLocation(String userCommand) {

        Pattern locationPattern = operationPatterns.get("setlocation");
        Matcher m = locationPattern.matcher(userCommand);

        if (m.find()) {

            int xLocation = Integer.parseInt(m.group(1));
            int yLocation = Integer.parseInt(m.group(2));

            currentLocation = new Location(xLocation, yLocation);
            System.out.println("trotiletre.info> Location is set to " + currentLocation + ".");
        }
        else System.out.println("trotiletre.error> Invalid usage of 'setlocation' command, check the help menu.");
    }

    public void processRegister(String userCommand) throws IOException, InterruptedException {

        Pattern registerPattern = operationPatterns.get("register");
        Matcher m = registerPattern.matcher(userCommand);

        if (m.find()) {

            String username = m.group(1);
            String password = m.group(2);

            boolean registerStatus = authManager.registerUser(username, password);

            if (registerStatus) System.out.println("trotiletre.info> Successfully registered as '" + username + "'.");
            else System.out.println("trotiletre.error> Account already exists!");
        }
        else System.out.println("trotiletre.error> Invalid usage of 'register' command, check the help menu.");
    }

    public void processLogin(String userCommand) throws IOException, InterruptedException {

        Pattern loginPattern = operationPatterns.get("login");
        Matcher m = loginPattern.matcher(userCommand);

        if (m.find()) {

            String username = m.group(1);
            String password = m.group(2);

            boolean loginStatus = authManager.loginUser(username, password);

            if (loginStatus) {
                System.out.println("trotiletre.info> Successfully logged in as '" + username + "'.");
                loggedInAs = username;
            }
            else System.out.println("trotiletre.error> Invalid username or password.");
        }
        else System.out.println("trotiletre.error> Invalid usage of 'login' command, check the help menu.");
    }

    public void process(String userCommand) throws InvocationTargetException, IllegalAccessException {

        String possibleCommand = userCommand.strip();
        if (userCommand.contains(" ")) possibleCommand = userCommand.substring(0, userCommand.indexOf(' '));

        if (!operations.containsKey(possibleCommand)) {
            System.out.println("Invalid syntax, use the command 'help' to check every operation.");
            return;
        }

        Method processor = operations.get(possibleCommand);
        processor.invoke(this, userCommand);
    }
}
