package net.modfest.timelock.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.modfest.timelock.world.TimelockData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record TimelockSendSelectionPayload(Identifier zone, List<ChunkPos> chunks) implements CustomPayload {

    public static CustomPayload.Id<TimelockSendSelectionPayload> ID = new Id<>(TimelockNetworking.SEND_SELECTION);
    public static final PacketCodec<RegistryByteBuf, TimelockSendSelectionPayload> CODEC = CustomPayload.codecOf(TimelockSendSelectionPayload::write, TimelockSendSelectionPayload::read);

    private static TimelockSendSelectionPayload read(RegistryByteBuf buf) {
        final var zone = buf.readIdentifier();
        final var chunks = buf.readCollection(ArrayList::new, PacketByteBuf::readChunkPos);
        return new TimelockSendSelectionPayload(zone, chunks);
    }

    private void write(RegistryByteBuf buf) {
        buf.writeIdentifier(zone);
        buf.writeCollection(chunks, PacketByteBuf::writeChunkPos);
    }

    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        final var players = player.getServerWorld().getPlayers(p -> p != player);
        final var manager = TimelockData.get(player.getServerWorld());

        server.execute(() -> {
            if (!player.hasPermissionLevel(4)) {
                player.sendMessage(Text.translatable("error.timelock.not_elevated").formatted(Formatting.RED));
                return;
            }
            var time = Optional.ofNullable(manager.zones().get(zone));
            TimelockNetworking.s2cUpdateData(players, chunks, time);
            manager.chunks().replaceValues(zone, chunks);
            manager.markDirty();
        });
    }


    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
