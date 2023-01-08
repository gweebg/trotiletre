package org.trotiletre.server.services;

import org.jetbrains.annotations.NotNull;
import org.trotiletre.models.Scooter;
import org.trotiletre.models.utils.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

public class ScooterMap {

    /**
     * + Assuma uma distribuição aleatória de uma dado número fixo de trotinetes pelo mapa,
     * todas livres, quando o servidor arranca.
     * <p>
     * + O mapa é uma matriz N x N locais, sendo as coordenadas geográficas pares discretos de índices.
     *     + A distância entre dois pontos é medida pela distância de Manhattan.
     */

    private List<Scooter>[][] map;
    private int totalScooters;
    private final int startingScooters;

    private ReentrantLock mapLock = new ReentrantLock();

    public ScooterMap(final int mapSize, final int startingScooters) {

        this.map = new ArrayList[mapSize][mapSize]; // Creating the map with the provided size.

        // Initializing the map with empty lists of scooters.
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                map[i][j] = new ArrayList<>();
            }
        }

        this.totalScooters = 0;
        this.startingScooters = startingScooters;
    }

    public void populateMap() {

        try {

            mapLock.lock();

            Random random = new Random();

            for (int i = 0; i < startingScooters; i++) {

                // Choose a random location in the matrix.
                int row = random.nextInt(map.length);
                int col = random.nextInt(map[row].length);

                // Choose a random UUID for the scooter.
                String scooterID = UUID.randomUUID().toString();

                // Place a value at the random location.
                map[row][col].add(new Scooter(scooterID, new Location(col, row), false));
                totalScooters++; // Incrementing the total count of scooters.
            }
        } finally {
          mapLock.unlock();
        }
    }

    public Scooter getClosestScooterWithinRange(int range, Location startingPoint) {

        try {

            mapLock.lock();

            int closestScooterDistance = 100_000;
            Scooter closestScooter = null;

            for (int i = 0; i < map.length; i++) {
                for (int j = 0; j < map[i].length; j++) {

                    List<Scooter> scootersInLocation = map[i][j];
                    int distance = distanceBetween(new Location(j,i), startingPoint);

                    if (distance < range && distance < closestScooterDistance) {
                        for (Scooter s : scootersInLocation) {
                            if (!s.isInUse()) {
                                closestScooter = s;
                                closestScooterDistance = distance;
                            }
                        }
                    }
                }
            }

            if (closestScooter != null) closestScooter.setInUse(true);
            return closestScooter;

        } finally {
            mapLock.unlock();
        }
    }

    public ArrayList<Scooter> getFreeScootersWithinRange(int range, Location startingPoint) {

        try {

            mapLock.lock();

            ArrayList<Scooter> results = new ArrayList<>();

            for (int i = 0; i < map.length; i++) {

                for (int j = 0; j < map[i].length; j++) {

                    List<Scooter> scootersInLocation = map[i][j];
                    int distance = distanceBetween(new Location(j,i), startingPoint);

                    for (Scooter s: scootersInLocation) {
                        if (distance < range && !s.isInUse()) {
                            results.add(s);
                        }
                    }
                }
            }

            return results;

        } finally {
            mapLock.unlock();
        }
    }

    /**
     * Calculates the distance between to points ({@link Location}), using
     * the Manhattan distance: {@code |ax - bx| + |ay - by|}.
     * @param a First location.
     * @param b Second location.
     * @return Distance between the points.
     */
    public int distanceBetween(@NotNull Location a, @NotNull Location b) {

        return Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y());
    }

    /**
     * Updates the location of a scooter in the scooter map.
     *
     * @param a The old location of the scooter.
     * @param b The new location of the scooter.
     * @param scooterId The identification of the scooter to update.
     */
    public void updateScooterLocation(Location a, Location b, String scooterId) {

        // Iterate through the scooters at the old location.
        for (Scooter s : map[a.y()][a.x()]) {
            // If a scooter is found with the specified ID, update its location to the new location.
            if (s.getScooterId().equals(scooterId)) {
                s.setLocation(b);
            }
        }
    }
}
