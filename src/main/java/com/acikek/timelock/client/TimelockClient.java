package com.acikek.timelock.client;

import com.acikek.timelock.TimelockChunk;
import com.acikek.timelock.network.TimelockNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.ChunkPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class TimelockClient implements ClientModInitializer {

    private static Map<ChunkPos, Long> chunkData = new HashMap<>();
    private static TimelockChunk timelock = null;

    public static Optional<TimelockChunk> timelock() {
        return Optional.ofNullable(timelock);
    }

    public static boolean isInTimelock(ChunkPos pos) {
        return timelock != null && timelock.pos().equals(pos);
    }

    public static void putData(Map<ChunkPos, Long> chunkData) {
        TimelockClient.chunkData = chunkData;
        TimelockClient.chunkData.put(new ChunkPos(1, 1), 19000L);
        System.out.println(chunkData);
    }

    public static void updateData(ChunkPos pos, Optional<Long> time) {
        time.ifPresent(value -> chunkData.put(pos, value));
        if (time.isEmpty()) {
            chunkData.remove(pos);
        }
        TimelockClient.tick(pos, true);
    }

    public static void tick(ChunkPos pos, boolean inTimelock) {
        if (isInTimelock(pos) != inTimelock) {
            return;
        }
        Long time = chunkData.get(pos);
        if (time == null) {
            if (timelock != null) {
                timelock = null;
            }
            return;
        }
        timelock = new TimelockChunk(pos, time);
    }

    public static void tick(ChunkPos pos) {
        tick(pos, false);
    }

    @Override
    public void onInitializeClient() {
        TimelockNetworking.registerClient();
    }
}
