package com.wikl.visual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ConfigNameScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget field;
    private String error = "";

    public ConfigNameScreen(Screen parent) {
        super(Text.literal("Новый конфиг"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        field = new TextFieldWidget(textRenderer, width / 2 - 100, height / 2 - 10, 200, 20, Text.literal("Имя"));
        field.setMaxLength(24);
        field.setText("Мой конфиг");
        addDrawableChild(field);
        setInitialFocus(field);
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // drawn in render()
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        int accent = WiklSettings.accent();
        ctx.fill(0, 0, width, height, 0xB0000000);
        int px = width / 2 - 130, py = height / 2 - 55;
        ctx.fill(px - 1, py - 1, px + 261, py + 111, (0xA0 << 24) | (accent & 0xFFFFFF));
        ctx.fill(px, py, px + 260, py + 110, 0xFF14141C);
        ctx.fill(px, py, px + 260, py + 2, accent);
        ctx.drawCenteredTextWithShadow(textRenderer, "Новый конфиг", width / 2, py + 12, 0xFFFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer, "Введите имя и нажмите Enter", width / 2, py + 26, 0xFFB0B0C0);
        super.render(ctx, mouseX, mouseY, delta);
        if (!error.isEmpty()) ctx.drawCenteredTextWithShadow(textRenderer, error, width / 2, py + 62, 0xFFFF5555);
        ctx.drawCenteredTextWithShadow(textRenderer, "ESC - отмена", width / 2, py + 92, 0xFF6C6C7C);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            String name = WiklConfig.sanitize(field.getText());
            if (name.isEmpty()) {
                error = "Имя не может быть пустым";
                return true;
            }
            if (!WiklConfig.saveProfile(name)) {
                error = "Не удалось сохранить";
                return true;
            }
            MinecraftClient.getInstance().setScreen(parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
