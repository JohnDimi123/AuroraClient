package dev.aurora.client.module.impl.hud;

import dev.aurora.client.Aurora;
import dev.aurora.client.gui.theme.Theme;
import dev.aurora.client.render.Render2D;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;

/**
 * Displays the player's four armour pieces with durability. Read from the
 * player inventory; purely informational.
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
            Render2D.roundedRect(0, y, slotSize, slotSize, 2, theme.surface());
            ItemStack stack = armor[armor.length - 1 - i];
            if (stack != null) {
                ItemRenderer ir = mc.getItemRenderer();
                ir.renderGuiItem(stack, 1, (int) y + 1);
            }
        }
    }
}
