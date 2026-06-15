package dev.aurora.client.gui.hudeditor;

import dev.aurora.client.Aurora;
import dev.aurora.client.gui.theme.Theme;
import dev.aurora.client.render.Render2D;
import dev.aurora.client.render.font.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.input.Keyboard;

/**
 * Drag-and-drop HUD layout editor.
 * <p>
 * Renders every registered {@link HudElement} in place with a selection outline,
 * lets the user drag elements freely, snaps to a configurable grid and to other
 * elements' edges, and adjusts scale with the scroll wheel. Layout changes are
 * persisted by the config system on close.
 */
public final class HudEditorScreen extends Screen {

    private static final int GRID = 8;
    private static final int SNAP_DISTANCE = 6;

    private HudElement selected;
    private double dragOffsetX, dragOffsetY;
    private boolean showGrid = true;

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        Theme theme = Aurora.INSTANCE.getThemeManager().getActive();
        FontRenderer font = Aurora.INSTANCE.getFontManager().regular();

        Render2D.rect(0, 0, width, height, 0x88000000);

        if (showGrid) {
            int gridColor = 0x22FFFFFF;
            for (int gx = 0; gx < width; gx += GRID) Render2D.rect(gx, 0, 1, height, gridColor);
            for (int gy = 0; gy < height; gy += GRID) Render2D.rect(0, gy, width, 1, gridColor);
        }

        // Render each element with an editor outline.
        for (HudElement element : Aurora.INSTANCE.getHudManager().getElements()) {
            element.renderElement();
            int outline = element == selected ? theme.accent() : theme.textMuted();
            Render2D.outline(element.getX(), element.getY(),
                    Math.max(element.getWidth(), 4), Math.max(element.getHeight(), 4), 1, outline);
        }

        // Help text.
        font.drawWithShadow("HUD Editor - drag to move, scroll to scale, G toggles grid, ESC saves & exits",
                8, height - 14, theme.text());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        selected = Aurora.INSTANCE.getHudManager().elementAt(mouseX, mouseY);
        if (selected != null) {
            dragOffsetX = mouseX - selected.getX();
            dragOffsetY = mouseY - selected.getY();
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (selected == null) return true;
        double nx = mouseX - dragOffsetX;
        double ny = mouseY - dragOffsetY;

        // Snap to grid.
        nx = Math.round(nx / GRID) * GRID;
        ny = Math.round(ny / GRID) * GRID;

        // Snap to other elements' edges.
        for (HudElement other : Aurora.INSTANCE.getHudManager().getElements()) {
            if (other == selected) continue;
            if (Math.abs(nx - other.getX()) < SNAP_DISTANCE) nx = other.getX();
            if (Math.abs(ny - other.getY()) < SNAP_DISTANCE) ny = other.getY();
            double otherRight = other.getX() + other.getWidth();
            if (Math.abs(nx - otherRight) < SNAP_DISTANCE) nx = otherRight;
        }

        // Clamp to screen.
        nx = Math.max(0, Math.min(width - selected.getWidth(), nx));
        ny = Math.max(0, Math.min(height - selected.getHeight(), ny));
        selected.setPosition(nx, ny);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        HudElement target = Aurora.INSTANCE.getHudManager().elementAt(mouseX, mouseY);
        if (target != null) {
            target.setScale(target.getScale() + (float) amount * 0.1f);
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == Keyboard.KEY_G) { showGrid = !showGrid; return true; }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            Aurora.INSTANCE.getConfigManager().save();
            if (mc != null) mc.openScreen(null);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override public boolean shouldPause() { return false; }
}
