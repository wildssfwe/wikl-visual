package com.wikl.visual;

public final class HudLayout {
    public static final int TARGET = 0, KEYS = 1, EFFECTS = 2;
    public static final String[] NAMES = {"Панель цели (HP)", "Клавиши", "Эффекты"};
    public static final int[] W = {110, 72, 110};
    public static final int[] H = {34, 87, 40};

    private HudLayout() {}

    public static int x(int id, int sw) {
        return Math.max(0, Math.min(Math.max(0, sw - W[id]), (int) (WiklSettings.HUD_X[id] * sw)));
    }

    public static int y(int id, int sh) {
        return Math.max(0, Math.min(Math.max(0, sh - H[id]), (int) (WiklSettings.HUD_Y[id] * sh)));
    }

    public static void set(int id, int px, int py, int sw, int sh) {
        int cx = Math.max(0, Math.min(Math.max(0, sw - W[id]), px));
        int cy = Math.max(0, Math.min(Math.max(0, sh - H[id]), py));
        WiklSettings.HUD_X[id] = cx / (double) Math.max(1, sw);
        WiklSettings.HUD_Y[id] = cy / (double) Math.max(1, sh);
    }

    public static void resetAll() {
        for (int i = 0; i < 3; i++) {
            WiklSettings.HUD_X[i] = WiklSettings.DEF_HUD_X[i];
            WiklSettings.HUD_Y[i] = WiklSettings.DEF_HUD_Y[i];
        }
    }
}
