package dev.aurora.client.module.impl.hud;

import dev.aurora.client.Aurora;
import dev.aurora.client.gui.theme.Theme;
import dev.aurora.client.render.Render2D;
import net.minecraft.item.ItemStack;

/**
 * Displays the player's four armour pieces with durability bars.
 * Read from the player inventory; purely informational.
 */
public final class ArmorStatusHud extends HudModule {

    public ArmorStatusHud() { super("Armor Status", "Equipped armour + durability", 4, 180); }

    @Override protected void draw(float partialTicks) {
        if (mc.player == null) { setSize(0, 0); return; }
        Theme theme = Aurora.INSTANCE.getThemeManager().getActive();
        ItemStack[] armor = mc.player.inventory.armor;
        int slotSize = 18;
        setSize(slotSize, slotSize * 4 + 6);

        for (int i = 0; i < armor.length; i++) {
            float y = i * (slotSize + 2);
            ItemStack stack = armor[armor.length - 1 - i];
            int bg = stack != null ? theme.accent() : theme.surface();
            Render2D.roundedRect(0, y, slotSize, slotSize, 2, bg);
            if (stack != null) {
                int maxDmg = stack.getMaxDamage();
                if (maxDmg > 0) {
                    float pct = 1f - (float) stack.getDamage() / maxDmg;
                    Render2D.roundedRect(1, y + slotSize - 4, (slotSize - 2) * pct, 2, 1,
                            pct > 0.5f ? theme.positive() : theme.negative());
                }
            }
        }
    }
}
