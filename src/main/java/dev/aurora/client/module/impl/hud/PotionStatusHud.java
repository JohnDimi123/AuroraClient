package dev.aurora.client.module.impl.hud;

/**
 * Lists active potion effects with remaining duration.
 * Effect API names vary by yarn build; rendering disabled until verified.
 */
public final class PotionStatusHud extends HudModule {

    public PotionStatusHud() { super("Potion Status", "Active potion effects", 200, 4); }

    @Override protected void draw(float partialTicks) {
        setSize(0, 0);
    }
}
