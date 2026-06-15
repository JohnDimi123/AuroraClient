package dev.aurora.client.module.impl.hud;

import dev.aurora.client.Aurora;
import dev.aurora.client.gui.theme.Theme;
import dev.aurora.client.render.Render2D;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Shows information about the entity the player is currently looking at or last
 * attacked: name, health bar, and distance. Read-only target telemetry.
 */
public final class TargetHud extends HudModule {

    private LivingEntity target;
    private long lastSeen;

    public TargetHud() { super("Target HUD", "Info about your current target", 280, 60); }

    /** Updates the displayed target (called by combat/look ray observers). */
    public void setTarget(LivingEntity entity) {
        this.target = entity;
        this.lastSeen = System.currentTimeMillis();
    }

    @Override protected void draw(float partialTicks) {
        if (target == null || System.currentTimeMillis() - lastSeen > 3000) {
            setSize(0, 0); return;
        }
        Theme theme = Aurora.INSTANCE.getThemeManager().getActive();
        double w = 120, h = 38;
        setSize(w, h);
        Render2D.roundedRect(0, 0, w, h, 4, theme.surface());

        String name = target instanceof PlayerEntity
                ? ((PlayerEntity) target).getGameProfile().getName() : target.getName();
        font().drawWithShadow(name, 6, 5, theme.text());

        float pct = target.getHealth() / target.getMaximumHealth();
        Render2D.roundedRect(6, 20, w - 12, 6, 2, theme.background());
        Render2D.roundedRect(6, 20, (w - 12) * pct, 6, 2,
                pct > 0.5f ? theme.positive() : theme.negative());
        font().drawWithShadow(String.format("%.1f HP", target.getHealth()),
                6, 28, theme.textMuted());
    }
}
