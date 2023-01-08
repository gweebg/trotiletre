package org.trotiletre.server.services;


import org.jetbrains.annotations.NotNull;
import org.trotiletre.models.Scooter;
import org.trotiletre.models.utils.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ScooterManager {

    private static final int mapSize = 10; // Size of the rows and collumns of the map.
    private static final int startingScooters = 10; // Number of starting scooters on the map.
    private AuthenticationManager authManager; // Authentication manager, used to check user status.
    private ScooterMap map; // Map containing the scooters.

    public ScooterManager(AuthenticationManager authManager) {

        this.authManager = authManager;

        this.map = new ScooterMap(ScooterManager.mapSize, ScooterManager.startingScooters);
        map.populateMap();

    }

    /**
     * Returns a list of the locations of free scooters within the given range of the lookup position.
     *
     * @param range The maximum range in meters to search for free scooters.
     * @param lookupPosition The position to use as the center of the search.
     * @return A list of the locations of free scooters within the given range, separated by commas.
     */
    private String listFreeScooters(final int range, @NotNull Location lookupPosition) {

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

}
