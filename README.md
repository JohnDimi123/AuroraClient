# Aurora Client

A **legitimate, performance-focused PvP enhancement client** for Minecraft **1.8.9**
(Fabric / Legacy Fabric), built for BedWars, SkyWars, Duels, Bridge and general PvP.

Aurora contains **no hacks, cheats, bypasses, or exploit modules**. Every feature is
either a cosmetic enhancement, a read-only information display, an input
quality-of-life helper, or a client-side performance optimisation. The bundled
"AntiCheat Assistant" is purely **informational** — it surfaces confidence-scored
observations to the local user and never takes action against anyone or transmits data.

---

## 1. Architecture

Aurora uses a service-locator core (`Aurora`) that owns a set of managers, wired
through a custom, allocation-light **event bus**.

```
Aurora (entrypoint + service locator)
├── EventBus ............. LambdaMetafactory-compiled listeners, priority-ordered
├── ThemeManager ........ Light / Dark / Competitive palettes
├── FontManager ......... cached texture-atlas FontRenderers
├── NotificationManager . sliding toast notifications
├── ModuleManager ....... constructs + stores every Module
├── HudManager .......... registers HudElements, persists layout
├── ConfigManager ....... JSON profiles, auto-save, timestamped backups
├── AntiCheatEngine ..... passive, informational heuristics
├── NetworkManager ...... ping / jitter / packet-rate diagnostics
├── PerformanceMonitor .. FPS / frametime / memory / CPU sampling
├── SessionStats ........ kills / deaths / KDR / combos
└── ClickTracker ........ rolling-window CPS
```

### Event flow
Mixins translate vanilla callbacks into Aurora events:

| Mixin | Emits / Does |
|-------|--------------|
| `MixinMinecraft` | `TickEvent`, frame sampling, GUI keybinds, Dynamic FPS |
| `MixinKeyboard` | `KeyEvent` (rising-edge), drives module binds |
| `MixinMouse` | CPS, combo, reach, `AttackEvent` |
| `MixinInGameHud` | `RenderHudEvent` + notification rendering |
| `MixinGameRenderer` | `RenderWorldEvent` |
| `MixinClientPlayNetworkHandler` | `ChatReceiveEvent`, `PacketEvent` (read-only) |
| `MixinParticleManager` | particle culling |
| `MixinWorldRenderer` | entity culling (never culls players) |

### Module model
A `Module` owns a list of `Setting`s and an enabled flag. Toggling on subscribes it
to the event bus and calls `onEnable()`; toggling off reverses both — so **disabled
modules cost nothing at runtime**.

### Design principles
- **SOLID / modular** — each subsystem is independent and dependency-injected via `Aurora.INSTANCE`.
- **Performance-first** — index-based event iteration, compiled invokers, cached fonts, sampled JMX.
- **Fairness-by-construction** — no module modifies movement, combat, or packets in a way that grants advantage.

---

## 2. Folder structure

```
src/main/java/dev/aurora/client/
├── Aurora.java
├── event/            event bus, annotations, impl events
├── setting/          Boolean/Number/Enum/Color/Keybind settings
├── module/           Module base, ModuleManager, Category
│   └── impl/         performance, render, hud, player, bedwars, util
├── render/           Render2D, font/, animation/, notification/
├── gui/
│   ├── clickgui/     ClickGuiScreen + components
│   ├── hudeditor/    HudElement, HudManager, HudEditorScreen
│   └── theme/        Theme, ThemeManager
├── config/           ConfigManager (JSON profiles + backups)
├── anticheat/        AntiCheatEngine, checks, observation model
├── network/          NetworkManager
├── util/             PerformanceMonitor, SessionStats, ClickTracker, Timer
└── mixin/            vanilla hooks
src/main/resources/
├── fabric.mod.json
├── aurora.mixins.json
└── assets/aurora/    icon, fonts
```

---

## 3. Features

**Performance:** FPS Booster (Potato / Performance / Balanced / Max-Quality profiles,
Dynamic FPS, smart GC hints), Chunk Optimizer, Entity Culling, Particle Optimizer,
Animation Optimizer.

**Render:** Combat Visuals (hit colour / particles / combo), Damage Indicators,
Reach Display, Crosshair Customizer.

**HUD:** FPS, Ping, Coordinates, CPS, Keystrokes, Armor Status, Potion Status,
Target HUD, Session Stats, Performance HUD — all editable in the HUD editor.

**Player:** Toggle Sprint, Toggle Sneak (input helpers only).

**BedWars:** Overlay with chat-derived bed status, final-kill counter and match timer.

**Utility:** AntiCheat Assistant (informational), Network Diagnostics.

---

## 4. Build instructions

Requirements: **JDK 8**, internet access for first-run dependency download.

```bash
# from the project root
./gradlew build            # produces build/libs/aurora-client-1.0.0.jar
```

Install: drop the built jar into `.minecraft/mods` alongside **Fabric Loader for 1.8.9**
(via [Legacy Fabric](https://legacyfabric.net)) and the matching Fabric API if required
by your loader version.

Run in dev:
```bash
./gradlew runClient        # launches a dev client with Aurora loaded
```

> Note: 1.8.9 mappings are provided by Legacy Fabric. If method names differ in your
> mappings version, adjust the `@Inject` targets in `src/main/java/.../mixin/`.

---

## 5. Default controls

| Key | Action |
|-----|--------|
| **Right Shift** | Open Click GUI |
| **Right Control** | Open HUD Editor |
| (per-module) | Configurable toggle keybinds in the GUI |

**Click GUI:** left-click a module to toggle, right-click to expand settings,
drag panel headers to move, type in the search box to filter, click the theme
button to cycle Light / Dark / Competitive.

**HUD Editor:** drag to move (snaps to grid + neighbours), scroll to scale,
**G** toggles the grid, **Esc** saves and exits.

---

## 6. Testing instructions

1. **Smoke test** — launch `runClient`; confirm the "Aurora v1.0.0 loaded" toast.
2. **GUI** — press Right Shift; toggle a HUD module (e.g. FPS HUD) and confirm it appears.
3. **HUD editor** — press Right Control; drag the FPS HUD, scroll to scale, press Esc; reopen and confirm the position persisted (written to `.minecraft/aurora/configs/default.json`).
4. **Performance** — enable FPS Booster + Entity Culling; in a populated world confirm distant entities stop rendering and the Performance HUD reflects FPS/frametime/memory/CPU.
5. **CPS / keystrokes** — click in-game and confirm the CPS counter and keystroke display react.
6. **BedWars overlay** — on a BedWars game, confirm the timer starts and bed-break messages update team status.
7. **AntiCheat** — enable the AntiCheat Assistant; it should remain silent in normal play (false-positive guard) and only surface confidence-scored notes for clearly anomalous patterns.
8. **Config** — change settings, restart the client, confirm they reload.

---

## 7. Future expansion

The architecture is built for extension:

- **New module:** subclass `Module` (or `HudModule`), add `Setting`s in the
  constructor, register it in `ModuleManager.registerAll()`. Event subscription,
  config persistence, GUI rendering and keybinds are automatic.
- **New setting type:** subclass `Setting<T>`, implement `toJson` / `fromJson`,
  and add a render/click branch in `ModuleComponent`.
- **New anti-cheat check:** implement `Check`, add it in the
  `AntiCheatEngine` constructor. It immediately participates in scoring and cooldowns.
- **New theme:** add a `Theme` in `ThemeManager`'s constructor.
- **New event:** subclass `Event`, post it from a mixin; any `@Listen` method
  with that parameter type receives it.

---

## 8. License & fair play

MIT licensed. Aurora is intended for fair, rules-compliant competitive play.
It deliberately omits any feature that would provide an unfair advantage. Always
follow the rules of the servers you play on.
