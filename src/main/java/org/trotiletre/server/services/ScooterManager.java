package org.trotiletre.server.services;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.trotiletre.models.Scooter;
import org.trotiletre.models.utils.Location;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class ScooterManager {

    // Static information about the application.
    private static final int mapSize = 10; // Size of the rows and collumns of the map.
    private static final int startingScooters = 50; // Number of starting scooters on the map.

    // Logic variables used on the application.
    private AuthenticationManager authManager; // Authentication manager, used to check user status.
    private ScooterMap map; // Map containing the scooters.
    private Map<UUID, Scooter> mapReservationsUUID = new HashMap<>(); // Reservations made by the users.
    private Map<String, List<UUID>> mapUserUUID = new HashMap<>(); // Users with the reservations.

    // Concurrency control variables.
    private ReentrantLock managerLock = new ReentrantLock();

    /**
     * Creates a new scooter manager using the given authentication manager.
     *
     * @param authManager The authentication manager to use for this scooter manager.
     */
    public ScooterManager(AuthenticationManager authManager) {

        // Setting up the authentication manager.
        this.authManager = authManager;

        // Create a new scooter map with the specified size and starting number of scooters.
        this.map = new ScooterMap(ScooterManager.mapSize, ScooterManager.startingScooters);
        map.populateMap(); // Populate the map with scooters.
    }

    /**
     * Returns a list of the locations of free scooters within the given range of the lookup position.
     *
     * @param range The maximum range in meters to search for free scooters.
     * @param lookupPosition The position to use as the center of the search.
     * @return A list of the locations of free scooters within the given range, separated by commas.
     */
    public @NotNull String listFreeScooters(final int range, @NotNull Location lookupPosition) {

        // Get a list of free scooters within the specified range.
        List<Scooter> nearbyScooters = map.getFreeScootersWithinRange(range, lookupPosition);

        // Map the scooter locations to strings.
        List<String> locations = nearbyScooters
                .stream()
                .map(s -> s.getLocation().toString())
                .toList();

        // Join the list of strings into a single string separated by commas.
        return String.join(",", locations);
    }

    /**
     * Reserve the free scooter closest to {@code local} within the range {@code range}.
     * <p>
     * When done, return the location of the scooter and the reservation code.
     *
     * @param range The range to lookup within.
     * @param local The local to start searching.
     * @param username The user's username, for cool information purpouses.
     * @return String containing the location and reservation code. Null if there are no scooters.
     */
    public @Nullable String reserveScooter(final int range, @NotNull Location local, String username) {

        // Get the closest free scooter of the provided location within the specified range.
        Scooter scooter = map.getClosestScooterWithinRange(range, local); // Scooter already comes marked with being used.

        try {

            // We're using local variables such as the maps, we need locks.
            managerLock.lock();

            // If we found an available scooter within the range.
            if (scooter != null) {

                UUID reservationCode = UUID.randomUUID(); // Generate the reservation code.
                Location scooterLocation = scooter.getLocation(); // Store the scooter location.

                // The reservation code should never be the same.
                mapReservationsUUID.put(reservationCode, scooter);

                // Adding the user to the registry of user-reservations if not exists.
                if (!mapUserUUID.containsKey(username)) {
                    mapUserUUID.put(username, new ArrayList<>());
                }

                // Updating user reservations.
                mapUserUUID.get(username).add(reservationCode);

                return reservationCode.toString() + "," + scooterLocation.toString();
            }

        } finally { managerLock.unlock(); }

        return null;
    }

    /**
     * Park a scooter by providing a previously generated reservation code.
     *
     * @param reservationCode Reservation code for the scooter.
     * @param newScooterLocation The new location of the scooter.
     * @return The price to pay for the travel or {@code -1} if the {@code reservationCode} is on correct.
     */
    public double parkScooter(String reservationCode, Location newScooterLocation, String username) {

        /*
        TODO:
        O custo realaciona o tempo passado desde a reserva e da distância percorrida.
        Verificar se é recompensa.
        Update nos valores do utilizador.
        */

        try {

            managerLock.lock();

            // Retrieving the scooter from the reservation map.
            @Nullable Scooter scooter = mapReservationsUUID.get(UUID.fromString(reservationCode));
            if (scooter == null) return -1d; // Error code indicating that the reservation code is bad.

            // Calculating the distance travelled.
            int distanceScooted = map.distanceBetween(scooter.getLocation(), newScooterLocation);

            // Calculating the price to pay for the trip.
            // The price is given by the formula: distance * 0.3.
            double priceToPay = distanceScooted * 0.3;

            // Update the location of the scooter to the new location provided.
            map.updateScooterLocation(scooter.getLocation(), newScooterLocation, scooter.getScooterId());

            // Update scooter state to not being used.
            scooter.setInUse(false);

            // Update user reservation information.
            List<UUID> userReservation = mapUserUUID.get(username);
            userReservation.remove(UUID.fromString(reservationCode));

            return priceToPay;

        } finally { managerLock.unlock(); }
    }

}
