package org.trotiletre.server.services;

import org.jetbrains.annotations.NotNull;
import org.trotiletre.common.IScooterManager;
import org.trotiletre.models.Reservation;
import org.trotiletre.models.Scooter;
import org.trotiletre.models.User;
import org.trotiletre.models.utils.GenericPair;
import org.trotiletre.models.utils.Location;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class ScooterManager implements IScooterManager {

    // Static information about the application.
    private final int mapSize; // Size of the rows and collumns of the map.

    // Logic variables used on the application.
    private final AuthenticationManager authManager; // Authentication manager, used to check user status.
    private final RewardManager rewardManager;
    private final ScooterMap map; // Map containing the scooters.
    private final Map<UUID, Reservation> reservation = new HashMap<>(); // Reservations made by the users.

    // Concurrency control variables.
    private final ReentrantLock managerLock = new ReentrantLock();

    /**
     * Creates a new scooter manager using the given authentication manager.
     *
     * @param authManager The authentication manager to use for this scooter manager.
     */
    public ScooterManager(ScooterMap scooterMap, AuthenticationManager authManager, RewardManager rewardManager) {

        // Setting up the authentication manager.
        this.authManager = authManager;
        this.rewardManager = rewardManager;

        // Create a new scooter map with the specified size and starting number of scooters.
        this.map = scooterMap;
        this.mapSize = scooterMap.getMapSize();

        map.populateMap(); // Populate the map with scooters.
    }

    public ScooterMap getScooterMap() {
        return this.map;
    }

    /**
     * Returns a list of the locations of free scooters within the given range of the lookup position.
     * Tested ✅
     *
     * @param range          The maximum range in meters to search for free scooters.
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
     * When done, return the location of the scooter and the reservation code.
     * Tested ✅
     *
     * @param range    The range to lookup within.
     * @param local    The local to start searching.
     * @param username The user's username, for cool information purpouses.
     * @return String containing the location and reservation code. Null if there are no scooters.
     */
    public GenericPair<String, Location> reserveScooter(final int range, @NotNull Location local, String username) {

        // Get the closest free scooter of the provided location within the specified range.
        Scooter scooter = map.getClosestScooterWithinRange(range, local, mapSize); // Scooter already comes marked with being used.

        try {

            // We're using local variables such as the maps, we need locks.
            managerLock.lock();

            // If we found an available scooter within the range.
            if (scooter != null) {

                UUID reservationCode = UUID.randomUUID(); // Generate the reservation code.
                Location scooterLocation = scooter.getLocation(); // Store the scooter location.

                // Adding the new reservation to the 'database'.
                Reservation res = new Reservation(reservationCode, username, scooter);
                reservation.put(reservationCode, res);

                return new GenericPair<>(reservationCode.toString(), scooterLocation);
            }

        } finally {
            managerLock.unlock();
        }

        return null;
    }

    /**
     * Park a scooter by providing a previously generated reservation code.
     *
     * @param reservationCode    Reservation code for the scooter.
     * @param newScooterLocation The new location of the scooter.
     * @return The price to pay for the travel or {@code -1} if the {@code reservationCode} is on correct.
     */
    public GenericPair<Double, Double> parkScooter(String reservationCode, Location newScooterLocation, String username) {

        try {

            managerLock.lock();

            // Retrieving the scooter from the reservation map.
            Reservation context = reservation.get(UUID.fromString(reservationCode));
            if (context == null) return null; // The provided reservation code is invalid.

            Scooter scooter = context.getScooter();
            scooter.setInUse(false);

            // Calculating the distance travelled.
            int distanceScooted = map.distanceBetween(
                    scooter.getLocation(),
                    newScooterLocation
            );

            // Calculating the price for the trip. The user pays 10 cents per unit of distance and 20 cents per minute.
            double priceToPay = context.getPriceOfTrip(distanceScooted, LocalDateTime.now());

            // Get bounty if exists
            Optional<Double> rewardOpt = this.rewardManager.getReward(scooter.getLocation(), newScooterLocation);
            Double reward = null;
            if (rewardOpt.isPresent())
                reward = rewardOpt.get();

            // Update the location of the scooter to the new location provided.
            map.updateScooterLocation(scooter.getLocation(), newScooterLocation, scooter.getScooterId());

            // Removing the reservation and updating the user's information.
            reservation.remove(UUID.fromString(reservationCode));

            User user = authManager.getUser(username);

            user.setBalance(user.getBalance() - priceToPay);
            user.setAmountRides(user.getAmountRides() + 1);
            user.setDistanceTraveled(user.getDistanceTraveled() + (double) distanceScooted);

            return new GenericPair<>(priceToPay, reward);

        } finally {
            managerLock.unlock();
        }
    }

    public AuthenticationManager getAuthManager() {
        return authManager;
    }
}
