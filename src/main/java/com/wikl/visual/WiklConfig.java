package com.wikl.visual;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class WiklConfig {
    private WiklConfig() {}

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("wikl_visual.properties");
    }

    private static boolean b(Properties p, String k, boolean def) {
        String v = p.getProperty(k);
        return v == null ? def : Boolean.parseBoolean(v);
    }

    private static int i(Properties p, String k, int def, int max) {
        try {
            int v = Integer.parseInt(p.getProperty(k, String.valueOf(def)));
            return Math.max(0, Math.min(max, v));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static double d(Properties p, String k, double def) {
        try {
            double v = Double.parseDouble(p.getProperty(k, String.valueOf(def)));
            return Math.max(0.0, Math.min(1.0, v));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static void load() {
        Path f = file();
        if (!Files.exists(f)) return;
        Properties p = new Properties();
        try (Reader r = Files.newBufferedReader(f, StandardCharsets.UTF_8)) {
            p.load(r);
        } catch (IOException e) {
            return;
        }
        WiklSettings.watermark = b(p, "watermark", WiklSettings.watermark);
        WiklSettings.fps = b(p, "fps", WiklSettings.fps);
        WiklSettings.coords = b(p, "coords", WiklSettings.coords);
        WiklSettings.direction = b(p, "direction", WiklSettings.direction);
        WiklSettings.animations = b(p, "animations", WiklSettings.animations);
        WiklSettings.pulse = b(p, "pulse", WiklSettings.pulse);
        WiklSettings.trajectory = b(p, "trajectory", WiklSettings.trajectory);
        WiklSettings.enemyTrajectory = b(p, "enemyTrajectory", WiklSettings.enemyTrajectory);
        WiklSettings.target = b(p, "target", WiklSettings.target);
        WiklSettings.invMove = b(p, "invMove", WiklSettings.invMove);
        WiklSettings.aspectEnabled = b(p, "aspectEnabled", WiklSettings.aspectEnabled);
        WiklSettings.keystrokes = b(p, "keystrokes", WiklSettings.keystrokes);
        WiklSettings.effectsHud = b(p, "effectsHud", WiklSettings.effectsHud);
        WiklSettings.damageNumbers = b(p, "damageNumbers", WiklSettings.damageNumbers);
        WiklSettings.hitAnim = b(p, "hitAnim", WiklSettings.hitAnim);
        WiklSettings.aspectIndex = i(p, "aspectIndex", WiklSettings.aspectIndex, WiklSettings.ASPECTS.length - 1);
        WiklSettings.accentIndex = i(p, "accentIndex", WiklSettings.accentIndex, WiklSettings.ACCENTS.length - 1);
        WiklSettings.hitAnimIndex = i(p, "hitAnimIndex", WiklSettings.hitAnimIndex, WiklSettings.HIT_ANIM_NAMES.length - 1);
        WiklSettings.espStyle = i(p, "espStyle", WiklSettings.espStyle, WiklSettings.ESP_NAMES.length - 1);
        WiklSettings.lastTab = i(p, "lastTab", WiklSettings.lastTab, 4);
        for (int n = 0; n < 3; n++) {
            WiklSettings.HUD_X[n] = d(p, "hudX" + n, WiklSettings.HUD_X[n]);
            WiklSettings.HUD_Y[n] = d(p, "hudY" + n, WiklSettings.HUD_Y[n]);
        }
    }

    public static void save() {
        Properties p = new Properties();
        p.setProperty("watermark", String.valueOf(WiklSettings.watermark));
        p.setProperty("fps", String.valueOf(WiklSettings.fps));
        p.setProperty("coords", String.valueOf(WiklSettings.coords));
        p.setProperty("direction", String.valueOf(WiklSettings.direction));
        p.setProperty("animations", String.valueOf(WiklSettings.animations));
        p.setProperty("pulse", String.valueOf(WiklSettings.pulse));
        p.setProperty("trajectory", String.valueOf(WiklSettings.trajectory));
        p.setProperty("enemyTrajectory", String.valueOf(WiklSettings.enemyTrajectory));
        p.setProperty("target", String.valueOf(WiklSettings.target));
        p.setProperty("invMove", String.valueOf(WiklSettings.invMove));
        p.setProperty("aspectEnabled", String.valueOf(WiklSettings.aspectEnabled));
        p.setProperty("keystrokes", String.valueOf(WiklSettings.keystrokes));
        p.setProperty("effectsHud", String.valueOf(WiklSettings.effectsHud));
        p.setProperty("damageNumbers", String.valueOf(WiklSettings.damageNumbers));
        p.setProperty("hitAnim", String.valueOf(WiklSettings.hitAnim));
        p.setProperty("aspectIndex", String.valueOf(WiklSettings.aspectIndex));
        p.setProperty("accentIndex", String.valueOf(WiklSettings.accentIndex));
        p.setProperty("hitAnimIndex", String.valueOf(WiklSettings.hitAnimIndex));
        p.setProperty("espStyle", String.valueOf(WiklSettings.espStyle));
        p.setProperty("lastTab", String.valueOf(WiklSettings.lastTab));
        for (int n = 0; n < 3; n++) {
            p.setProperty("hudX" + n, String.valueOf(WiklSettings.HUD_X[n]));
            p.setProperty("hudY" + n, String.valueOf(WiklSettings.HUD_Y[n]));
        }
        try (Writer w = Files.newBufferedWriter(file(), StandardCharsets.UTF_8)) {
            p.store(w, "wikl visual settings");
        } catch (IOException ignored) {
        }
    }
}
