package dev.aurora.client.event.impl;

import dev.aurora.client.event.Event;

/**
 * Fired during 2D HUD rendering. Carries the partial-tick fraction and the
 * scaled resolution so listeners can draw without re-querying GL state.
 */
public final class RenderHudEvent extends Event {
    public final float partialTicks;
    public final int scaledWidth;
    public final int scaledHeight;

    public RenderHudEvent(float partialTicks, int scaledWidth, int scaledHeight) {
        this.partialTicks = partialTicks;
        this.scaledWidth = scaledWidth;
        this.scaledHeight = scaledHeight;
    }
}
