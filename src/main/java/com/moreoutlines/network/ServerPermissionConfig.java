package com.moreoutlines.network;

import com.moreoutlines.MoreOutlines;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ServerPermissionConfig {
    private static final String CONFIG_FILE = "config/more-outlines-server.json";
    private static boolean modAllowed = true; // Default: allow mod usage
    
    public static class Config {
        public boolean allowMoreOutlinesMod = true;
        public String reason = "Server allows More Outlines mod";
    }
    
    private static Config config = new Config();

    public static void loadConfig() {
        File configFile = new File(CONFIG_FILE);
        
        if (!configFile.exists()) {
            // Create default config
            saveConfig();
            MoreOutlines.LOGGER.info("Created default server permission config");
            return;
        }

        try (FileReader reader = new FileReader(configFile)) {
            Gson gson = new Gson();
            Config loadedConfig = gson.fromJson(reader, Config.class);
            if (loadedConfig != null) {
                config = loadedConfig;
                modAllowed = config.allowMoreOutlinesMod;
                MoreOutlines.LOGGER.info("Loaded server permission config: mod {} ({})", 
                    modAllowed ? "allowed" : "blocked", config.reason);
            }
        } catch (IOException | JsonSyntaxException e) {
            MoreOutlines.LOGGER.error("Failed to load server permission config: {}", e.getMessage());
        }
    }

    public static void saveConfig() {
        File configDir = new File("config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(config, writer);
        } catch (IOException e) {
            MoreOutlines.LOGGER.error("Failed to save server permission config: {}", e.getMessage());
        }
    }

    public static boolean isModAllowed() {
        return modAllowed;
    }

    public static void setModAllowed(boolean allowed, String reason) {
        config.allowMoreOutlinesMod = allowed;
        config.reason = reason != null ? reason : (allowed ? "Server allows More Outlines mod" : "Server blocks More Outlines mod");
        modAllowed = allowed;
        saveConfig();
    }

    public static String getReason() {
        return config.reason;
    }
}