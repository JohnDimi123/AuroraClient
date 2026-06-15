package dev.aurora.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/** A setting selecting one value from a Java enum. */
public final class EnumSetting<E extends Enum<E>> extends Setting<E> {
    private final Class<E> type;

    public EnumSetting(String name, String description, E def) {
        super(name, description, def);
        this.type = def.getDeclaringClass();
    }

    public E get() { return getValue(); }

    /** Advances to the next enum constant, wrapping around. */
    public void cycle() {
        E[] values = type.getEnumConstants();
        setValue(values[(getValue().ordinal() + 1) % values.length]);
    }

    public E[] getValues() { return type.getEnumConstants(); }

    @Override public JsonElement toJson() { return new JsonPrimitive(getValue().name()); }
    @Override public void fromJson(JsonElement e) {
        try { setValue(Enum.valueOf(type, e.getAsString())); }
        catch (IllegalArgumentException ignored) { /* keep default on bad data */ }
    }
}
