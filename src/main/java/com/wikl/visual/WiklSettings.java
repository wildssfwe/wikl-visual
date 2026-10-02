package com.wikl.visual;

public final class WiklSettings {
    public static boolean watermark = true;
    public static boolean fps = false;
    public static boolean coords = false;
    public static boolean direction = false;
    public static boolean animations = true;
    public static boolean pulse = true;
    public static boolean trajectory = true;
    public static boolean enemyTrajectory = true;
    public static boolean target = true;
    public static boolean invMove = false;
    public static boolean aspectEnabled = false;
    public static int aspectIndex = 0;
    public static int accentIndex = 0;

    public static final String[] ACCENT_NAMES = {"Purple", "Blue", "Pink", "Green"};
    public static final int[] ACCENTS = {0xFF922BFF, 0xFF2B8CFF, 0xFFFF2BA6, 0xFF2BFF8A};

    public static final String[] ASPECT_NAMES = {
            "4:3 (1024x768)", "5:4 (1280x1024)", "3:2 (1440x960)", "16:10 (1280x800)",
            "16:9 (1920x1080)", "21:9 (2560x1080)", "1:1 (1080x1080)"
    };
    public static final float[] ASPECTS = {4f / 3f, 5f / 4f, 3f / 2f, 16f / 10f, 16f / 9f, 21f / 9f, 1f};

    public static int accent() { return ACCENTS[accentIndex]; }
    public static float targetAspect() { return ASPECTS[aspectIndex]; }
    public static void nextAspect() { aspectIndex = (aspectIndex + 1) % ASPECTS.length; }

    private WiklSettings() {}
}
