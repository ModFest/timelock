package net.modfest.timelock.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.modfest.timelock.Timelock;
import net.modfest.timelock.TimelockValue;
import net.modfest.timelock.client.TimelockClient;
import net.modfest.timelock.world.TimelockData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class TimelockNetworking {

    public static final Identifier PUT_DATA = Timelock.id("put_data");
    public static final Identifier UPDATE_DATA = Timelock.id("update_data");
    public static final Identifier START_SELECTION = Timelock.id("start_selection");
    public static final Identifier SEND_SELECTION = Timelock.id("send_selection");

    public static void s2cPutData(Collection<ServerPlayerEntity> players, ServerWorld world) {
        var data = TimelockData.get(world).getData();
        TimelockPutDataPayload payload = new TimelockPutDataPayload(data);
        for (var player : players) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void s2cUpdateData(Collection<ServerPlayerEntity> players, Collection<ChunkPos> chunks, Optional<TimelockValue> value) {
        TimelockUpdateDataPayload payload = new TimelockUpdateDataPayload(chunks.stream().toList(), value);
        for (var player : players) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void s2cStartSelection(ServerPlayerEntity player, Identifier zone) {
        var manager = TimelockData.get(player.getServerWorld());
        TimelockStartSelectionPayload startSelectionPayload = new TimelockStartSelectionPayload(zone, manager.zones().get(zone), manager.chunks().get(zone).stream().toList());
        ServerPlayNetworking.send(player, startSelectionPayload);
    }

    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(TimelockPutDataPayload.ID, (payload, context) -> {
            context.client().execute(() -> TimelockClient.putData(payload.data()));
        });
        ClientPlayNetworking.registerGlobalReceiver(TimelockUpdateDataPayload.ID, (payload, context) -> {
            context.client().execute(() -> TimelockClient.updateData(payload.chunks(), payload.time()));
        });
        ClientPlayNetworking.registerGlobalReceiver(TimelockStartSelectionPayload.ID, (payload, context) -> {
            context.client().execute(() -> TimelockClient.startSelection(payload.zone(), payload.time(), payload.chunks()));
        });
    }

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof ServerPlayerEntity player) {
                s2cPutData(Collections.singletonList(player), world);
            }
        });

        PayloadTypeRegistry.playC2S().register(TimelockSendSelectionPayload.ID, TimelockSendSelectionPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(TimelockSendSelectionPayload.ID, (payload, context) -> {
            payload.handle(context.server(), context.player());
        });

        PayloadTypeRegistry.playS2C().register(TimelockUpdateDataPayload.ID, TimelockUpdateDataPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(TimelockPutDataPayload.ID, TimelockPutDataPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(TimelockStartSelectionPayload.ID, TimelockStartSelectionPayload.CODEC);
    }
}
