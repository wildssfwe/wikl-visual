package com.wikl.visual;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.List;

public final class TrajectoryRenderer {
    private record Params(double speed, double gravity, double drag) {}
    private record Path(List<Vec3d> points, Vec3d landing) {}

    private TrajectoryRenderer() {}

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(TrajectoryRenderer::render);
    }

    private static Params paramsFor(Item item) {
        if (item == Items.ENDER_PEARL || item == Items.SNOWBALL || item == Items.EGG) return new Params(1.5, 0.03, 0.99);
        if (item == Items.EXPERIENCE_BOTTLE) return new Params(0.7, 0.07, 0.99);
        if (item == Items.SPLASH_POTION || item == Items.LINGERING_POTION) return new Params(0.5, 0.05, 0.99);
        if (item == Items.WIND_CHARGE) return new Params(1.5, 0.0, 1.0);
        return null;
    }

    private static Path simulate(MinecraftClient mc, Entity ignore, Vec3d start, Vec3d startVel,
                                 double gravity, double drag, double waterDrag) {
        List<Vec3d> pts = new ArrayList<>();
        Vec3d pos = start;
        Vec3d vel = startVel;
        Vec3d landing = null;
        pts.add(pos);
        for (int i = 0; i < 300; i++) {
            Vec3d next = pos.add(vel);
            BlockHitResult hit = mc.world.raycast(new RaycastContext(pos, next,
                    RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, ignore));
            if (hit.getType() != HitResult.Type.MISS) {
                landing = hit.getPos();
                pts.add(landing);
                break;
            }
            pos = next;
            pts.add(pos);
            boolean water = mc.world.getFluidState(BlockPos.ofFloored(pos)).isIn(FluidTags.WATER);
            vel = vel.multiply(water ? waterDrag : drag).subtract(0, gravity, 0);
            if (pos.y < mc.world.getBottomY() - 10) break;
        }
        return new Path(pts, landing);
    }

    private static void render(WorldRenderContext ctx) {
        if (!WiklSettings.trajectory && !WiklSettings.enemyTrajectory) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.world == null || ctx.consumers() == null || ctx.matrixStack() == null) return;

        Vec3d cam = ctx.camera().getPos();
        MatrixStack.Entry entry = ctx.matrixStack().peek();
        VertexConsumer vc = ctx.consumers().getBuffer(RenderLayer.getLines());

        // ----- your own throwable preview -----
        if (WiklSettings.trajectory) {
            Params prm = paramsFor(p.getMainHandStack().getItem());
            if (prm == null) prm = paramsFor(p.getOffHandStack().getItem());
            if (prm != null) {
                Vec3d eye = ctx.camera().isThirdPerson() ? p.getEyePos() : cam;
                Vec3d pos = new Vec3d(eye.x, eye.y - 0.1, eye.z);
                double yaw = Math.toRadians(p.getYaw());
                double pitch = Math.toRadians(p.getPitch());
                Vec3d dir = new Vec3d(-Math.sin(yaw) * Math.cos(pitch), -Math.sin(pitch), Math.cos(yaw) * Math.cos(pitch));
                Vec3d pv = p.getVelocity();
                Vec3d vel = dir.multiply(prm.speed()).add(pv.x, p.isOnGround() ? 0 : pv.y, pv.z);
                Path path = simulate(mc, p, pos, vel, prm.gravity(), prm.drag(), 0.8);
                int accent = WiklSettings.accent();
                draw(vc, entry, cam, path, (accent >> 16) & 0xFF, (accent >> 8) & 0xFF, accent & 0xFF);
            }
        }

        // ----- projectiles thrown or shot by others -----
        if (WiklSettings.enemyTrajectory) {
            for (Entity e : mc.world.getEntities()) {
                if (!(e instanceof ProjectileEntity pe)) continue;
                if (pe.getOwner() == p) continue;
                if (e.squaredDistanceTo(p) > 64 * 64) continue;
                Vec3d v = e.getVelocity();
                if (v.lengthSquared() < 1.0E-3) continue;

                double gravity, drag = 0.99, water = 0.8;
                if (e instanceof PersistentProjectileEntity) {
                    gravity = 0.05;
                    water = 0.6;
                } else if (e instanceof ThrownItemEntity tie) {
                    Params pp = paramsFor(tie.getStack().getItem());
                    gravity = pp != null ? pp.gravity() : 0.03;
                    drag = pp != null ? pp.drag() : 0.99;
                } else {
                    continue;
                }
                if (e.hasNoGravity()) { gravity = 0.0; drag = 1.0; }

                Path path = simulate(mc, e, e.getPos(), v, gravity, drag, water);
                draw(vc, entry, cam, path, 255, 70, 70);
            }
        }
    }

    private static void draw(VertexConsumer vc, MatrixStack.Entry entry, Vec3d cam, Path path, int r, int g, int b) {
        List<Vec3d> pts = path.points();
        for (int i = 0; i + 1 < pts.size(); i++) {
            line(vc, entry, pts.get(i).subtract(cam), pts.get(i + 1).subtract(cam), r, g, b);
        }
        Vec3d landing = path.landing();
        if (landing != null) {
            Vec3d c = landing.subtract(cam);
            double s = 0.25;
            for (int dx = -1; dx <= 1; dx += 2) {
                for (int dz = -1; dz <= 1; dz += 2) {
                    line(vc, entry, c.add(dx * s, -s, dz * s), c.add(dx * s, s, dz * s), r, g, b);
                }
            }
            for (int dy = -1; dy <= 1; dy += 2) {
                line(vc, entry, c.add(-s, dy * s, -s), c.add(s, dy * s, -s), r, g, b);
                line(vc, entry, c.add(s, dy * s, -s), c.add(s, dy * s, s), r, g, b);
                line(vc, entry, c.add(s, dy * s, s), c.add(-s, dy * s, s), r, g, b);
                line(vc, entry, c.add(-s, dy * s, s), c.add(-s, dy * s, -s), r, g, b);
            }
        }
    }

    private static void line(VertexConsumer vc, MatrixStack.Entry e, Vec3d a, Vec3d b, int r, int g, int bl) {
        Vec3d d = b.subtract(a);
        double len = d.length();
        if (len < 1.0E-6) return;
        d = d.multiply(1.0 / len);
        vc.vertex(e, (float) a.x, (float) a.y, (float) a.z).color(r, g, bl, 255).normal(e, (float) d.x, (float) d.y, (float) d.z);
        vc.vertex(e, (float) b.x, (float) b.y, (float) b.z).color(r, g, bl, 255).normal(e, (float) d.x, (float) d.y, (float) d.z);
    }
}
