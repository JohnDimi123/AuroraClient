package dev.aurora.client.module.impl.hud;

import dev.aurora.client.Aurora;
import dev.aurora.client.gui.theme.Theme;
import dev.aurora.client.render.Render2D;

/**
 * WASD + click keystroke display. Each key lights up with the accent colour
 * while held. Read-only reflection of the player's current input.
 */
public final class Keystrokes extends HudModule {

    public Keystrokes() { super("Keystrokes", "WASD + mouse display", 4, 120); }

    @Override protected void draw(float partialTicks) {
        if (mc.options == null) { setSize(0, 0); return; }
        Theme theme = Aurora.INSTANCE.getThemeManager().getActive();
        float key = 18, gap = 2;
        setSize(key * 3 + gap * 2, key * 2 + 8 + gap * 2);

        boolean w = mc.options.keyForward.isPressed();
        boolean a = mc.options.keyLeft.isPressed();
        boolean s = mc.options.keyBack.isPressed();
        boolean d = mc.options.keyRight.isPressed();

        drawKey(key + gap, 0, key, key, "W", w, theme);
        drawKey(0, key + gap, key, key, "A", a, theme);
        drawKey(key + gap, key + gap, key, key, "S", s, theme);
        drawKey((key + gap) * 2, key + gap, key, key, "D", d, theme);

        boolean lmb = mc.options.keyAttack.isPressed();
        boolean rmb = mc.options.keyUse.isPressed();
        float row = (key + gap) * 2;
        drawKey(0, row, key * 1.5f + gap / 2, key * 0.6f, "L", lmb, theme);
        drawKey(key * 1.5f + gap * 1.5f, row, key * 1.5f + gap / 2, key * 0.6f, "R", rmb, theme);
    }

    private void drawKey(float x, float y, float w, float h, String label, boolean pressed, Theme theme) {
        int bg = pressed ? theme.accent() : theme.surface();
        int fg = pressed ? 0xFF101010 : theme.text();
        Render2D.roundedRect(x, y, w, h, 2, bg);
        font().drawCentered(label, x + w / 2, y + (h - font().getHeight()) / 2 + 1, fg);
    }
}
