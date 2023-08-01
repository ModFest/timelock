package com.acikek.timelock.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;

public class TimelockNetworking {

    public static void s2cPutData(Collection<ServerPlayerEntity> players) {

    }

    @Environment(EnvType.CLIENT)
    public static void registerClient() {

    }
}
