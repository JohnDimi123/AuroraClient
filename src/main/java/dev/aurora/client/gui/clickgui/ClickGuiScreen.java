package dev.aurora.client.gui.clickgui;

import dev.aurora.client.Aurora;
import dev.aurora.client.gui.clickgui.component.CategoryPanel;
import dev.aurora.client.gui.theme.Theme;
import dev.aurora.client.module.Category;
import dev.aurora.client.render.Render2D;
import dev.aurora.client.render.font.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

/**
 * The custom Click GUI.
 * <p>
 * A fully bespoke interface (no vanilla widgets): a title bar, a live search
 * box, a theme cycle button, and one draggable {@link CategoryPanel} per module
 * {@link Category}. Drawing is immediate-mode via {@link Render2D} and the
 * custom {@link FontRenderer}. Mouse and keyboard navigation are both supported.
 */
public final class ClickGuiScreen extends Screen {

    private final List<CategoryPanel> panels = new ArrayList<>();
    private String searchQuery = "";
    private boolean searchFocused;

    public ClickGuiScreen() {
        int x = 12;
        for (Category category : Category.values()) {
            panels.add(new CategoryPanel(category, x, 40));
            x += 124;
        }
    }

    @Override protected void init() { /* layout fixed in constructor */ }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        Theme theme = Aurora.INSTANCE.getThemeManager().getActive();
        FontRenderer title = Aurora.INSTANCE.getFontManager().title();
        FontRenderer font = Aurora.INSTANCE.getFontManager().regular();

        // Dim/blur backdrop (approximated with a translucent fill for portability).
        Render2D.rect(0, 0, width, height, 0x99000000);

        // Title bar.
        Render2D.rect(0, 0, width, 32, theme.background());
        title.drawWithShadow("Aurora", 12, 4, theme.accent());
        font.drawWithShadow("v" + Aurora.VERSION, 12 + title.getWidth("Aurora") + 6, 14, theme.textMuted());

        // Search box.
        double sx = width - 220, sy = 6, sw = 150, sh = 20;
        Render2D.roundedRect(sx, sy, sw, sh, 4, theme.surface());
        if (searchFocused) Render2D.outline(sx, sy, sw, sh, 1, theme.accent());
        String shown = searchQuery.isEmpty() && !searchFocused ? "Search..." : searchQuery + (searchFocused ? "_" : "");
        font.draw(shown, (float) sx + 6, (float) sy + 6,
                searchQuery.isEmpty() && !searchFocused ? theme.textMuted() : theme.text());

        // Theme button.
        double tx = width - 60, ty = 6;
        Render2D.roundedRect(tx, ty, 50, 20, 4, theme.surface());
        font.drawCentered(theme.getName(), (float) tx + 25, (float) ty + 6, theme.text());

        // Panels.
        for (CategoryPanel panel : panels) {
            panel.setSearch(searchQuery);
            panel.render(mouseX, mouseY, font);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Search focus toggle.
        double sx = width - 220, sy = 6, sw = 150, sh = 20;
        searchFocused = mouseX >= sx && mouseX <= sx + sw && mouseY >= sy && mouseY <= sy + sh;

        // Theme button.
        double tx = width - 60, ty = 6;
        if (mouseX >= tx && mouseX <= tx + 50 && mouseY >= ty && mouseY <= ty + 20) {
            Aurora.INSTANCE.getThemeManager().cycle();
            return true;
        }

        for (CategoryPanel panel : panels) {
            if (panel.mouseClicked(mouseX, mouseY, button)) return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (CategoryPanel panel : panels) panel.mouseReleased();
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        for (CategoryPanel panel : panels) panel.mouseDragged(mouseX, mouseY);
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchFocused) {
            if (keyCode == Keyboard.KEY_BACK && searchQuery.length() > 0) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
            } else if (keyCode == Keyboard.KEY_RETURN) {
                searchFocused = false;
            }
            return true;
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (searchFocused && chr >= 32 && chr < 127) {
            searchQuery += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    private void close() {
        Aurora.INSTANCE.getConfigManager().save();
        if (mc != null) mc.openScreen(null);
    }

    @Override public boolean shouldPause() { return false; }
}
