package org.trotiletre.server.services;

import org.jetbrains.annotations.NotNull;
import org.trotiletre.models.Scooter;
import org.trotiletre.models.utils.Location;

import java.util.*;
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
    private final int startingScooters;

    private final ReentrantLock mapLock = new ReentrantLock();

    public ScooterMap(final int mapSize, final int startingScooters) {

        this.map = new ArrayList[mapSize][mapSize]; // Creating the map with the provided size.

        // Initializing the map with empty lists of scooters.
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                map[i][j] = new ArrayList<>();
            }
        }

        this.startingScooters = startingScooters;
    }

    public int getMapSize(){
        return this.map.length;
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
            }

            show();
        } finally {
          mapLock.unlock();
        }

    }

    public Scooter getClosestScooterWithinRange(int range, Location startingPoint, int size) {

        try {

            mapLock.lock();

            int closestScooterDistance = size + 1;
            Scooter closestScooter = null;

            for (int i = 0; i < map.length; i++) {

                for (int j = 0; j < map[i].length; j++) {

                    List<Scooter> scootersInLocation = map[i][j];
                    int distance = distanceBetween(new Location(j,i), startingPoint);

                    if (distance <= range && distance <= closestScooterDistance) {

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
                    int distance = distanceBetween(startingPoint, new Location(j,i));

                    if (distance <= range) {
                        for (Scooter s: scootersInLocation) {
                            if (!s.isInUse()) {
                                results.add(s);
                            }
                        }
                    }
                }
            }

            return results;

        } finally {
            mapLock.unlock();
        }
    }

    public int getNumberOfScootersAt(int x, int y){
        this.mapLock.lock();
        try {
            return this.map[y][x].size();
        } finally {
            this.mapLock.unlock();
        }
    }


    public Map<Location, Set<Location>> getRewardPaths(int emptyRadius){
        this.mapLock.lock();
        try {
            Map<Location, Set<Location>> rewardPaths = new HashMap<>();
            if(emptyRadius<=0)
                return rewardPaths;

            Set<Location> startList = new HashSet<>();
            Set<Location> finishList = new HashSet<>();

            for(int y=0;y<this.map.length;++y){
                for(int x=0;x<this.map.length;++x){
                    if(this.map[y][x].size()>1)
                        startList.add(new Location(x,y));
                    else if(this.map[y][x].size()==0){
                        boolean shouldHaveReward=true;
                        for(int i=emptyRadius;i>-emptyRadius && shouldHaveReward;--i){
                            int yy=y+i;
                            if(i==0 || yy<0 || yy>=this.map.length)
                                continue;
                            for(int j=emptyRadius;j>-emptyRadius && shouldHaveReward;--j){
                                int xx=x+j;
                                if(j==0 || xx<0 || xx>=this.map.length)
                                    continue;
                                if (this.map[yy][xx].size() > 0) {
                                    shouldHaveReward = false;
                                }
                            }
                        }
                        if(shouldHaveReward)
                            finishList.add(new Location(x,y));
                    }
                }
            }
            for(Location start : startList)
                rewardPaths.put(start, finishList);

            return rewardPaths;

        } finally {
            this.mapLock.unlock();
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
                map[b.y()][b.x()].add(s);
                map[a.y()][a.x()].remove(s);
                show();
                return; // For some reason if this return; is removed the code breaks ?
            }
        }
    }

    /**
     * This method prints the current location of all scooters in the map.
     * Each scooter's location is represented by a string, and the map is
     * printed as a matrix of strings.
     */
    private void show() {

        for (List<Scooter>[] lists : map) {
            for (List<Scooter> list : lists) {
                for (Scooter s : list) System.out.print(s.getLocation().toString() + " ");
                System.out.print(" | ");
            }
            System.out.print("\n");
        }
    }
}
