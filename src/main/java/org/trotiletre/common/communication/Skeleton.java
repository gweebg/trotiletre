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
     * @param in The input stream.
     * @param out The output stream.
     * @throws Exception If an error occurs while handling the streams.
     */
    public void handle(DataInputStream in, DataOutputStream out) throws Exception;
}
