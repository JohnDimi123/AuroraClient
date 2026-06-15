package dev.aurora.client.module;

import dev.aurora.client.Aurora;
import dev.aurora.client.setting.BooleanSetting;
import dev.aurora.client.setting.KeybindSetting;
import dev.aurora.client.setting.Setting;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for every feature module.
 * <p>
 * A module owns a list of {@link Setting}s and an enabled flag. When toggled on
 * it is registered with the {@link dev.aurora.client.event.EventBus}; when off
 * it is unregistered, so disabled modules cost nothing at runtime.
 */
public abstract class Module {

    protected final MinecraftClient mc = MinecraftClient.getInstance();

    private final String name;
    private final String description;
    private final Category category;

    private final List<Setting<?>> settings = new ArrayList<>();
    private final KeybindSetting keybind = new KeybindSetting("Keybind", "Toggle key", -1);
    private boolean enabled;

    protected Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
        addSettings(keybind);
    }

    /** Registers settings in declaration order; call from constructors. */
    protected final void addSettings(Setting<?>... toAdd) {
        for (Setting<?> s : toAdd) settings.add(s);
    }

    // ---- lifecycle hooks ----------------------------------------------------

    /** Called when the module transitions to enabled. */
    protected void onEnable() {}

    /** Called when the module transitions to disabled. */
    protected void onDisable() {}

    public final void toggle() { setEnabled(!enabled); }

    public final void setEnabled(boolean state) {
        if (this.enabled == state) return;
        this.enabled = state;
        if (state) {
            Aurora.INSTANCE.getEventBus().subscribe(this);
            onEnable();
        } else {
            onDisable();
            Aurora.INSTANCE.getEventBus().unsubscribe(this);
        }
    }

    // ---- accessors ----------------------------------------------------------

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public boolean isEnabled() { return enabled; }
    public List<Setting<?>> getSettings() { return settings; }
    public KeybindSetting getKeybind() { return keybind; }
}
