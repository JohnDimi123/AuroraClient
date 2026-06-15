package dev.aurora.client.event.impl;

import dev.aurora.client.event.Event;

/** Fired when a chat message is received; used by BedWars/stat parsers. */
public final class ChatReceiveEvent extends Event {
    public final String message;
    public ChatReceiveEvent(String message) { this.message = message; }
    @Override public boolean isCancellable() { return true; }
}
