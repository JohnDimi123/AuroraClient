package dev.aurora.client.setting;

import com.google.gson.JsonElement;

import java.util.function.Supplier;

/**
 * Base type for a configurable module property.
 *
 * @param <T> the value type held by this setting
 */
public abstract class Setting<T> {

    private final String name;
    private final String description;
    protected T value;
    private Supplier<Boolean> visibility = () -> true;

    protected Setting(String name, String description, T defaultValue) {
        this.name = name;
        this.description = description;
        this.value = defaultValue;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }

    public T getValue() { return value; }
    public void setValue(T value) { this.value = value; }

    /** Conditional visibility in the GUI (e.g. show only when a parent toggle is on). */
    public Setting<T> visibleWhen(Supplier<Boolean> condition) {
        this.visibility = condition;
        return this;
    }

    public boolean isVisible() { return visibility.get(); }

    /** Serialises the current value to JSON for config persistence. */
    public abstract JsonElement toJson();

    /** Restores the value from a JSON element produced by {@link #toJson()}. */
    public abstract void fromJson(JsonElement element);
}
