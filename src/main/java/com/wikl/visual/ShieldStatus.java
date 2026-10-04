package com.wikl.visual;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** Shows above other players who hold a shield whether it is ready (green) or disabled by an axe (red). */
public final class ShieldStatus {
    private static final long DISABLE_MS = 5000; // an axe disables a shield for 5 seconds
    private static final Map<UUID, Long> DISABLED = new HashMap<>();

    private ShieldStatus() {}

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(ShieldStatus::render);
    }

    public static void markDisabled(Entity e) {
        DISABLED.put(e.getUuid(), Util.getMeasuringTimeMs() + DISABLE_MS);
    }

    private static long remainingMs(Entity e) {
        Long end = DISABLED.get(e.getUuid());
        if (end == null) return 0;
        long rem = end - Util.getMeasuringTimeMs();
        if (rem <= 0) {
            DISABLED.remove(e.getUuid());
            return 0;
        }
        return rem;
    }

    private static void render(WorldRenderContext ctx) {
        if (!WiklSettings.shieldStatus) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null || ctx.consumers() == null || ctx.matrixStack() == null) return;

        TextRenderer tr = mc.textRenderer;
        MatrixStack ms = ctx.matrixStack();
        Vec3d cam = ctx.camera().getPos();

        for (AbstractClientPlayerEntity pl : mc.world.getPlayers()) {
            if (pl == mc.player || pl.isSpectator()) continue;
            if (mc.player.squaredDistanceTo(pl) > 32 * 32) continue;
            boolean hasShield = pl.getMainHandStack().isOf(Items.SHIELD) || pl.getOffHandStack().isOf(Items.SHIELD);
            if (!hasShield) continue;

            long rem = remainingMs(pl);
            String text;
            int color;
            if (rem > 0) {
                text = String.format(Locale.ROOT, "Щит сломан %.1f с", rem / 1000.0);
                color = 0xFFFF4444;
            } else {
                text = "Щит готов";
                color = 0xFF55FF55;
            }

            Vec3d pos = pl.getPos().add(0, pl.getHeight() + 0.75, 0);
            ms.push();
            ms.translate(pos.x - cam.x, pos.y - cam.y, pos.z - cam.z);
            ms.multiply(ctx.camera().getRotation());
            ms.scale(-0.025f, -0.025f, 0.025f);
            float w = tr.getWidth(text);
            tr.draw(text, -w / 2f, 0f, color, false, ms.peek().getPositionMatrix(), ctx.consumers(),
                    TextRenderer.TextLayerType.NORMAL, 0x70000000, 0xF000F0);
            ms.pop();
        }
    }
}
