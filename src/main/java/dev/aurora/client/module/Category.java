package dev.aurora.client.module;

/** Logical grouping for modules, used by the Click GUI category tabs. */
public enum Category {
    PERFORMANCE("Performance"),
    RENDER("Render"),
    HUD("HUD"),
    PLAYER("Player"),
    BEDWARS("BedWars"),
    UTILITY("Utility");

    private final String display;
    Category(String display) { this.display = display; }
    public String getDisplay() { return display; }
}
