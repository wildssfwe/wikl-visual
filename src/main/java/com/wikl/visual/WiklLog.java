package com.wikl.visual;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Writes a few diagnostic lines to logs/latest.log (search for "wikl visual") and optional action bar messages. */
public final class WiklLog {
    public static final Logger LOG = LoggerFactory.getLogger("wikl visual");
    private static int attacks;
    private static boolean renderLogged;
    private static boolean mixinLogged;

    private WiklLog() {}

    public static void register() {
        LOG.info("wikl visual loaded");
        WorldRenderEvents.AFTER_ENTITIES.register(ctx -> {
            if (renderLogged) return;
            renderLogged = true;
            LOG.info("world render hook works: consumers={}, matrixStack={}",
                    ctx.consumers() != null, ctx.matrixStack() != null);
        });
    }

    public static void say(String msg) {
        if (!WiklSettings.debug) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) mc.player.sendMessage(Text.literal("[wikl] " + msg), true);
    }

    public static void attack(Entity e) {
        attacks++;
        if (attacks <= 10) {
            LOG.info("attack event #{} on {} (hitAnim={}, aura={}, particleSetting={})", attacks,
                    e.getName().getString(), WiklSettings.hitAnim, WiklSettings.aura,
                    MinecraftClient.getInstance().options.getParticles().getValue());
        }
        say("удар по " + e.getName().getString());
    }

    public static void mixinSeen(byte status) {
        if (mixinLogged) return;
        mixinLogged = true;
        LOG.info("LivingEntityMixin is active (first status {})", status);
    }
}
