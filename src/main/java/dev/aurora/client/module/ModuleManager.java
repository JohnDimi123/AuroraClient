package dev.aurora.client.module;

import dev.aurora.client.event.Listen;
import dev.aurora.client.event.impl.KeyEvent;
import dev.aurora.client.module.impl.bedwars.BedwarsOverlay;
import dev.aurora.client.module.impl.hud.ArmorStatusHud;
import dev.aurora.client.module.impl.hud.CoordinatesHud;
import dev.aurora.client.module.impl.hud.CpsCounter;
import dev.aurora.client.module.impl.hud.FpsHud;
import dev.aurora.client.module.impl.hud.Keystrokes;
import dev.aurora.client.module.impl.hud.PerformanceHud;
import dev.aurora.client.module.impl.hud.PingHud;
import dev.aurora.client.module.impl.hud.PotionStatusHud;
import dev.aurora.client.module.impl.hud.SessionStatsHud;
import dev.aurora.client.module.impl.hud.TargetHud;
import dev.aurora.client.module.impl.performance.AnimationOptimizer;
import dev.aurora.client.module.impl.performance.ChunkOptimizer;
import dev.aurora.client.module.impl.performance.EntityCulling;
import dev.aurora.client.module.impl.performance.FpsBooster;
import dev.aurora.client.module.impl.performance.ParticleOptimizer;
import dev.aurora.client.module.impl.player.ToggleSneak;
import dev.aurora.client.module.impl.player.ToggleSprint;
import dev.aurora.client.module.impl.render.CombatVisuals;
import dev.aurora.client.module.impl.render.CrosshairCustomizer;
import dev.aurora.client.module.impl.render.DamageIndicators;
import dev.aurora.client.module.impl.render.ReachDisplay;
import dev.aurora.client.module.impl.util.AntiCheatAssistant;
import dev.aurora.client.module.impl.util.NetworkDiagnostics;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Central registry that constructs, stores and exposes every {@link Module}.
 * Insertion order is preserved so the GUI lists modules deterministically.
 */
public final class ModuleManager {

    private final Map<Class<? extends Module>, Module> modules = new LinkedHashMap<>();

    public ModuleManager() {
        registerAll();
    }

    private void registerAll() {
        // Performance
        register(new FpsBooster());
        register(new ChunkOptimizer());
        register(new EntityCulling());
        register(new ParticleOptimizer());
        register(new AnimationOptimizer());
        // Render
        register(new CombatVisuals());
        register(new DamageIndicators());
        register(new ReachDisplay());
        register(new CrosshairCustomizer());
        // HUD
        register(new FpsHud());
        register(new CpsCounter());
        register(new Keystrokes());
        register(new PingHud());
        register(new CoordinatesHud());
        register(new ArmorStatusHud());
        register(new PotionStatusHud());
        register(new TargetHud());
        register(new SessionStatsHud());
        register(new PerformanceHud());
        // Player
        register(new ToggleSprint());
        register(new ToggleSneak());
        // BedWars
        register(new BedwarsOverlay());
        // Utility
        register(new AntiCheatAssistant());
        register(new NetworkDiagnostics());
    }

    private void register(Module module) {
        modules.put(module.getClass(), module);
    }

    /** @return the singleton instance of the given module type. */
    @SuppressWarnings("unchecked")
    public <T extends Module> T get(Class<T> type) {
        return (T) modules.get(type);
    }

    public List<Module> getModules() {
        return new ArrayList<>(modules.values());
    }

    /** @return all modules belonging to a category, in registration order. */
    public List<Module> getByCategory(Category category) {
        List<Module> out = new ArrayList<>();
        for (Module m : modules.values()) {
            if (m.getCategory() == category) out.add(m);
        }
        return out;
    }

    /** Toggles modules whose keybind matches the pressed key. */
    @Listen
    public void onKey(KeyEvent event) {
        if (!event.pressed) return;
        for (Module m : modules.values()) {
            if (m.getKeybind().matches(event.key)) m.toggle();
        }
    }
}
