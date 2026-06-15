package dev.aurora.client.mixin;

import dev.aurora.client.module.impl.performance.EntityCulling;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies entity render culling. Players are never culled (PvP visibility and
 * fairness); other entities beyond the configured distance are skipped.
 */
@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {

    @Inject(method = "renderEntity", at = @At("HEAD"), cancellable = true)
    private void aurora$cullEntity(Entity entity, float tickDelta, CallbackInfo ci) {
        EntityCulling culling = EntityCulling.getActive();
        if (culling == null) return;
        if (entity instanceof PlayerEntity) return; // never cull players

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        double distSq = entity.squaredDistanceTo(mc.player);
        if (!culling.shouldRender(entity, distSq)) {
            ci.cancel();
        }
    }
}
