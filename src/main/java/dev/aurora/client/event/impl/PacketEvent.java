package dev.aurora.client.event.impl;

import dev.aurora.client.event.Event;
import net.minecraft.network.Packet;

/**
 * Fired for inbound and outbound packets. Cancellable for outbound; inbound
 * cancellation is observational only (used by diagnostics, not blocking).
 */
public final class PacketEvent extends Event {
    public enum Direction { INBOUND, OUTBOUND }
    public final Packet<?> packet;
    public final Direction direction;
    public PacketEvent(Packet<?> packet, Direction direction) {
        this.packet = packet; this.direction = direction;
    }
    @Override public boolean isCancellable() { return true; }
}
