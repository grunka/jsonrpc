package se.grunka.jsonrpc;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class Shard {
    private final int number;
    private final FileLock lock;
    private final FileChannel channel;


    public Shard(int number, FileLock lock, FileChannel channel) {
        this.number = number;
        this.lock = lock;
        this.channel = channel;
    }


    public int number() {
        return number;
    }


    public void release() {
        try {
            lock.release();
            channel.close();
        } catch (IOException e) {
            throw new Error("Failure while releasing lock", e);
        }
    }
}
