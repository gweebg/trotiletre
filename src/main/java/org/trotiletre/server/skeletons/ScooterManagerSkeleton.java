package org.trotiletre.server.skeletons;

import org.trotiletre.common.communication.Skeleton;
import org.trotiletre.common.communication.TaggedConnection;
import org.trotiletre.server.RewardThread;
import org.trotiletre.server.services.AuthenticationManager;
import org.trotiletre.server.services.ResponseManager;
import org.trotiletre.models.utils.Location;
import org.trotiletre.server.services.ScooterManager;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.nio.charset.StandardCharsets;

/**
 * A class that implements the {@link Skeleton} interface for the {@link ScooterManager} class.
 * <p>
 * This class delegates the handling of input and output streams to the server instance.
 */
public class ScooterManagerSkeleton implements Skeleton {

    private final ResponseManager responseManager;
    private final RewardThread rewardThread;
    private final ScooterManager scooterManager; // The server instance to delegate to.

    /**
     * Constructs a new skeleton implementation for the given server instance.
     *
     * @param server The server instance.
     */
    public ScooterManagerSkeleton(ScooterManager scooterManager, ResponseManager responseManager, RewardThread rewardThread) {
        this.scooterManager = scooterManager;
        this.responseManager = responseManager;
        this.rewardThread = rewardThread;
    }

    @Override
    public void handle(byte[] data, TaggedConnection connection) throws Exception {

        ByteArrayInputStream dataStream = new ByteArrayInputStream(data);
        DataInput payload = new DataInputStream(dataStream);

        int operation = payload.readInt();

        if (operation == 0) { // If operation is "List free scooters.".

            // The type of message is: x;y;range.
            var locationX = payload.readInt();
            var locationY = payload.readInt();
            var range = payload.readInt();

            String listedScooters = scooterManager.listFreeScooters(
                    range,
                    new Location(locationX, locationY)
            );

            System.out.println("Scooters: " + listedScooters);

            connection.send(0, listedScooters.getBytes());
        }

    }

}
