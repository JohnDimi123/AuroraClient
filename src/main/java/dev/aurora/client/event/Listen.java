package dev.aurora.client.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an event handler. The method must accept exactly one
 * parameter whose type extends {@link Event}.
 *
 * <pre>{@code
 * @Listen(EventPriority.HIGH)
 * public void onRender(RenderHudEvent event) { ... }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Listen {
    EventPriority value() default EventPriority.NORMAL;
}
