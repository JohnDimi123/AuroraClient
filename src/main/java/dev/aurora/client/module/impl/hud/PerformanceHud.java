package dev.aurora.client.module.impl.hud;

import dev.aurora.client.Aurora;
import dev.aurora.client.gui.theme.Theme;
import dev.aurora.client.render.Render2D;
import dev.aurora.client.util.PerformanceMonitor;

/**
 * Detailed performance panel: FPS, frametime, memory, CPU and ping. Useful for
 * diagnosing stutter and verifying the impact of optimisation modules.
 */
public final class PerformanceHud extends HudModule {

    public PerformanceHud() { super("Performance HUD", "FPS/frametime/memory/CPU/ping", 4, 320); }

    @Override protected void draw(float partialTicks) {
        Theme theme = Aurora.INSTANCE.getThemeManager().getActive();
        PerformanceMonitor p = Aurora.INSTANCE.getPerformanceMonitor();
        String[] lines = {
                "FPS: " + p.getFps(),
                String.format("Frame: %.1f ms", p.getFrameTimeMs()),
                "Mem: " + p.getUsedMemoryMb() + "/" + p.getMaxMemoryMb() + " MB (" + p.getMemoryPercent() + "%)",
                String.format("CPU: %.0f%%", p.getCpuLoad()),
                "Ping: " + Aurora.INSTANCE.getNetworkManager().getPing() + " ms"
        };
        int lineH = font().getHeight() + 1;
        double maxW = 0;
        for (String l : lines) maxW = Math.max(maxW, font().getWidth(l) + 10);
        double h = lineH * lines.length + 6;
        setSize(maxW, h);
        Render2D.roundedRect(0, 0, maxW, h, 4, theme.surface());
        Render2D.rect(0, 0, 2, h, theme.accentSecondary());
        for (int i = 0; i < lines.length; i++) {
            font().drawWithShadow(lines[i], 6, 4 + i * lineH, theme.text());
        }
    }
}
