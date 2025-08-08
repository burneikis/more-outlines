# More Outlines

A comprehensive Minecraft Fabric mod for Minecraft 1.21.8 that implements advanced outline rendering for various game objects including items, entities, block entities, and blocks.

## Features (Planned)

- **Item Outlines**: Render outlines around dropped items, items in inventories, and held items
- **Entity Outlines**: Highlight entities with customizable outline colors and thickness
- **Block Entity Outlines**: Outline interactive blocks like chests, furnaces, and other tile entities
- **Block Outlines**: Selective block highlighting with filtering capabilities

## Implementation Plan

### Phase 1: Core Infrastructure
1. **Rendering System Setup**
   - Create outline shader programs for different object types
   - Implement buffer management for outline geometry
   - Set up render pipeline integration with Minecraft's existing rendering

2. **Configuration System**
   - Create config files for outline customization
   - Implement in-game configuration GUI
   - Add keybind system for toggling outline types

3. **Core Classes Structure**
   ```
   com.moreoutlines/
   ├── config/
   │   ├── OutlineConfig.java           # Main configuration handler
   │   └── OutlineSettings.java         # Settings data class
   ├── render/
   │   ├── OutlineRenderer.java         # Main rendering coordinator
   │   ├── ShaderManager.java           # Shader program management
   │   └── BufferManager.java           # Geometry buffer handling
   ├── outline/
   │   ├── ItemOutlineHandler.java      # Item-specific outline logic
   │   ├── EntityOutlineHandler.java    # Entity-specific outline logic
   │   ├── BlockEntityOutlineHandler.java # Block entity outline logic
   │   └── BlockOutlineHandler.java     # Block outline logic
   └── mixins/
       ├── ItemRendererMixin.java       # Hook into item rendering
       ├── EntityRendererMixin.java     # Hook into entity rendering
       ├── BlockEntityRendererMixin.java # Hook into block entity rendering
       └── WorldRendererMixin.java      # Hook into world/block rendering
   ```

### Phase 2: Item Outline Implementation
1. **Dropped Item Outlines**
   - Detect dropped ItemEntity objects in world
   - Render outline geometry around item models
   - Implement distance-based outline fading

2. **Inventory Item Outlines**
   - Hook into GUI item rendering
   - Add outline support for inventory slots
   - Support for highlighting specific item types

3. **Held Item Outlines**
   - First-person and third-person held item outlines
   - Hand-specific outline customization

### Phase 3: Entity Outline Implementation
1. **Living Entity Outlines**
   - Player, mob, and animal outline rendering
   - Health-based outline color coding
   - Team/faction-based outline colors

2. **Non-Living Entity Outlines**
   - Projectile and vehicle outlines
   - Minecart, boat, and other rideable entity support

3. **Entity Filtering System**
   - Whitelist/blacklist entity types
   - Distance-based entity outline culling
   - Performance optimization for large entity counts

### Phase 4: Block Entity Outline Implementation
1. **Interactive Block Entity Detection**
   - Chests, furnaces, brewing stands, etc.
   - Automatic detection of inventory-holding block entities

2. **Custom Block Entity Support**
   - Mod compatibility for custom block entities
   - API for other mods to register outline support

3. **State-Based Outlines**
   - Different outline colors for different block entity states
   - (e.g., powered vs unpowered, full vs empty)

### Phase 5: Block Outline Implementation
1. **Selective Block Highlighting**
   - Specific block type filtering (ores, redstone components, etc.)
   - Custom block lists via configuration

2. **Area-Based Block Outlines**
   - Outline blocks within specified regions
   - Integration with world selection tools

3. **Conditional Block Outlines**
   - Light level based highlighting
   - Redstone power state highlighting
   - Block age/growth state highlighting

### Phase 6: Advanced Features
1. **Performance Optimization**
   - Level-of-detail (LOD) system for distant outlines
   - Frustum culling for outline rendering
   - Batch rendering optimizations

2. **Shader Effects**
   - Animated outline effects (pulsing, flowing)
   - Glow and bloom effects for special items/entities
   - Custom outline textures and patterns

3. **Integration Features**
   - Mod compatibility layer
   - Export/import outline configurations
   - Server-side outline synchronization for multiplayer

### Phase 7: User Interface & Quality of Life
1. **In-Game Configuration GUI**
   - Real-time outline preview
   - Color picker with preset palettes
   - Outline thickness and style controls

2. **Keybind System**
   - Toggle individual outline types
   - Quick-switch outline profiles
   - Temporary outline disable

3. **Performance Monitoring**
   - Frame rate impact display
   - Outline count statistics
   - Memory usage tracking

## Technical Requirements

### Dependencies
- Fabric API 0.128.1+1.21.8
- Minecraft 1.21.8
- Java 21+
- Fabric Loader 0.16.14+

### Key Minecraft Systems to Hook Into
1. **Rendering Pipeline**
   - `ItemRenderer` for item outlines
   - `EntityRenderer` for entity outlines
   - `BlockEntityRenderer` for block entity outlines
   - `WorldRenderer` for block outlines

2. **Event Systems**
   - Client tick events for dynamic outline updates
   - World load/unload events for outline cleanup
   - Player interaction events for context-sensitive outlines

### Performance Considerations
- Outline rendering should be optional and configurable
- Distance-based LOD to reduce overhead for distant objects
- Efficient frustum culling to avoid rendering off-screen outlines
- Batch rendering for multiple objects of the same type
- Memory management for outline geometry buffers

## Development Phases Timeline
1. **Phase 1-2**: Core infrastructure and item outlines (Foundation)
2. **Phase 3**: Entity outline system (Expansion)
3. **Phase 4**: Block entity support (Utility)
4. **Phase 5**: Block outline system (Completion)
5. **Phase 6**: Performance and visual enhancements (Polish)
6. **Phase 7**: User experience improvements (Finalization)

## Compatibility Notes
- Designed to be compatible with other rendering mods
- Shader mod compatibility (OptiFine, Iris, etc.)
- Modpack integration considerations
- Server-side compatibility for multiplayer environments