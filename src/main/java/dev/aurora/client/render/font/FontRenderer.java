package dev.aurora.client.render.font;

import org.lwjgl.opengl.GL11;

import org.lwjgl.BufferUtils;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

/**
 * A lightweight texture-atlas font renderer.
 * <p>
 * On construction it rasterises the printable ASCII range into a single
 * {@link BufferedImage}, uploads it as a {@link DynamicTexture}, and renders
 * glyphs as textured quads. This avoids per-frame Java2D work and gives crisp,
 * theme-consistent text independent of the vanilla bitmap font.
 */
public final class FontRenderer {

    private static final int FIRST_CHAR = 32;
    private static final int LAST_CHAR = 255;
    private static final int PADDING = 2;

    private final int glTextureId;
    private final int textureWidth;
    private final int textureHeight;
    private final int[] charX = new int[LAST_CHAR + 1];
    private final int[] charY = new int[LAST_CHAR + 1];
    private final int[] charW = new int[LAST_CHAR + 1];
    private final int charHeight;

    public FontRenderer(Font font) {
        // Measure glyphs on a throwaway image to compute atlas layout.
        BufferedImage measure = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D mg = measure.createGraphics();
        mg.setFont(font);
        FontMetrics fm = mg.getFontMetrics();
        this.charHeight = fm.getHeight();
        mg.dispose();

        int columns = 16;
        int cellW = 0;
        for (int c = FIRST_CHAR; c <= LAST_CHAR; c++) {
            cellW = Math.max(cellW, fm.charWidth((char) c) + PADDING);
        }
        int rows = (LAST_CHAR - FIRST_CHAR + columns) / columns;
        this.textureWidth = nextPow2(cellW * columns);
        this.textureHeight = nextPow2((charHeight + PADDING) * rows);

        BufferedImage atlas = new BufferedImage(textureWidth, textureHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = atlas.createGraphics();
        g.setFont(font);
        g.setColor(Color.WHITE);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        int x = 0, y = 0;
        for (int c = FIRST_CHAR; c <= LAST_CHAR; c++) {
            int w = fm.charWidth((char) c) + PADDING;
            if (x + w > textureWidth) { x = 0; y += charHeight + PADDING; }
            charX[c] = x; charY[c] = y; charW[c] = fm.charWidth((char) c);
            g.drawString(String.valueOf((char) c), x, y + fm.getAscent());
            x += w;
        }
        g.dispose();

        this.glTextureId = uploadAtlas(atlas);
    }

    /** @return pixel width of the string at scale 1. */
    public int getWidth(String text) {
        int w = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c <= LAST_CHAR) w += charW[c] + 1;
        }
        return w;
    }

    public int getHeight() { return charHeight; }

    /** Draws text with a 1px drop shadow for legibility on any background. */
    public void drawWithShadow(String text, float x, float y, int color) {
        draw(text, x + 0.5f, y + 0.5f, darken(color));
        draw(text, x, y, color);
    }

    /** Draws text at the given position with a packed ARGB colour. */
    public void draw(String text, float x, float y, int color) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, glTextureId);

        float a = (color >> 24 & 0xFF) / 255f;
        if (a == 0) a = 1f;
        GL11.glColor4f((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, a);

        float cursor = x;
        GL11.glBegin(GL11.GL_QUADS);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c > LAST_CHAR) continue;
            float u0 = (float) charX[c] / textureWidth;
            float v0 = (float) charY[c] / textureHeight;
            float u1 = (float) (charX[c] + charW[c]) / textureWidth;
            float v1 = (float) (charY[c] + charHeight) / textureHeight;
            float w = charW[c];
            GL11.glTexCoord2f(u0, v0); GL11.glVertex2f(cursor, y);
            GL11.glTexCoord2f(u0, v1); GL11.glVertex2f(cursor, y + charHeight);
            GL11.glTexCoord2f(u1, v1); GL11.glVertex2f(cursor + w, y + charHeight);
            GL11.glTexCoord2f(u1, v0); GL11.glVertex2f(cursor + w, y);
            cursor += w + 1;
        }
        GL11.glEnd();
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glPopMatrix();
    }

    /** Draws text centred horizontally about {@code cx}. */
    public void drawCentered(String text, float cx, float y, int color) {
        drawWithShadow(text, cx - getWidth(text) / 2f, y, color);
    }

    private static int darken(int color) {
        int a = color >> 24 & 0xFF;
        int r = (int) ((color >> 16 & 0xFF) * 0.25f);
        int g = (int) ((color >> 8 & 0xFF) * 0.25f);
        int b = (int) ((color & 0xFF) * 0.25f);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int uploadAtlas(BufferedImage img) {
        int id = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        int w = img.getWidth(), h = img.getHeight();
        int[] pixels = img.getRGB(0, 0, w, h, null, 0, w);
        ByteBuffer buf = BufferUtils.createByteBuffer(w * h * 4);
        for (int px : pixels) {
            buf.put((byte) ((px >> 16) & 0xFF));
            buf.put((byte) ((px >> 8) & 0xFF));
            buf.put((byte) (px & 0xFF));
            buf.put((byte) ((px >> 24) & 0xFF));
        }
        buf.flip();
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, w, h, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
        return id;
    }

    private static int nextPow2(int v) {
        int p = 1;
        while (p < v) p <<= 1;
        return p;
    }
}
