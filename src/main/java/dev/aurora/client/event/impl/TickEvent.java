package dev.aurora.client.event.impl;

import dev.aurora.client.event.Event;

/** Fired once per client tick (20 Hz). */
public final class TickEvent extends Event {
    public static final TickEvent INSTANCE = new TickEvent();
    private TickEvent() {}
}
