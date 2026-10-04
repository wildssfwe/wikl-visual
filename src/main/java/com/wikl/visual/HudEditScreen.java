package com.wikl.visual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class HudEditScreen extends Screen {
    private final Screen parent;
    private int dragging = -1;
    private int grabX, grabY;

    public HudEditScreen(Screen parent) {
        super(Text.literal("HUD"));
        this.parent = parent;
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // drawn manually in render()
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        int accent = WiklSettings.accent();
        ctx.fill(0, 0, width, height, 0x90000000);

        ctx.drawCenteredTextWithShadow(textRenderer, "Редактор HUD", width / 2, 12, 0xFFFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer, "Перетаскивайте элементы мышью.  R - сбросить   ESC - готово",
                width / 2, 26, 0xFFB0B0C0);

        for (int id = 0; id < HudLayout.NAMES.length; id++) {
            int x = HudLayout.x(id, width);
            int y = HudLayout.y(id, height);
            if (id == HudLayout.TARGET) TargetRenderer.renderHud(ctx, mc, x, y, true);
            else if (id == HudLayout.KEYS) KeystrokesHud.draw(ctx, mc, x, y, true);
            else if (id == HudLayout.EFFECTS) EffectsHud.draw(ctx, mc, x, y, true);
            else ArmorHud.draw(ctx, mc, x, y, true);

            int w = HudLayout.W[id], h = HudLayout.H[id];
            boolean hot = id == dragging || (dragging < 0 && mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h);
            int edge = hot ? 0xFFFFFFFF : ((0xB0 << 24) | (accent & 0xFFFFFF));
            ctx.fill(x - 2, y - 2, x + w + 2, y - 1, edge);
            ctx.fill(x - 2, y + h + 1, x + w + 2, y + h + 2, edge);
            ctx.fill(x - 2, y - 2, x - 1, y + h + 2, edge);
            ctx.fill(x + w + 1, y - 2, x + w + 2, y + h + 2, edge);
            ctx.drawText(textRenderer, HudLayout.NAMES[id], x, y - 12, hot ? 0xFFFFFFFF : 0xFFB0B0C0, true);
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            for (int id = HudLayout.NAMES.length - 1; id >= 0; id--) {
                int x = HudLayout.x(id, width), y = HudLayout.y(id, height);
                if (mx >= x && mx < x + HudLayout.W[id] && my >= y && my < y + HudLayout.H[id]) {
                    dragging = id;
                    grabX = (int) mx - x;
                    grabY = (int) my - y;
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging >= 0) {
            HudLayout.set(dragging, (int) mx - grabX, (int) my - grabY, width, height);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (dragging >= 0) {
            dragging = -1;
            WiklConfig.save();
            return true;
        }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R) {
            HudLayout.resetAll();
            WiklConfig.save();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }

    @Override
    public void removed() {
        WiklConfig.save();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
