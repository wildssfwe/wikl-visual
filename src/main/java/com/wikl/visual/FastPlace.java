package com.wikl.visual;

import com.wikl.visual.mixin.MinecraftClientAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;

/** Removes the delay between placing blocks while the use key is held. Single player by default. */
public final class FastPlace {
    private FastPlace() {}

    public static void tick(MinecraftClient mc) {
        if (!WiklSettings.fastPlace || mc.player == null || mc.currentScreen != null) return;
        if (!mc.isInSingleplayer() && !WiklSettings.fastPlaceServers) return;
        boolean block = mc.player.getMainHandStack().getItem() instanceof BlockItem
                || mc.player.getOffHandStack().getItem() instanceof BlockItem;
        if (!block || !mc.options.useKey.isPressed()) return;
        ((MinecraftClientAccessor) mc).wikl$setItemUseCooldown(0);
    }
}
