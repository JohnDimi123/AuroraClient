package dev.aurora.client.module.impl.hud;

import dev.aurora.client.Aurora;
import dev.aurora.client.event.Listen;
import dev.aurora.client.event.impl.RenderHudEvent;
import dev.aurora.client.gui.hudeditor.HudElement;
import dev.aurora.client.module.Category;
import dev.aurora.client.module.Module;
import dev.aurora.client.render.font.FontRenderer;
import org.lwjgl.opengl.GL11;

/**
 * Base class for on-screen HUD modules.
 * <p>
 * Each HUD module owns an internal {@link HudElement} so its position/scale are
 * editable in the HUD editor and persisted by the config system. Subclasses
 * implement {@link #draw(float)} and report {@link #width()}/{@link #height()}.
 */
public abstract class HudModule extends Module {

    private final HudElement element;
    private double width;
    private double height;

    protected HudModule(String name, String description, double defX, double defY) {
        super(name, description, Category.HUD);
        this.element = new HudElement(name.toLowerCase().replace(' ', '_'), defX, defY) {
            @Override public double getWidth() { return width * getScale(); }
            @Override public double getHeight() { return height * getScale(); }
            @Override public void renderElement() { drawScaled(); }
        };
    }

    @Override protected void onEnable() {
        if (!Aurora.INSTANCE.getHudManager().getElements().contains(element)) {
            Aurora.INSTANCE.getHudManager().register(element);
        }
    }

    /** Convenience accessor for the shared regular font. */
    protected FontRenderer font() { return Aurora.INSTANCE.getFontManager().regular(); }

    protected HudElement element() { return element; }

    /** Subclasses set their measured size each frame before drawing. */
    protected final void setSize(double w, double h) { this.width = w; this.height = h; }

    /** Draws content with the element's top-left at the GL origin (0,0). */
    protected abstract void draw(float partialTicks);

    private float lastPartial;

    @Listen
    public void onRenderHud(RenderHudEvent event) {
        // The HUD editor renders elements itself; avoid double-draw when open.
        if (mc.currentScreen instanceof dev.aurora.client.gui.hudeditor.HudEditorScreen) return;
        lastPartial = event.partialTicks;
        drawScaled();
    }

    private void drawScaled() {
        GL11.glPushMatrix();
        GL11.glTranslated(element.getX(), element.getY(), 0);
        GL11.glScalef(element.getScale(), element.getScale(), 1f);
        draw(lastPartial);
        GL11.glPopMatrix();
    }
}
