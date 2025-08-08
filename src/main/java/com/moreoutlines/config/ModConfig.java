package com.moreoutlines.config;

import net.minecraft.util.Identifier;
import java.util.HashMap;
import java.util.Map;

public class ModConfig {
    public static final ModConfig INSTANCE = new ModConfig();
    
    public boolean outlinesEnabled = false;
    public boolean itemOutlines = false;
    public boolean entityOutlines = false;
    public boolean blockEntityOutlines = false;
    public boolean blockOutlines = false;
    public boolean diamondBlockOutlines = false;
    
    public float outlineWidth = 2.0f;
    public int itemOutlineColor = 0xFFFFFFFF;
    public int entityOutlineColor = 0xFFFFFFFF;
    public int blockOutlineColor = 0xFFFFFFFF;

    // Specific item, entity, block selections
    public final Map<Identifier, ItemOutlineConfig> selectedItems = new HashMap<>();
    public final Map<Identifier, EntityOutlineConfig> selectedEntities = new HashMap<>();
    public final Map<Identifier, BlockOutlineConfig> selectedBlocks = new HashMap<>();
    
    public static class ItemOutlineConfig {
        public boolean enabled;
        public int color;
        
        public ItemOutlineConfig(boolean enabled, int color) {
            this.enabled = enabled;
            this.color = color;
        }
    }
    
    public static class EntityOutlineConfig {
        public boolean enabled;
        public int color;
        
        public EntityOutlineConfig(boolean enabled, int color) {
            this.enabled = enabled;
            this.color = color;
        }
    }
    
    public static class BlockOutlineConfig {
        public boolean enabled;
        public int color;
        
        public BlockOutlineConfig(boolean enabled, int color) {
            this.enabled = enabled;
            this.color = color;
        }
    }
    
    private ModConfig() {}
    
    public void toggleOutlinesEnabled() {
        outlinesEnabled = !outlinesEnabled;
    }
    
    public void toggleItemOutlines() {
        itemOutlines = !itemOutlines;
    }
    
    public void toggleEntityOutlines() {
        entityOutlines = !entityOutlines;
    }
    
    public void toggleBlockEntityOutlines() {
        blockEntityOutlines = !blockEntityOutlines;
    }
    
    public void toggleBlockOutlines() {
        blockOutlines = !blockOutlines;
    }
    
    public void toggleDiamondBlockOutlines() {
        diamondBlockOutlines = !diamondBlockOutlines;
    }
    
    // Methods for managing specific selections
    public void toggleItemSelection(Identifier itemId, int defaultColor) {
        ItemOutlineConfig config = selectedItems.get(itemId);
        if (config == null) {
            selectedItems.put(itemId, new ItemOutlineConfig(true, defaultColor));
        } else {
            config.enabled = !config.enabled;
        }
    }
    
    public void setItemColor(Identifier itemId, int color) {
        ItemOutlineConfig config = selectedItems.get(itemId);
        if (config != null) {
            config.color = color;
        }
    }
    
    public boolean isItemSelected(Identifier itemId) {
        ItemOutlineConfig config = selectedItems.get(itemId);
        return config != null && config.enabled;
    }
    
    public int getItemColor(Identifier itemId) {
        ItemOutlineConfig config = selectedItems.get(itemId);
        return config != null ? config.color : itemOutlineColor;
    }
    
    public void toggleEntitySelection(Identifier entityId, int defaultColor) {
        EntityOutlineConfig config = selectedEntities.get(entityId);
        if (config == null) {
            selectedEntities.put(entityId, new EntityOutlineConfig(true, defaultColor));
        } else {
            config.enabled = !config.enabled;
        }
    }
    
    public void setEntityColor(Identifier entityId, int color) {
        EntityOutlineConfig config = selectedEntities.get(entityId);
        if (config != null) {
            config.color = color;
        }
    }
    
    public boolean isEntitySelected(Identifier entityId) {
        EntityOutlineConfig config = selectedEntities.get(entityId);
        return config != null && config.enabled;
    }
    
    public int getEntityColor(Identifier entityId) {
        EntityOutlineConfig config = selectedEntities.get(entityId);
        return config != null ? config.color : entityOutlineColor;
    }
    
    public void toggleBlockSelection(Identifier blockId, int defaultColor) {
        BlockOutlineConfig config = selectedBlocks.get(blockId);
        if (config == null) {
            selectedBlocks.put(blockId, new BlockOutlineConfig(true, defaultColor));
        } else {
            config.enabled = !config.enabled;
        }
    }
    
    public void setBlockColor(Identifier blockId, int color) {
        BlockOutlineConfig config = selectedBlocks.get(blockId);
        if (config != null) {
            config.color = color;
        }
    }
    
    public boolean isBlockSelected(Identifier blockId) {
        BlockOutlineConfig config = selectedBlocks.get(blockId);
        return config != null && config.enabled;
    }
    
    public int getBlockColor(Identifier blockId) {
        BlockOutlineConfig config = selectedBlocks.get(blockId);
        return config != null ? config.color : blockOutlineColor;
    }
    
    // Method to add default selections for demonstration
    public void addDefaultSelections() {
        if (selectedItems.isEmpty() && selectedEntities.isEmpty() && selectedBlocks.isEmpty()) {
            // Add some default item selections
            selectedItems.put(Identifier.of("minecraft:diamond_sword"), new ItemOutlineConfig(false, 0xFF00FFFF));
            selectedItems.put(Identifier.of("minecraft:netherite_sword"), new ItemOutlineConfig(false, 0xFFFF0000));
            selectedItems.put(Identifier.of("minecraft:enchanted_book"), new ItemOutlineConfig(false, 0xFFFFFF00));
            
            // Add some default entity selections
            selectedEntities.put(Identifier.of("minecraft:zombie"), new EntityOutlineConfig(false, 0xFF00FF00));
            selectedEntities.put(Identifier.of("minecraft:creeper"), new EntityOutlineConfig(false, 0xFFFF0000));
            selectedEntities.put(Identifier.of("minecraft:skeleton"), new EntityOutlineConfig(false, 0xFFFFFFFF));
            
            // Add some default block selections
            selectedBlocks.put(Identifier.of("minecraft:diamond_ore"), new BlockOutlineConfig(false, 0xFF00FFFF));
            selectedBlocks.put(Identifier.of("minecraft:gold_ore"), new BlockOutlineConfig(false, 0xFFFFD700));
            selectedBlocks.put(Identifier.of("minecraft:iron_ore"), new BlockOutlineConfig(false, 0xFFC0C0C0));
        }
    }
}