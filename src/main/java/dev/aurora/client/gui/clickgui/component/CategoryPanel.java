package dev.aurora.client.gui.clickgui.component;

import dev.aurora.client.Aurora;
import dev.aurora.client.gui.theme.Theme;
import dev.aurora.client.module.Category;
import dev.aurora.client.module.Module;
import dev.aurora.client.render.Render2D;
import dev.aurora.client.render.font.FontRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * A draggable panel listing every module in one {@link Category}. Each module
 * row can be toggled and expanded to reveal its {@link ModuleComponent}
 * settings. Supports live search filtering by module name.
 */
public final class CategoryPanel {

    private static final int WIDTH = 116;
    private static final int HEADER_H = 18;
    private static final int ROW_H = 16;

    private final Category category;
    private double x, y;
    private boolean dragging;
    private double dragOffsetX, dragOffsetY;
    private boolean collapsed;
    private String search = "";

    private final List<ModuleComponent> components = new ArrayList<>();

    public CategoryPanel(Category category, double x, double y) {
        this.category = category;
        this.x = x;
        this.y = y;
        for (Module module : Aurora.INSTANCE.getModuleManager().getByCategory(category)) {
            components.add(new ModuleComponent(module));
        }
    }

    public void setSearch(String search) { this.search = search.toLowerCase(); }

    private boolean matches(Module m) {
        return search.isEmpty() || m.getName().toLowerCase().contains(search);
    }

    public void render(int mouseX, int mouseY, FontRenderer font) {
        Theme theme = Aurora.INSTANCE.getThemeManager().getActive();

        // Header.
        Render2D.roundedRect(x, y, WIDTH, HEADER_H, 3, theme.background());
        font.drawWithShadow(category.getDisplay(), (float) x + 6, (float) y + 5, theme.accent());

        if (collapsed) return;

        double rowY = y + HEADER_H + 2;
        for (ModuleComponent comp : components) {
            if (!matches(comp.getModule())) continue;
            rowY = comp.render(x, rowY, WIDTH, ROW_H, mouseX, mouseY, font, theme);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Header: left-drag to move, right-click to collapse.
        if (inHeader(mouseX, mouseY)) {
            if (button == 0) {
                dragging = true;
                dragOffsetX = mouseX - x;
                dragOffsetY = mouseY - y;
            } else if (button == 1) {
                collapsed = !collapsed;
            }
            return true;
        }
        if (collapsed) return false;

        double rowY = y + HEADER_H + 2;
        for (ModuleComponent comp : components) {
            if (!matches(comp.getModule())) continue;
            double consumed = comp.mouseClicked(x, rowY, WIDTH, ROW_H, mouseX, mouseY, button);
            if (consumed >= 0) return true;
            rowY = comp.lastBottom();
        }
        return false;
    }

    public void mouseDragged(double mouseX, double mouseY) {
        if (dragging) {
            x = mouseX - dragOffsetX;
            y = mouseY - dragOffsetY;
        }
    }

    public void mouseReleased() { dragging = false; }

    private boolean inHeader(double mx, double my) {
        return mx >= x && mx <= x + WIDTH && my >= y && my <= y + HEADER_H;
    }
}
