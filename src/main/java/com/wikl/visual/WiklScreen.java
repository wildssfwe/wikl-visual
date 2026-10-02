package com.wikl.visual;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class WiklScreen extends Screen {
    private record Module(String name, Supplier<String> desc, BooleanSupplier get, Consumer<Boolean> set, Runnable rightClick) {
        Module(String name, String desc, BooleanSupplier get, Consumer<Boolean> set) {
            this(name, () -> desc, get, set, null);
        }
    }

    private static final String[] TABS = {"HUD", "Combat", "Visual", "Movement", "Theme"};
    private static final int THEME_TAB = 4;
    private static final int SIDEBAR = 120;
    private static final int CARD_H = 40;
    private static final int CARD_GAP = 8;

    private final long openTime = Util.getMeasuringTimeMs();
    private final Map<String, Float> toggleAnim = new HashMap<>();
    private final Map<Integer, Float> tabHover = new HashMap<>();
    private int tab = 0;
    private int panelW = 460, panelH = 300, left, top;

    private final List<Module> hudModules = List.of(
            new Module("Watermark", "Show the wikl visual logo on screen", () -> WiklSettings.watermark, v -> WiklSettings.watermark = v),
            new Module("FPS counter", "Show your current FPS", () -> WiklSettings.fps, v -> WiklSettings.fps = v),
            new Module("Coordinates", "Show your XYZ position", () -> WiklSettings.coords, v -> WiklSettings.coords = v),
            new Module("Direction", "Show the direction you are facing", () -> WiklSettings.direction, v -> WiklSettings.direction = v)
    );
    private final List<Module> combatModules = List.of(
            new Module("Target highlight", "Outline and info panel for the mob or player you hit", () -> WiklSettings.target, v -> WiklSettings.target = v),
            new Module("Trajectory", "Show where your pearl, wind charge or throwable lands", () -> WiklSettings.trajectory, v -> WiklSettings.trajectory = v),
            new Module("Enemy trajectory", "Show where thrown or shot projectiles of others land", () -> WiklSettings.enemyTrajectory, v -> WiklSettings.enemyTrajectory = v)
    );
    private final List<Module> visualModules = List.of(
            new Module("Aspect ratio", () -> "Right click to change: " + WiklSettings.ASPECT_NAMES[WiklSettings.aspectIndex],
                    () -> WiklSettings.aspectEnabled, v -> WiklSettings.aspectEnabled = v, WiklSettings::nextAspect),
            new Module("Menu animation", "Slide and fade when the menu opens", () -> WiklSettings.animations, v -> WiklSettings.animations = v),
            new Module("Accent pulse", "Animated glow on the menu header", () -> WiklSettings.pulse, v -> WiklSettings.pulse = v)
    );
    private final List<Module> movementModules = List.of(
            new Module("Move in menus", "Walk while inventory or chests are open", () -> WiklSettings.invMove, v -> WiklSettings.invMove = v)
    );

    public WiklScreen() { super(Text.literal("wikl visual")); }

    @Override
    protected void init() {
        panelW = Math.min(460, width - 20);
        panelH = Math.min(300, height - 20);
        left = (width - panelW) / 2;
        top = (height - panelH) / 2;
    }

    // ---------- helpers ----------
    private static int withAlpha(int argb, float a) {
        int alpha = (int) (((argb >>> 24) & 0xFF) * Math.max(0f, Math.min(1f, a)));
        return (alpha << 24) | (argb & 0xFFFFFF);
    }

    private static int lerp(int c1, int c2, float t) {
        int a = (int) (((c1 >>> 24) & 0xFF) + (((c2 >>> 24) & 0xFF) - ((c1 >>> 24) & 0xFF)) * t);
        int r = (int) (((c1 >> 16) & 0xFF) + (((c2 >> 16) & 0xFF) - ((c1 >> 16) & 0xFF)) * t);
        int g = (int) (((c1 >> 8) & 0xFF) + (((c2 >> 8) & 0xFF) - ((c1 >> 8) & 0xFF)) * t);
        int b = (int) ((c1 & 0xFF) + ((c2 & 0xFF) - (c1 & 0xFF)) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static void rrect(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        ctx.fill(x1 + 2, y1, x2 - 2, y2, color);
        ctx.fill(x1, y1 + 2, x2, y2 - 2, color);
        ctx.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, color);
    }

    private static boolean inside(double mx, double my, int x1, int y1, int x2, int y2) {
        return mx >= x1 && mx < x2 && my >= y1 && my < y2;
    }

    private float progress() {
        if (!WiklSettings.animations) return 1f;
        float t = Math.min(1f, (Util.getMeasuringTimeMs() - openTime) / 250f);
        return 1f - (1f - t) * (1f - t) * (1f - t);
    }

    private List<Module> currentModules() {
        return switch (tab) { case 0 -> hudModules; case 1 -> combatModules; case 2 -> visualModules; default -> movementModules; };
    }

    private int cardsX1() { return left + SIDEBAR + 14; }
    private int cardsX2() { return left + panelW - 14; }
    private int cardY(int i) { return top + 78 + i * (CARD_H + CARD_GAP); }

    // ---------- rendering ----------
    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, withAlpha(0xFF05050A, 0.55f * progress()));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        float p = progress();
        int accent = WiklSettings.accent();
        int oy = (int) ((1f - p) * 18);
        int T = top + oy;
        long now = Util.getMeasuringTimeMs();

        // screen-corner logo (top-left)
        ctx.getMatrices().push();
        ctx.getMatrices().scale(2f, 2f, 1f);
        ctx.drawText(textRenderer, "wikl", 5, 4, withAlpha(accent, p), true);
        ctx.drawText(textRenderer, "visual", 5 + textRenderer.getWidth("wikl "), 4, withAlpha(0xFFFFFFFF, p), true);
        ctx.getMatrices().pop();

        // panel shadow + body
        rrect(ctx, left - 3, T - 3, left + panelW + 3, T + panelH + 3, withAlpha(accent, 0.18f * p));
        rrect(ctx, left, T, left + panelW, T + panelH, withAlpha(0xFF14141C, p));
        // sidebar
        ctx.fill(left + 1, T + 40, left + SIDEBAR, T + panelH - 1, withAlpha(0xFF0E0E14, p));
        // header
        ctx.fill(left + 2, T + 1, left + panelW - 2, T + 38, withAlpha(0xFF0B0B11, p));

        // animated accent line
        for (int x = 0; x < panelW - 4; x += 3) {
            float wave = WiklSettings.pulse ? (float) (0.5 + 0.5 * Math.sin(x * 0.035 - now * 0.004)) : 1f;
            int c = lerp(withAlpha(accent, 0.35f * p), withAlpha(accent, p), wave);
            ctx.fill(left + 2 + x, T + 38, Math.min(left + panelW - 2, left + 5 + x), T + 40, c);
        }

        // header text
        ctx.getMatrices().push();
        ctx.getMatrices().scale(1.4f, 1.4f, 1f);
        ctx.drawText(textRenderer, "wikl", (int) ((left + 14) / 1.4f), (int) ((T + 11) / 1.4f), withAlpha(accent, p), true);
        ctx.drawText(textRenderer, "visual", (int) ((left + 14) / 1.4f) + textRenderer.getWidth("wikl "), (int) ((T + 11) / 1.4f), withAlpha(0xFFFFFFFF, p), true);
        ctx.getMatrices().pop();
        ctx.drawText(textRenderer, "v1.0.0", left + panelW - 14 - textRenderer.getWidth("v1.0.0"), T + 15, withAlpha(0xFF8A8A99, p), false);

        // tabs
        for (int i = 0; i < TABS.length; i++) {
            int ty = T + 54 + i * 36;
            boolean hover = inside(mouseX, mouseY, left + 10, ty, left + SIDEBAR - 8, ty + 28);
            float h = tabHover.getOrDefault(i, 0f);
            h += ((hover || i == tab) ? 1f - h : -h) * Math.min(1f, delta * 0.35f);
            tabHover.put(i, h);
            int bg = lerp(withAlpha(0xFF1A1A24, p), withAlpha(0xFF272735, p), h);
            rrect(ctx, left + 10, ty, left + SIDEBAR - 8, ty + 28, bg);
            if (i == tab) ctx.fill(left + 10, ty + 5, left + 13, ty + 23, withAlpha(accent, p));
            ctx.drawText(textRenderer, TABS[i], left + 24, ty + 10, withAlpha(i == tab ? 0xFFFFFFFF : 0xFFB0B0C0, p), false);
        }

        // content title
        ctx.drawText(textRenderer, TABS[tab], cardsX1(), T + 50, withAlpha(0xFFFFFFFF, p), true);
        String sub = switch (tab) { case 0 -> "On-screen information"; case 1 -> "Fight helpers"; case 2 -> "Look and feel"; case 3 -> "Movement options"; default -> "Choose your accent color"; };
        ctx.drawText(textRenderer, sub, cardsX1(), T + 63, withAlpha(0xFF8A8A99, p), false);

        if (tab == THEME_TAB) renderThemeCards(ctx, mouseX, mouseY, p, oy, accent);
        else renderModuleCards(ctx, mouseX, mouseY, delta, p, oy, accent);

        // footer
        ctx.drawText(textRenderer, "Right Shift: open   ESC: close", left + 14, T + panelH - 14, withAlpha(0xFF6C6C7C, p), false);
    }

    private void renderModuleCards(DrawContext ctx, int mx, int my, float delta, float p, int oy, int accent) {
        List<Module> mods = currentModules();
        for (int i = 0; i < mods.size(); i++) {
            Module m = mods.get(i);
            int y = cardY(i) + oy;
            int x1 = cardsX1(), x2 = cardsX2();
            boolean hover = inside(mx, my, x1, y, x2, y + CARD_H);
            boolean on = m.get().getAsBoolean();

            float a = toggleAnim.getOrDefault(m.name(), on ? 1f : 0f);
            a += ((on ? 1f : 0f) - a) * Math.min(1f, delta * 0.4f);
            toggleAnim.put(m.name(), a);

            rrect(ctx, x1, y, x2, y + CARD_H, withAlpha(hover ? 0xFF2A2A38 : 0xFF21212C, p));
            ctx.fill(x1, y + 4, x1 + 2, y + CARD_H - 4, withAlpha(lerp(0xFF444455, accent, a), p));
            ctx.drawText(textRenderer, m.name(), x1 + 14, y + 9, withAlpha(0xFFF0F0F8, p), false);
            ctx.drawText(textRenderer, m.desc().get(), x1 + 14, y + 23, withAlpha(0xFF8A8A99, p), false);

            // toggle switch
            int sw = 34, sh = 16;
            int sx = x2 - sw - 14, sy = y + (CARD_H - sh) / 2;
            rrect(ctx, sx, sy, sx + sw, sy + sh, withAlpha(lerp(0xFF3A3A48, accent, a), p));
            int kx = sx + 2 + (int) ((sw - sh) * a);
            rrect(ctx, kx, sy + 2, kx + sh - 4, sy + sh - 2, withAlpha(0xFFFFFFFF, p));
        }
    }

    private void renderThemeCards(DrawContext ctx, int mx, int my, float p, int oy, int accent) {
        for (int i = 0; i < WiklSettings.ACCENTS.length; i++) {
            int y = cardY(i) + oy;
            if (y + CARD_H > top + panelH + oy - 22) break;
            int x1 = cardsX1(), x2 = cardsX2();
            boolean hover = inside(mx, my, x1, y, x2, y + CARD_H);
            boolean sel = i == WiklSettings.accentIndex;
            int col = WiklSettings.ACCENTS[i];
            rrect(ctx, x1, y, x2, y + CARD_H, withAlpha(hover ? 0xFF2A2A38 : 0xFF21212C, p));
            ctx.fill(x1, y + 4, x1 + 2, y + CARD_H - 4, withAlpha(col, p));
            rrect(ctx, x1 + 14, y + 10, x1 + 34, y + 30, withAlpha(col, p));
            ctx.drawText(textRenderer, WiklSettings.ACCENT_NAMES[i], x1 + 44, y + 16, withAlpha(0xFFF0F0F8, p), false);
            if (sel) ctx.drawText(textRenderer, "Selected", x2 - 14 - textRenderer.getWidth("Selected"), y + 16, withAlpha(col, p), false);
        }
    }

    // ---------- input ----------
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0 || button == 1) {
            for (int i = 0; i < TABS.length; i++) {
                int ty = top + 54 + i * 36;
                if (inside(mx, my, left + 10, ty, left + SIDEBAR - 8, ty + 28)) { tab = i; return true; }
            }
            if (tab == THEME_TAB) {
                for (int i = 0; i < WiklSettings.ACCENTS.length; i++) {
                    int y = cardY(i);
                    if (inside(mx, my, cardsX1(), y, cardsX2(), y + CARD_H)) { WiklSettings.accentIndex = i; return true; }
                }
            } else {
                List<Module> mods = currentModules();
                for (int i = 0; i < mods.size(); i++) {
                    int y = cardY(i);
                    if (inside(mx, my, cardsX1(), y, cardsX2(), y + CARD_H)) {
                        Module m = mods.get(i);
                        if (button == 1) {
                            if (m.rightClick() != null) m.rightClick().run();
                        } else {
                            m.set().accept(!m.get().getAsBoolean());
                        }
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean shouldPause() { return false; }
}
