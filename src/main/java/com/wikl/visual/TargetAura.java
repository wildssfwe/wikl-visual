package com.wikl.visual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;

import java.util.Random;

/** After you hit something, particles keep appearing around it and follow it for a few seconds. */
public final class TargetAura {
    private static final Random RND = new Random();
    private static int ticks;

    private TargetAura() {}

    private static ParticleEffect type(int style) {
        return switch (style) {
            case 0 -> ParticleTypes.ELECTRIC_SPARK;
            case 1 -> ParticleTypes.FLAME;
            case 2 -> ParticleTypes.HEART;
            case 3 -> ParticleTypes.SNOWFLAKE;
            case 4 -> ParticleTypes.SOUL;
            case 5 -> ParticleTypes.END_ROD;
            case 6 -> ParticleTypes.ENCHANT;
            default -> ParticleTypes.LARGE_SMOKE;
        };
    }

    public static void tick(MinecraftClient mc) {
        ticks++;
        if (!WiklSettings.aura || mc.world == null) return;
        LivingEntity t = TargetRenderer.active();
        if (t == null) return;

        ClientWorld w = mc.world;
        int style = WiklSettings.auraStyle;
        ParticleEffect pt = type(style);
        int count = style == 2 ? (ticks % 4 == 0 ? 1 : 0) : 3;
        double width = Math.max(0.4, t.getWidth());
        double height = Math.max(0.5, t.getHeight());

        for (int i = 0; i < count; i++) {
            double x = t.getX() + (RND.nextDouble() - 0.5) * width * 1.5;
            double y = t.getY() + RND.nextDouble() * height;
            double z = t.getZ() + (RND.nextDouble() - 0.5) * width * 1.5;
            double vx = 0, vy = 0.02 + RND.nextDouble() * 0.03, vz = 0;
            if (style == 0 || style == 5 || style == 6) {
                vx = (RND.nextDouble() - 0.5) * 0.1;
                vz = (RND.nextDouble() - 0.5) * 0.1;
            }
            w.addParticle(pt, x, y, z, vx, vy, vz);
        }
    }
}
