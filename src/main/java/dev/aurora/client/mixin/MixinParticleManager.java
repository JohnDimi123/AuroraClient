package dev.aurora.client.mixin;

import dev.aurora.client.module.impl.performance.ParticleOptimizer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies particle optimisation: when the {@link ParticleOptimizer} module is
 * active, a configurable fraction of newly added particles are skipped to cut
 * fill-rate and CPU cost.
 */
@Mixin(ParticleManager.class)
public abstract class MixinParticleManager {

    @Inject(method = "addParticle", at = @At("HEAD"), cancellable = true)
    private void aurora$filterParticle(Particle particle, CallbackInfo ci) {
        ParticleOptimizer optimizer = ParticleOptimizer.getActive();
        if (optimizer != null && !optimizer.shouldSpawn()) {
            ci.cancel();
        }
    }
}
