package com.acikek.timelock.client;

import com.acikek.timelock.network.TimelockNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;

import java.util.*;

@Environment(EnvType.CLIENT)
public class TimelockClient implements ClientModInitializer {

    private static Map<ChunkPos, Long> chunkData = new HashMap<>();

    private static ChunkPos timelockChunk = null;
    private static Long timelockValue = null;

    private static Identifier selectionZone = null;
    private static final List<ChunkPos> selectionChunks = new ArrayList<>();

    public static Optional<Long> timelock() {
        return Optional.ofNullable(timelockValue);
    }

    public static boolean isInTimelock(ChunkPos pos) {
        return timelockChunk != null && timelockChunk.equals(pos);
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

    public static void startSelection(Identifier zone) {
        selectionZone = zone;
    }

    public static void clearSelection() {
        selectionZone = null;
        selectionChunks.clear();
    }

    public static void tick(ChunkPos pos, boolean inTimelock) {
        if (isInTimelock(pos) == inTimelock) {
            timelockValue = chunkData.get(pos);
            timelockChunk = timelockValue == null ? pos : null;
        }
    }

    public static void tick(ChunkPos pos) {
        tick(pos, false);
    }

    @Override
    public void onInitializeClient() {
        TimelockNetworking.registerClient();
    }
}
