package com.acikek.timelock.mixin;

import com.acikek.timelock.client.TimelockClient;
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
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (!(livingEntity instanceof ClientPlayerEntity)) {
            return;
        }
        TimelockClient.tick(livingEntity.getChunkPos());
    }
}
