package dev.aurora.client.module.impl.player;

import dev.aurora.client.event.Listen;
import dev.aurora.client.event.impl.KeyEvent;
import dev.aurora.client.module.Category;
import dev.aurora.client.module.Module;
import dev.aurora.client.setting.KeybindSetting;
import org.lwjgl.input.Keyboard;

/**
 * Toggleable sneak: tap the bound key to latch sneak on/off instead of holding.
 * Mirrors the vanilla sneak key state; grants no movement advantage.
 */
public final class ToggleSneak extends Module {

    private final KeybindSetting toggleKey =
            new KeybindSetting("Toggle Key", "Key to latch sneak", Keyboard.KEY_LCONTROL);
    private boolean sneaking;

    public ToggleSneak() {
        super("Toggle Sneak", "Latching sneak toggle", Category.PLAYER);
        addSettings(toggleKey);
    }

    @Override protected void onDisable() { sneaking = false; applyState(); }

    @Listen
    public void onKey(KeyEvent event) {
        if (event.pressed && toggleKey.matches(event.key)) {
            sneaking = !sneaking;
            applyState();
        }
    }

    private void applyState() {
        if (mc.options != null) {
            mc.options.keySneak.setPressed(sneaking);
        }
    }
}
