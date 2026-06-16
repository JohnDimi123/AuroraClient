package dev.aurora.client.module.impl.util;

import dev.aurora.client.Aurora;
import dev.aurora.client.event.Listen;
import dev.aurora.client.event.impl.RenderHudEvent;
import dev.aurora.client.gui.theme.Theme;
import dev.aurora.client.module.Category;
import dev.aurora.client.module.Module;
import dev.aurora.client.network.NetworkManager;
import dev.aurora.client.render.Render2D;
import dev.aurora.client.render.font.FontRenderer;

/**
 * Surfaces network statistics gathered by {@link NetworkManager}: ping, jitter,
 * and packets-per-second in/out. Read-only diagnostics; no packet manipulation.
 */
public final class NetworkDiagnostics extends Module {

    public NetworkDiagnostics() {
        super("Network Diagnostics", "Ping/jitter/packet statistics", Category.UTILITY);
    }

    @Listen
    public void onRenderHud(RenderHudEvent event) {
        Theme theme = Aurora.INSTANCE.getThemeManager().getActive();
        NetworkManager net = Aurora.INSTANCE.getNetworkManager();
        FontRenderer font = Aurora.INSTANCE.getFontManager().regular();
        String[] lines = {
                "Ping: " + net.getPing() + " ms",
                "Jitter: " + net.getJitter() + " ms",
                "Pkt In/s: " + net.getInboundPerSecond(),
                "Pkt Out/s: " + net.getOutboundPerSecond()
        };
        int lineH = font.getHeight() + 1;
        double maxW = 0;
        for (String l : lines) maxW = Math.max(maxW, font.getWidth(l) + 10);
        double h = lineH * lines.length + 6;
        double x = event.scaledWidth - maxW - 4;
        double y = event.scaledHeight - h - 4;
        Render2D.roundedRect(x, y, maxW, h, 4, theme.surface());
        Render2D.rect(x, y, 2, h, theme.accentSecondary());
        for (int i = 0; i < lines.length; i++) {
            font.drawWithShadow(lines[i], (float) x + 6, (float) y + 4 + i * lineH, theme.text());
        }
    }
}
