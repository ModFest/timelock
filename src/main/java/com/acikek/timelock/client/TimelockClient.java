package com.acikek.timelock.client;

import com.acikek.timelock.Timelock;
import com.acikek.timelock.network.TimelockNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.*;

@Environment(EnvType.CLIENT)
public class TimelockClient implements ClientModInitializer {

    private static final Map<ChunkPos, Long> chunkData = new HashMap<>();

    private static ChunkPos timelockChunk = null;
    private static Long timelockValue = null;

    private static Identifier selectionZone = null;
    private static Long selectionTime = null;
    private static final List<ChunkPos> selectionChunks = new ArrayList<>();

    public static Optional<Long> getCurrentTimelock() {
        return Optional.ofNullable(timelockValue);
    }

    public static boolean inTimelock(ChunkPos pos) {
        return timelockChunk != null && timelockChunk.equals(pos);
    }

    public static Long getChunkTimelock(ChunkPos pos) {
        if (selectionTime == null) {
            return chunkData.get(pos);
        }
        if (!selectionChunks.contains(pos)) {
            return null;
        }
        return selectionTime;
    }

    public static void putData(Map<ChunkPos, Long> chunkData) {
        Timelock.LOGGER.debug("Received PUT: {}", chunkData);
        TimelockClient.chunkData.clear();
        TimelockClient.chunkData.putAll(chunkData);
    }

    public static void updateData(List<ChunkPos> chunks, Optional<Long> time) {
        Timelock.LOGGER.debug("Received UPDATE: {}L for {}", time.orElse(-1L), chunks);
        for (var chunk : chunks) {
            time.ifPresent(value -> chunkData.put(chunk, value));
            if (time.isEmpty()) {
                chunkData.remove(chunk);
            }
            TimelockClient.tick(chunk, true);
        }
    }

    public static void startSelection(Identifier zone, long time, Collection<ChunkPos> existing) {
        var player = MinecraftClient.getInstance().player;
        if (selectionZone != null) {
            player.sendMessage(Text.translatable("error.timelock.selection_in_progress").formatted(Formatting.RED));
            return;
        }
        selectionZone = zone;
        selectionTime = time;
        selectionChunks.addAll(existing);
        player.sendMessage(Text.translatable("command.timelock.selection.start_client", zone));
    }

    public static void clearSelection() {
        selectionZone = null;
        selectionTime = null;
        selectionChunks.clear();
    }

    public static void sendSelection() {
        var buf = PacketByteBufs.create();
        buf.writeIdentifier(selectionZone);
        buf.writeCollection(selectionChunks, PacketByteBuf::writeChunkPos);
        MinecraftClient.getInstance().player.sendMessage(
                Text.translatable("command.timelock.selection.commit_client", selectionChunks.size(), selectionZone)
        );
        ClientPlayNetworking.send(TimelockNetworking.SEND_SELECTION, buf);
        for (var chunk : selectionChunks) {
            chunkData.put(chunk, selectionTime);
        }
        clearSelection();
    }

    public static void tick(ChunkPos pos, boolean inTimelock) {
        if (inTimelock(pos) == inTimelock) {
            timelockValue = getChunkTimelock(pos);
            timelockChunk = timelockValue == null ? pos : null;
        }
    }

    public static void tick(ChunkPos pos) {
        tick(pos, false);
    }

    public static void select(BlockPos pos) {
        var chunk = new ChunkPos(pos);
        boolean exists = selectionChunks.contains(chunk);
        if (exists) {
            selectionChunks.remove(chunk);
        }
        else {
            selectionChunks.add(chunk);
        }
        TimelockClient.tick(chunk, true);
        MinecraftClient.getInstance().player.sendMessage(
                Text.translatable("message.timelock.chunk_" + (exists ? "deselect" : "select"), chunk)
        );
    }

    @Override
    public void onInitializeClient() {
        TimelockNetworking.registerClient();
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (selectionZone != null && hand == Hand.MAIN_HAND && world.isClient()) {
                select(hitResult.getBlockPos());
            }
            return ActionResult.PASS;
        });
    }
}
