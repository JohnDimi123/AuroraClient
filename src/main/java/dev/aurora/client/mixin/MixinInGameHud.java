package dev.aurora.client.mixin;

import dev.aurora.client.Aurora;
import dev.aurora.client.event.impl.RenderHudEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fires the {@link RenderHudEvent} after the vanilla HUD draws, so Aurora HUD
 * modules and notifications render on top. Also renders the notification stack.
 * <p>
 * The scaled resolution is computed via {@link Window}; on 1.8.9 this is the
 * standard way to obtain GUI-space dimensions that match vanilla HUD scaling.
 */
@Mixin(InGameHud.class)
public abstract class MixinInGameHud {

    @Inject(method = "render", at = @At("RETURN"))
    private void aurora$onRenderHud(float tickDelta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Window window = new Window(mc);
        int sw = window.getWidth();
        int sh = window.getHeight();

        Aurora.INSTANCE.getEventBus().post(new RenderHudEvent(tickDelta, sw, sh));
        Aurora.INSTANCE.getNotificationManager().render(sw, sh);
    }
}
