package net.modfest.timelock.network;

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
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
        var buf = PacketByteBufs.create();
        var data = TimelockData.get(world).getData();
        buf.writeMap(data, PacketByteBuf::writeChunkPos, (valueBuf, value) -> value.write(valueBuf));
        for (var player : players) {
            ServerPlayNetworking.send(player, PUT_DATA, buf);
        }
    }

    public static void s2cUpdateData(Collection<ServerPlayerEntity> players, Collection<ChunkPos> chunks, Optional<TimelockValue> value) {
        var buf = PacketByteBufs.create();
        buf.writeCollection(chunks, PacketByteBuf::writeChunkPos);
        buf.writeOptional(value, (valueBuf, v) -> v.write(valueBuf));
        for (var player : players) {
            ServerPlayNetworking.send(player, UPDATE_DATA, buf);
        }
    }

    public static void s2cStartSelection(ServerPlayerEntity player, Identifier zone) {
        var buf = PacketByteBufs.create();
        buf.writeIdentifier(zone);
        var manager = TimelockData.get(player.getServerWorld());
        manager.zones().get(zone).write(buf);
        buf.writeCollection(manager.chunks().get(zone), PacketByteBuf::writeChunkPos);
        ServerPlayNetworking.send(player, START_SELECTION, buf);
    }

    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(PUT_DATA, (client, handler, buf, responseSender) -> {
            final var data = buf.readMap(PacketByteBuf::readChunkPos, TimelockValue::read);
            client.execute(() -> TimelockClient.putData(data));
        });
        ClientPlayNetworking.registerGlobalReceiver(UPDATE_DATA, (client, handler, buf, responseSender) -> {
            final var chunks = buf.readCollection(ArrayList::new, PacketByteBuf::readChunkPos);
            final var time = buf.readOptional(TimelockValue::read);
            client.execute(() -> TimelockClient.updateData(chunks, time));
        });
        ClientPlayNetworking.registerGlobalReceiver(START_SELECTION, (client, handler, buf, responseSender) -> {
            final var zone = buf.readIdentifier();
            final var time = TimelockValue.read(buf);
            final var chunks = buf.readCollection(ArrayList::new, PacketByteBuf::readChunkPos);
            client.execute(() -> TimelockClient.startSelection(zone, time, chunks));
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
                if (!player.hasPermissionLevel(4)) {
                    player.sendMessage(Text.translatable("error.timelock.not_elevated").formatted(Formatting.RED));
                    return;
                }
                var time = Optional.ofNullable(manager.zones().get(zone));
                s2cUpdateData(players, chunks, time);
                manager.chunks().replaceValues(zone, chunks);
                manager.markDirty();
            });
        });
    }
}
