package com.acikek.timelock;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

public record TimelockValue(long ticks, boolean offset) {

    public static TimelockValue fromNbt(NbtCompound nbt) {
        long ticks = nbt.getLong("Ticks");
        boolean offset = nbt.getBoolean("Offset");
        return new TimelockValue(ticks, offset);
    }

    public NbtCompound toNbt() {
        var nbt = new NbtCompound();
        nbt.putLong("Ticks", ticks);
        nbt.putBoolean("Offset", offset);
        return nbt;
    }

    public static TimelockValue read(PacketByteBuf buf) {
        long ticks = buf.readLong();
        boolean offset = buf.readBoolean();
        return new TimelockValue(ticks, offset);
    }

    public void write(PacketByteBuf buf) {
        buf.writeLong(ticks);
        buf.writeBoolean(offset);
    }

    @Override
    public String toString() {
        return (offset ? "Offset" : "Fixed") + "[" + ticks + "L]";
    }

    @Environment(EnvType.CLIENT)
    private static long getTimeOfDay() {
        return MinecraftClient.getInstance().world.getLevelProperties().getTimeOfDay();
    }

    @Environment(EnvType.CLIENT)
    public long getTicks() {
        if (!offset) {
            return ticks;
        }
        long worldTicks = getTimeOfDay();
        long time = worldTicks + ticks;
        return time % 24000L;
    }
}
