package dev.aurora.client.gui.theme;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the available {@link Theme}s and the currently active one.
 */
public final class ThemeManager {

    private final List<Theme> themes = new ArrayList<>();
    private Theme active;

    public ThemeManager() {
        themes.add(new Theme("Dark",
                0xF21A1B22, 0xFF252631, 0xFF6C7BFF, 0xFF9B6CFF,
                0xFFFFFFFF, 0xFF9A9BAA, 0xFF4CD980, 0xFFFF5C6C));

        themes.add(new Theme("Light",
                0xF2F4F5FA, 0xFFFFFFFF, 0xFF4458F0, 0xFF7A4DF0,
                0xFF1A1B22, 0xFF6A6B78, 0xFF21A35A, 0xFFE03B4C));

        themes.add(new Theme("Competitive",
                0xF20D0F14, 0xFF15181F, 0xFF00E0B0, 0xFF00B0E0,
                0xFFEFFFFB, 0xFF7E8A88, 0xFF00E0B0, 0xFFFF3B5C));

        this.active = themes.get(0);
    }

    public List<Theme> getThemes() { return themes; }
    public Theme getActive() { return active; }

    public void setActive(Theme theme) { this.active = theme; }

    /** Selects a theme by name; no-op if not found. */
    public void setActive(String name) {
        for (Theme t : themes) {
            if (t.getName().equalsIgnoreCase(name)) { active = t; return; }
        }
    }

    /** Cycles to the next theme in the list. */
    public void cycle() {
        int idx = themes.indexOf(active);
        active = themes.get((idx + 1) % themes.size());
    }
}
