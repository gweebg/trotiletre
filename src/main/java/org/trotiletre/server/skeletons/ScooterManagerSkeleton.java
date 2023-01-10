package org.trotiletre.server.skeletons;

import org.trotiletre.common.AnswerTag;
import org.trotiletre.common.communication.Skeleton;
import org.trotiletre.models.utils.GenericPair;
import org.trotiletre.models.utils.Location;
import org.trotiletre.server.services.AuthenticationManager;
import org.trotiletre.server.services.ResponseManager;
import org.trotiletre.server.services.RewardManager;
import org.trotiletre.server.services.ScooterManager;

import java.io.*;
import java.net.SocketAddress;
import java.util.List;

/**
 * A class that implements the {@link Skeleton} interface for the {@link ScooterManager} class.
 * <p>
 * This class delegates the handling of input and output streams to the server instance.
 */
public class ScooterManagerSkeleton implements Skeleton {

    private final ScooterManager scooterManager; // The scooterManager instance to delegate to.
    private final AuthenticationManager authManager; // The authManager instance to delegate to.
    private final ResponseManager responseManager;
    private final RewardManager rewardManager;

    /**
     * Constructs a new skeleton implementation for the given server instance.
     *
     * @param scooterManager The server instance.
     */
    public ScooterManagerSkeleton(ScooterManager scooterManager, ResponseManager responseManager, RewardManager rewardManager) {
        this.authManager = scooterManager.getAuthManager();
        this.scooterManager = scooterManager;
        this.responseManager = responseManager;
        this.rewardManager = rewardManager;
    }

    /**
     * This method, receives the data from a request from a user, processes it and
     * replies to the client with the answer.
     *
     * @param data          Data received from the connection.
     * @param socketAddress The socket to the client object.
     * @throws Exception If it can't communicate with the client.
     */
    @Override
    public void handle(byte[] data, SocketAddress socketAddress) throws Exception {

        /* Unwrapping the data obtained in 'data' argument. */
        ByteArrayInputStream dataStream = new ByteArrayInputStream(data);
        DataInput payload = new DataInputStream(dataStream);

        int operation = payload.readInt(); // Operation value.

        if (operation == 0) {

            /*
             * This section handles the requests for listing the free scooters.
             * The message we are expecting to receive will have:
             *  + x coordinate of the user's position: Integer.
             *  + y coordinate of the user's position: Integer.
             *  + The range of the search for scooters: Integer.
             *
             *  This operation requires no authentication, and since the worst outcome
             *  is not having any scooters, we can just send the list with the
             *  coordinates as a string via the connection.
             */

            var locationX = payload.readInt(); // X coordinate of the starting position.
            var locationY = payload.readInt(); // Y coordinate of the starting position.
            var range = payload.readInt(); // Range for the search.

            // Executing the request by delegating the message to the system manager.
            String listedScooters = scooterManager.listFreeScooters(
                    range,
                    new Location(locationX, locationY)
            );

            System.out.println("server> Client asked requested for the list of scooters.");

            // Sending to the user the obtained results.
            responseManager.send(socketAddress, listedScooters.getBytes(), AnswerTag.ANSWER.tag);
        }

        if (operation == 1) {

            /*
             * This section handles the requests for renting a free scooters.
             * The message we are expecting to receive will have:
             *  + The range of the search for scooters: Integer.
             *  + x coordinate of the user's position: Integer.
             *  + y coordinate of the user's position: Integer.
             *  + The user's username, used to check whether he is authenticated.
             *
             *  This operation requires authentication.
             */

            // New byte array stream to put our results.
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            DataOutput dataOutput = new DataOutputStream(output);

            int range = payload.readInt(); // Range for the search.
            int locationX = payload.readInt(); // X coordinate of the starting position.
            int locationY = payload.readInt(); // Y coordinate of the starting position.
            String username = payload.readUTF(); // User's username, for authentication purpouses.

            System.out.println("server> User '" + username + "' requested a scooter.");

            /* Before running the task, let's check whether the user is logged in or not. */
            if (!authManager.isUserOnline(username)) {

                System.out.println("server> User '" + username + "' is not logged in.");
                dataOutput.writeInt(2);
                responseManager.send(socketAddress, output.toByteArray(), AnswerTag.ANSWER.tag);
                return;
            }

            // Reserving the scooter for the client.
            GenericPair<String, Location> reservationStatus = scooterManager.reserveScooter(
                    range,
                    new Location(locationX, locationY),
                    username
            );

            /*
             * Now we need to encapsulate the results obtained and send them to the client.
             * Since each response has more than one parameter we need to create our simple PDU:
             *   [response_code] | (reservation_code) | (x_location) | (y_location)
             *
             * Values within () are optional.
             *
             * The response code can be:
             *   0 - Normal behaviour;
             *   1 - There are no scooters available;
             *   2 - User is not logged in.
             */

            if (reservationStatus == null) dataOutput.writeInt(1);
            else {

                dataOutput.writeInt(0); // Response code.
                dataOutput.writeUTF(reservationStatus.getFirst()); // Reservation code as string.

                Location loc = reservationStatus.getSecond();

                dataOutput.writeInt(loc.x()); // Location x coordinate.
                dataOutput.writeInt(loc.y()); // Location y coordinate.

                rewardManager.signal();
            }

            // Packing the data onto a byte[] and sending to the client.
            byte[] responseData = output.toByteArray();
            responseManager.send(socketAddress, responseData, AnswerTag.ANSWER.tag);
        }

        if (operation == 2) {

            /*
             * This section handles the requests for parking a scooter.
             * The message we are expecting to receive will have:
             *  + The reservation identification of the scooter: UTF.
             *  + x coordinate of the new position: Integer.
             *  + y coordinate of the new position: Integer.
             *  + The user's username, used to check whether he is authenticated.
             *
             *  This operation requires authentication.
             */

            // New byte array stream to put our results.
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            DataOutput dataOutput = new DataOutputStream(output);

            String reservationCode = payload.readUTF(); // Reservation ID, used to obtain the scooter.
            int locationX = payload.readInt(); // X coordinate of the starting position.
            int locationY = payload.readInt(); // Y coordinate of the starting position.
            String username = payload.readUTF(); // User's username, for authentication purpouses.

            System.out.println("server> User '" + username + "' requested to park a scooter.");

            /* Before running the task, let's check whether the user is logged in or not. */
            if (!authManager.isUserOnline(username)) {

                System.out.println("server> User '" + username + "' is not logged in.");

                dataOutput.writeInt(2);
                responseManager.send(socketAddress, output.toByteArray(), AnswerTag.ANSWER.tag);
                return;
            }

            // Delegating the request to the scooter manager service.
            GenericPair<Double, Double> parkingStatus = scooterManager.parkScooter(
                    reservationCode,
                    new Location(locationX, locationY),
                    username
            );

            rewardManager.signal();

            /*
             * Once again, we need to encapsulate the results obtained and send them to the client.
             * Our PDU, this time will be represented by:
             *   [response_code] | [price] | (bounty_price)
             *
             * Values within () are optional.
             *
             * The response code can be:
             *   0 - Normal behaviour;
             *   1 - Reservation ID is not valid;
             *   2 - User is not logged in.
             *   3 - Packet includes a bounty price.
             */

            int response_code = 1;

            if (parkingStatus == null) dataOutput.writeInt(response_code);
            else {

                Double price = parkingStatus.getFirst(); // Price of the renting.
                Double bountyPrice = parkingStatus.getSecond(); // Price of the bounty, if available.

                if (bountyPrice == null) response_code = 0;
                else response_code = 3;

                dataOutput.writeInt(response_code); // Response code.
                dataOutput.writeDouble(price); // Writing price.

                if (bountyPrice != null) dataOutput.writeDouble(bountyPrice); // Writing bounty is available.
            }

            // Packing and sending the data to the client.
            byte[] responseData = output.toByteArray();
            responseManager.send(socketAddress, responseData, AnswerTag.ANSWER.tag);
        }

        if (operation == 3) {

            /*
             * This section handles the requests for listing rewards.
             * The message we are expecting to receive will have:
             *  + The reservation identification of the scooter: UTF.
             *  + x coordinate of the new position: Integer.
             *  + y coordinate of the new position: Integer.
             *  + The user's username, used to check whether he is authenticated.
             *
             *  This operation requires authentication.
             * public List<RewardPath> getRewardPaths(Location start, int radius)
             */

            // New byte array stream to put our results.
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            DataOutput dataOutput = new DataOutputStream(output);

            int locationX = payload.readInt(); // X coordinate of the starting position.
            int locationY = payload.readInt(); // Y coordinate of the starting position.
            int range = payload.readInt(); // Search range.

            System.out.println("server> User for listing of rewards.");

            // Delegating the request to the reward manager service.
            List<RewardManager.RewardPath> rewardList = rewardManager.getRewardPaths(
                    new Location(locationX, locationY),
                    range
            );

            dataOutput.writeInt(rewardList.size());

            for (RewardManager.RewardPath r : rewardList) {
                dataOutput.writeUTF(r.toString());
            }

            // Packing and sending the data to the client.
            byte[] responseData = output.toByteArray();
            responseManager.send(socketAddress, responseData, AnswerTag.ANSWER.tag);
        }
    }

}
