package com.acikek.timelock.client;

import com.acikek.timelock.TimelockChunk;
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

    public static void update(ChunkPos pos) {
        if (timelock != null && timelock.pos().equals(pos)) {
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

    @Override
    public void onInitializeClient() {
        chunkData.put(new ChunkPos(1, 1), 19000L);
    }
}
