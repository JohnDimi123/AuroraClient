package dev.aurora.client.event;

/**
 * Dispatch ordering for event listeners. Higher ordinal = earlier invocation.
 */
public enum EventPriority {
    LOWEST,
    LOW,
    NORMAL,
    HIGH,
    HIGHEST
}
