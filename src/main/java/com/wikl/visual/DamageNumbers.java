package com.wikl.visual;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public final class DamageNumbers {
    private record Num(Vec3d pos, String text, long born, int color) {}

    private static final List<Num> NUMS = new ArrayList<>();
    private static final Random RND = new Random();
    private static final long LIFE_MS = 1100;
    private static LivingEntity tracked;
    private static float lastHealth;
    private static long trackUntil;

    private DamageNumbers() {}

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(DamageNumbers::render);
    }

    public static void onHit(LivingEntity e) {
        long now = Util.getMeasuringTimeMs();
        if (tracked != e) {
            tracked = e;
            lastHealth = e.getHealth();
        }
        trackUntil = now + 1500;
    }

    public static void tick(MinecraftClient mc) {
        if (tracked == null) return;
        float h = tracked.getHealth();
        if (h < lastHealth - 0.01f && WiklSettings.damageNumbers) {
            float dmg = lastHealth - h;
            String t = Math.abs(dmg - Math.round(dmg)) < 0.05f
                    ? "-" + Math.round(dmg) : "-" + String.format(Locale.ROOT, "%.1f", dmg);
            Vec3d pos = tracked.getPos().add((RND.nextDouble() - 0.5) * 0.6,
                    tracked.getHeight() + 0.1, (RND.nextDouble() - 0.5) * 0.6);
            NUMS.add(new Num(pos, t, Util.getMeasuringTimeMs(), dmg >= 8f ? 0xFFFFAA00 : 0xFFFF5555));
        }
        lastHealth = h;
        if (tracked.isRemoved() || Util.getMeasuringTimeMs() > trackUntil) tracked = null;
    }

    private static void render(WorldRenderContext ctx) {
        if (NUMS.isEmpty() || !WiklSettings.damageNumbers) {
            if (!WiklSettings.damageNumbers) NUMS.clear();
            return;
        }
        if (ctx.consumers() == null || ctx.matrixStack() == null) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        TextRenderer tr = mc.textRenderer;
        MatrixStack ms = ctx.matrixStack();
        Vec3d cam = ctx.camera().getPos();
        long now = Util.getMeasuringTimeMs();

        Iterator<Num> it = NUMS.iterator();
        while (it.hasNext()) {
            Num n = it.next();
            long age = now - n.born();
            if (age > LIFE_MS) { it.remove(); continue; }
            float t = age / (float) LIFE_MS;
            float rise = 0.9f * (1f - (1f - t) * (1f - t));
            int alpha = Math.max(8, (int) (255 * (1f - t * t)));
            int color = (alpha << 24) | (n.color() & 0xFFFFFF);
            float pop = 1.0f + 0.5f * (1f - Math.min(1f, t * 5f));
            float sc = 0.035f * pop;

            ms.push();
            ms.translate(n.pos().x - cam.x, n.pos().y + rise - cam.y, n.pos().z - cam.z);
            ms.multiply(ctx.camera().getRotation());
            ms.scale(-sc, -sc, sc);
            float w = tr.getWidth(n.text());
            tr.draw(n.text(), -w / 2f, 0f, color, true, ms.peek().getPositionMatrix(), ctx.consumers(),
                    TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
            ms.pop();
        }
    }
}
