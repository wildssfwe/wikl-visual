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

        int accent = WiklSettings.accent();
        int r = (accent >> 16) & 0xFF, g = (accent >> 8) & 0xFF, b = accent & 0xFF;

        Vec3d cam = ctx.camera().getPos();
        Box box = t.getBoundingBox().expand(0.05).offset(-cam.x, -cam.y, -cam.z);
        MatrixStack.Entry e = ctx.matrixStack().peek();
        VertexConsumer vc = ctx.consumers().getBuffer(RenderLayer.getLines());

        double x1 = box.minX, y1 = box.minY, z1 = box.minZ, x2 = box.maxX, y2 = box.maxY, z2 = box.maxZ;
        // bottom
        line(vc, e, x1, y1, z1, x2, y1, z1, r, g, b);
        line(vc, e, x2, y1, z1, x2, y1, z2, r, g, b);
        line(vc, e, x2, y1, z2, x1, y1, z2, r, g, b);
        line(vc, e, x1, y1, z2, x1, y1, z1, r, g, b);
        // top
        line(vc, e, x1, y2, z1, x2, y2, z1, r, g, b);
        line(vc, e, x2, y2, z1, x2, y2, z2, r, g, b);
        line(vc, e, x2, y2, z2, x1, y2, z2, r, g, b);
        line(vc, e, x1, y2, z2, x1, y2, z1, r, g, b);
        // pillars
        line(vc, e, x1, y1, z1, x1, y2, z1, r, g, b);
        line(vc, e, x2, y1, z1, x2, y2, z1, r, g, b);
        line(vc, e, x2, y1, z2, x2, y2, z2, r, g, b);
        line(vc, e, x1, y1, z2, x1, y2, z2, r, g, b);
    }

    public static void renderHud(DrawContext ctx, MinecraftClient mc) {
        LivingEntity t = current();
        if (t == null) return;

        int accent = WiklSettings.accent();
        int w = 110, h = 34;
        int x = ctx.getScaledWindowWidth() / 2 + 24;
        int y = ctx.getScaledWindowHeight() / 2 + 14;

        ctx.fill(x, y, x + w, y + h, 0xD0141420);
        ctx.fill(x, y, x + 2, y + h, accent);

        String name = t.getName().getString();
        if (name.length() > 16) name = name.substring(0, 16);
        ctx.drawText(mc.textRenderer, name, x + 8, y + 5, 0xFFFFFFFF, true);

        float hp = Math.max(0f, t.getHealth());
        float max = Math.max(1f, t.getMaxHealth());
        float frac = Math.min(1f, hp / max);
        int bx = x + 8, by = y + 20, bw = w - 16;
        ctx.fill(bx, by, bx + bw, by + 6, 0xFF2A2A38);
        ctx.fill(bx, by, bx + (int) (bw * frac), by + 6, accent);
        String txt = String.format("%.1f HP", hp);
        ctx.drawText(mc.textRenderer, txt, x + w - 8 - mc.textRenderer.getWidth(txt), y + 5, 0xFFB0B0C0, false);
    }

    private static void line(VertexConsumer vc, MatrixStack.Entry e, double ax, double ay, double az,
                             double bx, double by, double bz, int r, int g, int bl) {
        double dx = bx - ax, dy = by - ay, dz = bz - az;
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1.0E-6) return;
        float nx = (float) (dx / len), ny = (float) (dy / len), nz = (float) (dz / len);
        vc.vertex(e, (float) ax, (float) ay, (float) az).color(r, g, bl, 255).normal(e, nx, ny, nz);
        vc.vertex(e, (float) bx, (float) by, (float) bz).color(r, g, bl, 255).normal(e, nx, ny, nz);
    }
}
