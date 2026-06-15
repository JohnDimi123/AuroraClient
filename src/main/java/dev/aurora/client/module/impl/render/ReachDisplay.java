package dev.aurora.client.module.impl.render;

import dev.aurora.client.module.Category;
import dev.aurora.client.module.Module;
import dev.aurora.client.setting.BooleanSetting;

/**
 * Measures and displays the distance to the entity the player last attacked.
 * Read-only telemetry — it does not extend reach or alter hit registration.
 */
public final class ReachDisplay extends Module {

    private double lastReach;
    private long lastUpdate;

    private final BooleanSetting blocksUnit =
            new BooleanSetting("Blocks Unit", "Show value in blocks", true);

    public ReachDisplay() {
        super("Reach Display", "Shows measured attack distance", Category.RENDER);
        addSettings(blocksUnit);
    }

    /** Records a measured reach distance from an observed attack. */
    public void record(double reach) {
        this.lastReach = reach;
        this.lastUpdate = System.currentTimeMillis();
    }

    public double getLastReach() { return lastReach; }
    public boolean isRecent() { return System.currentTimeMillis() - lastUpdate < 3000; }
}
