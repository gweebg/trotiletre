package org.trotiletre.client.workers;

import org.trotiletre.common.communication.Demultiplexer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * A class that listens for notification messages and prints them to the console.
 */
public class NotificationListener implements Runnable {

    private final Demultiplexer demultiplexer; // A Demultiplexer object used to receive notification messages.

    /**
     * Constructs a new {@code NotificationListener} object.
     *
     * @param demultiplexer a {@link Demultiplexer} object used to receive notification messages
     */
    public NotificationListener(Demultiplexer demultiplexer) {
        this.demultiplexer = demultiplexer;
    }

    /**
     * Runs the notification listener.
     */
    @Override
    public void run() {

        // Loop indefinitely.
        while (true) {

            try {
                /*
                 * The message we are expecting to receive will have:
                 *  + locations, a byte encoded string.
                 */

                // Receiving the notification data from the queue on the demultiplexer.
                byte[] notificationData = demultiplexer.receive(1);

                // Unpacking the binary encoded string into a normal UTF-8 encoded string.
                String locations = new String(notificationData, StandardCharsets.UTF_8);

                // Print the notification message
                System.out.println("trotiletre.notif> New rewards found at: " + locations);

            } catch (IOException | InterruptedException e) {
                // If there is an exception, wrap it in a RuntimeException and throw it.
                throw new RuntimeException(e);
            }
        }
    }
}
