package dev.aurora.client.module.impl.performance;

import dev.aurora.client.module.Category;
import dev.aurora.client.module.Module;
import dev.aurora.client.setting.BooleanSetting;
import dev.aurora.client.setting.NumberSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;

/**
 * Distance- and type-based entity render culling.
 * <p>
 * {@code MixinWorldRenderer} calls {@link #shouldRender(Entity, double)} per
 * entity. Entities beyond the cull distance, or dropped items when the
 * "hide items" option is on, are skipped to save draw calls. The local player
 * and players are never culled, preserving PvP fairness and visibility.
 */
public final class EntityCulling extends Module {

    private static EntityCulling active;

    private final NumberSetting maxDistance =
            new NumberSetting("Max Distance", "Cull entities beyond this", 48, 8, 128, 1);
    private final BooleanSetting hideDroppedItems =
            new BooleanSetting("Hide Far Items", "Cull distant dropped items", true);
    private final NumberSetting itemDistance =
            new NumberSetting("Item Distance", "Item cull distance", 24, 4, 64, 1)
                    .visibleWhen(() -> hideDroppedItems.get());

    public EntityCulling() {
        super("Entity Culling", "Skips rendering of distant/irrelevant entities", Category.PERFORMANCE);
        addSettings(maxDistance, hideDroppedItems, itemDistance);
    }

    @Override protected void onEnable() { active = this; }
    @Override protected void onDisable() { if (active == this) active = null; }

    public static EntityCulling getActive() { return active; }

    /**
     * @param entity the entity being considered for rendering
     * @param distanceSq squared distance from the camera
     * @return true if the entity should be rendered
     */
    public boolean shouldRender(Entity entity, double distanceSq) {
        double max = maxDistance.get();
        if (entity instanceof ItemEntity && hideDroppedItems.get()) {
            double id = itemDistance.get();
            return distanceSq <= id * id;
        }
        return distanceSq <= max * max;
    }
}
