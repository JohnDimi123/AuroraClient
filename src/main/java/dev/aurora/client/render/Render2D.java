package dev.aurora.client.render;

import net.minecraft.client.gui.DrawableHelper;
import org.lwjgl.opengl.GL11;

/**
 * Low-level immediate-mode 2D drawing helpers built on legacy OpenGL.
 * <p>
 * All colours are packed ARGB ints. Methods configure GL state defensively and
 * restore it, so callers can mix these freely with vanilla rendering.
 */
public final class Render2D {

    private Render2D() {}

    /** Unpacks the alpha channel as a 0..1 float. */
    public static float a(int argb) { return (argb >> 24 & 0xFF) / 255f; }
    public static float r(int argb) { return (argb >> 16 & 0xFF) / 255f; }
    public static float g(int argb) { return (argb >> 8 & 0xFF) / 255f; }
    public static float b(int argb) { return (argb & 0xFF) / 255f; }

    /** Linearly interpolates between two ARGB colours. */
    public static int lerpColor(int from, int to, float t) {
        int a = (int) (a(from) * 255 + (a(to) * 255 - a(from) * 255) * t);
        int r = (int) (r(from) * 255 + (r(to) * 255 - r(from) * 255) * t);
        int g = (int) (g(from) * 255 + (g(to) * 255 - g(from) * 255) * t);
        int b = (int) (b(from) * 255 + (b(to) * 255 - b(from) * 255) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /** Filled axis-aligned rectangle. */
    public static void rect(double x, double y, double w, double h, int color) {
        DrawableHelper.fill((int) x, (int) y, (int) (x + w), (int) (y + h), color);
    }

    /** Rounded rectangle approximation using arcs at each corner. */
    public static void roundedRect(double x, double y, double w, double h, double radius, int color) {
        radius = Math.min(radius, Math.min(w, h) / 2);
        prepare();
        setColor(color);
        // Centre cross + four corner fans.
        drawRect(x + radius, y, w - radius * 2, h);
        drawRect(x, y + radius, radius, h - radius * 2);
        drawRect(x + w - radius, y + radius, radius, h - radius * 2);
        arc(x + radius, y + radius, radius, 180, 270);
        arc(x + w - radius, y + radius, radius, 270, 360);
        arc(x + w - radius, y + h - radius, radius, 0, 90);
        arc(x + radius, y + h - radius, radius, 90, 180);
        restore();
    }

    /** 1px-wide outline rectangle. */
    public static void outline(double x, double y, double w, double h, float thickness, int color) {
        rect(x, y, w, thickness, color);
        rect(x, y + h - thickness, w, thickness, color);
        rect(x, y, thickness, h, color);
        rect(x + w - thickness, y, thickness, h, color);
    }

    /** Vertical gradient rectangle. */
    public static void gradientV(double x, double y, double w, double h, int top, int bottom) {
        prepare();
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glBegin(GL11.GL_QUADS);
        color(top);    GL11.glVertex2d(x, y);     GL11.glVertex2d(x + w, y);
        color(bottom); GL11.glVertex2d(x + w, y + h); GL11.glVertex2d(x, y + h);
        GL11.glEnd();
        GL11.glShadeModel(GL11.GL_FLAT);
        restore();
    }

    // ---- internal primitives -----------------------------------------------

    private static void drawRect(double x, double y, double w, double h) {
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2d(x, y); GL11.glVertex2d(x, y + h);
        GL11.glVertex2d(x + w, y + h); GL11.glVertex2d(x + w, y);
        GL11.glEnd();
    }

    private static void arc(double cx, double cy, double r, int start, int end) {
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2d(cx, cy);
        for (int i = start; i <= end; i++) {
            double a = Math.toRadians(i);
            GL11.glVertex2d(cx + Math.cos(a) * r, cy + Math.sin(a) * r);
        }
        GL11.glEnd();
    }

    private static void prepare() {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
    }

    private static void restore() {
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glColor4f(1, 1, 1, 1);
    }

    private static void setColor(int c) { color(c); }
    private static void color(int c) { GL11.glColor4f(r(c), g(c), b(c), a(c)); }
}
