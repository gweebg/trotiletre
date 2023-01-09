package org.trotiletre.models.utils;

/**
 * A record representing a location in two-dimensional space.
 * <p>
 * This record stores the x and y coordinates of the location.
 */
public record Location(int x, int y) {

    @Override
    public String toString() {
        return "("+ x + "," + y + ")";
    }

    public int manhattanDistance(Location other){
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }

}
