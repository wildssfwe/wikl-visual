package com.wikl.visual.mixin;

import com.wikl.visual.WiklSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.Window;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "getBasicProjectionMatrix", at = @At("RETURN"), cancellable = true, require = 0)
    private void wikl$stretch(float fovDegrees, CallbackInfoReturnable<Matrix4f> cir) {
        if (!WiklSettings.aspectEnabled) return;
        Window w = MinecraftClient.getInstance().getWindow();
        if (w.getFramebufferHeight() == 0) return;
        float real = (float) w.getFramebufferWidth() / (float) w.getFramebufferHeight();
        float target = WiklSettings.targetAspect();
        Matrix4f stretched = new Matrix4f().scale(real / target, 1.0f, 1.0f).mul(cir.getReturnValue());
        cir.setReturnValue(stretched);
    }
}
