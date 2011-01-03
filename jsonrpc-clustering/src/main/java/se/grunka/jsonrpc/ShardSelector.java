package se.grunka.jsonrpc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class ShardSelector {
    public static Shard select(String path, String name, int totalShards) {
        final File shardDirectory = new File(path, name);
        if (!shardDirectory.exists() && !shardDirectory.mkdirs()) {
            throw new Error("Could not create shard directory");
        }
        try {
            for (int shard = 0; shard < totalShards; shard++) {
                FileChannel channel = getChannel(shardDirectory, shard);
                FileLock lock = channel.tryLock();
                if (lock != null) {
                    return new Shard(shard, lock, channel);
                }
                channel.close();
            }
        } catch (IOException e) {
            throw new Error("Failure while trying to lock", e);
        }

        return waitForFreeShard(shardDirectory, totalShards);
    }


    private static Shard waitForFreeShard(File shardDirectory, int totalShards) {
        try {
            while (true) {
                for (int shard = 0; shard < totalShards; shard++) {
                    FileChannel channel = getChannel(shardDirectory, shard);
                    FileLock lock = channel.tryLock();
                    if (lock != null) {
                        return new Shard(shard, lock, channel);
                    }
                    channel.close();
                }
                Thread.sleep(100);
            }
        } catch (IOException e) {
            throw new Error("Failure while trying to lock", e);
        } catch (InterruptedException e) {
            throw new Error("Interrupted while waiting for shard", e);
        }
    }


    private static FileChannel getChannel(File shardDirectory, int shard) {
        try {
            File lockFile = new File(shardDirectory, shard + ".lock");
            return new RandomAccessFile(lockFile, "rw").getChannel();
        } catch (FileNotFoundException e) {
            throw new Error("Could not create file", e);
        }
    }
}
