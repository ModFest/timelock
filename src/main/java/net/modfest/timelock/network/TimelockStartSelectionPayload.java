package net.modfest.timelock.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.modfest.timelock.TimelockValue;

import java.util.ArrayList;
import java.util.List;

public record TimelockStartSelectionPayload(Identifier zone, TimelockValue time, List<ChunkPos> chunks) implements CustomPayload {
    public static CustomPayload.Id<TimelockStartSelectionPayload> ID = new Id<>(TimelockNetworking.START_SELECTION);
    public static final PacketCodec<RegistryByteBuf, TimelockStartSelectionPayload> CODEC = CustomPayload.codecOf(TimelockStartSelectionPayload::write, TimelockStartSelectionPayload::read);

    private static TimelockStartSelectionPayload read(RegistryByteBuf buf) {
        final var zone = buf.readIdentifier();
        final var time = TimelockValue.read(buf);
        final var chunks = buf.readCollection(ArrayList::new, PacketByteBuf::readChunkPos);
        return new TimelockStartSelectionPayload(zone, time, chunks);
    }

    private void write(RegistryByteBuf buf) {
        buf.writeIdentifier(zone);
        time.write(buf);
        buf.writeCollection(chunks, PacketByteBuf::writeChunkPos);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
