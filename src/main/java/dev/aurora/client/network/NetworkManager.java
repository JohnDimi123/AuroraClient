package dev.aurora.client.network;

import dev.aurora.client.event.Listen;
import dev.aurora.client.event.impl.PacketEvent;
import dev.aurora.client.event.impl.TickEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Passive network monitor.
 * <p>
 * Tracks ping (from the server-provided player list), derives a short-window
 * jitter estimate, and counts inbound/outbound packet rates. It is purely
 * observational — Aurora performs no packet dropping, delaying, or duplication,
 * so it provides no timer/lag advantage and stays within server rules.
 */
public final class NetworkManager {

    private int ping;
    private int jitter;
    private final Deque<Integer> pingHistory = new ArrayDeque<>();

    private int inboundCount;
    private int outboundCount;
    private int inboundPerSecond;
    private int outboundPerSecond;
    private long lastRateReset = System.currentTimeMillis();

    @Listen
    public void onPacket(PacketEvent event) {
        if (event.direction == PacketEvent.Direction.INBOUND) inboundCount++;
        else outboundCount++;
    }

    @Listen
    public void onTick(TickEvent event) {
        updatePing();
        long now = System.currentTimeMillis();
        if (now - lastRateReset >= 1000) {
            inboundPerSecond = inboundCount;
            outboundPerSecond = outboundCount;
            inboundCount = 0;
            outboundCount = 0;
            lastRateReset = now;
        }
    }

    private void updatePing() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        PlayerListEntry entry = mc.getNetworkHandler()
                .getPlayerListEntry(mc.player.getGameProfile().getId());
        if (entry == null) return;

        int newPing = Math.max(0, entry.getLatency());
        // Jitter = mean absolute deviation of recent pings.
        pingHistory.addLast(newPing);
        while (pingHistory.size() > 20) pingHistory.pollFirst();
        if (pingHistory.size() > 1) {
            double mean = pingHistory.stream().mapToInt(Integer::intValue).average().orElse(newPing);
            double mad = pingHistory.stream().mapToDouble(p -> Math.abs(p - mean)).average().orElse(0);
            jitter = (int) Math.round(mad);
        }
        ping = newPing;
    }

    public int getPing() { return ping; }
    public int getJitter() { return jitter; }
    public int getInboundPerSecond() { return inboundPerSecond; }
    public int getOutboundPerSecond() { return outboundPerSecond; }
}
