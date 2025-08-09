package com.moreoutlines.config;

import com.moreoutlines.network.ServerPermissionManager;
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
    
    /**
     * Saves configuration to disk (delegated to ConfigManager).
     */
    private void saveConfig() {
        // Don't save during batch operations
        if (batchMode) {
            return;
        }
        
        // Use a separate class to avoid circular dependency issues during initialization
        try {
            Class.forName("com.moreoutlines.config.ConfigManager")
                .getMethod("saveConfig")
                .invoke(null);
        } catch (Exception e) {
            // Silently ignore if ConfigManager is not available (during early initialization)
        }
    }
    
    /**
     * Gets the effective outlines enabled state, considering both local setting and server permission.
     */
    public boolean isOutlinesEnabled() {
        return outlinesEnabled && ServerPermissionManager.isModAllowed();
    }
    
    public void toggleOutlinesEnabled() {
        outlinesEnabled = !outlinesEnabled;
        saveConfig();
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
        saveConfig();
    }

    /**
     * Generic method to set color for any outline type.
     */
    private void setColor(Map<Identifier, OutlineConfig> selectionMap, Identifier id, int color) {
        OutlineConfig config = selectionMap.get(id);
        if (config != null) {
            config.color = color;
            saveConfig();
        }
    }    /**
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
    
    /**
     * Sets the default color for new outline configurations.
     * @param color The new default color
     */
    public void setDefaultColor(int color) {
        this.defaultColor = color;
        saveConfig();
    }
    
    /**
     * Manually saves the configuration to disk.
     * Use this when making bulk changes or when you want to ensure config is saved.
     */
    public void saveConfigManually() {
        saveConfig();
    }
    
    // Batch operation support
    private boolean batchMode = false;
    
    /**
     * Starts batch mode - configuration changes won't be saved until endBatch() is called.
     * Use this when making multiple configuration changes at once.
     */
    public void startBatch() {
        batchMode = true;
    }
    
    /**
     * Ends batch mode and saves the configuration.
     */
    public void endBatch() {
        batchMode = false;
        saveConfig();
    }
    
    /**
     * Checks if an identifier has any configuration entry (enabled or disabled).
     * This helps determine if we should override default entity glow behavior.
     */
    public boolean hasItemConfig(net.minecraft.util.Identifier itemId) {
        return selectedItems.containsKey(itemId);
    }
    
    /**
     * Checks if an identifier has any configuration entry (enabled or disabled).
     * This helps determine if we should override default entity glow behavior.
     */
    public boolean hasEntityConfig(net.minecraft.util.Identifier entityId) {
        return selectedEntities.containsKey(entityId);
    }
    
    /**
     * Checks if an identifier has any configuration entry (enabled or disabled).
     * This helps determine if we should override default entity glow behavior.
     */
    public boolean hasBlockConfig(net.minecraft.util.Identifier blockId) {
        return selectedBlocks.containsKey(blockId);
    }
}