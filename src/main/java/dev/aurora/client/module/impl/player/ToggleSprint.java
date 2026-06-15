package dev.aurora.client.module.impl.player;

import dev.aurora.client.event.Listen;
import dev.aurora.client.event.impl.TickEvent;
import dev.aurora.client.module.Category;
import dev.aurora.client.module.Module;
import dev.aurora.client.setting.BooleanSetting;

/**
 * Keeps the player sprinting without holding the sprint key — a pure quality-of-
 * life input helper (it presses the same key the player could hold themselves).
 * Sprint still obeys vanilla rules (hunger, forward movement, collisions).
 */
public final class ToggleSprint extends Module {

    private final BooleanSetting onlyForward =
            new BooleanSetting("Only Forward", "Sprint only while moving forward", true);

    public ToggleSprint() {
        super("Toggle Sprint", "Hold-free sprinting", Category.PLAYER);
        addSettings(onlyForward);
    }

    @Listen
    public void onTick(TickEvent event) {
        if (mc.player == null) return;
        boolean movingForward = mc.player.forwardSpeed > 0;
        if (!onlyForward.get() || movingForward) {
            // Vanilla still gates sprint by hunger/forward, so this only mirrors input.
            if (mc.player.getHungerManager().getFoodLevel() > 6f && movingForward) {
                mc.player.setSprinting(true);
            }
        }
    }
}
