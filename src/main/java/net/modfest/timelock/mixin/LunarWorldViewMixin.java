package net.modfest.timelock.mixin;

import net.modfest.timelock.client.TimelockClient;
import net.minecraft.world.LunarWorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LunarWorldView.class)
public interface LunarWorldViewMixin {

    @Inject(method = "getSkyAngle", cancellable = true, at = @At(value = "HEAD"))
    private void timelock$modifyTimeOfDay(float tickDelta, CallbackInfoReturnable<Float> cir) {
        TimelockClient.getSkyAngle().ifPresent(cir::setReturnValue);
    }
}
