package dev.aurora.client.gui.hudeditor;

/**
 * A draggable, resizable HUD element anchored by a top-left position.
 * Concrete HUD modules implement {@link #renderElement} and report their size.
 */
public abstract class HudElement {

    private final String id;
    private double x;
    private double y;
    private float scale = 1.0f;

    protected HudElement(String id, double defaultX, double defaultY) {
        this.id = id;
        this.x = defaultX;
        this.y = defaultY;
    }

    public String getId() { return id; }
    public double getX() { return x; }
    public double getY() { return y; }
    public float getScale() { return scale; }

    public void setPosition(double x, double y) { this.x = x; this.y = y; }
    public void setScale(float scale) { this.scale = Math.max(0.5f, Math.min(3.0f, scale)); }

    /** @return rendered width in pixels at the current scale. */
    public abstract double getWidth();

    /** @return rendered height in pixels at the current scale. */
    public abstract double getHeight();

    /** Draws the element. Called both in-game and inside the HUD editor. */
    public abstract void renderElement();

    /** @return true if the point lies within this element's bounds. */
    public boolean contains(double px, double py) {
        return px >= x && px <= x + getWidth() && py >= y && py <= y + getHeight();
    }
}
