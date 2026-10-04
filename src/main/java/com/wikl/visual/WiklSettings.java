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
    public static boolean keystrokes = true;
    public static boolean effectsHud = true;
    public static boolean damageNumbers = true;
    public static boolean hitAnim = true;
    public static boolean noBadEffects = false;
    public static boolean fullbright = false;
    public static boolean smoothGame = false;
    public static boolean customCrosshair = false;
    public static boolean fastPlace = false;
    public static boolean armorHud = true;
    public static boolean aura = true;
    public static boolean shieldStatus = true;
    public static boolean debug = false;
    public static int auraStyle = 0;
    public static boolean fastPlaceServers = false;
    public static int crossStyle = 0;
    public static int crossColor = 0;
    public static int crossSizeIdx = 3;
    public static int crossGapIdx = 2;
    public static int crossThickIdx = 0;
    public static boolean crossOutline = true;
    public static boolean crossDot = false;
    public static boolean boostApplied = false;
    public static int origMaxFps = 120;
    public static boolean origVsync = true;
    public static boolean origShadows = true;
    public static int hitAnimIndex = 0;
    public static int espStyle = 2;
    public static int lastTab = 0;
    public static int accentIndex = 0;

    public static final String[] HIT_ANIM_NAMES = {"Крит", "Звёзды", "Огонь", "Сердца", "Тотем", "Кольцо", "Спираль", "Фейерверк", "Души", "Снег", "Молния", "Магия", "Вулкан", "Взрыв"};
    public static final String[] AURA_NAMES = {"Искры", "Огонь", "Сердца", "Снег", "Души", "Звёзды", "Магия", "Дым"};
    public static final String[] ESP_NAMES = {"Рамка", "Кольцо", "Рамка + кольцо"};
    public static final double[] DEF_HUD_X = {0.53, 0.01, 0.86, 0.90};
    public static final double[] DEF_HUD_Y = {0.56, 0.42, 0.20, 0.62};
    public static final double[] HUD_X = {0.53, 0.01, 0.86, 0.90};
    public static final double[] HUD_Y = {0.56, 0.42, 0.20, 0.62};

    public static final String[] ACCENT_NAMES = {"Фиолетовый", "Синий", "Розовый", "Зелёный"};
    public static final int[] ACCENTS = {0xFF922BFF, 0xFF2B8CFF, 0xFFFF2BA6, 0xFF2BFF8A};

    public static final String[] ASPECT_NAMES = {
            "4:3 (1024x768)", "5:4 (1280x1024)", "3:2 (1440x960)", "16:10 (1280x800)",
            "16:9 (1920x1080)", "21:9 (2560x1080)", "1:1 (1080x1080)"
    };
    public static final float[] ASPECTS = {4f / 3f, 5f / 4f, 3f / 2f, 16f / 10f, 16f / 9f, 21f / 9f, 1f};

    public static final String[] CROSS_NAMES = {"Плюс", "Точка", "Круг", "Крест", "Квадрат", "Т-образный", "Уголки", "Круг с точкой", "Ромб"};
    public static final String[] CROSS_COLOR_NAMES = {"Цвет темы", "Белый", "Красный", "Зелёный", "Жёлтый", "Голубой", "Розовый"};
    public static final int[] CROSS_COLORS = {0, 0xFFFFFFFF, 0xFFFF3B3B, 0xFF3BFF6A, 0xFFFFE23B, 0xFF3BE6FF, 0xFFFF6AD5};
    public static final int[] CROSS_SIZES = {3, 4, 5, 6, 8, 10, 12};
    public static final int[] CROSS_GAPS = {0, 1, 2, 3, 4, 6};
    public static final int[] CROSS_THICKS = {1, 2, 3};

    public static int wrap(int v, int d, int n) { return ((v + d) % n + n) % n; }
    public static int crossColorValue() { return crossColor == 0 ? accent() : CROSS_COLORS[crossColor]; }

    public static int accent() { return ACCENTS[accentIndex]; }
    public static float targetAspect() { return ASPECTS[aspectIndex]; }
    public static void nextHitAnim() { hitAnimIndex = (hitAnimIndex + 1) % HIT_ANIM_NAMES.length; }
    public static void nextAura() { auraStyle = (auraStyle + 1) % AURA_NAMES.length; }
    public static void nextEsp() { espStyle = (espStyle + 1) % ESP_NAMES.length; }
    public static void nextAspect() { aspectIndex = (aspectIndex + 1) % ASPECTS.length; }

    private WiklSettings() {}
}
