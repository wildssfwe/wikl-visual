package com.wikl.visual;

public final class WiklSettings {
    public static boolean watermark = true;
    public static boolean fps = false;
    public static boolean coords = false;
    public static boolean direction = false;
    public static boolean animations = true;
    public static boolean pulse = true;
    public static int accentIndex = 0;

    public static final String[] ACCENT_NAMES = {"Purple", "Blue", "Pink", "Green"};
    public static final int[] ACCENTS = {0xFF922BFF, 0xFF2B8CFF, 0xFFFF2BA6, 0xFF2BFF8A};

    public static int accent() { return ACCENTS[accentIndex]; }

    private WiklSettings() {}
}
