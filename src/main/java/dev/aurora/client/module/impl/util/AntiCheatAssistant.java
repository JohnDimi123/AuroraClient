package dev.aurora.client.module.impl.util;

import dev.aurora.client.Aurora;
import dev.aurora.client.event.Listen;
import dev.aurora.client.event.impl.RenderHudEvent;
import dev.aurora.client.anticheat.DetectionResult;
import dev.aurora.client.gui.theme.Theme;
import dev.aurora.client.module.Category;
import dev.aurora.client.module.Module;
import dev.aurora.client.render.Render2D;
import dev.aurora.client.setting.NumberSetting;

import java.util.List;

/**
 * Module wrapper exposing the informational {@link dev.aurora.client.anticheat.AntiCheatEngine}
 * to the user. Enabling this turns the engine on and renders a small panel of
 * recent confidence-scored alerts.
 *
 * <p>This is strictly informational: it observes publicly visible behaviour,
 * surfaces a likelihood to the local user, and takes no action against anyone.
 */
public final class AntiCheatAssistant extends Module {

    private final NumberSetting threshold =
            new NumberSetting("Threshold %", "Minimum confidence to alert", 70, 50, 95, 5);

    public AntiCheatAssistant() {
        super("AntiCheat Assistant", "Informational suspicious-behaviour detector", Category.UTILITY);
        addSettings(threshold);
    }

    @Override protected void onEnable() {
        Aurora.INSTANCE.getAntiCheatEngine().setEnabled(true);
        Aurora.INSTANCE.getAntiCheatEngine().setConfidenceThreshold(threshold.get() / 100.0);
    }

    @Override protected void onDisable() {
        Aurora.INSTANCE.getAntiCheatEngine().setEnabled(false);
    }

    @Listen
    public void onRenderHud(RenderHudEvent event) {
        Aurora.INSTANCE.getAntiCheatEngine().setConfidenceThreshold(threshold.get() / 100.0);
        List<DetectionResult> alerts = Aurora.INSTANCE.getAntiCheatEngine().getRecentAlerts();
        if (alerts.isEmpty()) return;

        Theme theme = Aurora.INSTANCE.getThemeManager().getActive();
        var font = Aurora.INSTANCE.getFontManager().regular();
        int lineH = font.getHeight() + 2;
        int shown = Math.min(5, alerts.size());
        double w = 220, h = lineH * shown + 18;
        double x = event.scaledWidth - w - 4;
        double y = 60;

        Render2D.roundedRect(x, y, w, h, 4, theme.surface());
        Render2D.rect(x, y, 2, h, 0xFFFFB020);
        font.drawWithShadow("AntiCheat (informational)", (float) x + 6, (float) y + 4, theme.accent());
        for (int i = 0; i < shown; i++) {
            DetectionResult r = alerts.get(i);
            String line = "Possible " + r.checkName + " - " + r.getPercent() + "%";
            font.drawWithShadow(line, (float) x + 6, (float) y + 16 + i * lineH, theme.text());
        }
    }
}
