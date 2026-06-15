package dev.aurora.client.module.impl.hud;

import dev.aurora.client.Aurora;
import dev.aurora.client.gui.theme.Theme;
import dev.aurora.client.render.Render2D;
import dev.aurora.client.util.SessionStats;

/**
 * Compact panel of session statistics: kills, deaths, KDR, wins, best combo and
 * elapsed time. Sourced from {@link SessionStats}.
 */
public final class SessionStatsHud extends HudModule {

    public SessionStatsHud() { super("Session Stats", "Kills/deaths/KDR/time", 4, 220); }

    @Override protected void draw(float partialTicks) {
        Theme theme = Aurora.INSTANCE.getThemeManager().getActive();
        SessionStats s = Aurora.INSTANCE.getSessionStats();
        String[] lines = {
                "Kills: " + s.getKills(),
                "Deaths: " + s.getDeaths(),
                String.format("KDR: %.2f", s.getKdr()),
                "Wins: " + s.getWins(),
                "Best Combo: " + s.getBestCombo(),
                "Time: " + s.getFormattedDuration()
        };
        int lineH = font().getHeight() + 1;
        double maxW = 0;
        for (String l : lines) maxW = Math.max(maxW, font().getWidth(l) + 10);
        double h = lineH * lines.length + 6;
        setSize(maxW, h);
        Render2D.roundedRect(0, 0, maxW, h, 4, theme.surface());
        Render2D.rect(0, 0, 2, h, theme.accent());
        for (int i = 0; i < lines.length; i++) {
            font().drawWithShadow(lines[i], 6, 4 + i * lineH, theme.text());
        }
    }
}
