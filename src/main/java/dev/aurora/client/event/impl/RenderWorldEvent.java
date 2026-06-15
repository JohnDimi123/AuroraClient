package dev.aurora.client.event.impl;

import dev.aurora.client.event.Event;

/** Fired during 3D world rendering (after entities, before HUD). */
public final class RenderWorldEvent extends Event {
    public final float partialTicks;
    public RenderWorldEvent(float partialTicks) { this.partialTicks = partialTicks; }
}
