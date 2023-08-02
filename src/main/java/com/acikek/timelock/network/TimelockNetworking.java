package com.acikek.timelock.network;

import com.acikek.timelock.Timelock;
import com.acikek.timelock.client.TimelockClient;
import com.acikek.timelock.world.TimelockData;
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
    public static final Identifier QUERY_SELECTION = Timelock.id("query_selection");
    public static final Identifier SEND_SELECTION = Timelock.id("send_selection");

    public static void s2cPutData(Collection<ServerPlayerEntity> players, ServerWorld world) {
        var buf = PacketByteBufs.create();
        var data = TimelockData.get(world).getData();
        buf.writeMap(data, PacketByteBuf::writeChunkPos, PacketByteBuf::writeLong);
        for (var player : players) {
            ServerPlayNetworking.send(player, PUT_DATA, buf);
        }
    }

    public static void s2cUpdateData(Collection<ServerPlayerEntity> players, Collection<ChunkPos> chunks, Optional<Long> time) {
        var buf = PacketByteBufs.create();
        buf.writeCollection(chunks, PacketByteBuf::writeChunkPos);
        buf.writeOptional(time, PacketByteBuf::writeLong);
        for (var player : players) {
            ServerPlayNetworking.send(player, UPDATE_DATA, buf);
        }
    }

    public static void s2cStartSelection(ServerPlayerEntity player, Identifier zone) {
        var buf = PacketByteBufs.create();
        buf.writeIdentifier(zone);
        ServerPlayNetworking.send(player, START_SELECTION, buf);
    }

    public static void s2cQuerySelection(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, QUERY_SELECTION, PacketByteBufs.empty());
    }

    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(PUT_DATA, (client, handler, buf, responseSender) -> {
            final var data = buf.readMap(PacketByteBuf::readChunkPos, PacketByteBuf::readLong);
            client.execute(() -> TimelockClient.putData(data));
        });
        ClientPlayNetworking.registerGlobalReceiver(UPDATE_DATA, (client, handler, buf, responseSender) -> {
            final var chunks = buf.readCollection(ArrayList::new, PacketByteBuf::readChunkPos);
            final var time = buf.readOptional(PacketByteBuf::readLong);
            client.execute(() -> TimelockClient.updateData(chunks, time));
        });
        ClientPlayNetworking.registerGlobalReceiver(START_SELECTION, (client, handler, buf, responseSender) -> {
            final var zone = buf.readIdentifier();
            client.execute(() -> TimelockClient.startSelection(zone));
        });
        ClientPlayNetworking.registerGlobalReceiver(QUERY_SELECTION, (client, handler, buf, responseSender) -> {
            client.execute(TimelockClient::sendSelection);
        });
    }

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof ServerPlayerEntity player) {
                s2cPutData(Collections.singletonList(player), world);
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(SEND_SELECTION, (server, player, handler, buf, responseSender) -> {
            final var players = player.getServerWorld().getPlayers(p -> p != player);
            final var manager = TimelockData.get(player.getServerWorld());
            final var zone = buf.readIdentifier();
            final var chunks = buf.readCollection(ArrayList::new, PacketByteBuf::readChunkPos);
            server.execute(() -> {
                var time = Optional.ofNullable(manager.zones().get(zone));
                s2cUpdateData(players, chunks, time);
            });
        });
    }
}
