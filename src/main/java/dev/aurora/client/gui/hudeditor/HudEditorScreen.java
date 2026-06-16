package dev.aurora.client.gui.hudeditor;

import dev.aurora.client.Aurora;
import dev.aurora.client.gui.theme.Theme;
import dev.aurora.client.render.Render2D;
import dev.aurora.client.render.font.FontRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.input.Keyboard;

/**
 * Drag-and-drop HUD layout editor.
 * <p>
 * Renders every registered {@link HudElement} in place with a selection outline,
 * lets the user drag elements freely, snaps to a configurable grid and to other
 * elements' edges. Layout changes are persisted by the config system on close.
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

        for (HudElement element : Aurora.INSTANCE.getHudManager().getElements()) {
            element.renderElement();
            int outline = element == selected ? theme.accent() : theme.textMuted();
            Render2D.outline(element.getX(), element.getY(),
                    Math.max(element.getWidth(), 4), Math.max(element.getHeight(), 4), 1, outline);
        }

        font.drawWithShadow("HUD Editor - drag to move, G toggles grid, ESC saves & exits",
                8, height - 14, theme.text());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        selected = Aurora.INSTANCE.getHudManager().elementAt((double) mouseX, (double) mouseY);
        if (selected != null) {
            dragOffsetX = mouseX - selected.getX();
            dragOffsetY = mouseY - selected.getY();
        }
    }

    @Override
    public void mouseDragged(int mouseX, int mouseY, int button, long timeSinceLastClick) {
        if (selected == null) return;
        double nx = mouseX - dragOffsetX;
        double ny = mouseY - dragOffsetY;

        nx = Math.round(nx / GRID) * GRID;
        ny = Math.round(ny / GRID) * GRID;

        for (HudElement other : Aurora.INSTANCE.getHudManager().getElements()) {
            if (other == selected) continue;
            if (Math.abs(nx - other.getX()) < SNAP_DISTANCE) nx = other.getX();
            if (Math.abs(ny - other.getY()) < SNAP_DISTANCE) ny = other.getY();
            double otherRight = other.getX() + other.getWidth();
            if (Math.abs(nx - otherRight) < SNAP_DISTANCE) nx = otherRight;
        }

        nx = Math.max(0, Math.min(width - selected.getWidth(), nx));
        ny = Math.max(0, Math.min(height - selected.getHeight(), ny));
        selected.setPosition(nx, ny);
    }

    @Override
    public void keyPressed(char chr, int keyCode) {
        if (keyCode == Keyboard.KEY_G) { showGrid = !showGrid; return; }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            Aurora.INSTANCE.getConfigManager().save();
        }
        try { super.keyPressed(chr, keyCode); } catch (Exception ignored) {}
    }
}
