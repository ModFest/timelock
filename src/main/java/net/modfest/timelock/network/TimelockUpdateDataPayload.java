package net.modfest.timelock.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.ChunkPos;
import net.modfest.timelock.TimelockValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record TimelockUpdateDataPayload(List<ChunkPos> chunks, Optional<TimelockValue> time) implements CustomPayload {
    public static CustomPayload.Id<TimelockUpdateDataPayload> ID = new Id<>(TimelockNetworking.UPDATE_DATA);
    public static final PacketCodec<RegistryByteBuf, TimelockUpdateDataPayload> CODEC = CustomPayload.codecOf(TimelockUpdateDataPayload::write, TimelockUpdateDataPayload::read);

    private static TimelockUpdateDataPayload read(RegistryByteBuf buf) {
        final var chunks = buf.readCollection(ArrayList::new, PacketByteBuf::readChunkPos);
        final var time = buf.readOptional(TimelockValue::read);
        return new TimelockUpdateDataPayload(chunks, time);
    }

    private void write(RegistryByteBuf buf) {
        buf.writeCollection(chunks, PacketByteBuf::writeChunkPos);
        buf.writeOptional(time, (valueBuf, v) -> v.write(valueBuf));
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
