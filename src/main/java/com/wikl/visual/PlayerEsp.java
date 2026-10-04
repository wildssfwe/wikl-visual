package com.wikl.visual;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Player ESP: gives every other player the vanilla "glowing" outline on your screen only, so they are visible through walls.
 * The server is not told about it. Most public servers forbid this, so use it only where it is allowed.
 */
public final class PlayerEsp {
    private static final Set<UUID> MARKED = new HashSet<>();

    private PlayerEsp() {}

    public static void register() {
        ClientTickEvents.START_CLIENT_TICK.register(PlayerEsp::tick);
        WorldRenderEvents.START.register(ctx -> tick(MinecraftClient.getInstance()));
    }

    public static void tick(MinecraftClient mc) {
        if (mc.world == null || mc.player == null) {
            MARKED.clear();
            return;
        }
        if (WiklSettings.playerEsp) {
            for (AbstractClientPlayerEntity p : mc.world.getPlayers()) {
                if (p == mc.player) continue;
                if (!p.isGlowing()) p.setGlowing(true);
                MARKED.add(p.getUuid());
            }
        } else if (!MARKED.isEmpty()) {
            for (AbstractClientPlayerEntity p : mc.world.getPlayers()) {
                if (MARKED.contains(p.getUuid())) p.setGlowing(false);
            }
            MARKED.clear();
        }
    }
}
