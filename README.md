# wikl visual — Minecraft 1.21.4 (Fabric)

Client-side visual menu. Open with **Right Shift**, close with **Esc**.
The "wikl visual" logo is shown in the top-left corner of the screen.

Tabs: HUD (watermark, FPS, coordinates, direction), Visual (menu animation, accent pulse), Theme (accent color).

## Build
- Install JDK 21.
- Run `gradle build` in this folder.
- The mod JAR is created in `build/libs/` (use `wikl-visual-1.0.0.jar`, not the `-sources` one).
- Put it into `.minecraft/mods` together with Fabric Loader and Fabric API for 1.21.4.
