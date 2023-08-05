package com.acikek.timelock.client;

import com.acikek.timelock.Timelock;
import com.acikek.timelock.TimelockValue;
import com.acikek.timelock.client.config.TimelockConfig;
import com.acikek.timelock.network.TimelockNetworking;
import dev.isxander.yacl3.api.Binding;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
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
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class TimelockClient implements ClientModInitializer {

    private static final Map<ChunkPos, TimelockValue> chunkData = new HashMap<>();

    private static ChunkPos timelockChunk = null;
    private static TimelockValue timelockValue = null;
    private static Supplier<Float> timelockSkyAngle = null;

    private static Identifier selectionZone = null;
    private static TimelockValue selectionTime = null;
    private static final List<ChunkPos> selectionChunks = new ArrayList<>();

    private static TimelockConfig config;

    public static Optional<Float> getSkyAngle() {
        if (!config.enable || timelockSkyAngle == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(timelockSkyAngle.get());
    }

    public static Identifier getSelectionZone() {
        return selectionZone;
    }

    public static TimelockValue getChunkTimelock(ChunkPos pos) {
        if (selectionTime == null) {
            return chunkData.get(pos);
        }
        if (!selectionChunks.contains(pos)) {
            return null;
        }
        return selectionTime;
    }

    public static void putData(Map<ChunkPos, TimelockValue> chunkData) {
        Timelock.LOGGER.debug("Received PUT: {}", chunkData);
        TimelockClient.chunkData.clear();
        TimelockClient.chunkData.putAll(chunkData);
    }

    public static void updateData(List<ChunkPos> chunks, Optional<TimelockValue> value) {
        Timelock.LOGGER.debug("Received UPDATE: {} for {}", value.orElse(null), chunks);
        for (var chunk : chunks) {
            value.ifPresent(v -> chunkData.put(chunk, v));
            if (value.isEmpty()) {
                chunkData.remove(chunk);
            }
            if (chunk.equals(timelockChunk)) {
                TimelockClient.resetTimelock();
            }
        }
    }

    public static void startSelection(Identifier zone, TimelockValue value, Collection<ChunkPos> existing) {
        var player = MinecraftClient.getInstance().player;
        if (selectionZone != null) {
            player.sendMessage(Text.translatable("error.timelock.selection_in_progress").formatted(Formatting.RED));
            return;
        }
        selectionZone = zone;
        selectionTime = value;
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

    public static void setTimelock(ChunkPos pos) {
        timelockChunk = pos;
        timelockValue = getChunkTimelock(pos);
        timelockSkyAngle = timelockValue != null ? timelockValue.getSkyAngle() : null;
    }

    public static void tick(ChunkPos pos) {
        if (!pos.equals(timelockChunk)) {
            setTimelock(pos);
        }
    }

    public static void resetTimelock() {
        setTimelock(timelockChunk);
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
        TimelockClient.resetTimelock();
        MinecraftClient.getInstance().player.sendMessage(
                Text.translatable("message.timelock.chunk_" + (exists ? "deselect" : "select"), chunk)
        );
    }

    @Override
    public void onInitializeClient() {
        TimelockNetworking.registerClient();
        TimelockClientCommand.register();
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (selectionZone != null && hand == Hand.MAIN_HAND && world.isClient()) {
                select(hitResult.getBlockPos());
            }
            return ActionResult.PASS;
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            clearSelection();
        });
        config = TimelockConfig.read();
    }

    public static void debug() {
        Timelock.LOGGER.info("--- TIMELOCK DEBUG START---");
        Timelock.LOGGER.info("Timelock enabled: {}", config.enable);
        if (timelockValue != null) {
            Timelock.LOGGER.info("Current timelock value: {} for chunk {}", timelockValue, timelockChunk);
        }
        if (selectionZone != null) {
            Timelock.LOGGER.info("Current selection value: {} for zone '{}'", selectionTime, selectionZone);
            Timelock.LOGGER.info("Currently selected chunks ({}): {}", selectionChunks.size(), selectionChunks);
        }
        Timelock.LOGGER.info("Chunk data: {}", chunkData);
        Timelock.LOGGER.info("--- TIMELOCK DEBUG END ---");
    }

    public static final Binding<Boolean> ENABLE_BINDING = new Binding<>() {

        @Override
        public void setValue(Boolean value) {
            config.enable = value;
            config.write();
        }

        @Override
        public Boolean getValue() {
            return config.enable;
        }

        @Override
        public Boolean defaultValue() {
            return true;
        }
    };
}
