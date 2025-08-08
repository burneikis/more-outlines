# More Outlines

A comprehensive Minecraft Fabric mod for Minecraft 1.21.8 that implements advanced outline rendering for various game objects including items, entities, block entities, and blocks.

## Features (Planned)

- **Item Outlines**: Render outlines around dropped items
- **Entity Outlines**: Outline entities
- **Block Entity Outlines**: Outline block entities (chests, shulkers, beds)
- **Block Outlines**: Regular block outlining

## Implementation Plan

0. **GUI and Binds**
   - Basic In-game configuration GUI (rshift key)
   - Keybind system for toggling outline types

1. **Items**
   - Item outlines

2. **Entities**
   - Living entity outlines
   - Non-living entity outlines

3. **Block Entities**
   - Block entity outlines

4. **Diamond Blocks**
   - Specific diamond block outlining (we will expand this to selected blocks from gui later)

5. **Specific Item, Entity, Block Selection**
   - Specific item, entity, block selection for outlines

6. **Color Choice for Specific Things**
   - Custom color assignment for each outline type or each entity, item or block

## Technical Requirements

### Dependencies
- Fabric API 0.131.0+1.21.8
- Minecraft 1.21.8
- Java 21+
- Fabric Loader 0.16.14

## Compatibility Notes
- Designed to be compatible with other rendering mods
- Shader mod compatibility (OptiFine, Iris, etc.)
- Modpack integration considerations
