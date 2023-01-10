package org.trotiletre.models;

import org.trotiletre.models.utils.Location;

/**
 * A class that represents a scooter in the  application.
 * <p>
 * This class stores information about the scooter's ID, location, and usage status.
 */
public class Scooter {

    private String scooterId; // Scooter identification.
    private Location location; // Scooter location.
    private boolean inUse; // Indicates whether the scooter is being used or not.

    /**
     * Constructs a new scooter with the given information.
     *
     * @param scooterId The ID of the scooter.
     * @param location  The location of the scooter.
     * @param inUse     Whether the scooter is in use or not.
     */
    public Scooter(String scooterId, Location location, boolean inUse) {
        this.scooterId = scooterId;
        this.location = location;
        this.inUse = false;
    }

    /**
     * Returns the ID of the scooter.
     *
     * @return The ID of the scooter.
     */
    public String getScooterId() {
        return scooterId;
    }

    /**
     * Sets the ID of the scooter.
     *
     * @param scooterId The new ID of the scooter.
     */
    public void setScooterId(String scooterId) {
        this.scooterId = scooterId;
    }

    /**
     * Returns the location of the scooter.
     *
     * @return The location of the scooter.
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Sets the location of the scooter.
     *
     * @param location The new location of the scooter.
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Returns the state of the scooter.
     *
     * @return {@code true} if it is in use, {@code false} otherwise.
     */
    public boolean isInUse() {
        return inUse;
    }

    /**
     * Sets the inUse state of the scooter.
     *
     * @param inUse The new state of the scooter.
     */
    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }
}
