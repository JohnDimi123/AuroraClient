package dev.aurora.client.render.font;

import java.awt.Font;

/**
 * Lazily creates and caches {@link FontRenderer}s at common sizes so the atlas
 * rasterisation cost is paid once per size.
 */
public final class FontManager {

    private FontRenderer regular;
    private FontRenderer medium;
    private FontRenderer large;
    private FontRenderer title;

    private final String family;

    public FontManager() {
        // Prefer a clean sans family; fall back to logical SansSerif.
        Font probe = new Font("Inter", Font.PLAIN, 12);
        this.family = "Inter".equalsIgnoreCase(probe.getFamily()) ? "Inter" : "SansSerif";
    }

    public FontRenderer regular() {
        if (regular == null) regular = make(16);
        return regular;
    }

    public FontRenderer medium() {
        if (medium == null) medium = make(18);
        return medium;
    }

    public FontRenderer large() {
        if (large == null) large = make(22);
        return large;
    }

    public FontRenderer title() {
        if (title == null) title = make(28);
        return title;
    }

    private FontRenderer make(int size) {
        return new FontRenderer(new Font(family, Font.PLAIN, size));
    }
}
