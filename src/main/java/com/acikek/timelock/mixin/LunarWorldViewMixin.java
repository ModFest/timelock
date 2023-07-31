package com.acikek.timelock.mixin;

import com.acikek.timelock.client.TimelockClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LunarWorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LunarWorldView.class)
public interface LunarWorldViewMixin {

    /*private static float timelock$getSkyAngle(long time) {
        double d = MathHelper.fractionalPart(time / 24000.0 - 0.25);
        double e = 0.5 - Math.cos(d * Math.PI) / 2.0;
        return (float) (d * 2.0 + e) / 3.0f;
    }*/

    @Inject(method = "getSkyAngle", cancellable = true, at = @At(value = "HEAD"))
    private void timelock$modifyTimeOfDay(float tickDelta, CallbackInfoReturnable<Float> cir) {
        TimelockClient.timelock().ifPresent(data -> cir.setReturnValue((float) data.time()));
    }
}
