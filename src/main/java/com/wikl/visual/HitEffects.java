package com.wikl.visual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public final class HitEffects {
    private static final Random RND = new Random();

    private HitEffects() {}

    private static void burst(ClientWorld w, ParticleEffect type, Vec3d c, int count, double speed) {
        for (int i = 0; i < count; i++) {
            double vx = (RND.nextDouble() - 0.5) * speed * 2;
            double vy = (RND.nextDouble() - 0.3) * speed * 2;
            double vz = (RND.nextDouble() - 0.5) * speed * 2;
            w.addParticle(type, c.x, c.y, c.z, vx, vy, vz);
        }
    }

    public static void play(Entity e) {
        if (!WiklSettings.hitAnim) return;
        ClientWorld w = MinecraftClient.getInstance().world;
        if (w == null) return;
        Vec3d c = e.getPos().add(0, e.getHeight() * 0.6, 0);
        switch (WiklSettings.hitAnimIndex) {
            case 0 -> burst(w, ParticleTypes.CRIT, c, 14, 0.5);
            case 1 -> burst(w, ParticleTypes.END_ROD, c, 14, 0.2);
            case 2 -> burst(w, ParticleTypes.FLAME, c, 12, 0.1);
            case 3 -> {
                for (int i = 0; i < 5; i++) {
                    w.addParticle(ParticleTypes.HEART,
                            c.x + (RND.nextDouble() - 0.5) * 0.8, c.y + 0.4 + RND.nextDouble() * 0.4,
                            c.z + (RND.nextDouble() - 0.5) * 0.8, 0, 0.05, 0);
                }
            }
            case 4 -> burst(w, ParticleTypes.TOTEM_OF_UNDYING, c, 16, 0.5);
            default -> {
                for (int i = 0; i < 24; i++) {
                    double a = Math.PI * 2 * i / 24.0;
                    w.addParticle(ParticleTypes.ENCHANTED_HIT,
                            c.x + Math.cos(a) * 0.6, c.y, c.z + Math.sin(a) * 0.6,
                            Math.cos(a) * 0.1, 0.02, Math.sin(a) * 0.1);
                }
            }
        }
    }
}
