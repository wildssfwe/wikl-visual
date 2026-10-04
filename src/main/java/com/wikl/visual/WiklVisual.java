package com.wikl.visual;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class WiklVisual implements ClientModInitializer {
    private static KeyBinding openMenu;

    private static String dirName(String d) {
        return switch (d) {
            case "north" -> "север";
            case "south" -> "юг";
            case "east" -> "восток";
            case "west" -> "запад";
            default -> d;
        };
    }

    @Override
    public void onInitializeClient() {
        WiklConfig.load();

        openMenu = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.wikl_visual.open_menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.wikl_visual"));

        TrajectoryRenderer.register();
        TargetRenderer.register();
        DamageNumbers.register();
        EffectCleaner.register();
        ShieldStatus.register();

        ClientTickEvents.START_CLIENT_TICK.register(InvMove::tick);
        ClientTickEvents.START_CLIENT_TICK.register(FpsBoost::tick);
        ClientTickEvents.END_CLIENT_TICK.register(HitEffects::tick);
        ClientTickEvents.END_CLIENT_TICK.register(TargetAura::tick);
        ClientTickEvents.END_CLIENT_TICK.register(FastPlace::tick);
        ClientTickEvents.END_CLIENT_TICK.register(DamageNumbers::tick);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenu.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new WiklScreen());
            }
        });

        HudRenderCallback.EVENT.register((ctx, tickCounter) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.options.hudHidden
                    || mc.currentScreen instanceof WiklScreen || mc.currentScreen instanceof HudEditScreen) return;

            int sw = ctx.getScaledWindowWidth();
            int sh = ctx.getScaledWindowHeight();
            int accent = WiklSettings.accent();
            int y = 6;
            if (WiklSettings.watermark) {
                ctx.getMatrices().push();
                ctx.getMatrices().scale(1.5f, 1.5f, 1f);
                ctx.drawText(mc.textRenderer, "wikl", 4, 4, accent, true);
                ctx.drawText(mc.textRenderer, "visual", 4 + mc.textRenderer.getWidth("wikl "), 4, 0xFFFFFFFF, true);
                ctx.getMatrices().pop();
                y = 28;
            }
            if (WiklSettings.fps) { ctx.drawText(mc.textRenderer, "FPS: " + mc.getCurrentFps(), 6, y, 0xFFFFFFFF, true); y += 11; }
            if (WiklSettings.coords) {
                String c = String.format("XYZ: %.1f / %.1f / %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ());
                ctx.drawText(mc.textRenderer, c, 6, y, 0xFFFFFFFF, true); y += 11;
            }
            if (WiklSettings.direction) {
                ctx.drawText(mc.textRenderer, "Смотрите: " + dirName(mc.player.getHorizontalFacing().asString()), 6, y, 0xFFFFFFFF, true);
            }

            if (WiklSettings.customCrosshair && mc.currentScreen == null && mc.options.getPerspective().isFirstPerson()) {
                CrosshairRenderer.draw(ctx, sw / 2, sh / 2);
            }

            KeystrokesHud.poll(mc);
            if (WiklSettings.keystrokes) {
                KeystrokesHud.draw(ctx, mc, HudLayout.x(HudLayout.KEYS, sw), HudLayout.y(HudLayout.KEYS, sh), false);
            }
            if (WiklSettings.effectsHud) {
                EffectsHud.draw(ctx, mc, HudLayout.x(HudLayout.EFFECTS, sw), HudLayout.y(HudLayout.EFFECTS, sh), false);
            }
            if (WiklSettings.armorHud) {
                ArmorHud.draw(ctx, mc, HudLayout.x(HudLayout.ARMOR, sw), HudLayout.y(HudLayout.ARMOR, sh), false);
            }
            TargetRenderer.renderHud(ctx, mc, HudLayout.x(HudLayout.TARGET, sw), HudLayout.y(HudLayout.TARGET, sh), false);
            TrajectoryRenderer.renderHud(ctx, mc);
        });
    }
}
