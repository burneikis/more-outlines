package com.moreoutlines.config;

import net.minecraft.util.Identifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for the More Outlines mod.
 * Manages outline settings for items, entities, and blocks.
 */
public class ModConfig {
    public static final ModConfig INSTANCE = new ModConfig();
    
    public boolean outlinesEnabled = false;
    
    public int defaultColor = 0xFFFFFFFF; // White by default

    // Specific item, entity, block selections
    public final Map<Identifier, OutlineConfig> selectedItems = new HashMap<>();
    public final Map<Identifier, OutlineConfig> selectedEntities = new HashMap<>();
    public final Map<Identifier, OutlineConfig> selectedBlocks = new HashMap<>();
    
    /**
     * Generic outline configuration class for all outline types.
     */
    public static class OutlineConfig {
        public boolean enabled;
        public int color;
        
        public OutlineConfig(boolean enabled, int color) {
            this.enabled = enabled;
            this.color = color;
        }
    }
    
    private ModConfig() {}
    
    public void toggleOutlinesEnabled() {
        outlinesEnabled = !outlinesEnabled;
    }
    
    /**
     * Generic method to toggle selection for any outline type.
     */
    private void toggleSelection(Map<Identifier, OutlineConfig> selectionMap, Identifier id, int defaultColor) {
        OutlineConfig config = selectionMap.get(id);
        if (config == null) {
            selectionMap.put(id, new OutlineConfig(true, defaultColor));
        } else {
            config.enabled = !config.enabled;
        }
    }
    
    /**
     * Generic method to set color for any outline type.
     */
    private void setColor(Map<Identifier, OutlineConfig> selectionMap, Identifier id, int color) {
        OutlineConfig config = selectionMap.get(id);
        if (config != null) {
            config.color = color;
        }
    }
    
    /**
     * Generic method to check if an item is selected.
     */
    private boolean isSelected(Map<Identifier, OutlineConfig> selectionMap, Identifier id) {
        OutlineConfig config = selectionMap.get(id);
        return config != null && config.enabled;
    }
    
    /**
     * Generic method to get color for any outline type.
     */
    private int getColor(Map<Identifier, OutlineConfig> selectionMap, Identifier id) {
        OutlineConfig config = selectionMap.get(id);
        return config != null ? config.color : defaultColor;
    }
    
    // Methods for managing specific selections
    public void toggleItemSelection(Identifier itemId, int defaultColor) {
        toggleSelection(selectedItems, itemId, defaultColor);
    }
    
    public void setItemColor(Identifier itemId, int color) {
        setColor(selectedItems, itemId, color);
    }
    
    public boolean isItemSelected(Identifier itemId) {
        return isSelected(selectedItems, itemId);
    }
    
    public int getItemColor(Identifier itemId) {
        return getColor(selectedItems, itemId);
    }
    
    public void toggleEntitySelection(Identifier entityId, int defaultColor) {
        toggleSelection(selectedEntities, entityId, defaultColor);
    }
    
    public void setEntityColor(Identifier entityId, int color) {
        setColor(selectedEntities, entityId, color);
    }
    
    public boolean isEntitySelected(Identifier entityId) {
        return isSelected(selectedEntities, entityId);
    }
    
    public int getEntityColor(Identifier entityId) {
        return getColor(selectedEntities, entityId);
    }
    
    public void toggleBlockSelection(Identifier blockId, int defaultColor) {
        toggleSelection(selectedBlocks, blockId, defaultColor);
    }
    
    public void setBlockColor(Identifier blockId, int color) {
        setColor(selectedBlocks, blockId, color);
    }
    
    public boolean isBlockSelected(Identifier blockId) {
        return isSelected(selectedBlocks, blockId);
    }
    
    public int getBlockColor(Identifier blockId) {
        return getColor(selectedBlocks, blockId);
    }
}