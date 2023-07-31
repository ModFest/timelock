package com.acikek.timelock.client;

import com.acikek.timelock.TimelockData;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.ChunkPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class TimelockClient implements ClientModInitializer {

    private static Map<ChunkPos, Long> chunkData = new HashMap<>();
    private static TimelockData timelock = null;

    public static Optional<TimelockData> timelock() {
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
        timelock = new TimelockData(pos, time);
    }

    @Override
    public void onInitializeClient() {
        chunkData.put(new ChunkPos(1, 1), 19000L);
    }
}
