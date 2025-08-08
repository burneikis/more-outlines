# Persistent Configuration System

The More Outlines mod now includes a comprehensive persistent configuration system that automatically saves and loads your outline preferences.

## How It Works

### Automatic Configuration Management

The configuration system automatically:
- **Loads** your saved settings when the mod starts
- **Saves** changes immediately when you modify any settings
- **Creates** a default configuration file if none exists
- **Backs up** corrupted configuration files and recreates them

### Configuration File Location

Your configuration is saved as a JSON file in your Minecraft config directory:
```
<minecraft-directory>/config/more-outlines-config.json
```

### Configuration Structure

The configuration file contains:
- `outlinesEnabled`: Global toggle for all outlines (boolean)
- `defaultColor`: Default color for new outline selections (integer)
- `selectedItems`: Map of item IDs to their outline configurations
- `selectedEntities`: Map of entity IDs to their outline configurations  
- `selectedBlocks`: Map of block IDs to their outline configurations

Each selection contains:
- `enabled`: Whether the outline is currently enabled (boolean)
- `color`: The outline color as an integer value (integer)

### Example Configuration

```json
{
  "outlinesEnabled": true,
  "defaultColor": -1,
  "selectedItems": {
    "minecraft:diamond": {
      "enabled": true,
      "color": -16711936
    },
    "minecraft:gold_ingot": {
      "enabled": true, 
      "color": -256
    }
  },
  "selectedEntities": {
    "minecraft:cow": {
      "enabled": true,
      "color": -6746368
    }
  },
  "selectedBlocks": {
    "minecraft:diamond_ore": {
      "enabled": true,
      "color": -16711681
    }
  }
}
```

## Features

### Automatic Persistence
- All changes are automatically saved when you toggle outlines or change colors
- No manual save button required
- Settings persist across game restarts

### Error Handling
- Corrupted configuration files are automatically backed up and replaced with defaults
- Missing configuration files are created with sensible defaults
- JSON parsing errors are logged and handled gracefully

### Configuration Validation
- Configuration integrity is checked on load
- Invalid entries are logged and filtered out
- Color values are validated for visibility

### Debug Utilities
The mod includes several utility functions for debugging and configuration management:

- `ConfigUtil.logConfigStats()` - Prints configuration statistics to the log
- `ConfigUtil.validateConfig()` - Validates configuration and reports issues  
- `ConfigUtil.exportConfigToString()` - Exports configuration as formatted JSON string
- `ConfigUtil.reloadConfig()` - Manually reloads configuration from disk

## Migration from Previous Versions

If you're upgrading from a version without persistent configuration:
1. Your first run will create a new configuration file with default settings
2. Any outline selections you make will be automatically saved
3. The configuration file will be created in your config directory

## Troubleshooting

### Configuration Not Saving
- Check that Minecraft has write permissions to the config directory
- Look for error messages in the latest.log file
- Try manually deleting the config file to recreate defaults

### Corrupted Configuration
- The mod automatically creates backups of corrupted files
- Backup files are saved as `more-outlines-config.json.backup`
- You can manually restore from backup if needed

### Reset to Defaults
To reset your configuration:
1. Close Minecraft
2. Delete `more-outlines-config.json` from your config directory
3. Restart Minecraft - a new default configuration will be created

## Technical Implementation

### Components

1. **ConfigManager**: Handles file I/O and JSON serialization
2. **ModConfig**: Core configuration class with auto-save functionality  
3. **ConfigUtil**: Utility functions for configuration management

### Serialization
- Uses Gson for JSON serialization/deserialization
- Custom type adapter for Minecraft Identifier objects
- Pretty-printed JSON for human readability

### Thread Safety
- Configuration operations are designed to be called from the main thread
- File I/O operations include proper error handling
- Reflection is used to avoid circular dependencies during initialization
