package com.wikl.visual;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

public final class WiklConfig {
    private WiklConfig() {}

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("wikl_visual.properties");
    }

    private static Path profilesDir() {
        return FabricLoader.getInstance().getConfigDir().resolve("wikl_visual_configs");
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

    private static Properties readFile(Path f) {
        if (!Files.exists(f)) return null;
        Properties p = new Properties();
        try (Reader r = Files.newBufferedReader(f, StandardCharsets.UTF_8)) {
            p.load(r);
        } catch (IOException e) {
            return null;
        }
        return p;
    }

    private static boolean writeFile(Path f, Properties p) {
        try {
            if (f.getParent() != null) Files.createDirectories(f.getParent());
            try (Writer w = Files.newBufferedWriter(f, StandardCharsets.UTF_8)) {
                p.store(w, "wikl visual settings");
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static void apply(Properties p) {
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
        WiklSettings.noBadEffects = b(p, "noBadEffects", WiklSettings.noBadEffects);
        WiklSettings.fullbright = b(p, "fullbright", WiklSettings.fullbright);
        WiklSettings.smoothGame = b(p, "smoothGame", WiklSettings.smoothGame);
        WiklSettings.customCrosshair = b(p, "customCrosshair", WiklSettings.customCrosshair);
        WiklSettings.fastPlace = b(p, "fastPlace", WiklSettings.fastPlace);
        WiklSettings.armorHud = b(p, "armorHud", WiklSettings.armorHud);
        WiklSettings.aura = b(p, "aura", WiklSettings.aura);
        WiklSettings.shieldStatus = b(p, "shieldStatus", WiklSettings.shieldStatus);
        WiklSettings.debug = b(p, "debug", WiklSettings.debug);
        WiklSettings.playerEsp = b(p, "playerEsp", WiklSettings.playerEsp);
        WiklSettings.auraStyle = i(p, "auraStyle", WiklSettings.auraStyle, WiklSettings.AURA_NAMES.length - 1);
        WiklSettings.fastPlaceServers = b(p, "fastPlaceServers", WiklSettings.fastPlaceServers);
        WiklSettings.crossStyle = i(p, "crossStyle", WiklSettings.crossStyle, WiklSettings.CROSS_NAMES.length - 1);
        WiklSettings.crossColor = i(p, "crossColor", WiklSettings.crossColor, WiklSettings.CROSS_COLORS.length - 1);
        WiklSettings.crossSizeIdx = i(p, "crossSizeIdx", WiklSettings.crossSizeIdx, WiklSettings.CROSS_SIZES.length - 1);
        WiklSettings.crossGapIdx = i(p, "crossGapIdx", WiklSettings.crossGapIdx, WiklSettings.CROSS_GAPS.length - 1);
        WiklSettings.crossThickIdx = i(p, "crossThickIdx", WiklSettings.crossThickIdx, WiklSettings.CROSS_THICKS.length - 1);
        WiklSettings.crossOutline = b(p, "crossOutline", WiklSettings.crossOutline);
        WiklSettings.crossDot = b(p, "crossDot", WiklSettings.crossDot);
        WiklSettings.aspectIndex = i(p, "aspectIndex", WiklSettings.aspectIndex, WiklSettings.ASPECTS.length - 1);
        WiklSettings.accentIndex = i(p, "accentIndex", WiklSettings.accentIndex, WiklSettings.ACCENTS.length - 1);
        WiklSettings.hitAnimIndex = i(p, "hitAnimIndex", WiklSettings.hitAnimIndex, WiklSettings.HIT_ANIM_NAMES.length - 1);
        WiklSettings.espStyle = i(p, "espStyle", WiklSettings.espStyle, WiklSettings.ESP_NAMES.length - 1);
        WiklSettings.lastTab = i(p, "lastTab", WiklSettings.lastTab, 6);
        for (int n = 0; n < WiklSettings.HUD_X.length; n++) {
            WiklSettings.HUD_X[n] = d(p, "hudX" + n, WiklSettings.HUD_X[n]);
            WiklSettings.HUD_Y[n] = d(p, "hudY" + n, WiklSettings.HUD_Y[n]);
        }
    }

    private static Properties collect() {
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
        p.setProperty("noBadEffects", String.valueOf(WiklSettings.noBadEffects));
        p.setProperty("fullbright", String.valueOf(WiklSettings.fullbright));
        p.setProperty("smoothGame", String.valueOf(WiklSettings.smoothGame));
        p.setProperty("customCrosshair", String.valueOf(WiklSettings.customCrosshair));
        p.setProperty("fastPlace", String.valueOf(WiklSettings.fastPlace));
        p.setProperty("armorHud", String.valueOf(WiklSettings.armorHud));
        p.setProperty("aura", String.valueOf(WiklSettings.aura));
        p.setProperty("shieldStatus", String.valueOf(WiklSettings.shieldStatus));
        p.setProperty("debug", String.valueOf(WiklSettings.debug));
        p.setProperty("playerEsp", String.valueOf(WiklSettings.playerEsp));
        p.setProperty("auraStyle", String.valueOf(WiklSettings.auraStyle));
        p.setProperty("fastPlaceServers", String.valueOf(WiklSettings.fastPlaceServers));
        p.setProperty("crossStyle", String.valueOf(WiklSettings.crossStyle));
        p.setProperty("crossColor", String.valueOf(WiklSettings.crossColor));
        p.setProperty("crossSizeIdx", String.valueOf(WiklSettings.crossSizeIdx));
        p.setProperty("crossGapIdx", String.valueOf(WiklSettings.crossGapIdx));
        p.setProperty("crossThickIdx", String.valueOf(WiklSettings.crossThickIdx));
        p.setProperty("crossOutline", String.valueOf(WiklSettings.crossOutline));
        p.setProperty("crossDot", String.valueOf(WiklSettings.crossDot));
        p.setProperty("aspectIndex", String.valueOf(WiklSettings.aspectIndex));
        p.setProperty("accentIndex", String.valueOf(WiklSettings.accentIndex));
        p.setProperty("hitAnimIndex", String.valueOf(WiklSettings.hitAnimIndex));
        p.setProperty("espStyle", String.valueOf(WiklSettings.espStyle));
        p.setProperty("lastTab", String.valueOf(WiklSettings.lastTab));
        for (int n = 0; n < WiklSettings.HUD_X.length; n++) {
            p.setProperty("hudX" + n, String.valueOf(WiklSettings.HUD_X[n]));
            p.setProperty("hudY" + n, String.valueOf(WiklSettings.HUD_Y[n]));
        }
        return p;
    }

    // state needed to undo the "smooth game" option changes; stored only in the main file, not in named configs
    private static void applyBoost(Properties p) {
        WiklSettings.boostApplied = b(p, "boostApplied", false);
        WiklSettings.origMaxFps = i(p, "origMaxFps", WiklSettings.origMaxFps, 260);
        WiklSettings.origVsync = b(p, "origVsync", WiklSettings.origVsync);
        WiklSettings.origShadows = b(p, "origShadows", WiklSettings.origShadows);
    }

    private static void collectBoost(Properties p) {
        p.setProperty("boostApplied", String.valueOf(WiklSettings.boostApplied));
        p.setProperty("origMaxFps", String.valueOf(WiklSettings.origMaxFps));
        p.setProperty("origVsync", String.valueOf(WiklSettings.origVsync));
        p.setProperty("origShadows", String.valueOf(WiklSettings.origShadows));
    }

    // ----- autosave of the current settings -----
    public static void load() {
        Properties p = readFile(file());
        if (p != null) {
            apply(p);
            applyBoost(p);
        }
    }

    public static void save() {
        Properties p = collect();
        collectBoost(p);
        writeFile(file(), p);
    }

    // ----- named configs -----
    public static String sanitize(String raw) {
        if (raw == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char ch : raw.toCharArray()) {
            if (Character.isLetterOrDigit(ch) || ch == ' ' || ch == '_' || ch == '-') sb.append(ch);
        }
        String s = sb.toString().trim().replaceAll("\\s+", " ");
        return s.length() > 24 ? s.substring(0, 24).trim() : s;
    }

    private static Path profileFile(String name) {
        return profilesDir().resolve(sanitize(name) + ".properties");
    }

    public static List<String> listProfiles() {
        List<String> names = new ArrayList<>();
        Path dir = profilesDir();
        if (!Files.isDirectory(dir)) return names;
        try (Stream<Path> s = Files.list(dir)) {
            s.forEach(path -> {
                String fn = path.getFileName().toString();
                if (fn.endsWith(".properties")) names.add(fn.substring(0, fn.length() - ".properties".length()));
            });
        } catch (IOException ignored) {
        }
        Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    public static boolean saveProfile(String name) {
        if (sanitize(name).isEmpty()) return false;
        return writeFile(profileFile(name), collect());
    }

    public static boolean loadProfile(String name) {
        Properties p = readFile(profileFile(name));
        if (p == null) return false;
        int tab = WiklSettings.lastTab;
        apply(p);
        WiklSettings.lastTab = tab;
        save();
        return true;
    }

    public static boolean deleteProfile(String name) {
        try {
            return Files.deleteIfExists(profileFile(name));
        } catch (IOException e) {
            return false;
        }
    }
}
