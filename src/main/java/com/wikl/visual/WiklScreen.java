package com.wikl.visual;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class WiklScreen extends Screen {
    private record Module(String name, Supplier<String> desc, BooleanSupplier get, Consumer<Boolean> set,
                          Runnable rightClick, Runnable action) {
        Module(String name, String desc, BooleanSupplier get, Consumer<Boolean> set) {
            this(name, () -> desc, get, set, null, null);
        }

        Module(String name, Supplier<String> desc, BooleanSupplier get, Consumer<Boolean> set, Runnable rightClick) {
            this(name, desc, get, set, rightClick, null);
        }
    }

    private static final String[] TABS = {"HUD", "Бой", "Визуал", "Прицел", "Движение", "Тема", "Конфиги"};
    private static final int THEME_TAB = 5;
    private static final int CONFIG_TAB = 6;
    private static final int SIDEBAR = 124;
    private static final int CARD_H = 40;
    private static final int CARD_GAP = 8;

    private final long openTime = Util.getMeasuringTimeMs();
    private long tabTime = Util.getMeasuringTimeMs();
    private final Map<String, Float> toggleAnim = new HashMap<>();
    private int tab;
    private float scroll, scrollTarget, indRel = -1;
    private int panelW = 500, panelH = 330, left, top;
    private List<String> configNames = new ArrayList<>();
    private String statusText = "";
    private long statusUntil;
    private String pendingDelete;
    private long pendingDeleteUntil;

    private final List<Module> hudModules = List.of(
            new Module("Водяной знак", "Показывать логотип wikl visual на экране", () -> WiklSettings.watermark, v -> WiklSettings.watermark = v),
            new Module("Счётчик FPS", "Показывать текущий FPS", () -> WiklSettings.fps, v -> WiklSettings.fps = v),
            new Module("Координаты", "Показывать ваши координаты XYZ", () -> WiklSettings.coords, v -> WiklSettings.coords = v),
            new Module("Направление", "Показывать, куда вы смотрите", () -> WiklSettings.direction, v -> WiklSettings.direction = v),
            new Module("Клавиши и CPS", "Показывать нажатые WASD, пробел и клики мыши", () -> WiklSettings.keystrokes, v -> WiklSettings.keystrokes = v),
            new Module("Эффекты", "Список эффектов с таймерами", () -> WiklSettings.effectsHud, v -> WiklSettings.effectsHud = v),
            new Module("Броня (Armor HUD)", "Показывать надетую броню и её прочность", () -> WiklSettings.armorHud, v -> WiklSettings.armorHud = v),
            new Module("Редактор HUD", () -> "Нажмите, чтобы переместить клавиши, эффекты, броню и панель HP",
                    () -> false, v -> {}, null, this::openEditor)
    );
    private final List<Module> combatModules = List.of(
            new Module("Таргет ESP", () -> "ПКМ - стиль: " + WiklSettings.ESP_NAMES[WiklSettings.espStyle],
                    () -> WiklSettings.target, v -> WiklSettings.target = v, WiklSettings::nextEsp),
            new Module("Частицы на цели", () -> "ПКМ - стиль: " + WiklSettings.AURA_NAMES[WiklSettings.auraStyle] + " (идут за целью)",
                    () -> WiklSettings.aura, v -> WiklSettings.aura = v, WiklSettings::nextAura),
            new Module("Статус щита врага", "Зелёный - щит готов, красный - сломан топором", () -> WiklSettings.shieldStatus, v -> WiklSettings.shieldStatus = v),
            new Module("Цифры урона", "Всплывающие цифры урона над целью", () -> WiklSettings.damageNumbers, v -> WiklSettings.damageNumbers = v),
            new Module("Анимация удара", () -> "ПКМ - сменить: " + WiklSettings.HIT_ANIM_NAMES[WiklSettings.hitAnimIndex],
                    () -> WiklSettings.hitAnim, v -> WiklSettings.hitAnim = v, WiklSettings::nextHitAnim),
            new Module("Траектория", "Полёт перла, ветра, лука и время до падения", () -> WiklSettings.trajectory, v -> WiklSettings.trajectory = v),
            new Module("Траектория врага", "Куда летят снаряды других игроков", () -> WiklSettings.enemyTrajectory, v -> WiklSettings.enemyTrajectory = v)
    );
    private final List<Module> visualModules = List.of(
            new Module("Соотношение сторон", () -> "ПКМ - сменить: " + WiklSettings.ASPECT_NAMES[WiklSettings.aspectIndex],
                    () -> WiklSettings.aspectEnabled, v -> WiklSettings.aspectEnabled = v, WiklSettings::nextAspect),
            new Module("Полная яркость", "В шахте и ночью всегда светло", () -> WiklSettings.fullbright, v -> WiklSettings.fullbright = v),
            new Module("Без плохих эффектов", "Убирает тьму Вардена, слепоту и тошноту с экрана", () -> WiklSettings.noBadEffects, v -> WiklSettings.noBadEffects = v),
            new Module("Плавная игра (FPS)", "Снимает лимит FPS, выключает V-Sync и тени мобов", () -> WiklSettings.smoothGame, v -> WiklSettings.smoothGame = v),
            new Module("Диагностика", "Сообщения над хотбаром при ударе и сломанном щите", () -> WiklSettings.debug, v -> WiklSettings.debug = v),
            new Module("Анимация меню", "Плавное появление меню", () -> WiklSettings.animations, v -> WiklSettings.animations = v),
            new Module("Пульсация", "Анимированная подсветка заголовка", () -> WiklSettings.pulse, v -> WiklSettings.pulse = v)
    );
    private final List<Module> crosshairModules = List.of(
            new Module("Свой прицел", () -> "ПКМ - стиль: " + WiklSettings.CROSS_NAMES[WiklSettings.crossStyle],
                    () -> WiklSettings.customCrosshair, v -> WiklSettings.customCrosshair = v,
                    () -> WiklSettings.crossStyle = WiklSettings.wrap(WiklSettings.crossStyle, 1, WiklSettings.CROSS_NAMES.length)),
            new Module("Цвет", () -> "ЛКМ - дальше, ПКМ - назад: " + WiklSettings.CROSS_COLOR_NAMES[WiklSettings.crossColor],
                    () -> false, v -> {},
                    () -> WiklSettings.crossColor = WiklSettings.wrap(WiklSettings.crossColor, -1, WiklSettings.CROSS_COLORS.length),
                    () -> WiklSettings.crossColor = WiklSettings.wrap(WiklSettings.crossColor, 1, WiklSettings.CROSS_COLORS.length)),
            new Module("Размер", () -> "ЛКМ - больше, ПКМ - меньше: " + WiklSettings.CROSS_SIZES[WiklSettings.crossSizeIdx],
                    () -> false, v -> {},
                    () -> WiklSettings.crossSizeIdx = WiklSettings.wrap(WiklSettings.crossSizeIdx, -1, WiklSettings.CROSS_SIZES.length),
                    () -> WiklSettings.crossSizeIdx = WiklSettings.wrap(WiklSettings.crossSizeIdx, 1, WiklSettings.CROSS_SIZES.length)),
            new Module("Зазор", () -> "ЛКМ - больше, ПКМ - меньше: " + WiklSettings.CROSS_GAPS[WiklSettings.crossGapIdx],
                    () -> false, v -> {},
                    () -> WiklSettings.crossGapIdx = WiklSettings.wrap(WiklSettings.crossGapIdx, -1, WiklSettings.CROSS_GAPS.length),
                    () -> WiklSettings.crossGapIdx = WiklSettings.wrap(WiklSettings.crossGapIdx, 1, WiklSettings.CROSS_GAPS.length)),
            new Module("Толщина", () -> "ЛКМ - толще, ПКМ - тоньше: " + WiklSettings.CROSS_THICKS[WiklSettings.crossThickIdx],
                    () -> false, v -> {},
                    () -> WiklSettings.crossThickIdx = WiklSettings.wrap(WiklSettings.crossThickIdx, -1, WiklSettings.CROSS_THICKS.length),
                    () -> WiklSettings.crossThickIdx = WiklSettings.wrap(WiklSettings.crossThickIdx, 1, WiklSettings.CROSS_THICKS.length)),
            new Module("Обводка", "Чёрная обводка для лучшей видимости", () -> WiklSettings.crossOutline, v -> WiklSettings.crossOutline = v),
            new Module("Центральная точка", "Маленькая точка в центре прицела", () -> WiklSettings.crossDot, v -> WiklSettings.crossDot = v)
    );
    private final List<Module> movementModules = List.of(
            new Module("Ходьба в меню", "Двигаться при открытом инвентаре или сундуке", () -> WiklSettings.invMove, v -> WiklSettings.invMove = v),
            new Module("Быстрая установка", "Блоки ставятся без задержки (в одиночной игре)", () -> WiklSettings.fastPlace, v -> WiklSettings.fastPlace = v),
            new Module("Быстрая на серверах", "Включает и на серверах. Может привести к бану!", () -> WiklSettings.fastPlaceServers, v -> WiklSettings.fastPlaceServers = v)
    );

    public WiklScreen() {
        super(Text.literal("wikl visual"));
        tab = Math.max(0, Math.min(TABS.length - 1, WiklSettings.lastTab));
        refreshConfigs();
    }

    private void refreshConfigs() {
        configNames = WiklConfig.listProfiles();
    }

    private void status(String text) {
        statusText = text;
        statusUntil = Util.getMeasuringTimeMs() + 3500;
    }

    private void openEditor() {
        if (client != null) client.setScreen(new HudEditScreen(this));
    }

    @Override
    protected void init() {
        panelW = Math.min(500, width - 20);
        panelH = Math.min(330, height - 20);
        left = (width - panelW) / 2;
        top = (height - panelH) / 2;
        refreshConfigs();
    }

    // ---------- helpers ----------
    private static int withAlpha(int argb, float a) {
        int alpha = (int) (((argb >>> 24) & 0xFF) * Math.max(0f, Math.min(1f, a)));
        return (alpha << 24) | (argb & 0xFFFFFF);
    }

    private static int lerp(int c1, int c2, float t) {
        int a = (int) (((c1 >>> 24) & 0xFF) + (((c2 >>> 24) & 0xFF) - ((c1 >>> 24) & 0xFF)) * t);
        int r = (int) (((c1 >> 16) & 0xFF) + (((c2 >> 16) & 0xFF) - ((c1 >> 16) & 0xFF)) * t);
        int g = (int) (((c1 >> 8) & 0xFF) + (((c2 >> 8) & 0xFF) - ((c1 >> 8) & 0xFF)) * t);
        int b = (int) ((c1 & 0xFF) + ((c2 & 0xFF) - (c1 & 0xFF)) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static void rrect(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        ctx.fill(x1 + 2, y1, x2 - 2, y2, color);
        ctx.fill(x1, y1 + 2, x2, y2 - 2, color);
        ctx.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, color);
    }

    private static boolean inside(double mx, double my, int x1, int y1, int x2, int y2) {
        return mx >= x1 && mx < x2 && my >= y1 && my < y2;
    }

    private static double frac(double v) {
        return v - Math.floor(v);
    }

    private float progress() {
        if (!WiklSettings.animations) return 1f;
        float t = Math.min(1f, (Util.getMeasuringTimeMs() - openTime) / 250f);
        return 1f - (1f - t) * (1f - t) * (1f - t);
    }

    private List<Module> currentModules() {
        return switch (tab) {
            case 0 -> hudModules;
            case 1 -> combatModules;
            case 2 -> visualModules;
            case 3 -> crosshairModules;
            default -> movementModules;
        };
    }

    private int cardCount() {
        if (tab == CONFIG_TAB) return 1 + configNames.size();
        return tab == THEME_TAB ? WiklSettings.ACCENTS.length : currentModules().size();
    }

    private int cardsX1() { return left + SIDEBAR + 14; }
    private int cardsX2() { return left + panelW - 20; }
    private int viewTop() { return top + 72; }
    private int viewBottom() { return top + panelH - 26; }
    private int cardY(int i) { return viewTop() + 4 + i * (CARD_H + CARD_GAP) - (int) scroll; }

    private float maxScroll() {
        float content = cardCount() * (CARD_H + CARD_GAP) + 8;
        return Math.max(0f, content - (viewBottom() - viewTop()));
    }

    private void switchTab(int i) {
        tab = i;
        WiklSettings.lastTab = i;
        refreshConfigs();
        scroll = 0;
        scrollTarget = 0;
        tabTime = Util.getMeasuringTimeMs();
    }

    // ---------- rendering ----------
    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // drawn in render()
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        float p = progress();
        int accent = WiklSettings.accent();
        int oy = (int) ((1f - p) * 18);
        int T = top + oy;
        long now = Util.getMeasuringTimeMs();

        // background dim + tint
        ctx.fillGradient(0, 0, width, height, withAlpha(0xFF05050A, 0.72f * p),
                withAlpha(lerp(0xFF05050A, accent, 0.22f), 0.78f * p));

        // floating particles
        for (int i = 0; i < 40; i++) {
            double seed = i * 12.9898;
            double px = frac(Math.sin(seed) * 43758.5453) * width + Math.sin(now * 0.0004 + i) * 10;
            double py = frac(Math.cos(seed) * 24634.6345) * height - now * 0.012 * (0.4 + (i % 5) * 0.2);
            py = ((py % height) + height) % height;
            int s = (i % 3 == 0) ? 3 : 2;
            ctx.fill((int) px, (int) py, (int) px + s, (int) py + s, withAlpha(accent, 0.30f * p));
        }

        // corner logo
        ctx.getMatrices().push();
        ctx.getMatrices().scale(2f, 2f, 1f);
        ctx.drawText(textRenderer, "wikl", 5, 4, withAlpha(accent, p), true);
        ctx.drawText(textRenderer, "visual", 5 + textRenderer.getWidth("wikl "), 4, withAlpha(0xFFFFFFFF, p), true);
        ctx.getMatrices().pop();

        // glow + panel
        for (int k = 4; k >= 1; k--) {
            rrect(ctx, left - k * 2, T - k * 2, left + panelW + k * 2, T + panelH + k * 2, withAlpha(accent, 0.05f * p));
        }
        rrect(ctx, left, T, left + panelW, T + panelH, withAlpha(0xFF12121A, p));
        ctx.fillGradient(left + 1, T + 40, left + panelW - 1, T + panelH - 1,
                withAlpha(accent, 0.0f), withAlpha(accent, 0.12f * p));
        ctx.fill(left + 1, T + 40, left + SIDEBAR, T + panelH - 1, withAlpha(0xFF0C0C12, 0.85f * p));
        ctx.fill(left + SIDEBAR, T + 40, left + SIDEBAR + 1, T + panelH - 1, withAlpha(accent, 0.25f * p));

        // header
        ctx.fillGradient(left + 2, T + 1, left + panelW - 2, T + 38,
                withAlpha(accent, 0.32f * p), withAlpha(0xFF0B0B11, p));
        for (int x = 0; x < panelW - 4; x += 3) {
            float wave = WiklSettings.pulse ? (float) (0.5 + 0.5 * Math.sin(x * 0.035 - now * 0.004)) : 1f;
            int c = lerp(withAlpha(accent, 0.30f * p), withAlpha(accent, p), wave);
            ctx.fill(left + 2 + x, T + 38, Math.min(left + panelW - 2, left + 5 + x), T + 40, c);
        }
        ctx.getMatrices().push();
        ctx.getMatrices().scale(1.4f, 1.4f, 1f);
        ctx.drawText(textRenderer, "wikl", (int) ((left + 14) / 1.4f), (int) ((T + 11) / 1.4f), withAlpha(accent, p), true);
        ctx.drawText(textRenderer, "visual", (int) ((left + 14) / 1.4f) + textRenderer.getWidth("wikl "), (int) ((T + 11) / 1.4f), withAlpha(0xFFFFFFFF, p), true);
        ctx.getMatrices().pop();
        ctx.drawText(textRenderer, "v1.1.0", left + panelW - 14 - textRenderer.getWidth("v1.1.0"), T + 15, withAlpha(0xFF9A9AAA, p), false);

        // tabs with sliding indicator
        float targetInd = 54 + tab * 36;
        if (indRel < 0) indRel = targetInd;
        indRel += (targetInd - indRel) * Math.min(1f, 0.25f + delta * 0.1f);
        rrect(ctx, left + 10, T + (int) indRel, left + SIDEBAR - 8, T + (int) indRel + 28, withAlpha(accent, 0.24f * p));
        ctx.fill(left + 10, T + (int) indRel + 5, left + 13, T + (int) indRel + 23, withAlpha(accent, p));
        for (int i = 0; i < TABS.length; i++) {
            int ty = T + 54 + i * 36;
            boolean hover = inside(mouseX, mouseY, left + 10, ty, left + SIDEBAR - 8, ty + 28);
            if (hover && i != tab) rrect(ctx, left + 10, ty, left + SIDEBAR - 8, ty + 28, withAlpha(0xFFFFFFFF, 0.07f * p));
            int tc = i == tab ? 0xFFFFFFFF : hover ? 0xFFE0E0F0 : 0xFFA8A8BC;
            ctx.drawText(textRenderer, TABS[i], left + 24, ty + 10, withAlpha(tc, p), false);
        }

        // content title
        ctx.drawText(textRenderer, TABS[tab], cardsX1(), T + 46, withAlpha(0xFFFFFFFF, p), true);
        String sub = switch (tab) {
            case 0 -> "Информация на экране";
            case 1 -> "Помощь в бою";
            case 2 -> "Внешний вид";
            case 3 -> "Свой прицел";
            case 4 -> "Движение";
            case 5 -> "Выберите цвет темы";
            default -> "Сохранённые наборы настроек";
        };
        ctx.drawText(textRenderer, sub, cardsX1(), T + 59, withAlpha(0xFF8A8A99, p), false);

        if (tab == 3) {
            int bx = left + panelW - 78, by = T + 42;
            ctx.fill(bx, by, bx + 52, by + 28, withAlpha(0xFF0A0A10, p));
            ctx.fill(bx, by, bx + 52, by + 1, withAlpha(accent, 0.6f * p));
            ctx.enableScissor(bx, by, bx + 52, by + 28);
            CrosshairRenderer.draw(ctx, bx + 26, by + 14);
            ctx.disableScissor();
        }

        // scroll smoothing
        scrollTarget = Math.max(0f, Math.min(maxScroll(), scrollTarget));
        scroll += (scrollTarget - scroll) * 0.3f;

        // cards
        ctx.enableScissor(cardsX1() - 4, viewTop() + oy, cardsX2() + 4, viewBottom() + oy);
        for (int i = 0; i < cardCount(); i++) {
            float cp = Math.max(0f, Math.min(1f, (now - tabTime - i * 50L) / 220f));
            float ease = 1f - (1f - cp) * (1f - cp) * (1f - cp);
            int slide = (int) ((1f - ease) * 30);
            int y = cardY(i) + oy;
            if (y + CARD_H < viewTop() + oy || y > viewBottom() + oy) continue;
            if (tab == CONFIG_TAB) renderConfigCard(ctx, mouseX, mouseY, i, y, slide, p * ease, accent);
            else if (tab == THEME_TAB) renderThemeCard(ctx, mouseX, mouseY, i, y, slide, p * ease, accent);
            else renderModuleCard(ctx, mouseX, mouseY, delta, i, y, slide, p * ease, accent);
        }
        ctx.disableScissor();

        // scrollbar
        float ms = maxScroll();
        if (ms > 0) {
            int trackTop = viewTop() + oy, trackH = viewBottom() - viewTop();
            int barH = Math.max(18, (int) (trackH * trackH / (trackH + ms)));
            int barY = trackTop + (int) ((trackH - barH) * (scroll / ms));
            ctx.fill(left + panelW - 12, trackTop, left + panelW - 9, trackTop + trackH, withAlpha(0xFFFFFFFF, 0.06f * p));
            ctx.fill(left + panelW - 12, barY, left + panelW - 9, barY + barH, withAlpha(accent, 0.85f * p));
        }

        // footer
        ctx.fill(left + 1, T + panelH - 22, left + panelW - 1, T + panelH - 21, withAlpha(0xFFFFFFFF, 0.05f * p));
        if (now < statusUntil) {
            ctx.drawText(textRenderer, statusText, left + 14, T + panelH - 14, withAlpha(accent, p), false);
        } else {
            ctx.drawText(textRenderer, "ЛКМ - вкл/выкл   ПКМ - настройка   Колесо - прокрутка   Правый Shift / ESC - закрыть",
                    left + 14, T + panelH - 14, withAlpha(0xFF6C6C7C, p), false);
        }
    }

    private void renderModuleCard(DrawContext ctx, int mx, int my, float delta, int i, int y, int slide, float p, int accent) {
        List<Module> mods = currentModules();
        Module m = mods.get(i);
        int x1 = cardsX1() + slide, x2 = cardsX2() + slide;
        boolean hover = inside(mx, my, cardsX1(), cardCardY(i), cardsX2(), cardCardY(i) + CARD_H)
                && my >= viewTop() && my < viewBottom();
        boolean on = m.get().getAsBoolean();

        float a = toggleAnim.getOrDefault(m.name(), on ? 1f : 0f);
        a += ((on ? 1f : 0f) - a) * Math.min(1f, 0.25f + delta * 0.1f);
        toggleAnim.put(m.name(), a);

        if (hover) rrect(ctx, x1 - 1, y - 1, x2 + 1, y + CARD_H + 1, withAlpha(accent, 0.22f * p));
        rrect(ctx, x1, y, x2, y + CARD_H, withAlpha(hover ? 0xFF2A2A38 : 0xFF1F1F2A, p));
        ctx.fill(x1, y + 4, x1 + 2, y + CARD_H - 4, withAlpha(lerp(0xFF444455, accent, a), p));
        ctx.drawText(textRenderer, m.name(), x1 + 14, y + 9, withAlpha(0xFFF0F0F8, p), false);
        ctx.drawText(textRenderer, m.desc().get(), x1 + 14, y + 23, withAlpha(0xFF8A8A99, p), false);

        int sw = 34, sh = 16;
        int sx = x2 - sw - 14, sy = y + (CARD_H - sh) / 2;
        if (m.action() != null) {
            ctx.drawText(textRenderer, ">", x2 - 20, y + 16, withAlpha(accent, p), true);
        } else {
            rrect(ctx, sx, sy, sx + sw, sy + sh, withAlpha(lerp(0xFF3A3A48, accent, a), p));
            int kx = sx + 2 + (int) ((sw - sh) * a);
            rrect(ctx, kx, sy + 2, kx + sh - 4, sy + sh - 2, withAlpha(0xFFFFFFFF, p));
            if (m.rightClick() != null) {
                String chip = "ПКМ";
                ctx.drawText(textRenderer, chip, sx - 8 - textRenderer.getWidth(chip), y + 16, withAlpha(accent, 0.9f * p), false);
            }
        }
    }

    private int cardCardY(int i) { return cardY(i); }

    private int configBtnX(int k) {
        return cardsX2() - 8 - 50 - (2 - k) * 54;
    }

    private void renderConfigCard(DrawContext ctx, int mx, int my, int i, int y, int slide, float p, int accent) {
        int x1 = cardsX1() + slide, x2 = cardsX2() + slide;
        boolean hover = inside(mx, my, cardsX1(), cardY(i), cardsX2(), cardY(i) + CARD_H)
                && my >= viewTop() && my < viewBottom();
        if (hover) rrect(ctx, x1 - 1, y - 1, x2 + 1, y + CARD_H + 1, withAlpha(accent, 0.22f * p));
        rrect(ctx, x1, y, x2, y + CARD_H, withAlpha(hover ? 0xFF2A2A38 : 0xFF1F1F2A, p));
        ctx.fill(x1, y + 4, x1 + 2, y + CARD_H - 4, withAlpha(accent, p));

        if (i == 0) {
            ctx.drawText(textRenderer, "+ Создать конфиг", x1 + 14, y + 9, withAlpha(0xFFF0F0F8, p), false);
            ctx.drawText(textRenderer, "Сохранить текущие настройки под своим именем", x1 + 14, y + 23, withAlpha(0xFF8A8A99, p), false);
            ctx.drawText(textRenderer, ">", x2 - 20, y + 16, withAlpha(accent, p), true);
            return;
        }

        String name = configNames.get(i - 1);
        String shown = name.length() > 16 ? name.substring(0, 16) + ".." : name;
        ctx.drawText(textRenderer, shown, x1 + 14, y + 9, withAlpha(0xFFF0F0F8, p), false);
        ctx.drawText(textRenderer, "Свой конфиг", x1 + 14, y + 23, withAlpha(0xFF8A8A99, p), false);

        long now = Util.getMeasuringTimeMs();
        boolean pend = name.equals(pendingDelete) && now < pendingDeleteUntil;
        String[] labels = {"Загр.", "Сохр.", pend ? "Точно?" : "Удал."};
        int[] colors = {accent, 0xFF4C8CFF, 0xFFFF5555};
        for (int k = 0; k < 3; k++) {
            int bx = configBtnX(k) + slide, by = y + 12;
            boolean bh = inside(mx, my, configBtnX(k), cardY(i) + 12, configBtnX(k) + 50, cardY(i) + 28)
                    && my >= viewTop() && my < viewBottom();
            rrect(ctx, bx, by, bx + 50, by + 16, withAlpha(colors[k], (bh ? 0.55f : 0.28f) * p));
            ctx.drawText(textRenderer, labels[k], bx + (50 - textRenderer.getWidth(labels[k])) / 2, by + 4,
                    withAlpha(0xFFFFFFFF, p), false);
        }
    }

    private void renderThemeCard(DrawContext ctx, int mx, int my, int i, int y, int slide, float p, int accent) {
        int x1 = cardsX1() + slide, x2 = cardsX2() + slide;
        boolean hover = inside(mx, my, cardsX1(), cardY(i), cardsX2(), cardY(i) + CARD_H) && my >= viewTop() && my < viewBottom();
        boolean sel = i == WiklSettings.accentIndex;
        int col = WiklSettings.ACCENTS[i];
        if (hover || sel) rrect(ctx, x1 - 1, y - 1, x2 + 1, y + CARD_H + 1, withAlpha(col, 0.25f * p));
        rrect(ctx, x1, y, x2, y + CARD_H, withAlpha(hover ? 0xFF2A2A38 : 0xFF1F1F2A, p));
        ctx.fill(x1, y + 4, x1 + 2, y + CARD_H - 4, withAlpha(col, p));
        rrect(ctx, x1 + 14, y + 10, x1 + 34, y + 30, withAlpha(col, p));
        ctx.drawText(textRenderer, WiklSettings.ACCENT_NAMES[i], x1 + 44, y + 16, withAlpha(0xFFF0F0F8, p), false);
        if (sel) ctx.drawText(textRenderer, "Выбрано", x2 - 14 - textRenderer.getWidth("Выбрано"), y + 16, withAlpha(col, p), false);
    }

    // ---------- input ----------
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0 || button == 1) {
            for (int i = 0; i < TABS.length; i++) {
                int ty = top + 54 + i * 36;
                if (inside(mx, my, left + 10, ty, left + SIDEBAR - 8, ty + 28)) {
                    if (i != tab) switchTab(i);
                    return true;
                }
            }
            if (my >= viewTop() && my < viewBottom()) {
                if (tab == CONFIG_TAB) {
                    for (int i = 0; i < cardCount(); i++) {
                        int y = cardY(i);
                        if (!inside(mx, my, cardsX1(), y, cardsX2(), y + CARD_H)) continue;
                        if (i == 0) {
                            if (client != null) client.setScreen(new ConfigNameScreen(this));
                            return true;
                        }
                        String name = configNames.get(i - 1);
                        for (int k = 0; k < 3; k++) {
                            int bx = configBtnX(k);
                            if (!inside(mx, my, bx, y + 12, bx + 50, y + 28)) continue;
                            long now = Util.getMeasuringTimeMs();
                            if (k == 0) {
                                status(WiklConfig.loadProfile(name) ? "Конфиг загружен: " + name : "Не удалось загрузить конфиг");
                            } else if (k == 1) {
                                status(WiklConfig.saveProfile(name) ? "Конфиг сохранён: " + name : "Не удалось сохранить конфиг");
                            } else if (name.equals(pendingDelete) && now < pendingDeleteUntil) {
                                WiklConfig.deleteProfile(name);
                                pendingDelete = null;
                                refreshConfigs();
                                scrollTarget = Math.min(scrollTarget, maxScroll());
                                status("Конфиг удалён: " + name);
                            } else {
                                pendingDelete = name;
                                pendingDeleteUntil = now + 3000;
                                status("Нажмите «Удал.» ещё раз для подтверждения");
                            }
                            return true;
                        }
                        return true;
                    }
                    return true;
                }
                if (tab == THEME_TAB) {
                    for (int i = 0; i < WiklSettings.ACCENTS.length; i++) {
                        int y = cardY(i);
                        if (inside(mx, my, cardsX1(), y, cardsX2(), y + CARD_H)) {
                            WiklSettings.accentIndex = i;
                            return true;
                        }
                    }
                } else {
                    List<Module> mods = currentModules();
                    for (int i = 0; i < mods.size(); i++) {
                        int y = cardY(i);
                        if (inside(mx, my, cardsX1(), y, cardsX2(), y + CARD_H)) {
                            Module m = mods.get(i);
                            if (button == 1) {
                                if (m.rightClick() != null) m.rightClick().run();
                            } else if (m.action() != null) {
                                m.action().run();
                            } else {
                                m.set().accept(!m.get().getAsBoolean());
                            }
                            return true;
                        }
                    }
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double horizontal, double vertical) {
        scrollTarget = Math.max(0f, Math.min(maxScroll(), scrollTarget - (float) vertical * 26f));
        return true;
    }

    @Override
    public void removed() {
        WiklConfig.save();
    }

    @Override
    public boolean shouldPause() { return false; }
}
