package dev.aurora.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/** An ARGB colour setting, stored as a packed int. */
public final class ColorSetting extends Setting<Integer> {
    public ColorSetting(String name, String description, int argb) {
        super(name, description, argb);
    }
    public int get() { return getValue(); }
    @Override public JsonElement toJson() { return new JsonPrimitive(getValue()); }
    @Override public void fromJson(JsonElement e) { setValue(e.getAsInt()); }
}
