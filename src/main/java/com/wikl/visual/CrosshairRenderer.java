package com.wikl.visual;

import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public final class CrosshairRenderer {
    private CrosshairRenderer() {}

    private static void add(List<int[]> r, int x1, int y1, int x2, int y2) {
        r.add(new int[]{x1, y1, x2, y2});
    }

    private static void square(List<int[]> r, int x, int y, int t) {
        add(r, x - t / 2, y - t / 2, x - t / 2 + t, y - t / 2 + t);
    }

    public static void draw(DrawContext ctx, int cx, int cy) {
        int s = WiklSettings.CROSS_SIZES[WiklSettings.crossSizeIdx];
        int g = WiklSettings.CROSS_GAPS[WiklSettings.crossGapIdx];
        int t = WiklSettings.CROSS_THICKS[WiklSettings.crossThickIdx];
        int col = WiklSettings.crossColorValue();
        int style = WiklSettings.crossStyle;
        List<int[]> r = new ArrayList<>();

        switch (style) {
            case 0 -> { // plus
                add(r, cx - t / 2, cy - g - s, cx - t / 2 + t, cy - g);
                add(r, cx - t / 2, cy + g, cx - t / 2 + t, cy + g + s);
                add(r, cx - g - s, cy - t / 2, cx - g, cy - t / 2 + t);
                add(r, cx + g, cy - t / 2, cx + g + s, cy - t / 2 + t);
            }
            case 1 -> add(r, cx - t, cy - t, cx + t, cy + t); // dot
            case 2, 7 -> { // circle (7 = circle with dot)
                int rad = s + g + 2;
                for (int i = 0; i < 48; i++) {
                    double a = Math.PI * 2 * i / 48.0;
                    square(r, cx + (int) Math.round(Math.cos(a) * rad), cy + (int) Math.round(Math.sin(a) * rad), t);
                }
                if (style == 7) add(r, cx - t, cy - t, cx + t, cy + t);
            }
            case 3 -> { // X cross
                for (int k = g; k < g + s; k++) {
                    square(r, cx + k, cy + k, t);
                    square(r, cx - k, cy + k, t);
                    square(r, cx + k, cy - k, t);
                    square(r, cx - k, cy - k, t);
                }
            }
            case 4 -> { // square frame
                int q = s + g;
                add(r, cx - q, cy - q, cx + q + 1, cy - q + t);
                add(r, cx - q, cy + q + 1 - t, cx + q + 1, cy + q + 1);
                add(r, cx - q, cy - q, cx - q + t, cy + q + 1);
                add(r, cx + q + 1 - t, cy - q, cx + q + 1, cy + q + 1);
            }
            case 5 -> { // T shape
                add(r, cx - t / 2, cy + g, cx - t / 2 + t, cy + g + s);
                add(r, cx - g - s, cy - t / 2, cx - g, cy - t / 2 + t);
                add(r, cx + g, cy - t / 2, cx + g + s, cy - t / 2 + t);
            }
            case 6 -> { // corner brackets
                int q = s + g;
                int len = Math.max(3, s);
                add(r, cx - q, cy - q, cx - q + len, cy - q + t);
                add(r, cx - q, cy - q, cx - q + t, cy - q + len);
                add(r, cx + q + 1 - len, cy - q, cx + q + 1, cy - q + t);
                add(r, cx + q + 1 - t, cy - q, cx + q + 1, cy - q + len);
                add(r, cx - q, cy + q + 1 - t, cx - q + len, cy + q + 1);
                add(r, cx - q, cy + q + 1 - len, cx - q + t, cy + q + 1);
                add(r, cx + q + 1 - len, cy + q + 1 - t, cx + q + 1, cy + q + 1);
                add(r, cx + q + 1 - t, cy + q + 1 - len, cx + q + 1, cy + q + 1);
            }
            default -> { // diamond
                int q = s + g + 1;
                for (int dx = -q; dx <= q; dx++) {
                    int dy = q - Math.abs(dx);
                    square(r, cx + dx, cy + dy, t);
                    square(r, cx + dx, cy - dy, t);
                }
            }
        }

        if (WiklSettings.crossDot && style != 1 && style != 7) add(r, cx - 1, cy - 1, cx + 1, cy + 1);

        if (WiklSettings.crossOutline) {
            for (int[] q : r) ctx.fill(q[0] - 1, q[1] - 1, q[2] + 1, q[3] + 1, 0xFF000000);
        }
        for (int[] q : r) ctx.fill(q[0], q[1], q[2], q[3], col);
    }
}
