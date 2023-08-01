package com.acikek.timelock.network;

import com.acikek.timelock.Timelock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;

import java.util.Collection;

public class TimelockNetworking {

    public static final Identifier PUT_DATA = Timelock.id("put_data");
    public static final Identifier UPDATE_CHUNK = Timelock.id("update_chunk");

    public static void s2cPutData(Collection<ServerPlayerEntity> players) {

    }

    public static void s2cUpdateChunk(Collection<ServerPlayerEntity> players, ChunkPos chunk, long time) {

    }

    @Environment(EnvType.CLIENT)
    public static void registerClient() {

    }
}
