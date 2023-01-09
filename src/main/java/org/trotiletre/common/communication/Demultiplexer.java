package org.trotiletre.common.communication;

import org.trotiletre.common.communication.TaggedConnection.Frame;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A class that demultiplexes incoming data from a client over
 * a {@link TaggedConnection} into separate {@link Frame} objects.
 */
public class Demultiplexer {

    private TaggedConnection connection; // TCP tagged connection with the client.
    private HashMap<Integer, FrameBuffer> bufferMap = new HashMap<>(); // Map from tag to FrameBuffer, congestion avoidance.

    private IOException exception = null; // Needed to wake up threads if an exception was caught.
    private ReentrantLock demultiplexerLock = new ReentrantLock(); // Lock for thread safe usage.

    /**
     * A class that represents a buffer for incoming {@link Frame} objects.
     */
    public class FrameBuffer {
        public int waiters = 0; // The number of messages waiting on this FrameBuffer.
        public Queue<byte[]> queue = new ArrayDeque<>(); // A queue of byte arrays that represent the incoming Frame data.
        public Condition c = demultiplexerLock.newCondition(); // A condition object used to signal waiting threads when new data is available in the buffer.
    }

    /**
     * Constructs a new {@code Demultiplexer} object.
     *
     * @param c a {@link TaggedConnection} object representing the connection to the client
     */
    public Demultiplexer(TaggedConnection c){
        this.connection = c;
    }

    /**
     * Starts the demultiplexing process.
     */
    public void start() {

        // Demultiplexer thread worker.
        Runnable worker = () -> {
            try {
                // Loop indefinitely.
                while (true) {

                    // Receive a frame from the connection.
                    TaggedConnection.Frame frame = connection.receive();
                    demultiplexerLock.lock();
                    try {
                        // Get the FrameBuffer for the received frame's tag.
                        FrameBuffer buffer = bufferMap.get(frame.tag);

                        // If there is no FrameBuffer for this tag, create a new one.
                        if (buffer == null) {
                            buffer = new FrameBuffer();
                            bufferMap.put(frame.tag, buffer);
                        }

                        // Add the frame data to the queue and signal any waiting threads.
                        buffer.queue.add(frame.data);
                        buffer.c.signal();
                    }
                    finally {
                        demultiplexerLock.unlock();
                    }
                }
            }
            catch (IOException err) {
                // If there is an exception, store it in the exception field
                exception = err;
            }
        };

        // Create a new thread to perform the demultiplexing.
        new Thread(worker).start(); // Starts the demultiplexing thread.
    }

    /**
     * Receives a frame with the specified tag.
     *
     * @param tag The tag of the frame to receive.
     * @return The data of the received frame as a {@code byte} array.
     * @throws IOException If an I/O error occurs.
     * @throws InterruptedException If the thread is interrupted while waiting for a frame to be received.
     */
    public byte[] receive(int tag) throws IOException, InterruptedException {

        try {
            demultiplexerLock.lock();
            FrameBuffer buffer;

            // Get the FrameBuffer for the specified tag.
            buffer = bufferMap.get(tag);

            // If there is no FrameBuffer for this tag, create a new one.
            if (buffer == null) {
                buffer = new FrameBuffer();
                bufferMap.put(tag, buffer);
            }

            // Increment the number of waiters on this FrameBuffer.
            buffer.waiters++;
            while(true) {

                // If the queue is not empty, return the first element.
                if(!buffer.queue.isEmpty()) {

                    buffer.waiters--;
                    byte[] reply = buffer.queue.poll();

                    // If the queue is now empty and there are no more waiters, remove the FrameBuffer from the map.
                    if (buffer.waiters == 0 && buffer.queue.isEmpty())
                        bufferMap.remove(tag);

                    return reply;
                }

                // If an exception occurred while receiving frames, throw it.
                if (exception != null) {
                    throw exception;
                }

                // If the queue is empty, wait for a new frame to be received.
                buffer.c.await();
            }
        }
        finally { demultiplexerLock.unlock(); }
    }

    /**
     * Closes the {@link TaggedConnection} and releases any resources associated with it.
     *
     * @throws IOException if an I/O error occurs while closing the connection
     */
    public void close() throws IOException {
        try {
            // Close the TaggedConnection.
            connection.close();
        } catch (Exception err) {
            // If there is an exception, wrap it in a RuntimeException and throw it.
            throw new RuntimeException(err);
        }
    }
}