package dev.aurora.client.mixin;

import dev.aurora.client.Aurora;
import dev.aurora.client.event.impl.AttackEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks the local attack action to drive CPS counting, combo tracking, reach
 * measurement, and the anti-cheat self-observation. This mirrors input the
 * player already performed; it never initiates or modifies attacks.
 */
@Mixin(MinecraftClient.class)
public abstract class MixinMouse {

    @Inject(method = "doAttack", at = @At("HEAD"))
    private void aurora$onAttack(CallbackInfo ci) {
        Aurora.INSTANCE.getClickTracker().onLeftClick();
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.targetedEntity != null && mc.player != null) {
            Entity target = mc.targetedEntity;
            double reach = Math.sqrt(mc.player.squaredDistanceTo(target));
            Aurora.INSTANCE.getModuleManager()
                    .get(dev.aurora.client.module.impl.render.ReachDisplay.class)
                    .record(reach);
            Aurora.INSTANCE.getEventBus().post(new AttackEvent(target));
        }
    }

    @Inject(method = "doItemUse", at = @At("HEAD"))
    private void aurora$onUse(CallbackInfo ci) {
        Aurora.INSTANCE.getClickTracker().onRightClick();
    }
}
