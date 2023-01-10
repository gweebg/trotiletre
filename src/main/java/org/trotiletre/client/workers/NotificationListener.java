package org.trotiletre.client.workers;

import org.trotiletre.common.CommunicationTags;
import org.trotiletre.common.communication.Demultiplexer;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
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

                // Receiving the notification data from the queue on the demultiplexer.
                byte[] notificationData = demultiplexer.receive(CommunicationTags.NOTIFICATION.tag);

                DataInput dataInput = new DataInputStream(new ByteArrayInputStream(notificationData));
                int numPaths = dataInput.readInt();

                for(int i=0;i<numPaths;++i){
                    int x = dataInput.readInt();
                    int y = dataInput.readInt();
                    int fx = dataInput.readInt();
                    int fy = dataInput.readInt();
                    double reward = dataInput.readDouble();

                    System.out.println("\ntrotiletre.notif> Found reward: Start: ("+x+","+y+") End: ("+fx+","+fy+") Reward: "+reward+"€");
                }
                System.out.print("trotiletre>");

            } catch (IOException | InterruptedException e) {
                // If there is an exception, wrap it in a RuntimeException and throw it.
                throw new RuntimeException(e);
            }
        }
    }
}
