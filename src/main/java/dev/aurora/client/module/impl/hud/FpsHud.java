package dev.aurora.client.module.impl.hud;

import dev.aurora.client.Aurora;
import dev.aurora.client.render.Render2D;

/** Displays the current FPS. */
public final class FpsHud extends FpsHudBase {
    public FpsHud() { super("FPS HUD", "Shows frames per second", 4, 4); }
    @Override protected String text() {
        return Aurora.INSTANCE.getPerformanceMonitor().getFps() + " FPS";
    }
}
