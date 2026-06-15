package dev.aurora.client.gui.theme;

/**
 * An immutable colour palette for the GUI and HUD. Colours are packed ARGB.
 */
public final class Theme {

    private final String name;
    private final int background;
    private final int surface;
    private final int accent;
    private final int accentSecondary;
    private final int text;
    private final int textMuted;
    private final int positive;
    private final int negative;

    public Theme(String name, int background, int surface, int accent, int accentSecondary,
                 int text, int textMuted, int positive, int negative) {
        this.name = name;
        this.background = background;
        this.surface = surface;
        this.accent = accent;
        this.accentSecondary = accentSecondary;
        this.text = text;
        this.textMuted = textMuted;
        this.positive = positive;
        this.negative = negative;
    }

    public String getName() { return name; }
    public int background() { return background; }
    public int surface() { return surface; }
    public int accent() { return accent; }
    public int accentSecondary() { return accentSecondary; }
    public int text() { return text; }
    public int textMuted() { return textMuted; }
    public int positive() { return positive; }
    public int negative() { return negative; }
}
