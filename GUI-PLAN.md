# Minecraft Outline Mod GUI Structure

## Main Window Layout (480x300 pixels)

### Header Section (480x40)
- **Title**: "More Outlines Menu" (centered)
- **Close Button**: Standard "Close"

### Tabbed Interface (480x260)

#### Tab 1: "Items" (160x20 tab header)
- **Search Bar** (460x20): Filter items by name/ID
- **Item Grid** (230x200): 
  - scrollable grid showing item icons
  - Each slot shows item icon + indication of if its enabled
  - Click to select item
- **Selected Item Panel** (230x200):
  - Large item icon display
  - Item name/ID
  - **Enable Outline Toggle**:
  - **Color Picker**: 2 rows of 3 and 1 row of 2 colors
  

#### Tab 2: "Entities" (160x20 tab header)
- **Search Bar** (460x20): Filter entities by name
- **Entity List** (230x200):
  - Scrollable list with entity names + small preview icons
  - indicator for enabled outlines
  - Categories: Passive Mobs, Hostile Mobs, NPCs, Players
- **Selected Entity Panel** (230x200):
  - Entity name and icon
  - **Enable Outline Toggle**:
  - **Color Picker**: Same as items tab

#### Tab 3: "Blocks" (160x20 tab header)
- **Search Bar** (460x20): Filter blocks
- **Block Grid** (120x200): Shows blocks from selected category
- **Selected Block Panel** (230x200):
  - Block icon and name
  - **Enable Outline Toggle**
  - **Color Picker**: Same as other tabs

## Additional Features

### Color Picker Details
**Preset Colors** (2 rows of 3 and 1 row of 2 colors):
- Red (#FF0000), Green (#00FF00), Blue (#0000FF), 
- Magenta (#FF00FF), Cyan (#00FFFF), Yellow (#FFFF00),
- White (#FFFFFF), Black (#000000)

**Custom Color**: RGB sliders (Red: 0-255, Green: 0-255, Blue: 0-255)

### Search Functionality
- **Filters**: Show only enabled, show only disabled
- **Tags**: Support for mod compatibility (search by mod name)

### Data Management
**Config Structure**:
```json
{
  "items": {
    "minecraft:diamond_sword": {
      "enabled": true,
      "color": "#FF0000"
    }
  },
  "entities": {
    "minecraft:zombie": {
      "enabled": true,
      "color": "#00FF00",
    }
  },
  "blocks": {
    "minecraft:diamond_ore": {
      "enabled": true,
      "color": "#00FFFF",
    }
  }
}
```

### FUTURE MAYBE
- **Visual Feedback**: Preview outlines in real-time while configuring