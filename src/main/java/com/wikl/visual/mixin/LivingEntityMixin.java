package com.wikl.visual.mixin;

import com.wikl.visual.ShieldStatus;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    /** Entity status 30 is sent when an axe disables a player's shield. */
    @Inject(method = "handleStatus", at = @At("HEAD"), require = 0)
    private void wikl$status(byte status, CallbackInfo ci) {
        com.wikl.visual.WiklLog.mixinSeen(status);
        if (status == 30) ShieldStatus.markDisabled((LivingEntity) (Object) this);
    }
}
