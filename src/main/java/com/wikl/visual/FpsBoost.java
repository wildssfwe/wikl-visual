package com.wikl.visual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;

/** "Smooth game": lifts the FPS limit, turns off V-Sync and mob shadows. Original values are restored when disabled. */
public final class FpsBoost {
    private FpsBoost() {}

    public static void tick(MinecraftClient mc) {
        if (mc.options == null) return;
        GameOptions o = mc.options;
        if (WiklSettings.smoothGame && !WiklSettings.boostApplied) {
            WiklSettings.origMaxFps = o.getMaxFps().getValue();
            WiklSettings.origVsync = o.getEnableVsync().getValue();
            WiklSettings.origShadows = o.getEntityShadows().getValue();
            o.getMaxFps().setValue(260);
            o.getEnableVsync().setValue(false);
            o.getEntityShadows().setValue(false);
            WiklSettings.boostApplied = true;
            WiklConfig.save();
        } else if (!WiklSettings.smoothGame && WiklSettings.boostApplied) {
            o.getMaxFps().setValue(WiklSettings.origMaxFps);
            o.getEnableVsync().setValue(WiklSettings.origVsync);
            o.getEntityShadows().setValue(WiklSettings.origShadows);
            WiklSettings.boostApplied = false;
            WiklConfig.save();
        }
    }
}
