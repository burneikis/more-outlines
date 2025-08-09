# More Outlines

A comprehensive Minecraft Fabric mod for Minecraft 1.21.8 that implements advanced outline rendering for various game objects including items, entities, block entities, and blocks.


![Demo of glowing entities](https://cdn.modrinth.com/data/cached_images/62a07212226ed3596315f12cdc31e83361293a5e_0.webp)
![GUI](https://cdn.modrinth.com/data/cached_images/e387fbffdbe427704f1239c03ee887074d4643b1_0.webp)

## Features

### Outlines

- Items
- Entities
- Block Entities
- Blocks

### GUI

- Toggle outlines for all objects
- Toggle outlines for specific:
  - Items
  - Entities
  - Block Entities
  - Blocks
- Choose color for each outline

### Keybinds

- Toggling all outlines (default: `O`)
- Opening the GUI (default: `RShift`)

## Technical Requirements

### Dependencies

- Fabric API 0.131.0+1.21.8
- Minecraft 1.21.8
- Java 21+
- Fabric Loader 0.17.2

## Compatibility Notes

- Designed to be compatible with other rendering mods
- Shader mod compatibility (OptiFine, Iris, etc.)
- Modpack integration considerations

## How This Mod Works

This mod uses a multi-layered architecture combining Fabric's Mixin system, custom rendering, and dynamic scanning to provide comprehensive outline functionality.

### Core Architecture

#### 1. **Mod Entry Points**

```java
// Main mod initializer
public class MoreOutlines implements ModInitializer {
    public static final String MOD_ID = "more-outlines";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("More Outlines mod initialized!");
    }
}

// Client-side initialization
public class MoreOutlinesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModKeybinds.registerKeyBinds();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ModKeybinds.handleKeyPress();

            if (ModKeybinds.openConfigGui.wasPressed()) {
                MinecraftClient.getInstance().setScreen(
                    new ModConfigScreen(MinecraftClient.getInstance().currentScreen)
                );
            }

            // Handle block selection scanning
            if (ModConfig.INSTANCE.outlinesEnabled &&
                !ModConfig.INSTANCE.selectedBlocks.isEmpty()) {
                BlockSelectionScanner.getInstance().tick(client);
            }
        });
    }
}
```

#### 2. **Configuration System**

```java
public class ModConfig {
    public static final ModConfig INSTANCE = new ModConfig();

    // Global toggles
    public boolean outlinesEnabled = false;

    // Color and appearance
    public int defaultColor = 0xFFFFFFFF; // White by default

    // Specific selections with individual colors
    public final Map<Identifier, ItemOutlineConfig> selectedItems = new HashMap<>();
    public final Map<Identifier, EntityOutlineConfig> selectedEntities = new HashMap<>();
    public final Map<Identifier, BlockOutlineConfig> selectedBlocks = new HashMap<>();

    // Configuration classes for individual items/entities/blocks
    public static class ItemOutlineConfig {
        public boolean enabled;
        public int color;

        public ItemOutlineConfig(boolean enabled, int color) {
            this.enabled = enabled;
            this.color = color;
        }
    }
}
```

### Rendering Implementation

#### 3. **Entity & Item Outlines**

Uses Mixin to modify the Entity's `isGlowing()` method:

```java
@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    private void onIsGlowing(CallbackInfoReturnable<Boolean> cir) {
        if (!ModConfig.INSTANCE.outlinesEnabled) {
            return;
        }

        Entity entity = (Entity) (Object) this;

        // Handle item entities with specific item selection
        if (entity instanceof ItemEntity itemEntity) {
            Identifier itemId = Registries.ITEM.getId(itemEntity.getStack().getItem());
            if (ModConfig.INSTANCE.isItemSelected(itemId)) {
                cir.setReturnValue(true);
                return;
            }
        }

        // Handle other entities
        Identifier entityId = Registries.ENTITY_TYPE.getId(entity.getType());
        if (ModConfig.INSTANCE.isEntitySelected(entityId)) {
            cir.setReturnValue(true);
        }
    }
}
```

#### 4. **Custom Color Application**

```java
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderEntityHead(Entity entity, double x, double y, double z,
                                   float tickProgress, MatrixStack matrices,
                                   VertexConsumerProvider vertexConsumers,
                                   int light, CallbackInfo ci) {
        if (!ModConfig.INSTANCE.outlinesEnabled || !entity.isGlowing()) {
            return;
        }

        if (vertexConsumers instanceof OutlineVertexConsumerProvider outlineProvider) {
            int color = getEntityOutlineColor(entity);
            if (color != -1) {
                // Extract RGBA components
                int red = (color >> 16) & 0xFF;
                int green = (color >> 8) & 0xFF;
                int blue = color & 0xFF;
                int alpha = (color >> 24) & 0xFF;

                outlineProvider.setColor(red, green, blue, alpha);
            }
        }
    }
}
```

#### 5. **Block Entity Outlines**

```java
@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Redirect(method = "renderBlockEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/entity/BlockEntityRenderDispatcher;render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V"))
    private void redirectBlockEntityRender(BlockEntityRenderDispatcher dispatcher, BlockEntity blockEntity,
            float tickProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {

        // Always render normally first
        dispatcher.render(blockEntity, tickProgress, matrices, vertexConsumers);

        // Only render outlines if enabled AND the specific block entity type is selected
        if (ModConfig.INSTANCE.outlinesEnabled && blockEntity != null) {
            // Get the block entity's block type
            Identifier blockId = Registries.BLOCK.getId(blockEntity.getCachedState().getBlock());

            // Only render outline if this specific block type is selected
            if (ModConfig.INSTANCE.isBlockSelected(blockId)) {
                OutlineVertexConsumerProvider outlineProvider = this.bufferBuilders.getOutlineVertexConsumers();

                // Get the color for this specific block type
                int color = ModConfig.INSTANCE.getBlockColor(blockId);
                int red = (color >> 16) & 0xFF;
                int green = (color >> 8) & 0xFF;
                int blue = color & 0xFF;
                int alpha = (color >> 24) & 0xFF;

                outlineProvider.setColor(red, green, blue, alpha);
                dispatcher.render(blockEntity, tickProgress, matrices, outlineProvider);
            }
        }
    }
}
```

### Advanced Block Outlines

#### 6. **Dynamic Block Scanning**

```java
public class BlockSelectionScanner {
    private static final BlockSelectionScanner INSTANCE = new BlockSelectionScanner();
    private final Map<Identifier, Set<BlockPos>> trackedBlocksByType = new HashMap<>();
    private final int SCAN_RADIUS = 32;

    public void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return;
        }

        trackedBlocksByType.clear();
        BlockPos playerPos = client.player.getBlockPos();
        ClientWorld world = client.world;

        // Scan 32-block radius around player
        for (int x = -SCAN_RADIUS; x <= SCAN_RADIUS; x++) {
            for (int y = -SCAN_RADIUS; y <= SCAN_RADIUS; y++) {
                for (int z = -SCAN_RADIUS; z <= SCAN_RADIUS; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    BlockState state = world.getBlockState(pos);

                    if (!state.isAir()) {
                        Identifier blockId = Registries.BLOCK.getId(state.getBlock());

                        if (ModConfig.INSTANCE.isBlockSelected(blockId)) {
                            trackedBlocksByType
                                .computeIfAbsent(blockId, k -> new HashSet<>())
                                .add(pos);
                        }
                    }
                }
            }
        }
    }
}
```

#### 7. **Custom Block Outline Rendering**

```java
public class BlockSelectionOutlineRenderer {

    public static void renderBlockSelectionOutlines(MatrixStack matrices, Camera camera,
                                                   OutlineVertexConsumerProvider outlineProvider,
                                                   World world, Map<Identifier, Set<BlockPos>> blocksByType) {

        Vec3d cameraPos = camera.getPos();
        BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();

        // Render each block type with its specific color
        for (Map.Entry<Identifier, Set<BlockPos>> entry : blocksByType.entrySet()) {
            Identifier blockId = entry.getKey();
            Set<BlockPos> positions = entry.getValue();

            int color = ModConfig.INSTANCE.getBlockColor(blockId);
            int red = (color >> 16) & 0xFF;
            int green = (color >> 8) & 0xFF;
            int blue = color & 0xFF;

            outlineProvider.setColor(red, green, blue, 255);

            // Render each block position
            for (BlockPos pos : positions) {
                BlockState state = world.getBlockState(pos);
                if (!state.isAir()) {
                    matrices.push();
                    matrices.translate(
                        pos.getX() - cameraPos.x,
                        pos.getY() - cameraPos.y,
                        pos.getZ() - cameraPos.z
                    );

                    // Render with invisible faces but visible outlines
                    renderInvisibleBlock(state, pos, matrices, outlineProvider, blockRenderManager, world);

                    matrices.pop();
                }
            }

            outlineProvider.draw();
        }
    }
}
```

### User Interface

#### 8. **Keybind System**

```java
public class ModKeybinds {
    public static KeyBinding openConfigGui;
    public static KeyBinding toggleAllOutlines;

    public static void registerKeyBinds() {
        openConfigGui = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.more-outlines.open_config",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            "key.category.more-outlines"
        ));

        toggleAllOutlines = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.more-outlines.toggle_all_outlines",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            "key.category.more-outlines"
        ));
    }

    public static void handleKeyPress() {
        if (toggleAllOutlines.wasPressed()) {
            ModConfig.INSTANCE.toggleOutlinesEnabled();
            String status = ModConfig.INSTANCE.outlinesEnabled ? "ON" : "OFF";
            ToggleNotificationHud.getInstance().showNotification("All Outlines: " + status);
        }
    }
}
```

#### 9. **Configuration GUI**

The mod provides a comprehensive GUI with search functionality and unified item/block/entity management:

```java
public class ModConfigScreen extends Screen {
    private OutlineListWidget list;
    private TextFieldWidget searchField;

    @Override
    protected void init() {
        // Search field for filtering items/blocks/entities
        this.searchField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 6, 200, 20, Text.literal("Search..."));

        // Unified list showing items, blocks, and entities with individual toggles
        this.list = new OutlineListWidget(this.client, this.width, this.height - 60, 35, 25);

        // Bulk toggle buttons
        addDrawableChild(ButtonWidget.builder(Text.literal("All Items"), button -> toggleAllItems()));
        addDrawableChild(ButtonWidget.builder(Text.literal("All Blocks"), button -> toggleAllBlocks()));
        addDrawableChild(ButtonWidget.builder(Text.literal("All Entities"), button -> toggleAllEntities()));
    }
}
```

This architecture provides:

- **Performance**: Performs well except for blocks, which may require additional optimization.
- **Flexibility**: Individual color control per item/block/entity type
- **Compatibility**: Uses Minecraft's built-in outline system
- **Extensibility**: Easy to add new outline types or features

The mod integrates seamlessly with Minecraft's rendering pipeline while providing extensive customization options through its intuitive GUI.

## Testing

This mod includes test scripts to set up different server configurations for testing the permission system and mod functionality.

### Test Scripts

Navigate to the `test/` directory to find three test scripts:

#### 1. **Vanilla Server** (`test-vanilla-server`)
Sets up and runs a vanilla Fabric server without the More Outlines mod.
```bash
cd test
./test-vanilla-server
```
- Use case: Testing compatibility and baseline performance
- Expected behavior: Client shows "mod not allowed" message, outlines disabled
- Server available at: `localhost:25565`

#### 2. **Allowed Server** (`test-allowed-server`)
Sets up and runs a server with the More Outlines mod installed and **allowed** by server configuration.
```bash
cd test
./test-allowed-server
```
- Builds the mod automatically before server setup
- Server config: `allowMoreOutlinesMod: true`
- Expected behavior: Mod functions normally, outlines work as intended
- Server available at: `localhost:25565`

#### 3. **Blocked Server** (`test-blocked-server`)
Sets up and runs a server with the More Outlines mod installed but **blocked** by server configuration.
```bash
cd test
./test-blocked-server
```
- Builds the mod automatically before server setup  
- Server config: `allowMoreOutlinesMod: false`
- Expected behavior: Client shows "mod not allowed" message, outlines disabled
- Server available at: `localhost:25565`

### Testing Workflow

1. **Start a test server:**
   ```bash
   ./test/test-allowed-server  # or test-blocked-server or test-vanilla-server
   ```

2. **Connect with client** (in a new terminal):
   ```bash
   ./gradlew runClient
   ```

3. **Join the server** in the Minecraft client:
   - Multiplayer → Direct Connect → `localhost:25565`

4. **Test mod functionality:**
   - Press `O` to toggle outlines
   - Press `Right Shift` to open mod configuration GUI
   - Verify expected behavior based on server configuration

### Notes

- Each test server creates its own directory (e.g., `test/vanilla-server/`, `test/allowed-server/`)
- Server files and downloads are automatically gitignored
- All servers run on port `25565` (only one can run at a time)
- Servers are configured for creative mode with peaceful difficulty for easy testing
- Test servers require you to accept the EULA in terminal by typing `yes`
