package dev.aurora.client.module.impl.render;

import dev.aurora.client.event.Listen;
import dev.aurora.client.event.impl.RenderHudEvent;
import dev.aurora.client.module.Category;
import dev.aurora.client.module.Module;
import dev.aurora.client.render.Render2D;
import dev.aurora.client.setting.BooleanSetting;
import dev.aurora.client.setting.ColorSetting;
import dev.aurora.client.setting.EnumSetting;
import dev.aurora.client.setting.NumberSetting;

/**
 * Replaces the vanilla crosshair with a customisable cross/dot/circle. Purely
 * cosmetic; rendered in screen space at the centre of the display.
 */
public final class CrosshairCustomizer extends Module {

    public enum Style { CROSS, DOT, CIRCLE }

    private final EnumSetting<Style> style =
            new EnumSetting<>("Style", "Crosshair shape", Style.CROSS);
    private final NumberSetting size =
            new NumberSetting("Size", "Crosshair size", 5, 1, 16, 1);
    private final NumberSetting gap =
            new NumberSetting("Gap", "Centre gap", 2, 0, 10, 1);
    private final NumberSetting thickness =
            new NumberSetting("Thickness", "Line thickness", 1, 1, 4, 1);
    private final ColorSetting color =
            new ColorSetting("Color", "Crosshair colour", 0xFFFFFFFF);
    private final BooleanSetting hideOnGui =
            new BooleanSetting("Hide On GUI", "Hide when a screen is open", true);

    public CrosshairCustomizer() {
        super("Crosshair", "Customisable crosshair", Category.RENDER);
        addSettings(style, size, gap, thickness, color, hideOnGui);
    }

    @Listen
    public void onRenderHud(RenderHudEvent event) {
        if (mc.player == null) return;
        if (hideOnGui.get() && mc.currentScreen != null) return;
        float cx = event.scaledWidth / 2f;
        float cy = event.scaledHeight / 2f;
        int c = color.get();
        float s = size.getFloat();
        float g = gap.getFloat();
        float t = thickness.getFloat();

        switch (style.get()) {
            case DOT:
                Render2D.rect(cx - t / 2, cy - t / 2, t, t, c);
                break;
            case CIRCLE:
                Render2D.outline(cx - s, cy - s, s * 2, s * 2, t, c);
                break;
            case CROSS:
            default:
                Render2D.rect(cx - g - s, cy - t / 2, s, t, c);   // left
                Render2D.rect(cx + g, cy - t / 2, s, t, c);       // right
                Render2D.rect(cx - t / 2, cy - g - s, t, s, c);   // top
                Render2D.rect(cx - t / 2, cy + g, t, s, c);       // bottom
                break;
        }
    }
}
