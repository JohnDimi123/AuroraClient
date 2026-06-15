package dev.aurora.client.module.impl.hud;

import dev.aurora.client.Aurora;

/** Displays the local player's ping in milliseconds. */
public final class PingHud extends FpsHudBase {
    public PingHud() { super("Ping HUD", "Shows ping to server", 4, 16); }
    @Override protected String text() {
        return Aurora.INSTANCE.getNetworkManager().getPing() + " ms";
    }
}
