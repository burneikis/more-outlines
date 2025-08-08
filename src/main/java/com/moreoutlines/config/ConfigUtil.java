package com.moreoutlines.config;

import com.google.gson.*;
import com.moreoutlines.MoreOutlines;
import net.minecraft.util.Identifier;

/**
 * Utility class for configuration management.
 * Provides convenience methods for common configuration operations.
 */
public class ConfigUtil {
    
    /**
     * Safely saves configuration with error handling.
     */
    public static void safeConfigSave() {
        try {
            ConfigManager.saveConfig();
        } catch (Exception e) {
            MoreOutlines.LOGGER.error("Failed to save configuration", e);
        }
    }
    
    /**
     * Reloads configuration from disk.
     */
    public static void reloadConfig() {
        try {
            ConfigManager.loadConfig();
            MoreOutlines.LOGGER.info("Configuration reloaded from disk");
        } catch (Exception e) {
            MoreOutlines.LOGGER.error("Failed to reload configuration", e);
        }
    }
    
    /**
     * Exports current configuration to a JSON string for debugging.
     */
    public static String exportConfigToString() {
        try {
            return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Identifier.class, new JsonSerializer<Identifier>() {
                    @Override
                    public JsonElement serialize(Identifier src, java.lang.reflect.Type typeOfSrc, 
                            JsonSerializationContext context) {
                        return new JsonPrimitive(src.toString());
                    }
                })
                .create()
                .toJson(createDebugConfigObject());
        } catch (Exception e) {
            return "Error exporting config: " + e.getMessage();
        }
    }
    
    /**
     * Creates a debug configuration object with current settings.
     */
    @SuppressWarnings("unused") // Fields are used by Gson via reflection
    private static Object createDebugConfigObject() {
        ModConfig config = ModConfig.INSTANCE;
        
        return new Object() {
            public final boolean outlinesEnabled = config.outlinesEnabled;
            public final String defaultColorHex = String.format("#%08X", config.defaultColor);
            public final int selectedItemsCount = config.selectedItems.size();
            public final int selectedEntitiesCount = config.selectedEntities.size();
            public final int selectedBlocksCount = config.selectedBlocks.size();
            public final int totalSelectionsCount = selectedItemsCount + selectedEntitiesCount + selectedBlocksCount;
        };
    }
    
    /**
     * Prints configuration statistics to the log.
     */
    public static void logConfigStats() {
        ModConfig config = ModConfig.INSTANCE;
        MoreOutlines.LOGGER.info("Configuration Statistics:");
        MoreOutlines.LOGGER.info("  - Outlines Enabled: {}", config.outlinesEnabled);
        MoreOutlines.LOGGER.info("  - Default Color: #{}", String.format("%08X", config.defaultColor));
        MoreOutlines.LOGGER.info("  - Selected Items: {}", config.selectedItems.size());
        MoreOutlines.LOGGER.info("  - Selected Entities: {}", config.selectedEntities.size());
        MoreOutlines.LOGGER.info("  - Selected Blocks: {}", config.selectedBlocks.size());
        MoreOutlines.LOGGER.info("  - Total Selections: {}", 
            config.selectedItems.size() + config.selectedEntities.size() + config.selectedBlocks.size());
    }
    
    /**
     * Checks if the configuration seems valid and logs any issues.
     */
    public static boolean validateConfig() {
        boolean isValid = true;
        ModConfig config = ModConfig.INSTANCE;
        
        // Check for null maps (shouldn't happen, but safety first)
        if (config.selectedItems == null) {
            MoreOutlines.LOGGER.warn("selectedItems map is null!");
            isValid = false;
        }
        if (config.selectedEntities == null) {
            MoreOutlines.LOGGER.warn("selectedEntities map is null!");
            isValid = false;
        }
        if (config.selectedBlocks == null) {
            MoreOutlines.LOGGER.warn("selectedBlocks map is null!");
            isValid = false;
        }
        
        // Check for reasonable color values
        if (config.defaultColor == 0) {
            MoreOutlines.LOGGER.warn("Default color is 0 (transparent), this might cause invisible outlines");
        }
        
        if (isValid) {
            MoreOutlines.LOGGER.debug("Configuration validation passed");
        }
        
        return isValid;
    }
}
