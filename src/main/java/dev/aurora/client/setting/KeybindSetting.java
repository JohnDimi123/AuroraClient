package dev.aurora.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/** A single LWJGL key code binding (-1 = unbound). */
public final class KeybindSetting extends Setting<Integer> {
    public KeybindSetting(String name, String description, int key) {
        super(name, description, key);
    }
    public int get() { return getValue(); }
    public boolean matches(int key) { return key != -1 && getValue() == key; }
    @Override public JsonElement toJson() { return new JsonPrimitive(getValue()); }
    @Override public void fromJson(JsonElement e) { setValue(e.getAsInt()); }
}
