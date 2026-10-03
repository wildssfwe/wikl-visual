package com.wikl.visual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.ArrayList;
import java.util.List;

public final class EffectsHud {
    private record Row(String name, String time, int color, boolean low) {}

    private EffectsHud() {}

    private static String fmt(int ticks) {
        int sec = (ticks + 19) / 20;
        return String.format("%d:%02d", sec / 60, sec % 60);
    }

    public static void draw(DrawContext ctx, MinecraftClient mc, int x, int y, boolean preview) {
        TextRenderer tr = mc.textRenderer;
        List<Row> rows = new ArrayList<>();
        if (preview) {
            rows.add(new Row("Скорость II", "1:24", 0xFF55FF88, false));
            rows.add(new Row("Сила", "0:45", 0xFF55FF88, false));
            rows.add(new Row("Слабость", "0:08", 0xFFFF5555, true));
        } else if (mc.player != null) {
            for (StatusEffectInstance inst : mc.player.getStatusEffects()) {
                String name = inst.getEffectType().value().getName().getString();
                if (inst.getAmplifier() > 0) name += " " + (inst.getAmplifier() + 1);
                boolean inf = inst.isInfinite();
                String time = inf ? "inf" : fmt(inst.getDuration());
                int color = inst.getEffectType().value().isBeneficial() ? 0xFF55FF88 : 0xFFFF5555;
                rows.add(new Row(name, time, color, !inf && inst.getDuration() < 200));
                if (rows.size() >= 8) break;
            }
        }
        int w = HudLayout.W[HudLayout.EFFECTS];
        for (int i = 0; i < rows.size(); i++) {
            Row r = rows.get(i);
            int ry = y + i * 13;
            ctx.fill(x, ry, x + w, ry + 12, 0xA0101018);
            ctx.fill(x, ry, x + 2, ry + 12, r.color());
            ctx.drawText(tr, r.name(), x + 6, ry + 2, 0xFFFFFFFF, false);
            ctx.drawText(tr, r.time(), x + w - 4 - tr.getWidth(r.time()), ry + 2, r.low() ? 0xFFFF5555 : 0xFFB0B0C0, false);
        }
    }
}
