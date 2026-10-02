package com.wikl.visual;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.ingame.BookEditScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public final class InvMove {
    private InvMove() {}

    private static boolean canMoveIn(Screen s) {
        if (s == null) return false;
        if (s instanceof ChatScreen || s instanceof AbstractSignEditScreen || s instanceof BookEditScreen
                || s instanceof CreativeInventoryScreen || s instanceof AnvilScreen) return false;
        if (s.getFocused() instanceof TextFieldWidget) return false;
        return s instanceof HandledScreen<?> || s instanceof WiklScreen;
    }

    public static void tick(MinecraftClient mc) {
        if (!WiklSettings.invMove || mc.player == null || !canMoveIn(mc.currentScreen)) return;
        long handle = mc.getWindow().getHandle();
        KeyBinding[] keys = {
                mc.options.forwardKey, mc.options.backKey, mc.options.leftKey, mc.options.rightKey,
                mc.options.jumpKey, mc.options.sneakKey, mc.options.sprintKey
        };
        for (KeyBinding k : keys) {
            InputUtil.Key bound = KeyBindingHelper.getBoundKeyOf(k);
            if (bound.getCategory() == InputUtil.Type.KEYSYM) {
                k.setPressed(InputUtil.isKeyPressed(handle, bound.getCode()));
            }
        }
    }
}
