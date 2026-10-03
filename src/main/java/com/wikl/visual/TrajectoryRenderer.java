package com.wikl.visual;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class TrajectoryRenderer {
    private record Params(double speed, double gravity, double drag, double water) {}
    private record Path(List<Vec3d> points, Vec3d landing, Entity hitEntity, double ticks) {}

    // purple used to outline an entity that your projectile would hit
    private static final int PR = 146, PG = 43, PB = 255;

    // values for the HUD text (filled during world rendering)
    private static double ownTicks = -1;
    private static String ownHitName = null;
    private static double enemyTicks = -1;

    private TrajectoryRenderer() {}

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(TrajectoryRenderer::render);
    }

    private static Params paramsFor(Item item) {
        if (item == Items.ENDER_PEARL || item == Items.SNOWBALL || item == Items.EGG) return new Params(1.5, 0.03, 0.99, 0.8);
        if (item == Items.EXPERIENCE_BOTTLE) return new Params(0.7, 0.07, 0.99, 0.8);
        if (item == Items.SPLASH_POTION || item == Items.LINGERING_POTION) return new Params(0.5, 0.05, 0.99, 0.8);
        if (item == Items.WIND_CHARGE) return new Params(1.5, 0.0, 1.0, 1.0);
        return null;
    }

    /** Throwables, drawn bow and loaded crossbow in either hand. */
    private static Params heldParams(ClientPlayerEntity p) {
        ItemStack[] hands = {p.getMainHandStack(), p.getOffHandStack()};
        for (ItemStack st : hands) {
            Params prm = paramsFor(st.getItem());
            if (prm != null) return prm;
            if (st.isOf(Items.BOW) && p.isUsingItem() && p.getActiveItem().isOf(Items.BOW)) {
                float pull = BowItem.getPullProgress(p.getItemUseTime());
                if (pull > 0.1f) return new Params(pull * 3.0, 0.05, 0.99, 0.6);
            }
            if (st.isOf(Items.CROSSBOW) && CrossbowItem.isCharged(st)) {
                return new Params(3.15, 0.05, 0.99, 0.6);
            }
        }
        return null;
    }

    private static Path simulate(MinecraftClient mc, Entity ignore, Vec3d start, Vec3d startVel,
                                 double gravity, double drag, double waterDrag, boolean checkEntities) {
        List<Vec3d> pts = new ArrayList<>();
        Vec3d pos = start;
        Vec3d vel = startVel;
        Vec3d landing = null;
        Entity hitEntity = null;
        double ticks = -1;
        pts.add(pos);
        for (int i = 0; i < 300; i++) {
            Vec3d next = pos.add(vel);

            BlockHitResult bh = mc.world.raycast(new RaycastContext(pos, next,
                    RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, ignore));
            Vec3d blockPos = bh.getType() != HitResult.Type.MISS ? bh.getPos() : null;

            Entity hitE = null;
            Vec3d entPos = null;
            double best = Double.MAX_VALUE;
            if (checkEntities) {
                Box sweep = new Box(pos, next).expand(1.0);
                List<Entity> candidates = mc.world.getOtherEntities(ignore, sweep,
                        ent -> ent instanceof LivingEntity && ent.isAlive() && !ent.isSpectator());
                for (Entity e : candidates) {
                    Optional<Vec3d> r = e.getBoundingBox().expand(0.3).raycast(pos, next);
                    if (r.isPresent()) {
                        double d = r.get().squaredDistanceTo(pos);
                        if (d < best) { best = d; hitE = e; entPos = r.get(); }
                    }
                }
            }

            Vec3d end = null;
            if (entPos != null && (blockPos == null || best <= blockPos.squaredDistanceTo(pos))) {
                end = entPos;
            } else {
                hitE = null;
                if (blockPos != null) end = blockPos;
            }

            if (end != null) {
                landing = end;
                pts.add(end);
                double seg = pos.distanceTo(next);
                double frac = seg > 1.0E-9 ? pos.distanceTo(end) / seg : 0;
                ticks = i + frac;
                hitEntity = hitE;
                break;
            }

            pos = next;
            pts.add(pos);
            boolean water = mc.world.getFluidState(BlockPos.ofFloored(pos)).isIn(FluidTags.WATER);
            vel = vel.multiply(water ? waterDrag : drag).subtract(0, gravity, 0);
            if (pos.y < mc.world.getBottomY() - 10) break;
        }
        return new Path(pts, landing, hitEntity, ticks);
    }

    private static void render(WorldRenderContext ctx) {
        ownTicks = -1;
        ownHitName = null;
        enemyTicks = -1;

        if (!WiklSettings.trajectory && !WiklSettings.enemyTrajectory) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.world == null || ctx.consumers() == null || ctx.matrixStack() == null) return;

        Vec3d cam = ctx.camera().getPos();
        MatrixStack.Entry entry = ctx.matrixStack().peek();
        VertexConsumer vc = ctx.consumers().getBuffer(RenderLayer.getLines());

        // ----- your own throwable / bow preview -----
        if (WiklSettings.trajectory) {
            Params prm = heldParams(p);
            if (prm != null) {
                Vec3d eye = ctx.camera().isThirdPerson() ? p.getEyePos() : cam;
                Vec3d pos = new Vec3d(eye.x, eye.y - 0.1, eye.z);
                double yaw = Math.toRadians(p.getYaw());
                double pitch = Math.toRadians(p.getPitch());
                Vec3d dir = new Vec3d(-Math.sin(yaw) * Math.cos(pitch), -Math.sin(pitch), Math.cos(yaw) * Math.cos(pitch));
                Vec3d pv = p.getVelocity();
                Vec3d vel = dir.multiply(prm.speed()).add(pv.x, p.isOnGround() ? 0 : pv.y, pv.z);

                Path path = simulate(mc, p, pos, vel, prm.gravity(), prm.drag(), prm.water(), true);

                boolean hitsEntity = path.hitEntity() != null;
                int accent = WiklSettings.accent();
                int r = hitsEntity ? PR : (accent >> 16) & 0xFF;
                int g = hitsEntity ? PG : (accent >> 8) & 0xFF;
                int b = hitsEntity ? PB : accent & 0xFF;
                draw(vc, entry, cam, path, r, g, b);

                if (hitsEntity) {
                    drawBox(vc, entry, path.hitEntity().getBoundingBox().expand(0.05), cam, PR, PG, PB);
                    ownHitName = path.hitEntity().getName().getString();
                }
                ownTicks = path.ticks();
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

                Path path = simulate(mc, e, e.getPos(), v, gravity, drag, water, false);
                draw(vc, entry, cam, path, 255, 70, 70);
                if (path.ticks() >= 0 && (enemyTicks < 0 || path.ticks() < enemyTicks)) enemyTicks = path.ticks();
            }
        }
    }

    public static void renderHud(DrawContext ctx, MinecraftClient mc) {
        int cx = ctx.getScaledWindowWidth() / 2 + 14;
        int cy = ctx.getScaledWindowHeight() / 2;
        if (WiklSettings.trajectory && ownTicks >= 0) {
            double sec = ownTicks / 20.0;
            int ms = (int) Math.round(sec * 1000.0);
            String txt = ownHitName != null
                    ? String.format("Попадание в %s: %.2f с (%d мс)", ownHitName, sec, ms)
                    : String.format("Приземлится через %.2f с (%d мс)", sec, ms);
            ctx.drawText(mc.textRenderer, txt, cx, cy - 24,
                    ownHitName != null ? 0xFFB06CFF : 0xFFFFFFFF, true);
        }
        if (WiklSettings.enemyTrajectory && enemyTicks >= 0) {
            double sec = enemyTicks / 20.0;
            int ms = (int) Math.round(sec * 1000.0);
            ctx.drawText(mc.textRenderer, String.format("Снаряд врага: %.2f с (%d мс)", sec, ms),
                    cx, cy - 13, 0xFFFF5555, true);
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

    private static void drawBox(VertexConsumer vc, MatrixStack.Entry e, Box worldBox, Vec3d cam, int r, int g, int b) {
        Box box = worldBox.offset(-cam.x, -cam.y, -cam.z);
        double x1 = box.minX, y1 = box.minY, z1 = box.minZ, x2 = box.maxX, y2 = box.maxY, z2 = box.maxZ;
        Vec3d[] bottom = {new Vec3d(x1, y1, z1), new Vec3d(x2, y1, z1), new Vec3d(x2, y1, z2), new Vec3d(x1, y1, z2)};
        Vec3d[] top = {new Vec3d(x1, y2, z1), new Vec3d(x2, y2, z1), new Vec3d(x2, y2, z2), new Vec3d(x1, y2, z2)};
        for (int i = 0; i < 4; i++) {
            int j = (i + 1) % 4;
            line(vc, e, bottom[i], bottom[j], r, g, b);
            line(vc, e, top[i], top[j], r, g, b);
            line(vc, e, bottom[i], top[i], r, g, b);
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
