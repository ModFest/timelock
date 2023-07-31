package com.acikek.timelock.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.ChunkPos;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class TimelockClient implements ClientModInitializer {

    public static Map<ChunkPos, Integer> timelockData = new HashMap<>();

    public static ChunkPos lockingChunk;
    public static long time;

    public static long targetTime;
    public static long increment;
    public static long targetEnding;

    public static void updateLockingChunk(ChunkPos chunkPos) {
        lockingChunk = chunkPos;
        targetTime = timelockData.get(chunkPos);
        increment = (targetTime - time) / 20L;
        if (time == -1) {
            time = getTimeOfDay();
        }
    }

    public static void resetLockingChunk(boolean isEnding) {
        lockingChunk = null;
        targetTime = -1L;
        if (isEnding) {
            targetEnding = estimateEnding();
            increment = (targetEnding - time) / 20L;
        }
    }

    public static boolean canIncrement(long target) {
        return (increment > 0 && time < target) || (increment < 0 && time > target);
    }

    public static long getTimeOfDay() {
        return MinecraftClient.getInstance().world.getLevelProperties().getTimeOfDay();
    }

    public static long estimateEnding() {
        return getTimeOfDay() + 2L;
    }

    @Override
    public void onInitializeClient() {
        resetLockingChunk(false);
        timelockData.put(new ChunkPos(1, 1), 19000);

        ClientTickEvents.END_WORLD_TICK.register(world -> {
            if (targetTime != -1L && canIncrement(targetTime)) {
                System.out.println(increment);
                time += increment;
            }
            else if (targetEnding != -1L && time != -1L) {
                if (canIncrement(getTimeOfDay())) {
                    System.out.println(increment);
                    time += increment;
                }
                else {
                    time = -1L;
                    increment = 0;
                    targetEnding = -1L;
                }
            }
        });
    }
}
