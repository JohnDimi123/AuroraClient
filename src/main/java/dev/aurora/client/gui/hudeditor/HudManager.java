package dev.aurora.client.gui.hudeditor;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry of {@link HudElement}s. Owns layout persistence (position + scale)
 * and exposes the elements for both in-game rendering and the HUD editor.
 */
public final class HudManager {

    private final List<HudElement> elements = new ArrayList<>();

    public void register(HudElement element) { elements.add(element); }
    public List<HudElement> getElements() { return elements; }

    /** Finds the topmost element under a cursor position, or null. */
    public HudElement elementAt(double x, double y) {
        for (int i = elements.size() - 1; i >= 0; i--) {
            HudElement e = elements.get(i);
            if (e.contains(x, y)) return e;
        }
        return null;
    }

    /** Serialises positions and scales for every element. */
    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        for (HudElement e : elements) {
            JsonObject o = new JsonObject();
            o.addProperty("x", e.getX());
            o.addProperty("y", e.getY());
            o.addProperty("scale", e.getScale());
            root.add(e.getId(), o);
        }
        return root;
    }

    /** Restores positions and scales from a serialised layout. */
    public void fromJson(JsonObject root) {
        for (HudElement e : elements) {
            if (!root.has(e.getId())) continue;
            JsonObject o = root.getAsJsonObject(e.getId());
            e.setPosition(o.get("x").getAsDouble(), o.get("y").getAsDouble());
            if (o.has("scale")) e.setScale(o.get("scale").getAsFloat());
        }
    }
}
