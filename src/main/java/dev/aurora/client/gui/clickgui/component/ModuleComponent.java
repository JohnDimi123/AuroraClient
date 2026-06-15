package dev.aurora.client.gui.clickgui.component;

import dev.aurora.client.gui.theme.Theme;
import dev.aurora.client.module.Module;
import dev.aurora.client.render.Render2D;
import dev.aurora.client.render.font.FontRenderer;
import dev.aurora.client.setting.BooleanSetting;
import dev.aurora.client.setting.EnumSetting;
import dev.aurora.client.setting.NumberSetting;
import dev.aurora.client.setting.Setting;

/**
 * A single module row inside a {@link CategoryPanel}. Left-click toggles the
 * module; right-click expands its settings. Renders {@link BooleanSetting},
 * {@link NumberSetting} (slider) and {@link EnumSetting} (cycler) inline.
 */
public final class ModuleComponent {

    private final Module module;
    private boolean expanded;
    private double lastBottom;
    private Setting<?> draggingSlider;

    public ModuleComponent(Module module) { this.module = module; }

    public Module getModule() { return module; }
    public double lastBottom() { return lastBottom; }

    public double render(double x, double y, double w, double rowH,
                         int mouseX, int mouseY, FontRenderer font, Theme theme) {
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + rowH;
        int bg = module.isEnabled() ? theme.accent() : (hovered ? theme.surface() : theme.background());
        int fg = module.isEnabled() ? 0xFF101010 : theme.text();

        Render2D.rect(x, y, w, rowH, bg);
        font.draw(module.getName(), (float) x + 6, (float) y + 4, fg);

        double cursor = y + rowH;
        if (expanded) {
            for (Setting<?> setting : module.getSettings()) {
                if (!setting.isVisible()) continue;
                if (setting == module.getKeybind()) continue; // keybind UI omitted for brevity
                cursor = renderSetting(setting, x, cursor, w, font, theme, mouseX, mouseY);
            }
        }
        lastBottom = cursor;
        return cursor;
    }

    private double renderSetting(Setting<?> setting, double x, double y, double w,
                                 FontRenderer font, Theme theme, int mouseX, int mouseY) {
        double h = 14;
        Render2D.rect(x, y, w, h, theme.surface());

        if (setting instanceof BooleanSetting) {
            BooleanSetting b = (BooleanSetting) setting;
            font.draw(setting.getName(), (float) x + 6, (float) y + 3, theme.text());
            int box = b.get() ? theme.positive() : theme.textMuted();
            Render2D.roundedRect(x + w - 16, y + 3, 8, 8, 2, box);
        } else if (setting instanceof NumberSetting) {
            NumberSetting n = (NumberSetting) setting;
            font.draw(setting.getName() + ": " + trim(n.get()), (float) x + 6, (float) y + 3, theme.text());
            double frac = (n.get() - n.getMin()) / (n.getMax() - n.getMin());
            Render2D.rect(x + 4, y + h - 3, w - 8, 2, theme.background());
            Render2D.rect(x + 4, y + h - 3, (w - 8) * frac, 2, theme.accent());
            // Live drag handling.
            if (draggingSlider == setting) {
                double rel = Math.max(0, Math.min(1, (mouseX - (x + 4)) / (w - 8)));
                n.setValue(n.getMin() + rel * (n.getMax() - n.getMin()));
            }
        } else if (setting instanceof EnumSetting) {
            EnumSetting<?> e = (EnumSetting<?>) setting;
            font.draw(setting.getName() + ": " + e.get().name(), (float) x + 6, (float) y + 3, theme.text());
        }
        return y + h;
    }

    public double mouseClicked(double x, double y, double w, double rowH,
                               double mouseX, double mouseY, int button) {
        boolean onRow = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + rowH;
        if (onRow) {
            if (button == 0) module.toggle();
            else if (button == 1) expanded = !expanded;
            return y + rowH;
        }
        if (!expanded) return -1;

        double cursor = y + rowH;
        for (Setting<?> setting : module.getSettings()) {
            if (!setting.isVisible() || setting == module.getKeybind()) continue;
            double sh = 14;
            boolean on = mouseX >= x && mouseX <= x + w && mouseY >= cursor && mouseY <= cursor + sh;
            if (on) {
                handleSettingClick(setting, button);
                return cursor + sh;
            }
            cursor += sh;
        }
        return -1;
    }

    private void handleSettingClick(Setting<?> setting, int button) {
        if (setting instanceof BooleanSetting) {
            ((BooleanSetting) setting).toggle();
        } else if (setting instanceof EnumSetting) {
            if (button == 0) ((EnumSetting<?>) setting).cycle();
        } else if (setting instanceof NumberSetting) {
            draggingSlider = setting; // begins drag; released globally
        }
    }

    private static String trim(double v) {
        return v == Math.floor(v) ? String.valueOf((int) v) : String.format("%.1f", v);
    }
}
