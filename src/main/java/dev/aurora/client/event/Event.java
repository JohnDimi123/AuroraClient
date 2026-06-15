package dev.aurora.client.event;

/**
 * Base type for all events dispatched on the {@link EventBus}.
 * <p>
 * Events are lightweight, allocation-friendly value objects. Subclasses should
 * keep field counts minimal and avoid heap churn in hot paths (render/tick).
 */
public abstract class Event {

    private boolean cancelled;

    /** @return {@code true} if a listener has requested cancellation. */
    public final boolean isCancelled() {
        return cancelled;
    }

    /**
     * Marks this event cancelled. Only meaningful for events whose dispatcher
     * checks {@link #isCancelled()} (i.e. cancellable events).
     */
    public final void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /** @return whether this event type supports cancellation semantics. */
    public boolean isCancellable() {
        return false;
    }
}
