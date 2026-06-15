package dev.aurora.client.event.impl;

import dev.aurora.client.event.Event;

/** Fired on a keyboard state change. */
public final class KeyEvent extends Event {
    public final int key;
    public final boolean pressed;
    public KeyEvent(int key, boolean pressed) { this.key = key; this.pressed = pressed; }
}
