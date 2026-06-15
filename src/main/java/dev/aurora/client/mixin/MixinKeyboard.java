package dev.aurora.client.mixin;

import dev.aurora.client.Aurora;
import dev.aurora.client.event.impl.KeyEvent;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Polls keyboard state once per tick and dispatches a {@link KeyEvent} on the
 * rising edge of any tracked key, driving module keybinds and latching toggles.
 * <p>
 * State is sampled with {@link Keyboard#isKeyDown(int)} rather than draining the
 * LWJGL event queue, so vanilla key handling is never disturbed. Binds only fire
 * when no screen is open, preventing accidental toggles while typing.
 */
@Mixin(MinecraftClient.class)
public abstract class MixinKeyboard {

    /** Previous-tick down state, indexed by LWJGL key code. */
    private final boolean[] aurora$prev = new boolean[Keyboard.KEYBOARD_SIZE];

    @Inject(method = "tick", at = @At("RETURN"))
    private void aurora$pollKeys(CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        boolean inGame = mc.currentScreen == null;
        for (int key = 1; key < Keyboard.KEYBOARD_SIZE; key++) {
            boolean down = Keyboard.isKeyDown(key);
            if (down && !aurora$prev[key] && inGame) {
                Aurora.INSTANCE.getEventBus().post(new KeyEvent(key, true));
            }
            aurora$prev[key] = down;
        }
    }
}
