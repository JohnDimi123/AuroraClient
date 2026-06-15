package dev.aurora.client.module.impl.render;

import dev.aurora.client.Aurora;
import dev.aurora.client.event.Listen;
import dev.aurora.client.event.impl.AttackEvent;
import dev.aurora.client.module.Category;
import dev.aurora.client.module.Module;
import dev.aurora.client.setting.BooleanSetting;
import dev.aurora.client.setting.ColorSetting;

/**
 * Cosmetic combat feedback: custom hit colour flash, hit particles, and combo
 * tracking. Purely visual — it reacts to attacks the player already landed and
 * never initiates or modifies combat.
 */
public final class CombatVisuals extends Module {

    private final BooleanSetting hitColor =
            new BooleanSetting("Hit Color", "Custom damage-flash colour", true);
    private final ColorSetting color =
            new ColorSetting("Color", "Hit flash colour", 0xFFFF4060)
                    .visibleWhen(() -> hitColor.get());
    private final BooleanSetting hitParticles =
            new BooleanSetting("Hit Particles", "Spawn crit-style particles on hit", true);
    private final BooleanSetting comboCounter =
            new BooleanSetting("Combo Counter", "Track consecutive hits", true);

    public CombatVisuals() {
        super("Combat Visuals", "Hit colour, particles and combo tracking", Category.RENDER);
        addSettings(hitColor, color, hitParticles, comboCounter);
    }

    public boolean isHitColor() { return hitColor.get(); }
    public int getColor() { return color.get(); }
    public boolean isHitParticles() { return hitParticles.get(); }

    @Listen
    public void onAttack(AttackEvent event) {
        if (comboCounter.get()) Aurora.INSTANCE.getSessionStats().onHit();
    }
}
