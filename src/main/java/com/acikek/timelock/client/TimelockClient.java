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

    /*public static long targetTime;
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
    }*/

    public static long getTimeOfDay() {
        return MinecraftClient.getInstance().world.getLevelProperties().getTimeOfDay();
    }

    /*public static long estimateEnding() {
        return getTimeOfDay() + 2L;
    }*/

    @Override
    public void onInitializeClient() {
        chunkData.put(new ChunkPos(1, 1), 19000L);

        /*ClientTickEvents.END_WORLD_TICK.register(world -> {
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
        });*/
    }
}
