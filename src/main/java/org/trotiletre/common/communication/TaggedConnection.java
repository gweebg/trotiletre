package org.trotiletre.common.communication;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A class that represents a connection with tagged frames.
 * <p>
 * This class wraps a socket and provides methods to send and receive frames, where each frame has a tag
 * and a payload of data. The tags are used to identify the type of data contained in the frame.
 */
public class TaggedConnection implements AutoCloseable {

    private final Socket socket; // The wrapped socket.
    private final DataInputStream in; // The input stream.
    private final DataOutputStream out; // The output stream.

    private ReentrantLock sendLock = new ReentrantLock(); // Lock for sending frames.
    private ReentrantLock receiveLock = new ReentrantLock(); // Lock for receiving frames.

    /**
     * Constructs a new tagged connection from the given socket.
     *
     * @param socket The socket to wrap.
     * @throws IOException If an error occurs while creating the input and output streams.
     */
    public TaggedConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        this.out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    /**
     * Sends the given frame over the connection.
     * <p>
     * This method sends the tag and data payload of the frame over the connection.
     *
     * @param frame The frame to send.
     * @throws IOException If an error occurs while sending the frame.
     */
    public void send(Frame frame) throws IOException {
        this.send(frame.tag, frame.data);
    }

    /**
     * Sends a frame with the given tag and data payload over the connection.
     *
     * @param tag  The tag of the frame.
     * @param data The data payload of the frame.
     * @throws IOException If an error occurs while sending the frame.
     */
    public void send(int tag, byte[] data) throws IOException {

        try {
            this.sendLock.lock();

            out.writeInt(tag);
            out.writeInt(data.length);
            out.write(data);

            out.flush();

        } finally {

            this.sendLock.unlock();

        }
    }

    /**
     * Receives a frame from the connection.
     *
     * @return The received frame.
     * @throws IOException If an error occurs while receiving the frame.
     */
    public Frame receive() throws IOException {

        try {

            receiveLock.lock();

            int tag = in.readInt();
            int size = in.readInt();
            byte[] data = new byte[size];

            in.readFully(data);
            return new Frame(tag, data);

        } finally {

            receiveLock.unlock();
        }
    }

    /**
     * Closes the socket.
     *
     * @throws Exception If an error occurs while closing the frame.
     */
    @Override
    public void close() throws Exception {
        socket.close();
    }

    /**
     * A class that represents a frame.
     * <p>
     * A frame consists of a tag and a payload of data.
     */
    public static class Frame {

        public final int tag;
        public final byte[] data;

        /**
         * Constructs a new frame with the given tag and data.
         *
         * @param tag  The tag for the frame.
         * @param data The payload of data for the frame.
         */
        public Frame(int tag, byte[] data) {
            this.tag = tag;
            this.data = data;
        }
    }
}
