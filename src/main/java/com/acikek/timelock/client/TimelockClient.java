package com.acikek.timelock.client;

import com.acikek.timelock.network.TimelockNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
        System.out.println("received put: " + chunkData);
        System.out.println(chunkData);
    }

    public static void updateData(List<ChunkPos> chunks, Optional<Long> time) {
        System.out.println("received update: " + chunks);
        for (var chunk : chunks) {
            time.ifPresent(value -> chunkData.put(chunk, value));
            if (time.isEmpty()) {
                chunkData.remove(chunk);
            }
            TimelockClient.tick(chunk, true);
        }
    }

    public static void startSelection(Identifier zone) {
        var player = MinecraftClient.getInstance().player;
        if (selectionZone != null) {
            player.sendMessage(Text.translatable("error.timelock.selection_in_progress").formatted(Formatting.RED));
            return;
        }
        selectionZone = zone;
        player.sendMessage(Text.translatable("command.timelock.selection.start_client", zone));
    }

    public static void clearSelection() {
        selectionZone = null;
        selectionChunks.clear();
    }

    public static void sendSelection() {
        var buf = PacketByteBufs.create();
        buf.writeIdentifier(selectionZone);
        buf.writeCollection(selectionChunks, PacketByteBuf::writeChunkPos);
        ClientPlayNetworking.send(TimelockNetworking.SEND_SELECTION, buf);
        MinecraftClient.getInstance().player.sendMessage(
                Text.translatable("command.timelock.selection.commit_client", selectionChunks.size(), selectionZone)
        );
        clearSelection();
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
