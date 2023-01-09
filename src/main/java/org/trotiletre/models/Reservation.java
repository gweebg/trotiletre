package org.trotiletre.models;

import org.trotiletre.models.Scooter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public class Reservation {

    private UUID reservationId; // The reservation identification, a UUID object.
    private LocalDateTime reservationTimestamp = LocalDateTime.now(); // The instant the reservation was made.
    private String user; // The username of the user who made the reservation.
    private Scooter scooter; // The scooter that got reserved.

    /**
     * Constructor for the {@code Reservation} class.
     *
     * @param reservationId a {@link UUID} object representing the reservation ID.
     * @param username a {@link String} representing the username.
     * @param scooter a {@link Scooter} representing the scooter reserved.
     */
    public Reservation(UUID reservationId, String username, Scooter scooter) {
        this.reservationId = reservationId;
        this.user = username;
        this.scooter = scooter;
    }

    /**
     * Calculates and returns the price of a trip.
     *
     * @param distance an {@code int} representing the distance of the trip
     * @param end a {@link java.time.LocalDateTime} object representing the end time of the trip
     * @return the price of the trip as a {@code double}
     */
    public double getPriceOfTrip(int distance, LocalDateTime end) {

        // The price for a trip is based on the distance percurred and its duration.
        // We can say that, price(dist, dur) = dist * 0.10 + duration * 0.20

        Duration durationOfTrip = Duration.between(reservationTimestamp, end);
        System.out.println(durationOfTrip.toMinutes());
        return distance * 0.1 + durationOfTrip.toMinutes() * 0.2;
    }

    public Scooter getScooter() {
        return scooter;
    }
}
