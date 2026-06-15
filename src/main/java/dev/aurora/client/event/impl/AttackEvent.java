package dev.aurora.client.event.impl;

import dev.aurora.client.event.Event;
import net.minecraft.entity.Entity;

/** Fired when the local player lands an attack on an entity. */
public final class AttackEvent extends Event {
    public final Entity target;
    public AttackEvent(Entity target) { this.target = target; }
}
