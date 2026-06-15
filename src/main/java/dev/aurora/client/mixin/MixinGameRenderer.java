package dev.aurora.client.mixin;

import dev.aurora.client.Aurora;
import dev.aurora.client.event.impl.RenderWorldEvent;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fires the {@link RenderWorldEvent} during world rendering so 3D overlays
 * (damage indicators, target lines) can draw in world space.
 */
@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

    @Inject(method = "renderWorld", at = @At("RETURN"))
    private void aurora$onRenderWorld(float tickDelta, long nanoTime, CallbackInfo ci) {
        Aurora.INSTANCE.getEventBus().post(new RenderWorldEvent(tickDelta));
    }
}
