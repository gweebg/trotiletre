package org.trotiletre.common.communication;

import org.trotiletre.common.communication.TaggedConnection.Frame;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Demultiplexer {

    private TaggedConnection c;
    private ReentrantLock lock = new ReentrantLock();
    private HashMap<Integer, FrameBuffer> bufferMap = new HashMap<>();

    private IOException exception = null;

    public class FrameBuffer{

        public int waiters = 0;
        public Queue<byte[]> queue = new ArrayDeque<>();
        public Condition c = lock.newCondition();
    }

    public Demultiplexer(TaggedConnection c){
        this.c = c;
    }

    public void start() {
        new Thread(() -> {
            try {
                while (true) {
                    TaggedConnection.Frame frame = c.receive();
                    lock.lock();
                    try {
                        FrameBuffer f = bufferMap.get(frame.tag);
                        if (f == null) {
                            f = new FrameBuffer();
                            bufferMap.put(frame.tag, f);
                        }
                        f.queue.add(frame.data);
                        f.c.signal();
                    }
                    finally {
                        lock.unlock();
                    }
                }
            }
            catch (IOException e) {
                exception = e;
            }
        }).start();
    }

    public void send(Frame frame) throws IOException {
        c.send(frame);
    }
    public void send(int tag, byte[] data) throws IOException {
        c.send(tag,data);
    }
    public byte[] receive(int tag) throws IOException, InterruptedException {
        lock.lock();
        FrameBuffer f;
        try {
            f = bufferMap.get(tag);
            if (f == null) {
                f = new FrameBuffer();
                bufferMap.put(tag, f);
            }
            f.waiters++;
            while(true) {
                if(! f.queue.isEmpty()) {
                    f.waiters--;
                    byte[] reply = f.queue.poll();
                    if (f.waiters == 0 && f.queue.isEmpty())
                        bufferMap.remove(tag);
                    return reply;
                }
                if (exception != null) {
                    throw exception;
                }
                f.c.await();
            }
        }
        finally {
            lock.unlock();
        }
    }

    public void close() throws IOException {
        try {
            c.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}