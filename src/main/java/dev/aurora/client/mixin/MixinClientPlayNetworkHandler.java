package dev.aurora.client.mixin;

import dev.aurora.client.Aurora;
import dev.aurora.client.event.impl.ChatReceiveEvent;
import dev.aurora.client.event.impl.PacketEvent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Observes inbound packets for diagnostics and parses chat messages for the
 * BedWars overlay and stat trackers. Purely observational — no packet is
 * modified, delayed, or dropped.
 */
@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {

    @Inject(method = "onChatMessage", at = @At("HEAD"))
    private void aurora$onChat(ChatMessageS2CPacket packet, CallbackInfo ci) {
        try {
            String text = packet.getMessage().getString();
            Aurora.INSTANCE.getEventBus().post(new ChatReceiveEvent(text));
            Aurora.INSTANCE.getEventBus().post(
                    new PacketEvent(packet, PacketEvent.Direction.INBOUND));
        } catch (Throwable ignored) { /* defensive: never break vanilla chat */ }
    }
}
