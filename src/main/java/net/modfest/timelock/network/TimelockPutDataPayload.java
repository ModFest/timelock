package net.modfest.timelock.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.ChunkPos;
import net.modfest.timelock.TimelockValue;
import net.modfest.timelock.world.TimelockData;

import java.util.Map;

public record TimelockPutDataPayload(Map<ChunkPos, TimelockValue> data) implements CustomPayload {
    public static CustomPayload.Id<TimelockPutDataPayload> ID = new Id<>(TimelockNetworking.PUT_DATA);
    public static final PacketCodec<RegistryByteBuf, TimelockPutDataPayload> CODEC = CustomPayload.codecOf(TimelockPutDataPayload::write, TimelockPutDataPayload::read);

    private static TimelockPutDataPayload read(RegistryByteBuf buf) {
        final var data = buf.readMap(PacketByteBuf::readChunkPos, TimelockValue::read);
        return new TimelockPutDataPayload(data);
    }

    private void write(RegistryByteBuf buf) {
        buf.writeMap(data, PacketByteBuf::writeChunkPos, (valueBuf, value) -> value.write(valueBuf));
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
