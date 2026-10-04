package com.wikl.visual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public final class HitEffects {
    private static final Random RND = new Random();

    /** Effects that keep spawning particles for a few ticks and follow the target. */
    private static final class Active {
        final Entity entity;
        final int style;
        int age;

        Active(Entity entity, int style) {
            this.entity = entity;
            this.style = style;
        }
    }

    private static final List<Active> ACTIVE = new ArrayList<>();

    private HitEffects() {}

    /** Spawns through the particle manager directly so the "Minimal" particle setting does not hide it. */
    public static void spawn(ParticleEffect type, double x, double y, double z, double vx, double vy, double vz) {
        MinecraftClient.getInstance().particleManager.addParticle(type, x, y, z, vx, vy, vz);
    }

    private static void burst(ClientWorld w, ParticleEffect type, Vec3d c, int count, double speed) {
        for (int i = 0; i < count; i++) {
            double vx = (RND.nextDouble() - 0.5) * speed * 2;
            double vy = (RND.nextDouble() - 0.3) * speed * 2;
            double vz = (RND.nextDouble() - 0.5) * speed * 2;
            spawn(type, c.x, c.y, c.z, vx, vy, vz);
        }
    }

    private static int maxAge(int style) {
        return switch (style) {
            case 6 -> 16;
            case 11 -> 18;
            case 12 -> 8;
            default -> 0;
        };
    }

    private static int plays;

    public static void play(Entity e) {
        if (!WiklSettings.hitAnim) return;
        ClientWorld w = MinecraftClient.getInstance().world;
        if (w == null) return;
        if (++plays <= 5) WiklLog.LOG.info("hit effect #{} style {}", plays, WiklSettings.hitAnimIndex);
        Vec3d c = e.getPos().add(0, e.getHeight() * 0.6, 0);
        int style = WiklSettings.hitAnimIndex;
        switch (style) {
            case 0 -> burst(w, ParticleTypes.CRIT, c, 14, 0.5);
            case 1 -> burst(w, ParticleTypes.END_ROD, c, 14, 0.2);
            case 2 -> burst(w, ParticleTypes.FLAME, c, 12, 0.1);
            case 3 -> {
                for (int i = 0; i < 5; i++) {
                    spawn(ParticleTypes.HEART,
                            c.x + (RND.nextDouble() - 0.5) * 0.8, c.y + 0.4 + RND.nextDouble() * 0.4,
                            c.z + (RND.nextDouble() - 0.5) * 0.8, 0, 0.05, 0);
                }
            }
            case 4 -> burst(w, ParticleTypes.TOTEM_OF_UNDYING, c, 16, 0.5);
            case 5 -> {
                for (int i = 0; i < 24; i++) {
                    double a = Math.PI * 2 * i / 24.0;
                    spawn(ParticleTypes.ENCHANTED_HIT,
                            c.x + Math.cos(a) * 0.6, c.y, c.z + Math.sin(a) * 0.6,
                            Math.cos(a) * 0.1, 0.02, Math.sin(a) * 0.1);
                }
            }
            case 7 -> burst(w, ParticleTypes.FIREWORK, c, 18, 0.25);
            case 8 -> {
                for (int i = 0; i < 8; i++) {
                    spawn(ParticleTypes.SOUL,
                            c.x + (RND.nextDouble() - 0.5) * 0.6, c.y - 0.3 + RND.nextDouble() * 0.5,
                            c.z + (RND.nextDouble() - 0.5) * 0.6, 0, 0.08 + RND.nextDouble() * 0.05, 0);
                }
            }
            case 9 -> burst(w, ParticleTypes.SNOWFLAKE, c, 18, 0.15);
            case 10 -> burst(w, ParticleTypes.ELECTRIC_SPARK, c, 20, 0.4);
            case 13 -> burst(w, ParticleTypes.POOF, c, 10, 0.15);
            default -> ACTIVE.add(new Active(e, style)); // 6 spiral, 11 magic, 12 volcano
        }
    }

    public static void tick(MinecraftClient mc) {
        if (ACTIVE.isEmpty()) return;
        ClientWorld w = mc.world;
        if (w == null) {
            ACTIVE.clear();
            return;
        }
        Iterator<Active> it = ACTIVE.iterator();
        while (it.hasNext()) {
            Active a = it.next();
            a.age++;
            Entity e = a.entity;
            if (e.isRemoved() || a.age > maxAge(a.style)) {
                it.remove();
                continue;
            }
            double ex = e.getX(), ey = e.getY(), ez = e.getZ(), h = e.getHeight();
            switch (a.style) {
                case 6 -> {
                    double ang = a.age * 0.7;
                    double yy = ey + h * a.age / 16.0;
                    for (int s = 0; s < 2; s++) {
                        double aa = ang + s * Math.PI;
                        spawn(ParticleTypes.END_ROD, ex + Math.cos(aa) * 0.7, yy, ez + Math.sin(aa) * 0.7, 0, 0.01, 0);
                    }
                }
                case 11 -> {
                    for (int s = 0; s < 3; s++) {
                        double aa = RND.nextDouble() * Math.PI * 2;
                        spawn(ParticleTypes.ENCHANT,
                                ex + Math.cos(aa) * 0.9, ey + h * 0.5 + (RND.nextDouble() - 0.5) * 0.8, ez + Math.sin(aa) * 0.9,
                                -Math.cos(aa) * 0.3, 0.1, -Math.sin(aa) * 0.3);
                    }
                }
                case 12 -> {
                    for (int s = 0; s < 3; s++) {
                        spawn(ParticleTypes.FLAME, ex, ey + h, ez,
                                (RND.nextDouble() - 0.5) * 0.1, 0.25 + RND.nextDouble() * 0.15, (RND.nextDouble() - 0.5) * 0.1);
                    }
                    spawn(ParticleTypes.LAVA, ex, ey + h, ez, 0, 0, 0);
                }
                default -> { }
            }
        }
    }
}
