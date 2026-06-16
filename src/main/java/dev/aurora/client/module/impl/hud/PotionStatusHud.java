package dev.aurora.client.module.impl.hud;

import dev.aurora.client.Aurora;
import dev.aurora.client.gui.theme.Theme;
import dev.aurora.client.render.Render2D;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.Collection;

/**
 * Lists active potion effects with remaining duration. Read-only view of the
 * player's status-effect set.
 */
public final class PotionStatusHud extends HudModule {

    public PotionStatusHud() { super("Potion Status", "Active potion effects", 200, 4); }

    @Override protected void draw(float partialTicks) {
        if (mc.player == null) { setSize(0, 0); return; }
        Theme theme = Aurora.INSTANCE.getThemeManager().getActive();
        Collection<StatusEffectInstance> effects = mc.player.getActivePotionEffects();
        if (effects.isEmpty()) { setSize(0, 0); return; }

        int lineH = font().getHeight() + 2;
        double maxW = 0;
        int i = 0;
        for (StatusEffectInstance e : effects) {
            String name = net.minecraft.client.resource.language.I18n.translate(
                    e.getPotion().getTranslationKey());
            String line = name + " " + format(e.getDuration());
            maxW = Math.max(maxW, font().getWidth(line) + 8);
        }
        setSize(maxW, lineH * effects.size() + 4);
        Render2D.roundedRect(0, 0, maxW, lineH * effects.size() + 4, 3, theme.surface());
        for (StatusEffectInstance e : effects) {
            String name = net.minecraft.client.resource.language.I18n.translate(
                    e.getPotion().getTranslationKey());
            font().drawWithShadow(name + " " + format(e.getDuration()), 4, 3 + i * lineH, theme.text());
            i++;
        }
    }

    private String format(int ticks) {
        int s = ticks / 20;
        return String.format("%d:%02d", s / 60, s % 60);
    }
}
