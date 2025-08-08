package com.moreoutlines.config;

import com.google.gson.*;
import com.moreoutlines.MoreOutlines;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages persistent configuration for the More Outlines mod.
 * Handles saving and loading configuration to/from JSON files.
 */
public class ConfigManager {
    private static final String CONFIG_FILE_NAME = "more-outlines-config.json";
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve(CONFIG_FILE_NAME);
    
    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(Identifier.class, new IdentifierTypeAdapter())
        .create();

    /**
     * Loads configuration from file or creates default if file doesn't exist.
     */
    public static void loadConfig() {
        try {
            if (Files.exists(CONFIG_FILE)) {
                MoreOutlines.LOGGER.info("Loading configuration from: {}", CONFIG_FILE);
                
                try (FileReader reader = new FileReader(CONFIG_FILE.toFile())) {
                    ConfigData configData = GSON.fromJson(reader, ConfigData.class);
                    
                    if (configData != null) {
                        applyConfigData(configData);
                        MoreOutlines.LOGGER.info("Configuration loaded successfully!");
                    } else {
                        MoreOutlines.LOGGER.warn("Config file is empty or invalid, using defaults");
                        saveConfig(); // Save default config
                    }
                }
            } else {
                MoreOutlines.LOGGER.info("No configuration file found, creating default configuration");
                saveConfig(); // Create default config file
            }
        } catch (IOException e) {
            MoreOutlines.LOGGER.error("Failed to load configuration", e);
            // Continue with default configuration
        } catch (JsonSyntaxException e) {
            MoreOutlines.LOGGER.error("Configuration file has invalid JSON syntax", e);
            // Create backup and use default
            createBackupAndReset();
        }
    }

    /**
     * Saves current configuration to file.
     */
    public static void saveConfig() {
        try {
            // Ensure config directory exists
            Files.createDirectories(CONFIG_DIR);
            
            ConfigData configData = createConfigData();
            
            try (FileWriter writer = new FileWriter(CONFIG_FILE.toFile())) {
                GSON.toJson(configData, writer);
                MoreOutlines.LOGGER.debug("Configuration saved to: {}", CONFIG_FILE);
            }
        } catch (IOException e) {
            MoreOutlines.LOGGER.error("Failed to save configuration", e);
        }
    }

    /**
     * Creates a ConfigData object from the current ModConfig state.
     */
    private static ConfigData createConfigData() {
        ModConfig config = ModConfig.INSTANCE;
        
        ConfigData data = new ConfigData();
        data.outlinesEnabled = config.outlinesEnabled;
        data.defaultColor = config.defaultColor;
        
        // Convert maps to serializable format
        data.selectedItems = new HashMap<>(config.selectedItems);
        data.selectedEntities = new HashMap<>(config.selectedEntities);
        data.selectedBlocks = new HashMap<>(config.selectedBlocks);
        
        return data;
    }

    /**
     * Applies loaded ConfigData to the ModConfig instance.
     */
    private static void applyConfigData(ConfigData data) {
        ModConfig config = ModConfig.INSTANCE;
        
        config.outlinesEnabled = data.outlinesEnabled;
        config.defaultColor = data.defaultColor;
        
        // Clear and repopulate maps
        config.selectedItems.clear();
        config.selectedEntities.clear();
        config.selectedBlocks.clear();
        
        if (data.selectedItems != null) {
            config.selectedItems.putAll(data.selectedItems);
        }
        if (data.selectedEntities != null) {
            config.selectedEntities.putAll(data.selectedEntities);
        }
        if (data.selectedBlocks != null) {
            config.selectedBlocks.putAll(data.selectedBlocks);
        }
    }

    /**
     * Creates a backup of corrupted config and resets to defaults.
     */
    private static void createBackupAndReset() {
        try {
            Path backupPath = CONFIG_DIR.resolve(CONFIG_FILE_NAME + ".backup");
            Files.move(CONFIG_FILE, backupPath);
            MoreOutlines.LOGGER.info("Corrupted config backed up to: {}", backupPath);
            saveConfig(); // Create new default config
        } catch (IOException e) {
            MoreOutlines.LOGGER.error("Failed to create backup of corrupted config", e);
        }
    }

    /**
     * Configuration data structure for JSON serialization.
     */
    private static class ConfigData {
        public boolean outlinesEnabled = false;
        public int defaultColor = 0xFFFFFFFF;
        public Map<Identifier, ModConfig.OutlineConfig> selectedItems = new HashMap<>();
        public Map<Identifier, ModConfig.OutlineConfig> selectedEntities = new HashMap<>();
        public Map<Identifier, ModConfig.OutlineConfig> selectedBlocks = new HashMap<>();
    }

    /**
     * Custom Gson type adapter for Minecraft Identifier serialization.
     */
    private static class IdentifierTypeAdapter implements JsonSerializer<Identifier>, JsonDeserializer<Identifier> {
        @Override
        public JsonElement serialize(Identifier src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }

        @Override
        public Identifier deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) 
                throws JsonParseException {
            try {
                return Identifier.of(json.getAsString());
            } catch (Exception e) {
                throw new JsonParseException("Invalid identifier: " + json.getAsString(), e);
            }
        }
    }
}
