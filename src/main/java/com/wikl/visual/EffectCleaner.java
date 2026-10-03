package com.wikl.visual;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;

/** Removes purely visual negative effects (darkness, blindness, nausea) from the local player. */
public final class EffectCleaner {
    private EffectCleaner() {}

    public static void register() {
        ClientTickEvents.START_CLIENT_TICK.register(EffectCleaner::tick);
        WorldRenderEvents.START.register(ctx -> tick(MinecraftClient.getInstance()));
    }

    private static void clear(ClientPlayerEntity p, RegistryEntry<StatusEffect> effect) {
        if (p.hasStatusEffect(effect)) p.removeStatusEffect(effect);
    }

    private static boolean fullbrightApplied = false;

    /** Gives the local player an endless, hidden night vision effect (client side only). */
    private static void fullbright(ClientPlayerEntity p) {
        StatusEffectInstance cur = p.getStatusEffect(StatusEffects.NIGHT_VISION);
        if (WiklSettings.fullbright) {
            if (cur != null && cur.isInfinite()) return;
            if (cur != null) p.removeStatusEffect(StatusEffects.NIGHT_VISION);
            p.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, -1, 0, false, false, false));
            fullbrightApplied = true;
        } else if (fullbrightApplied) {
            if (cur != null && cur.isInfinite()) p.removeStatusEffect(StatusEffects.NIGHT_VISION);
            fullbrightApplied = false;
        }
    }

    public static void tick(MinecraftClient mc) {
        if (mc.player == null) return;
        fullbright(mc.player);
        if (!WiklSettings.noBadEffects) return;
        clear(mc.player, StatusEffects.DARKNESS);
        clear(mc.player, StatusEffects.BLINDNESS);
        clear(mc.player, StatusEffects.NAUSEA);
    }
}
