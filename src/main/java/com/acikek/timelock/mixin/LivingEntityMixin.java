package com.acikek.timelock.mixin;

import com.acikek.timelock.client.TimelockClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "travel", at = @At("TAIL"))
    private void timelock$updateLockingChunk(Vec3d movementInput, CallbackInfo ci) {
        // Check if running on client player
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (!(livingEntity instanceof ClientPlayerEntity)) {
            return;
        }
        // If the client is already locking some chunk
        boolean isLockingChunk = TimelockClient.lockingChunk != null;
        // If the current chunk can be timelocked
        boolean isCurrentChunkLockable = TimelockClient.timelockData.containsKey(livingEntity.getChunkPos());
        // If this chunk can't be timelocked, return
        if (isLockingChunk || !isCurrentChunkLockable) {
            // If was once locking chunk and now this chunk can't be locked, reset state
            if (isLockingChunk && !isCurrentChunkLockable) {
                TimelockClient.resetLockingChunk(true);
            }
            return;
        }
        // Update locking chunk
        TimelockClient.updateLockingChunk(livingEntity.getChunkPos());
    }
}
