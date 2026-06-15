package dev.aurora.client.module.impl.performance;

import dev.aurora.client.module.Category;
import dev.aurora.client.module.Module;
import dev.aurora.client.setting.BooleanSetting;
import dev.aurora.client.setting.NumberSetting;

/**
 * Reduces chunk-rendering cost.
 * <p>
 * Provides a clamped render-distance override and a toggle to skip rebuilding
 * chunks that are outside the configured distance. Read by
 * {@code MixinWorldRenderer}. Values are advisory and never modify world state.
 */
public final class ChunkOptimizer extends Module {

    private static ChunkOptimizer active;

    private final BooleanSetting overrideRenderDistance =
            new BooleanSetting("Override Distance", "Force a render distance", false);
    private final NumberSetting renderDistance =
            new NumberSetting("Render Distance", "Chunks", 6, 2, 16, 1)
                    .visibleWhen(() -> overrideRenderDistance.get());
    private final BooleanSetting skipFarRebuilds =
            new BooleanSetting("Skip Far Rebuilds", "Defer distant chunk rebuilds", true);

    public ChunkOptimizer() {
        super("Chunk Optimizer", "Optimises chunk rendering and rebuilds", Category.PERFORMANCE);
        addSettings(overrideRenderDistance, renderDistance, skipFarRebuilds);
    }

    @Override protected void onEnable() { active = this; }
    @Override protected void onDisable() { if (active == this) active = null; }

    public static ChunkOptimizer getActive() { return active; }
    public boolean isOverride() { return overrideRenderDistance.get(); }
    public int getRenderDistance() { return renderDistance.getInt(); }
    public boolean isSkipFarRebuilds() { return skipFarRebuilds.get(); }
}
