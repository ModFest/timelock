package net.modfest.timelock.client;

import dev.doublekekse.area_lib.Area;
import dev.doublekekse.area_lib.data.AreaClientData;
import dev.doublekekse.area_lib.data.AreaSavedData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.modfest.timelock.Timelock;
import net.modfest.timelock.client.config.TimelockConfig;
import dev.isxander.yacl3.api.Binding;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.modfest.timelock.world.TimelockAreaComponent;

import java.util.*;

@Environment(EnvType.CLIENT)
public class TimelockClient implements ClientModInitializer {
    private static TimelockConfig config;

    public static Optional<Float> getSkyAngle() {
        if (!config.enable) {
            return Optional.empty();
        }

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return Optional.empty();
        AreaSavedData data = AreaClientData.getClientLevelData();
        if (data == null) return Optional.empty();

        return data.findTrackedAreasContaining(player)
                .stream().filter(area -> area.has(Timelock.TIMELOCK_AREA_COMPONENT))
                .max(Comparator.comparingInt(Area::getPriority))
                .map(area -> {
                    TimelockAreaComponent timeZone = area.get(Timelock.TIMELOCK_AREA_COMPONENT);
                    long ticks = timeZone.ticks();
                    if (timeZone.offset()) {
                        return getSkyAngle(MinecraftClient.getInstance().world.getLevelProperties().getTimeOfDay() + ticks);
                    }
                    return getSkyAngle(ticks);
                });
    }

    private static float getSkyAngle(long ticks) {
        double d = MathHelper.fractionalPart(ticks / 24000.0 - 0.25);
        double e = 0.5 - Math.cos(d * Math.PI) / 2.0;
        return (float) (d * 2.0 + e) / 3.0f;
    }

    @Override
    public void onInitializeClient() {
        config = TimelockConfig.read();
    }

    public static final Binding<Boolean> ENABLE_BINDING = new Binding<>() {

        @Override
        public void setValue(Boolean value) {
            config.enable = value;
            config.write();
        }

        @Override
        public Boolean getValue() {
            return config.enable;
        }

        @Override
        public Boolean defaultValue() {
            return true;
        }
    };
}
