package dev.aurora.client.module.impl.hud;

import dev.aurora.client.Aurora;
import dev.aurora.client.gui.theme.Theme;
import dev.aurora.client.render.Render2D;
import dev.aurora.client.setting.BooleanSetting;

/** Displays left (and optionally right) clicks-per-second. */
public final class CpsCounter extends HudModule {

    private final BooleanSetting showRight =
            new BooleanSetting("Show Right", "Also show right-click CPS", true);

    public CpsCounter() {
        super("CPS Counter", "Clicks per second", 4, 40);
        addSettings(showRight);
    }

    @Override protected void draw(float partialTicks) {
        Theme theme = Aurora.INSTANCE.getThemeManager().getActive();
        int l = Aurora.INSTANCE.getClickTracker().getLeftCps();
        String t = "L: " + l + " CPS";
        if (showRight.get()) t += "  R: " + Aurora.INSTANCE.getClickTracker().getRightCps();
        double w = font().getWidth(t) + 8;
        double h = font().getHeight() + 4;
        setSize(w, h);
        Render2D.roundedRect(0, 0, w, h, 3, theme.surface());
        Render2D.rect(0, 0, 2, h, theme.accent());
        font().drawWithShadow(t, 5, 3, theme.text());
    }
}
