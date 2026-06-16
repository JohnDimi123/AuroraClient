package dev.aurora.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.function.Supplier;

/** A bounded numeric (double) setting with min, max and step. */
public final class NumberSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double step;

    public NumberSetting(String name, String description, double def, double min, double max, double step) {
        super(name, description, def);
        this.min = min; this.max = max; this.step = step;
    }

    public double getMin() { return min; }
    public double getMax() { return max; }
    public double getStep() { return step; }

    public double get() { return getValue(); }
    public int getInt() { return (int) Math.round(getValue()); }
    public float getFloat() { return getValue().floatValue(); }

    @Override public void setValue(Double v) {
        // Clamp and snap to the configured step.
        double clamped = Math.max(min, Math.min(max, v));
        double snapped = min + Math.round((clamped - min) / step) * step;
        super.setValue(Math.max(min, Math.min(max, snapped)));
    }

    @Override public NumberSetting visibleWhen(Supplier<Boolean> condition) {
        super.visibleWhen(condition); return this;
    }
    @Override public JsonElement toJson() { return new JsonPrimitive(getValue()); }
    @Override public void fromJson(JsonElement e) { setValue(e.getAsDouble()); }
}
