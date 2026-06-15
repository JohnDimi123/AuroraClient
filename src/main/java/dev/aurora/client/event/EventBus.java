package dev.aurora.client.event;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * A reflection-free (after registration) event bus.
 * <p>
 * During {@link #subscribe(Object)} each annotated method is compiled once into
 * a {@link BiConsumer} via {@link LambdaMetafactory}, giving near-direct-call
 * dispatch performance in hot paths. Listeners are stored per event type and
 * sorted by {@link EventPriority}.
 */
public final class EventBus {

    /** A single compiled listener bound to its owning instance. */
    private static final class Handler {
        final Object owner;
        final EventPriority priority;
        final BiConsumer<Object, Event> invoker;

        Handler(Object owner, EventPriority priority, BiConsumer<Object, Event> invoker) {
            this.owner = owner;
            this.priority = priority;
            this.invoker = invoker;
        }
    }

    private final Map<Class<? extends Event>, List<Handler>> handlers = new ConcurrentHashMap<>();
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    /**
     * Registers all {@link Listen}-annotated methods on the given object.
     * Calling this twice for the same object duplicates listeners; pair with
     * {@link #unsubscribe(Object)}.
     */
    @SuppressWarnings("unchecked")
    public void subscribe(Object subscriber) {
        for (Method method : subscriber.getClass().getDeclaredMethods()) {
            Listen annotation = method.getAnnotation(Listen.class);
            if (annotation == null) continue;
            if (method.getParameterCount() != 1) continue;

            Class<?> param = method.getParameterTypes()[0];
            if (!Event.class.isAssignableFrom(param)) continue;

            BiConsumer<Object, Event> invoker = createInvoker(subscriber, method);
            Handler handler = new Handler(subscriber, annotation.value(), invoker);

            handlers.computeIfAbsent((Class<? extends Event>) param, k -> new ArrayList<>())
                    .add(handler);
        }
        // Keep each list sorted highest-priority-first.
        handlers.values().forEach(list ->
                list.sort(Comparator.comparingInt((Handler h) -> h.priority.ordinal()).reversed()));
    }

    /** Removes every listener owned by the given object. */
    public void unsubscribe(Object subscriber) {
        handlers.values().forEach(list -> list.removeIf(h -> h.owner == subscriber));
    }

    /**
     * Dispatches an event to all registered listeners in priority order.
     *
     * @return the same event instance (for fluent cancellation checks).
     */
    public <T extends Event> T post(T event) {
        List<Handler> list = handlers.get(event.getClass());
        if (list == null) return event;
        // Iterate by index to avoid iterator allocation in hot paths.
        for (int i = 0, n = list.size(); i < n; i++) {
            list.get(i).invoker.accept(list.get(i).owner, event);
        }
        return event;
    }

    /** Compiles a reflective method into a fast {@link BiConsumer} via LambdaMetafactory. */
    @SuppressWarnings("unchecked")
    private static BiConsumer<Object, Event> createInvoker(Object owner, Method method) {
        try {
            method.setAccessible(true);
            MethodHandle handle = LOOKUP.unreflect(method);
            return (BiConsumer<Object, Event>) LambdaMetafactory.metafactory(
                    LOOKUP,
                    "accept",
                    MethodType.methodType(BiConsumer.class),
                    MethodType.methodType(void.class, Object.class, Object.class),
                    handle,
                    MethodType.methodType(void.class, owner.getClass(), method.getParameterTypes()[0])
            ).getTarget().invoke();
        } catch (Throwable t) {
            // Fallback to plain reflection if metafactory binding fails.
            method.setAccessible(true);
            return (o, e) -> {
                try {
                    method.invoke(o, e);
                } catch (Exception ex) {
                    throw new RuntimeException("Event dispatch failed: " + method, ex);
                }
            };
        }
    }
}
