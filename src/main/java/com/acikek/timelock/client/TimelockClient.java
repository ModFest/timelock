package com.acikek.timelock.client;

import com.acikek.timelock.Timelock;
import com.acikek.timelock.TimelockValue;
import com.acikek.timelock.client.config.TimelockConfig;
import com.acikek.timelock.network.TimelockNetworking;
import dev.isxander.yacl3.api.Binding;
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
import net.minecraft.util.math.MathHelper;

import java.util.*;

@Environment(EnvType.CLIENT)
public class TimelockClient implements ClientModInitializer {

    private static final Map<ChunkPos, TimelockValue> chunkData = new HashMap<>();

    private static ChunkPos timelockChunk = null;
    private static TimelockValue timelockValue = null;
    private static float timelockSkyAngle = -1.0f;

    private static Identifier selectionZone = null;
    private static TimelockValue selectionTime = null;
    private static final List<ChunkPos> selectionChunks = new ArrayList<>();

    private static TimelockConfig config;

    public static Optional<Float> getSkyAngle() {
        if (!config.enable) {
            return Optional.empty();
        }
        return Optional.ofNullable(timelockSkyAngle != -1.0f ? timelockSkyAngle : null);
    }

    public static boolean inTimelock(ChunkPos pos) {
        return timelockChunk != null && timelockChunk.equals(pos);
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
            TimelockClient.tick(chunk, true);
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

    private static float getSkyAngle(TimelockValue value) {
        double d = MathHelper.fractionalPart(value.getTicks() / 24000.0 - 0.25);
        double e = 0.5 - Math.cos(d * Math.PI) / 2.0;
        return (float) (d * 2.0 + e) / 3.0f;
    }

    public static void tick(ChunkPos pos, boolean inTimelock) {
        boolean timelock = inTimelock(pos);
        boolean offset = timelockValue != null && timelockValue.offset();
        if (config.enable && (offset || timelock == inTimelock)) {
            if (!timelock || inTimelock) {
                timelockValue = getChunkTimelock(pos);
            }
            timelockChunk = timelockValue != null ? pos : null;
            timelockSkyAngle = timelockValue != null ? getSkyAngle(timelockValue) : -1.0f;
        }
    }

    public static void tick(ChunkPos pos) {
        tick(pos, false);
    }

    public static void resetTimelock() {
        tick(timelockChunk, true);
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
        TimelockClientCommand.register();
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (selectionZone != null && hand == Hand.MAIN_HAND && world.isClient()) {
                select(hitResult.getBlockPos());
            }
            return ActionResult.PASS;
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
