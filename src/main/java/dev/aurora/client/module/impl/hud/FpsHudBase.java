package dev.aurora.client.module.impl.hud;

import dev.aurora.client.Aurora;
import dev.aurora.client.gui.theme.Theme;
import dev.aurora.client.render.Render2D;

/** Shared single-line label HUD base with a themed pill background. */
abstract class FpsHudBase extends HudModule {
    protected FpsHudBase(String name, String desc, double x, double y) { super(name, desc, x, y); }

    protected abstract String text();

    @Override protected void draw(float partialTicks) {
        Theme theme = Aurora.INSTANCE.getThemeManager().getActive();
        String t = text();
        double w = font().getWidth(t) + 8;
        double h = font().getHeight() + 4;
        setSize(w, h);
        Render2D.roundedRect(0, 0, w, h, 3, theme.surface());
        Render2D.rect(0, 0, 2, h, theme.accent());
        font().drawWithShadow(t, 5, 3, theme.text());
    }
}
