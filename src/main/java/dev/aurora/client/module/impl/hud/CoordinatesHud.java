package dev.aurora.client.module.impl.hud;

/** Displays the player's XYZ coordinates. */
public final class CoordinatesHud extends FpsHudBase {
    public CoordinatesHud() { super("Coordinates", "Shows XYZ position", 4, 28); }
    @Override protected String text() {
        if (mc.player == null) return "X: - Y: - Z: -";
        return String.format("X: %.0f Y: %.0f Z: %.0f",
                mc.player.x, mc.player.y, mc.player.z);
    }
}
