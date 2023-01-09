package org.trotiletre.models;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.trotiletre.models.utils.Location;

/**
 * A class that represents a user in the application.
 * <p>
 * This class stores information about the user's username, password hash, balance,
 * distance traveled, and number of rides taken.
 */
public class User {

    private String username; // 'username' acts as the identification of a user.
    private String passwordHash; // Hashed password with the algoritm Argon2.
    private Double balance = 100.0d; // User balance.
    private Double distanceTraveled = 0.0d; // Distance that the user has travelled.
    private int amountRides = 0; // Amount of rides user has taken.
    private boolean notificationsAllowed = false; // Whether push notifications are allowed.

    public User(String username, String hashedPassword) {
        this.username = username;
        this.passwordHash = hashedPassword;
    }

    public User(String username) {
        this.username = username;
    }

    /**
     * Sets the password for the user.
     * <p>
     * The password is encoded using the Argon2 algorithm before being stored.
     *
     * @param password The plaintext password to be set.
     */
    public void setPassword(String password) {
        Argon2PasswordEncoder encoder = new Argon2PasswordEncoder(32, 64, 1, 15*1024, 2);
        this.passwordHash = encoder.encode(password);
    }

    /**
     * Checks if the provided password is correct or not.
     *
     * @param password The hashed password to test.
     * @return {@code true} if they match, {@code false} otherwise.
     */
    public boolean matchPassword(String password) {
        Argon2PasswordEncoder encoder = new Argon2PasswordEncoder(32, 64, 1, 15*1024, 2);
        return encoder.matches(password, passwordHash);
    }

    /**
     * Returns the username of the user.
     *
     * @return The username of the user.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the user.
     *
     * @param username The new username for the user.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the password hash of the user.
     *
     * @return The password hash of the user.
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Sets the password hash of the user.
     *
     * @param passwordHash The new password hash for the user.
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Returns the balance of the user.
     *
     * @return The balance of the user.
     */
    public Double getBalance() {
        return balance;
    }

    /**
     * Sets the balance of the user.
     *
     * @param balance The new balance for the user.
     */
    public void setBalance(Double balance) {
        this.balance = balance;
    }

    /**
     * Returns the distance traveled by the user.
     *
     * @return The distance traveled by the user.
     */
    public Double getDistanceTraveled() {
        return distanceTraveled;
    }

    /**
     * Sets the distance traveled by the user.
     *
     * @param distanceTraveled The new distance traveled by the user.
     */
    public void setDistanceTraveled(Double distanceTraveled) {
        this.distanceTraveled = distanceTraveled;
    }

    /**
     * Returns the number of rides taken by the user.
     *
     * @return The number of rides taken by the user.
     */
    public int getAmountRides() {
        return amountRides;
    }

    /**
     * Sets the number of rides taken by the user.
     *
     * @param amountRides The new number of rides taken by the user.
     */
    public void setAmountRides(int amountRides) {
        this.amountRides = amountRides;
    }

    public boolean isNotificationsAllowed() {
        return notificationsAllowed;
    }

    public void setNotificationsAllowed(boolean notificationsAllowed) {
        this.notificationsAllowed = notificationsAllowed;
    }
}
