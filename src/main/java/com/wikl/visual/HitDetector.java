package com.wikl.visual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;

/** Backup hit detection: if you swing at the entity under your crosshair and it just got hurt, count it as your hit. */
public final class HitDetector {
    private HitDetector() {}

    public static void tick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return;
        if (!mc.player.handSwinging) return;
        if (!(mc.crosshairTarget instanceof EntityHitResult ehr)) return;
        if (!(ehr.getEntity() instanceof LivingEntity le) || le == mc.player) return;
        if (le.hurtTime > 0 && le.hurtTime >= le.maxHurtTime - 2) TargetRenderer.registerHit(le);
    }
}
