package dev.aurora.client.module.impl.performance;

import dev.aurora.client.event.Listen;
import dev.aurora.client.event.impl.TickEvent;
import dev.aurora.client.module.Category;
import dev.aurora.client.module.Module;
import dev.aurora.client.setting.BooleanSetting;
import dev.aurora.client.setting.EnumSetting;
import dev.aurora.client.setting.NumberSetting;

/**
 * Master FPS optimisation controller.
 * <p>
 * Exposes a global {@link PerformanceProfile} that other performance modules
 * read via {@link #getProfile()}. Also implements <em>Dynamic FPS</em> (lowers
 * the frame cap when the window is unfocused) and a smart frame limiter that
 * suggests a target FPS. These behaviours are read by the {@code MixinMinecraft}
 * frame-cap hook and the renderer optimisers.
 */
public final class FpsBooster extends Module {

    private static FpsBooster active;

    private final EnumSetting<PerformanceProfile> profile =
            new EnumSetting<>("Profile", "Optimisation aggressiveness", PerformanceProfile.BALANCED);

    private final BooleanSetting dynamicFps =
            new BooleanSetting("Dynamic FPS", "Reduce FPS when window unfocused", true);

    private final NumberSetting unfocusedFps =
            new NumberSetting("Unfocused FPS", "Frame cap when unfocused", 10, 1, 60, 1)
                    .visibleWhen(() -> dynamicFps.get());

    private final BooleanSetting smartGc =
            new BooleanSetting("Smart GC Hints", "Request GC on world unload", true);

    private final BooleanSetting reduceMemoryAllocations =
            new BooleanSetting("Lean Buffers", "Reuse render buffers where safe", true);

    public FpsBooster() {
        super("FPS Booster", "Master performance optimisation controller", Category.PERFORMANCE);
        addSettings(profile, dynamicFps, unfocusedFps, smartGc, reduceMemoryAllocations);
    }

    @Override protected void onEnable() { active = this; }
    @Override protected void onDisable() { if (active == this) active = null; }

    /** @return the FpsBooster instance if enabled, else null. */
    public static FpsBooster getActive() { return active; }

    public PerformanceProfile getProfile() { return profile.get(); }
    public boolean isDynamicFps() { return dynamicFps.get(); }
    public int getUnfocusedFps() { return unfocusedFps.getInt(); }
    public boolean isLeanBuffers() { return reduceMemoryAllocations.get(); }

    @Listen
    public void onTick(TickEvent event) {
        // Hook point for future adaptive logic (e.g. auto-lower profile under load).
    }
}
