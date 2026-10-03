package com.wikl.visual;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Util;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class TargetRenderer {
    private static LivingEntity target;
    private static long hitTime;
    private static final long SHOW_MS = 4000;

    private TargetRenderer() {}

    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient() && entity instanceof LivingEntity le) {
                target = le;
                hitTime = Util.getMeasuringTimeMs();
                DamageNumbers.onHit(le);
                HitEffects.play(le);
            }
            return ActionResult.PASS;
        });
        WorldRenderEvents.AFTER_ENTITIES.register(TargetRenderer::renderWorld);
    }

    private static LivingEntity current() {
        if (!WiklSettings.target || target == null) return null;
        if (target.isRemoved() || !target.isAlive() || Util.getMeasuringTimeMs() - hitTime > SHOW_MS) {
            target = null;
            return null;
        }
        return target;
    }

    private static void renderWorld(WorldRenderContext ctx) {
        LivingEntity t = current();
        if (t == null || ctx.consumers() == null || ctx.matrixStack() == null) return;

        float frac = Math.min(1f, Math.max(0f, t.getHealth() / Math.max(1f, t.getMaxHealth())));
        int col = frac < 0.35f ? 0xFFFF4444 : WiklSettings.accent();
        int r = (col >> 16) & 0xFF, g = (col >> 8) & 0xFF, b = col & 0xFF;
        long now = Util.getMeasuringTimeMs();
        int style = WiklSettings.espStyle;

        Vec3d cam = ctx.camera().getPos();
        MatrixStack.Entry e = ctx.matrixStack().peek();
        VertexConsumer vc = ctx.consumers().getBuffer(RenderLayer.getLines());
        Box bb = t.getBoundingBox();

        if (style == 0 || style == 2) {
            double pulse = 0.5 + 0.5 * Math.sin(now * 0.006);
            int a = (int) (90 + 110 * pulse);
            Box box = bb.expand(0.05).offset(-cam.x, -cam.y, -cam.z);
            double[] xs = {box.minX, box.maxX}, ys = {box.minY, box.maxY}, zs = {box.minZ, box.maxZ};
            // thin full outline
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    line(vc, e, xs[0], ys[i], zs[j], xs[1], ys[i], zs[j], r, g, b, a);
                    line(vc, e, xs[i], ys[0], zs[j], xs[i], ys[1], zs[j], r, g, b, a);
                    line(vc, e, xs[i], ys[j], zs[0], xs[i], ys[j], zs[1], r, g, b, a);
                }
            }
            // bright corner brackets
            double len = Math.min(0.3, Math.min(box.maxY - box.minY, box.maxX - box.minX) * 0.35);
            for (int xi = 0; xi < 2; xi++) {
                for (int yi = 0; yi < 2; yi++) {
                    for (int zi = 0; zi < 2; zi++) {
                        double cx = xs[xi], cy = ys[yi], cz = zs[zi];
                        double dx = xi == 0 ? len : -len, dy = yi == 0 ? len : -len, dz = zi == 0 ? len : -len;
                        line(vc, e, cx, cy, cz, cx + dx, cy, cz, r, g, b, 255);
                        line(vc, e, cx, cy, cz, cx, cy + dy, cz, r, g, b, 255);
                        line(vc, e, cx, cy, cz, cx, cy, cz + dz, r, g, b, 255);
                    }
                }
            }
        }

        if (style == 1 || style == 2) {
            double cx = (bb.minX + bb.maxX) / 2 - cam.x;
            double cz = (bb.minZ + bb.maxZ) / 2 - cam.z;
            double baseY = bb.minY - cam.y;
            double h = bb.maxY - bb.minY;
            double radius = Math.max(bb.maxX - bb.minX, bb.maxZ - bb.minZ) * 0.5 + 0.35;
            double t01 = 0.5 + 0.5 * Math.sin(now * 0.0032);
            double rot = now * 0.004;
            int segs = 24;
            double step = Math.PI * 2 / segs;
            for (int layer = 0; layer < 3; layer++) {
                double y = baseY + Math.max(0.02, h * t01 - layer * 0.14);
                int al = layer == 0 ? 255 : layer == 1 ? 140 : 60;
                for (int k = 0; k < segs; k++) {
                    double a0 = rot + k * step;
                    double a1 = a0 + step * 0.65;
                    for (int s = 0; s < 3; s++) {
                        double aa = a0 + (a1 - a0) * s / 3.0;
                        double ab = a0 + (a1 - a0) * (s + 1) / 3.0;
                        line(vc, e, cx + Math.cos(aa) * radius, y, cz + Math.sin(aa) * radius,
                                cx + Math.cos(ab) * radius, y, cz + Math.sin(ab) * radius, r, g, b, al);
                    }
                }
            }
        }
    }

    public static void renderHud(DrawContext ctx, MinecraftClient mc, int x, int y, boolean preview) {
        LivingEntity t = preview ? null : current();
        if (!preview && t == null) return;

        int accent = WiklSettings.accent();
        int w = HudLayout.W[HudLayout.TARGET], h = HudLayout.H[HudLayout.TARGET];

        String name = preview ? "Игрок" : t.getName().getString();
        if (name.length() > 14) name = name.substring(0, 14);
        float hp = preview ? 14f : Math.max(0f, t.getHealth());
        float max = preview ? 20f : Math.max(1f, t.getMaxHealth());
        float frac = Math.min(1f, hp / max);
        int barColor = frac < 0.35f ? 0xFFFF4444 : accent;

        ctx.fill(x, y, x + w, y + h, 0xD0141420);
        ctx.fill(x, y, x + 2, y + h, accent);

        ctx.drawText(mc.textRenderer, name, x + 8, y + 5, 0xFFFFFFFF, true);
        String txt = String.format("%.1f HP", hp);
        ctx.drawText(mc.textRenderer, txt, x + w - 8 - mc.textRenderer.getWidth(txt), y + 5, 0xFFB0B0C0, false);

        int bx = x + 8, by = y + 20, bw = w - 16;
        ctx.fill(bx, by, bx + bw, by + 6, 0xFF2A2A38);
        ctx.fill(bx, by, bx + (int) (bw * frac), by + 6, barColor);
    }

    private static void line(VertexConsumer vc, MatrixStack.Entry e, double ax, double ay, double az,
                             double bx, double by, double bz, int r, int g, int bl, int a) {
        double dx = bx - ax, dy = by - ay, dz = bz - az;
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1.0E-6) return;
        float nx = (float) (dx / len), ny = (float) (dy / len), nz = (float) (dz / len);
        vc.vertex(e, (float) ax, (float) ay, (float) az).color(r, g, bl, a).normal(e, nx, ny, nz);
        vc.vertex(e, (float) bx, (float) by, (float) bz).color(r, g, bl, a).normal(e, nx, ny, nz);
    }
}
