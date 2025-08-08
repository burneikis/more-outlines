package com.moreoutlines.util;

import com.moreoutlines.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * Utility class for managing entity glow states and outline behavior.
 * Helps ensure proper cleanup when entities are toggled off.
 */
public class EntityGlowUtil {
    
    /**
     * Checks if an entity should be forced to glow based on mod configuration.
     * @param entity The entity to check
     * @return true if the entity should glow, false if it should not glow, null if no override needed
     */
    public static Boolean shouldEntityGlow(Entity entity) {
        if (!ModConfig.INSTANCE.outlinesEnabled) {
            // If outlines are globally disabled, check if this entity has config and force false
            if (hasEntityConfiguration(entity)) {
                return false;
            }
            return null; // No override needed
        }
        
        // Handle item entities
        if (entity instanceof ItemEntity itemEntity) {
            Identifier itemId = Registries.ITEM.getId(itemEntity.getStack().getItem());
            if (ModConfig.INSTANCE.hasItemConfig(itemId)) {
                return ModConfig.INSTANCE.isItemSelected(itemId);
            }
        }
        
        // Handle other entities
        Identifier entityId = Registries.ENTITY_TYPE.getId(entity.getType());
        if (ModConfig.INSTANCE.hasEntityConfig(entityId)) {
            return ModConfig.INSTANCE.isEntitySelected(entityId);
        }
        
        return null; // No override needed
    }
    
    /**
     * Checks if an entity has any configuration entry (for determining override behavior).
     */
    public static boolean hasEntityConfiguration(Entity entity) {
        if (entity instanceof ItemEntity itemEntity) {
            Identifier itemId = Registries.ITEM.getId(itemEntity.getStack().getItem());
            return ModConfig.INSTANCE.hasItemConfig(itemId);
        }
        
        Identifier entityId = Registries.ENTITY_TYPE.getId(entity.getType());
        return ModConfig.INSTANCE.hasEntityConfig(entityId);
    }
    
    /**
     * Forces refresh of entity glow states for all entities in the world.
     * Useful after configuration changes that might affect many entities.
     */
    public static void refreshAllEntityGlowStates() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) {
            return;
        }
        
        // Note: We can't directly modify entity glow states from here, but this method
        // can be called to trigger re-evaluation on the next render cycle
        // The EntityMixin will handle the actual state changes during rendering
    }
    
    /**
     * Gets the expected outline color for an entity, or -1 if no outline should be applied.
     */
    public static int getEntityOutlineColor(Entity entity) {
        if (!ModConfig.INSTANCE.outlinesEnabled) {
            return -1;
        }
        
        // Handle item entities
        if (entity instanceof ItemEntity itemEntity) {
            Identifier itemId = Registries.ITEM.getId(itemEntity.getStack().getItem());
            if (ModConfig.INSTANCE.isItemSelected(itemId)) {
                return ModConfig.INSTANCE.getItemColor(itemId);
            }
        }
        
        // Handle other entities
        Identifier entityId = Registries.ENTITY_TYPE.getId(entity.getType());
        if (ModConfig.INSTANCE.isEntitySelected(entityId)) {
            return ModConfig.INSTANCE.getEntityColor(entityId);
        }
        
        return -1;
    }
    
    /**
     * Debug method to log entity glow state information.
     */
    public static void debugEntityGlowState(Entity entity) {
        Boolean shouldGlow = shouldEntityGlow(entity);
        boolean actuallyGlowing = entity.isGlowing();
        int expectedColor = getEntityOutlineColor(entity);
        
        com.moreoutlines.MoreOutlines.LOGGER.debug(
            "Entity {} ({}): shouldGlow={}, actuallyGlowing={}, expectedColor={}",
            entity.getDisplayName().getString(),
            Registries.ENTITY_TYPE.getId(entity.getType()),
            shouldGlow,
            actuallyGlowing,
            expectedColor == -1 ? "none" : String.format("#%08X", expectedColor)
        );
    }
}
