package org.trotiletre.common.communication;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * An interface for handling data streams.
 * <p>
 * This interface defines a single method for handling input and output streams.
 */
public interface Skeleton {

    /**
     * Handles the given input and output streams.
     *
     * @param receivedData The connection object.
     * @throws Exception If an error occurs while handling the streams.
     */
    public void handle(byte[] receivedData) throws Exception;
}
