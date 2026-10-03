package com.wikl.visual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;

public final class KeystrokesHud {
    private static boolean leftDown, rightDown;
    private static final Deque<Long> leftClicks = new ArrayDeque<>();
    private static final Deque<Long> rightClicks = new ArrayDeque<>();

    private KeystrokesHud() {}

    /** Call once per rendered frame. */
    public static void poll(MinecraftClient mc) {
        long handle = mc.getWindow().getHandle();
        boolean l = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean r = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        long now = Util.getMeasuringTimeMs();
        if (mc.currentScreen == null) {
            if (l && !leftDown) leftClicks.add(now);
            if (r && !rightDown) rightClicks.add(now);
        }
        leftDown = l;
        rightDown = r;
        while (!leftClicks.isEmpty() && now - leftClicks.peekFirst() > 1000) leftClicks.pollFirst();
        while (!rightClicks.isEmpty() && now - rightClicks.peekFirst() > 1000) rightClicks.pollFirst();
    }

    private static void box(DrawContext ctx, int x, int y, int w, int h, boolean down, int accent) {
        int bg = down ? ((0xC0 << 24) | (accent & 0xFFFFFF)) : 0x90101018;
        ctx.fill(x, y, x + w, y + h, bg);
        int edge = down ? 0xFFFFFFFF : 0x60FFFFFF;
        ctx.fill(x, y, x + w, y + 1, edge);
        ctx.fill(x, y + h - 1, x + w, y + h, edge);
        ctx.fill(x, y, x + 1, y + h, edge);
        ctx.fill(x + w - 1, y, x + w, y + h, edge);
    }

    private static void key(DrawContext ctx, TextRenderer tr, int x, int y, int w, int h, String label, boolean down, int accent) {
        box(ctx, x, y, w, h, down, accent);
        ctx.drawText(tr, label, x + (w - tr.getWidth(label)) / 2, y + (h - 8) / 2, 0xFFFFFFFF, true);
    }

    private static void mouse(DrawContext ctx, TextRenderer tr, int x, int y, int w, int h, String label, int cps, boolean down, int accent) {
        box(ctx, x, y, w, h, down, accent);
        ctx.drawText(tr, label, x + (w - tr.getWidth(label)) / 2, y + 3, 0xFFFFFFFF, true);
        String c = cps + " CPS";
        ctx.drawText(tr, c, x + (w - tr.getWidth(c)) / 2, y + 12, 0xFFC8C8D8, false);
    }

    public static void draw(DrawContext ctx, MinecraftClient mc, int x, int y, boolean preview) {
        TextRenderer tr = mc.textRenderer;
        int accent = WiklSettings.accent();
        boolean w = false, a = false, s = false, d = false, sp = false;
        if (!preview) {
            w = mc.options.forwardKey.isPressed();
            a = mc.options.leftKey.isPressed();
            s = mc.options.backKey.isPressed();
            d = mc.options.rightKey.isPressed();
            sp = mc.options.jumpKey.isPressed();
        }
        key(ctx, tr, x + 25, y, 22, 22, "W", w, accent);
        key(ctx, tr, x, y + 25, 22, 22, "A", a, accent);
        key(ctx, tr, x + 25, y + 25, 22, 22, "S", s, accent);
        key(ctx, tr, x + 50, y + 25, 22, 22, "D", d, accent);
        box(ctx, x, y + 50, 72, 12, sp, accent);
        ctx.fill(x + 24, y + 55, x + 48, y + 57, 0xFFFFFFFF);
        mouse(ctx, tr, x, y + 65, 34, 22, "ЛКМ", leftClicks.size(), !preview && leftDown, accent);
        mouse(ctx, tr, x + 38, y + 65, 34, 22, "ПКМ", rightClicks.size(), !preview && rightDown, accent);
    }
}
