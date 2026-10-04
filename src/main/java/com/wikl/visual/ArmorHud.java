package com.wikl.visual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public final class ArmorHud {
    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private ArmorHud() {}

    private static ItemStack sample(int i) {
        ItemStack st = switch (i) {
            case 0 -> new ItemStack(Items.DIAMOND_HELMET);
            case 1 -> new ItemStack(Items.DIAMOND_CHESTPLATE);
            case 2 -> new ItemStack(Items.IRON_LEGGINGS);
            default -> new ItemStack(Items.NETHERITE_BOOTS);
        };
        st.setDamage((int) (st.getMaxDamage() * (0.1 + 0.2 * i)));
        return st;
    }

    public static void draw(DrawContext ctx, MinecraftClient mc, int x, int y, boolean preview) {
        TextRenderer tr = mc.textRenderer;
        int w = HudLayout.W[HudLayout.ARMOR];
        for (int i = 0; i < 4; i++) {
            ItemStack st = preview ? sample(i) : mc.player.getEquippedStack(SLOTS[i]);
            int ry = y + i * 18;
            ctx.fill(x, ry, x + w, ry + 17, 0x90101018);
            if (st.isEmpty()) continue;

            ctx.drawItem(st, x + 1, ry + 1);
            ctx.drawStackOverlay(tr, st, x + 1, ry + 1);

            if (st.isDamageable()) {
                int max = st.getMaxDamage();
                int left = Math.max(0, max - st.getDamage());
                float f = Math.min(1f, left / (float) Math.max(1, max));
                int color = 0xFF000000 | ((int) (255 * (1f - f)) << 16) | ((int) (255 * f) << 8) | 0x40;
                ctx.drawText(tr, String.valueOf(left), x + 21, ry + 5, color, true);
            }
        }
    }
}
