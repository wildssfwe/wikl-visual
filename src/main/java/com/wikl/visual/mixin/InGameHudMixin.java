package com.wikl.visual.mixin;

import com.wikl.visual.WiklSettings;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    /** Hides the vanilla crosshair while the custom one is enabled. */
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true, require = 0)
    private void wikl$hideCrosshair(CallbackInfo ci) {
        if (WiklSettings.customCrosshair) ci.cancel();
    }
}
