package dev.aurora.client.module.impl.render;

import dev.aurora.client.Aurora;
import dev.aurora.client.event.Listen;
import dev.aurora.client.event.impl.RenderWorldEvent;
import dev.aurora.client.module.Category;
import dev.aurora.client.module.Module;
import dev.aurora.client.setting.BooleanSetting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Floating damage numbers above entities. Indicators are derived from observed
 * health changes (read-only) and fade out over time. No combat interaction.
 */
public final class DamageIndicators extends Module {

    /** A single floating number with a world position and spawn time. */
    public static final class Indicator {
        public final double x, y, z;
        public final float amount;
        public final long spawned = System.currentTimeMillis();
        public Indicator(double x, double y, double z, float amount) {
            this.x = x; this.y = y; this.z = z; this.amount = amount;
        }
        public boolean expired() { return System.currentTimeMillis() - spawned > 1000; }
    }

    private final List<Indicator> indicators = new ArrayList<>();

    private final BooleanSetting showHealing =
            new BooleanSetting("Show Healing", "Display green healing numbers", false);

    public DamageIndicators() {
        super("Damage Indicators", "Floating damage numbers", Category.RENDER);
        addSettings(showHealing);
    }

    public List<Indicator> getIndicators() { return indicators; }
    public boolean isShowHealing() { return showHealing.get(); }

    /** Registers an observed damage event for display. */
    public void spawn(double x, double y, double z, float amount) {
        indicators.add(new Indicator(x, y, z, amount));
    }

    @Listen
    public void onRenderWorld(RenderWorldEvent event) {
        // Render handled by RenderWorldEvent consumer; here we just prune.
        Iterator<Indicator> it = indicators.iterator();
        while (it.hasNext()) if (it.next().expired()) it.remove();
    }
}
