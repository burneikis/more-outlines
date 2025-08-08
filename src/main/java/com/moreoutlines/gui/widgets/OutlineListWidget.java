package com.moreoutlines.gui.widgets;

/**
 * List widget for More Outlines configuration.
 * Design inspired by ReEntityOutliner by Globox_Z
 * https://github.com/Globox-Z/ReEntityOutliner
 */

import com.moreoutlines.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.Text;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.item.Item;
import net.minecraft.entity.EntityType;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.BlockItem;

import java.util.*;
import java.util.stream.Collectors;

public class OutlineListWidget extends AlwaysSelectedEntryListWidget<OutlineListWidget.Entry> {
    private Map<String, List<UnifiedEntry>> searchCache = new HashMap<>();
    
    public OutlineListWidget(net.minecraft.client.MinecraftClient client, int width, int height, int top, int itemHeight) {
        super(client, width, height, top, itemHeight);
        initializeSearchCache();
        updateSearchResults("");
    }
    
    public void updateSearchResults(String searchText) {
        this.clearEntries();
        
        if (searchCache.containsKey(searchText)) {
            List<UnifiedEntry> results = searchCache.get(searchText);
            
            for (UnifiedEntry unifiedEntry : results) {
                this.addEntry(new UnifiedItemEntry(unifiedEntry));
            }
        }
        
        if (this.children().isEmpty()) {
            this.addEntry(new HeaderEntry("No results found"));
        }
    }
    
    private void initializeSearchCache() {
        searchCache.clear();
        
        List<UnifiedEntry> allResults = new ArrayList<>();
        searchCache.put("", allResults);
        
        // Create a set to track all unique identifiers
        Set<Identifier> processedIds = new HashSet<>();
        
        // Process all items and find corresponding blocks/entities
        List<Item> items = Registries.ITEM.stream()
            .sorted((a, b) -> Registries.ITEM.getId(a).toString().compareTo(Registries.ITEM.getId(b).toString()))
            .collect(Collectors.toList());
            
        for (Item item : items) {
            Identifier itemId = Registries.ITEM.getId(item);
            if (processedIds.contains(itemId)) continue;
            processedIds.add(itemId);
            
            // Check if there's a corresponding block
            Block block = null;
            if (item instanceof BlockItem) {
                block = ((BlockItem) item).getBlock();
            }
            
            // Check if there's a corresponding entity (for spawn eggs, boat items, etc.)
            EntityType<?> entityType = null;
            String itemName = itemId.getPath();
            
            // Auto-map boat items to boat entities
            if (itemName.endsWith("_boat") || itemName.endsWith("_raft")) {
                // Try direct mapping first (works for most boats and rafts)
                entityType = Registries.ENTITY_TYPE.get(Identifier.of(itemId.getNamespace() + ":" + itemName));
                
                // If direct mapping doesn't work, the entity might not exist
                if (entityType == Registries.ENTITY_TYPE.get(Identifier.of("minecraft:pig"))) {
                    entityType = null; // Reset if we got the default fallback
                }
            } else if (itemName.endsWith("_spawn_egg")) {
                // Handle spawn eggs - map spawn egg items to their entities
                String entityName = itemName.replace("_spawn_egg", "");
                entityType = Registries.ENTITY_TYPE.get(Identifier.of(itemId.getNamespace() + ":" + entityName));
                
                // If direct mapping doesn't work, the entity might not exist
                if (entityType == Registries.ENTITY_TYPE.get(Identifier.of("minecraft:pig"))) {
                    entityType = null; // Reset if we got the default fallback
                }
            }
            
            UnifiedEntry entry = new UnifiedEntry(itemId.toString(), item, block, entityType);
            String name = itemId.toString().toLowerCase();
            allResults.add(entry);
            addToPrefixTree(name, entry);
            
            // Mark the entity as processed if it was linked to this item
            if (entityType != null) {
                Identifier entityId = Registries.ENTITY_TYPE.getId(entityType);
                processedIds.add(entityId);
            }
        }
        
        // Process remaining blocks that don't have items
        List<Block> blocks = Registries.BLOCK.stream()
            .sorted((a, b) -> Registries.BLOCK.getId(a).toString().compareTo(Registries.BLOCK.getId(b).toString()))
            .collect(Collectors.toList());
            
        for (Block block : blocks) {
            Identifier blockId = Registries.BLOCK.getId(block);
            if (processedIds.contains(blockId)) continue;
            processedIds.add(blockId);
            
            UnifiedEntry entry = new UnifiedEntry(blockId.toString(), null, block, null);
            String name = blockId.toString().toLowerCase();
            allResults.add(entry);
            addToPrefixTree(name, entry);
        }
        
        // Process remaining entities that don't have items or spawn eggs
        List<EntityType<?>> entities = Registries.ENTITY_TYPE.stream()
            .sorted((a, b) -> Registries.ENTITY_TYPE.getId(a).toString().compareTo(Registries.ENTITY_TYPE.getId(b).toString()))
            .collect(Collectors.toList());
            
        for (EntityType<?> entity : entities) {
            Identifier entityId = Registries.ENTITY_TYPE.getId(entity);
            if (processedIds.contains(entityId)) continue;
            processedIds.add(entityId);
            
            UnifiedEntry entry = new UnifiedEntry(entityId.toString(), null, null, entity);
            String name = entityId.toString().toLowerCase();
            allResults.add(entry);
            addToPrefixTree(name, entry);
        }
    }
    
    private void addToPrefixTree(String name, UnifiedEntry entry) {
        List<String> prefixes = new ArrayList<>();
        prefixes.add("");
        
        for (int i = 0; i < name.length(); i++) {
            char character = name.charAt(i);
            for (int p = 0; p < prefixes.size(); p++) {
                String prefix = prefixes.get(p) + character;
                prefixes.set(p, prefix);
                
                List<UnifiedEntry> results = searchCache.computeIfAbsent(prefix, k -> new ArrayList<>());
                results.add(entry);
            }
            
            if (Character.isWhitespace(character) || character == '_' || character == ':') {
                prefixes.add("");
            }
        }
    }
    
    public void selectAllVisible() {
        for (Entry entry : this.children()) {
            entry.setSelected(true);
        }
    }
    
    public void deselectAllVisible() {
        for (Entry entry : this.children()) {
            entry.setSelected(false);
        }
    }
    
    public void toggleAllItems() {
        boolean allItemsSelected = true;
        int totalItems = 0;
        
        // Check if ALL items are currently selected
        for (Entry entry : this.children()) {
            if (entry instanceof UnifiedItemEntry) {
                UnifiedItemEntry unifiedEntry = (UnifiedItemEntry) entry;
                if (unifiedEntry.unifiedEntry.hasItem()) {
                    totalItems++;
                    if (!unifiedEntry.itemSelected) {
                        allItemsSelected = false;
                        break;
                    }
                }
            }
        }
        
        // If no items exist, do nothing
        if (totalItems == 0) return;
        
        // Toggle all items based on current state
        // If ALL items are selected, deselect all; otherwise select all
        for (Entry entry : this.children()) {
            if (entry instanceof UnifiedItemEntry) {
                UnifiedItemEntry unifiedEntry = (UnifiedItemEntry) entry;
                if (unifiedEntry.unifiedEntry.hasItem()) {
                    if (allItemsSelected) {
                        // All items are selected, so turn them off
                        if (unifiedEntry.itemSelected) {
                            unifiedEntry.toggleItemSelection(false);
                        }
                    } else {
                        // Not all items are selected, so turn them all on
                        if (!unifiedEntry.itemSelected) {
                            unifiedEntry.toggleItemSelection(true);
                        }
                    }
                }
            }
        }
    }
    
    public void toggleAllEntities() {
        boolean allEntitiesSelected = true;
        int totalEntities = 0;
        
        // Check if ALL entities are currently selected
        for (Entry entry : this.children()) {
            if (entry instanceof UnifiedItemEntry) {
                UnifiedItemEntry unifiedEntry = (UnifiedItemEntry) entry;
                if (unifiedEntry.unifiedEntry.hasEntity()) {
                    totalEntities++;
                    if (!unifiedEntry.entitySelected) {
                        allEntitiesSelected = false;
                        break;
                    }
                }
            }
        }
        
        // If no entities exist, do nothing
        if (totalEntities == 0) return;
        
        // Toggle all entities based on current state
        // If ALL entities are selected, deselect all; otherwise select all
        for (Entry entry : this.children()) {
            if (entry instanceof UnifiedItemEntry) {
                UnifiedItemEntry unifiedEntry = (UnifiedItemEntry) entry;
                if (unifiedEntry.unifiedEntry.hasEntity()) {
                    if (allEntitiesSelected) {
                        // All entities are selected, so turn them off
                        if (unifiedEntry.entitySelected) {
                            unifiedEntry.toggleEntitySelection(false);
                        }
                    } else {
                        // Not all entities are selected, so turn them all on
                        if (!unifiedEntry.entitySelected) {
                            unifiedEntry.toggleEntitySelection(true);
                        }
                    }
                }
            }
        }
    }
    
    public void toggleAllBlocks() {
        boolean allBlocksSelected = true;
        int totalBlocks = 0;
        
        // Check if ALL blocks are currently selected
        for (Entry entry : this.children()) {
            if (entry instanceof UnifiedItemEntry) {
                UnifiedItemEntry unifiedEntry = (UnifiedItemEntry) entry;
                if (unifiedEntry.unifiedEntry.hasBlock()) {
                    totalBlocks++;
                    if (!unifiedEntry.blockSelected) {
                        allBlocksSelected = false;
                        break;
                    }
                }
            }
        }
        
        // If no blocks exist, do nothing
        if (totalBlocks == 0) return;
        
        // Toggle all blocks based on current state
        // If ALL blocks are selected, deselect all; otherwise select all
        for (Entry entry : this.children()) {
            if (entry instanceof UnifiedItemEntry) {
                UnifiedItemEntry unifiedEntry = (UnifiedItemEntry) entry;
                if (unifiedEntry.unifiedEntry.hasBlock()) {
                    if (allBlocksSelected) {
                        // All blocks are selected, so turn them off
                        if (unifiedEntry.blockSelected) {
                            unifiedEntry.toggleBlockSelection(false);
                        }
                    } else {
                        // Not all blocks are selected, so turn them all on
                        if (!unifiedEntry.blockSelected) {
                            unifiedEntry.toggleBlockSelection(true);
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public int getRowWidth() {
        return 400;
    }
    
    protected int getScrollbarX() {
        return this.width - 6;
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Handle up and down arrow keys for navigation
        if (keyCode == 264) { // Down arrow
            this.moveSelection(1);
            return true;
        } else if (keyCode == 265) { // Up arrow
            this.moveSelection(-1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    private void moveSelection(int direction) {
        if (this.children().isEmpty()) return;
        
        int currentIndex = -1;
        Entry selectedEntry = this.getSelectedOrNull();
        
        if (selectedEntry != null) {
            currentIndex = this.children().indexOf(selectedEntry);
        }
        
        int newIndex = currentIndex + direction;
        if (newIndex < 0) {
            newIndex = this.children().size() - 1; // Wrap to bottom
        } else if (newIndex >= this.children().size()) {
            newIndex = 0; // Wrap to top
        }
        
        this.setSelected(this.children().get(newIndex));
        this.ensureVisible(this.children().get(newIndex));
    }
    
        @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // Call parent render method first for the list content
        super.renderWidget(context, mouseX, mouseY, delta);
        
        // Draw column headers on top
        net.minecraft.client.MinecraftClient minecraft = net.minecraft.client.MinecraftClient.getInstance();
        int headerY = this.getY() - 15;
        
        // Calculate the actual entry start position to match the entries
        // AlwaysSelectedEntryListWidget centers the entry row within the widget bounds
        int rowWidth = this.getRowWidth(); // 400
        int leftMargin = Math.max(0, (this.width - rowWidth) / 2);
        int entryStartX = this.getX() + leftMargin;
        int checkboxBaseX = entryStartX + 200; // This matches currentX = x + 200 in render method
        
        // Draw headers aligned with actual checkbox positions
        // Item header - centered over checkbox (16px wide)
        int itemHeaderX = checkboxBaseX + 8 - minecraft.textRenderer.getWidth("Item") / 2;
        context.drawText(minecraft.textRenderer, "Item", itemHeaderX, headerY, 0xFFFFFFFF, false);
        
        // Block header - centered over checkbox (50 pixels right of item checkbox)
        int blockHeaderX = checkboxBaseX + 50 + 8 - minecraft.textRenderer.getWidth("Block") / 2;
        context.drawText(minecraft.textRenderer, "Block", blockHeaderX, headerY, 0xFFFFFFFF, false);
        
        // Entity header - centered over checkbox (100 pixels right of item checkbox)
        int entityHeaderX = checkboxBaseX + 100 + 8 - minecraft.textRenderer.getWidth("Entity") / 2;
        context.drawText(minecraft.textRenderer, "Entity", entityHeaderX, headerY, 0xFFFFFFFF, false);
    }
    
    // Base Entry class
    public abstract static class Entry extends AlwaysSelectedEntryListWidget.Entry<Entry> {
        public abstract void setSelected(boolean selected);
    }
    
    // Header Entry for "no results"
    public static class HeaderEntry extends Entry {
        private final String title;
        
        public HeaderEntry(String title) {
            this.title = title;
        }
        
        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            net.minecraft.client.MinecraftClient minecraft = net.minecraft.client.MinecraftClient.getInstance();
            int textWidth = minecraft.textRenderer.getWidth(title);
            int textX = x + (entryWidth - textWidth) / 2;
            int textY = y + (entryHeight - minecraft.textRenderer.fontHeight) / 2;
            
            context.drawText(minecraft.textRenderer, title, textX, textY, 0xFFFFFFFF, false);
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return false;
        }
        
        @Override
        public void setSelected(boolean selected) {
            // Do nothing for headers
        }
        
        @Override
        public Text getNarration() {
            return Text.literal(title);
        }
    }
    
    // Item Entry
    public static class ItemEntry extends Entry {
        private final Item item;
        private boolean selected;
        
        public ItemEntry(Item item) {
            this.item = item;
            Identifier itemId = Registries.ITEM.getId(item);
            this.selected = ModConfig.INSTANCE.isItemSelected(itemId);
        }
        
        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            net.minecraft.client.MinecraftClient minecraft = net.minecraft.client.MinecraftClient.getInstance();
            
            // Draw item icon
            context.drawItem(new ItemStack(item), x + 2, y + 2);
            
            // Draw checkbox
            int checkboxSize = 16;
            int checkboxX = x + 25;
            int checkboxY = y + 2;
            
            // Draw checkbox background
            context.fill(checkboxX, checkboxY, checkboxX + checkboxSize, checkboxY + checkboxSize, selected ? 0xFF4CAF50 : 0xFF333333);
            context.drawBorder(checkboxX, checkboxY, checkboxSize, checkboxSize, 0xFF666666);
            
            if (selected) {
                // Draw checkmark - centered in checkbox
                String checkmark = "✓";
                int checkmarkWidth = minecraft.textRenderer.getWidth(checkmark);
                int checkmarkHeight = minecraft.textRenderer.fontHeight;
                int checkmarkX = checkboxX + (checkboxSize - checkmarkWidth) / 2;
                int checkmarkY = checkboxY + (checkboxSize - checkmarkHeight) / 2;
                context.drawText(minecraft.textRenderer, checkmark, checkmarkX, checkmarkY, 0xFFFFFFFF, false);
            }
            
            // Draw item name
            String name = item.getName().getString();
            context.drawText(minecraft.textRenderer, name, x + 50, y + 6, 0xFFFFFFFF, false);
            
            // Draw color widget if selected
            if (selected) {
                Identifier itemId = Registries.ITEM.getId(item);
                int color = ModConfig.INSTANCE.getItemColor(itemId);
                String colorText = getColorName(color);
                
                int colorX = x + entryWidth - 80;
                int colorY = y + 2;
                int colorWidth = 75;
                int colorHeight = 16;
                
                context.fill(colorX, colorY, colorX + colorWidth, colorY + colorHeight, 0xFF444444);
                context.drawBorder(colorX, colorY, colorWidth, colorHeight, 0xFF666666);
                context.drawCenteredTextWithShadow(minecraft.textRenderer, colorText, colorX + colorWidth / 2, colorY + 4, color);
            }
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            Identifier itemId = Registries.ITEM.getId(item);
            
            // Check if clicking on color widget (rough approximation)
            if (selected && mouseX >= 300) {
                cycleColor();
                return true;
            }
            
            // Toggle selection
            ModConfig.INSTANCE.toggleItemSelection(itemId, ModConfig.INSTANCE.defaultColor);
            this.selected = ModConfig.INSTANCE.isItemSelected(itemId);
            return true;
        }
        
        private void cycleColor() {
            Identifier itemId = Registries.ITEM.getId(item);
            int currentColor = ModConfig.INSTANCE.getItemColor(itemId);
            int newColor = getNextColor(currentColor);
            ModConfig.INSTANCE.setItemColor(itemId, newColor);
        }
        
        @Override
        public void setSelected(boolean selected) {
            Identifier itemId = Registries.ITEM.getId(item);
            if (selected != ModConfig.INSTANCE.isItemSelected(itemId)) {
                ModConfig.INSTANCE.toggleItemSelection(itemId, ModConfig.INSTANCE.defaultColor);
            }
            this.selected = ModConfig.INSTANCE.isItemSelected(itemId);
        }
        
        @Override
        public Text getNarration() {
            return Text.literal(item.getName().getString());
        }
    }
    
    // Entity Entry
    public static class EntityEntry extends Entry {
        private final EntityType<?> entityType;
        private boolean selected;
        
        public EntityEntry(EntityType<?> entityType) {
            this.entityType = entityType;
            Identifier entityId = Registries.ENTITY_TYPE.getId(entityType);
            this.selected = ModConfig.INSTANCE.isEntitySelected(entityId);
        }
        
        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            net.minecraft.client.MinecraftClient minecraft = net.minecraft.client.MinecraftClient.getInstance();
            
            // Draw checkbox
            int checkboxSize = 16;
            int checkboxX = x + 2;
            int checkboxY = y + 2;
            
            context.fill(checkboxX, checkboxY, checkboxX + checkboxSize, checkboxY + checkboxSize, selected ? 0xFF4CAF50 : 0xFF333333);
            context.drawBorder(checkboxX, checkboxY, checkboxSize, checkboxSize, 0xFF666666);
            
            if (selected) {
                // Draw checkmark - centered in checkbox
                String checkmark = "✓";
                int checkmarkWidth = minecraft.textRenderer.getWidth(checkmark);
                int checkmarkHeight = minecraft.textRenderer.fontHeight;
                int checkmarkX = checkboxX + (checkboxSize - checkmarkWidth) / 2;
                int checkmarkY = checkboxY + (checkboxSize - checkmarkHeight) / 2;
                context.drawText(minecraft.textRenderer, checkmark, checkmarkX, checkmarkY, 0xFFFFFFFF, false);
            }
            
            // Draw entity name
            String name = entityType.getName().getString();
            context.drawText(minecraft.textRenderer, name, x + 25, y + 6, 0xFFFFFFFF, false);
            
            // Draw color widget if selected
            if (selected) {
                Identifier entityId = Registries.ENTITY_TYPE.getId(entityType);
                int color = ModConfig.INSTANCE.getEntityColor(entityId);
                String colorText = getColorName(color);
                
                int colorX = x + entryWidth - 80;
                int colorY = y + 2;
                int colorWidth = 75;
                int colorHeight = 16;
                
                context.fill(colorX, colorY, colorX + colorWidth, colorY + colorHeight, 0xFF444444);
                context.drawBorder(colorX, colorY, colorWidth, colorHeight, 0xFF666666);
                context.drawCenteredTextWithShadow(minecraft.textRenderer, colorText, colorX + colorWidth / 2, colorY + 4, color);
            }
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            Identifier entityId = Registries.ENTITY_TYPE.getId(entityType);
            
            // Check if clicking on color widget (rough approximation)
            if (selected && mouseX >= 300) {
                cycleColor();
                return true;
            }
            
            // Toggle selection
            ModConfig.INSTANCE.toggleEntitySelection(entityId, ModConfig.INSTANCE.defaultColor);
            this.selected = ModConfig.INSTANCE.isEntitySelected(entityId);
            return true;
        }
        
        private void cycleColor() {
            Identifier entityId = Registries.ENTITY_TYPE.getId(entityType);
            int currentColor = ModConfig.INSTANCE.getEntityColor(entityId);
            int newColor = getNextColor(currentColor);
            ModConfig.INSTANCE.setEntityColor(entityId, newColor);
        }
        
        @Override
        public void setSelected(boolean selected) {
            Identifier entityId = Registries.ENTITY_TYPE.getId(entityType);
            if (selected != ModConfig.INSTANCE.isEntitySelected(entityId)) {
                ModConfig.INSTANCE.toggleEntitySelection(entityId, ModConfig.INSTANCE.defaultColor);
            }
            this.selected = ModConfig.INSTANCE.isEntitySelected(entityId);
        }
        
        @Override
        public Text getNarration() {
            return Text.literal(entityType.getName().getString());
        }
    }
    
    // Block Entry
    public static class BlockEntry extends Entry {
        private final Block block;
        private boolean selected;
        
        public BlockEntry(Block block) {
            this.block = block;
            Identifier blockId = Registries.BLOCK.getId(block);
            this.selected = ModConfig.INSTANCE.isBlockSelected(blockId);
        }
        
        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            net.minecraft.client.MinecraftClient minecraft = net.minecraft.client.MinecraftClient.getInstance();
            
            // Draw block icon
            context.drawItem(new ItemStack(block), x + 2, y + 2);
            
            // Draw checkbox
            int checkboxSize = 16;
            int checkboxX = x + 25;
            int checkboxY = y + 2;
            
            context.fill(checkboxX, checkboxY, checkboxX + checkboxSize, checkboxY + checkboxSize, selected ? 0xFF4CAF50 : 0xFF333333);
            context.drawBorder(checkboxX, checkboxY, checkboxSize, checkboxSize, 0xFF666666);
            
            if (selected) {
                // Draw checkmark - centered in checkbox
                String checkmark = "✓";
                int checkmarkWidth = minecraft.textRenderer.getWidth(checkmark);
                int checkmarkHeight = minecraft.textRenderer.fontHeight;
                int checkmarkX = checkboxX + (checkboxSize - checkmarkWidth) / 2;
                int checkmarkY = checkboxY + (checkboxSize - checkmarkHeight) / 2;
                context.drawText(minecraft.textRenderer, checkmark, checkmarkX, checkmarkY, 0xFFFFFFFF, false);
            }
            
            // Draw block name
            String name = block.getName().getString();
            context.drawText(minecraft.textRenderer, name, x + 50, y + 6, 0xFFFFFFFF, false);
            
            // Draw color widget if selected
            if (selected) {
                Identifier blockId = Registries.BLOCK.getId(block);
                int color = ModConfig.INSTANCE.getBlockColor(blockId);
                String colorText = getColorName(color);
                
                int colorX = x + entryWidth - 80;
                int colorY = y + 2;
                int colorWidth = 75;
                int colorHeight = 16;
                
                context.fill(colorX, colorY, colorX + colorWidth, colorY + colorHeight, 0xFF444444);
                context.drawBorder(colorX, colorY, colorWidth, colorHeight, 0xFF666666);
                context.drawCenteredTextWithShadow(minecraft.textRenderer, colorText, colorX + colorWidth / 2, colorY + 4, color);
            }
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            Identifier blockId = Registries.BLOCK.getId(block);
            
            // Check if clicking on color widget (rough approximation)
            if (selected && mouseX >= 300) {
                cycleColor();
                return true;
            }
            
            // Toggle selection
            ModConfig.INSTANCE.toggleBlockSelection(blockId, ModConfig.INSTANCE.defaultColor);
            this.selected = ModConfig.INSTANCE.isBlockSelected(blockId);
            return true;
        }
        
        private void cycleColor() {
            Identifier blockId = Registries.BLOCK.getId(block);
            int currentColor = ModConfig.INSTANCE.getBlockColor(blockId);
            int newColor = getNextColor(currentColor);
            ModConfig.INSTANCE.setBlockColor(blockId, newColor);
        }
        
        @Override
        public void setSelected(boolean selected) {
            Identifier blockId = Registries.BLOCK.getId(block);
            if (selected != ModConfig.INSTANCE.isBlockSelected(blockId)) {
                ModConfig.INSTANCE.toggleBlockSelection(blockId, ModConfig.INSTANCE.defaultColor);
            }
            this.selected = ModConfig.INSTANCE.isBlockSelected(blockId);
        }
        
        @Override
        public Text getNarration() {
            return Text.literal(block.getName().getString());
        }
    }
    
    // Unified Entry for holding item/block/entity data
    public static class UnifiedEntry {
        public final String name;
        public final Item item;
        public final Block block;
        public final EntityType<?> entityType;
        
        public UnifiedEntry(String name, Item item, Block block, EntityType<?> entityType) {
            this.name = name;
            this.item = item;
            this.block = block;
            this.entityType = entityType;
        }
        
        public boolean hasItem() {
            return item != null;
        }
        
        public boolean hasBlock() {
            return block != null;
        }
        
        public boolean hasEntity() {
            return entityType != null;
        }
    }
    
    // Unified Item Entry with multiple checkboxes
    public static class UnifiedItemEntry extends Entry {
        private final UnifiedEntry unifiedEntry;
        private boolean itemSelected;
        private boolean blockSelected;
        private boolean entitySelected;
        
        // Store checkbox positions from render method
        private int itemCheckboxX = -1;
        private int blockCheckboxX = -1; 
        private int entityCheckboxX = -1;
        private int checkboxY = -1;
        private int colorWidgetX = -1;
        private int colorWidgetY = -1;
        
        public UnifiedItemEntry(UnifiedEntry unifiedEntry) {
            this.unifiedEntry = unifiedEntry;
            
            // Initialize selection states
            if (unifiedEntry.hasItem()) {
                Identifier itemId = Registries.ITEM.getId(unifiedEntry.item);
                this.itemSelected = ModConfig.INSTANCE.isItemSelected(itemId);
            }
            
            if (unifiedEntry.hasBlock()) {
                Identifier blockId = Registries.BLOCK.getId(unifiedEntry.block);
                this.blockSelected = ModConfig.INSTANCE.isBlockSelected(blockId);
            }
            
            if (unifiedEntry.hasEntity()) {
                Identifier entityId = Registries.ENTITY_TYPE.getId(unifiedEntry.entityType);
                this.entitySelected = ModConfig.INSTANCE.isEntitySelected(entityId);
            }
        }
        
        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            net.minecraft.client.MinecraftClient minecraft = net.minecraft.client.MinecraftClient.getInstance();
            
            int currentX = x + 2;
            
            // Draw icon (item if available, otherwise block)
            if (unifiedEntry.hasItem()) {
                context.drawItem(new ItemStack(unifiedEntry.item), currentX, y + 2);
            } else if (unifiedEntry.hasBlock()) {
                context.drawItem(new ItemStack(unifiedEntry.block), currentX, y + 2);
            }
            currentX += 22;
            
            // Draw name
            String displayName;
            if (unifiedEntry.hasItem()) {
                displayName = unifiedEntry.item.getName().getString();
            } else if (unifiedEntry.hasBlock()) {
                displayName = unifiedEntry.block.getName().getString();
            } else if (unifiedEntry.hasEntity()) {
                displayName = unifiedEntry.entityType.getName().getString();
            } else {
                displayName = unifiedEntry.name;
            }
            context.drawText(minecraft.textRenderer, displayName, currentX, y + 6, 0xFFFFFFFF, false);
            currentX = x + 200;
            
            int checkboxSize = 16;
            this.checkboxY = y + 2; // Store checkbox Y position
            
            // Draw item checkbox if item exists
            if (unifiedEntry.hasItem()) {
                this.itemCheckboxX = currentX; // Store item checkbox X position
                context.fill(currentX, y + 2, currentX + checkboxSize, y + 2 + checkboxSize, itemSelected ? 0xFF4CAF50 : 0xFF333333);
                context.drawBorder(currentX, y + 2, checkboxSize, checkboxSize, 0xFF666666);
                
                if (itemSelected) {
                    String checkmark = "✓";
                    int checkmarkWidth = minecraft.textRenderer.getWidth(checkmark);
                    int checkmarkHeight = minecraft.textRenderer.fontHeight;
                    int checkmarkX = currentX + (checkboxSize - checkmarkWidth) / 2;
                    int checkmarkY = y + 2 + (checkboxSize - checkmarkHeight) / 2;
                    context.drawText(minecraft.textRenderer, checkmark, checkmarkX, checkmarkY, 0xFFFFFFFF, false);
                }
            }
            currentX += 50;
            
            // Draw block checkbox if block exists
            if (unifiedEntry.hasBlock()) {
                this.blockCheckboxX = currentX; // Store block checkbox X position
                context.fill(currentX, y + 2, currentX + checkboxSize, y + 2 + checkboxSize, blockSelected ? 0xFF4CAF50 : 0xFF333333);
                context.drawBorder(currentX, y + 2, checkboxSize, checkboxSize, 0xFF666666);
                
                if (blockSelected) {
                    String checkmark = "✓";
                    int checkmarkWidth = minecraft.textRenderer.getWidth(checkmark);
                    int checkmarkHeight = minecraft.textRenderer.fontHeight;
                    int checkmarkX = currentX + (checkboxSize - checkmarkWidth) / 2;
                    int checkmarkY = y + 2 + (checkboxSize - checkmarkHeight) / 2;
                    context.drawText(minecraft.textRenderer, checkmark, checkmarkX, checkmarkY, 0xFFFFFFFF, false);
                }
            }
            currentX += 50;
            
            // Draw entity checkbox if entity exists
            if (unifiedEntry.hasEntity()) {
                this.entityCheckboxX = currentX; // Store entity checkbox X position
                context.fill(currentX, y + 2, currentX + checkboxSize, y + 2 + checkboxSize, entitySelected ? 0xFF4CAF50 : 0xFF333333);
                context.drawBorder(currentX, y + 2, checkboxSize, checkboxSize, 0xFF666666);
                
                if (entitySelected) {
                    String checkmark = "✓";
                    int checkmarkWidth = minecraft.textRenderer.getWidth(checkmark);
                    int checkmarkHeight = minecraft.textRenderer.fontHeight;
                    int checkmarkX = currentX + (checkboxSize - checkmarkWidth) / 2;
                    int checkmarkY = y + 2 + (checkboxSize - checkmarkHeight) / 2;
                    context.drawText(minecraft.textRenderer, checkmark, checkmarkX, checkmarkY, 0xFFFFFFFF, false);
                }
            }
            
            // Draw single color widget if any type is selected
            if (itemSelected || blockSelected || entitySelected) {
                currentX = x + entryWidth - 80;
                this.colorWidgetX = currentX;
                this.colorWidgetY = y + 2;
                int color = getUnifiedColor();
                drawColorWidget(context, minecraft, currentX, y + 2, color);
            }
        }
        
        private void drawColorWidget(DrawContext context, net.minecraft.client.MinecraftClient minecraft, int x, int y, int color) {
            int colorWidth = 75;
            int colorHeight = 16;
            
            context.fill(x, y, x + colorWidth, y + colorHeight, 0xFF444444);
            context.drawBorder(x, y, colorWidth, colorHeight, 0xFF666666);
            context.drawCenteredTextWithShadow(minecraft.textRenderer, getColorName(color), x + colorWidth / 2, y + 4, color);
        }
        
        private int getUnifiedColor() {
            // Return the color from any selected type (they should all be the same)
            if (itemSelected && unifiedEntry.hasItem()) {
                return ModConfig.INSTANCE.getItemColor(Registries.ITEM.getId(unifiedEntry.item));
            }
            if (blockSelected && unifiedEntry.hasBlock()) {
                return ModConfig.INSTANCE.getBlockColor(Registries.BLOCK.getId(unifiedEntry.block));
            }
            if (entitySelected && unifiedEntry.hasEntity()) {
                return ModConfig.INSTANCE.getEntityColor(Registries.ENTITY_TYPE.getId(unifiedEntry.entityType));
            }
            return ModConfig.INSTANCE.defaultColor;
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            // Use absolute coordinates with stored positions from render method
            int checkboxSize = 16;
            
            // Check unified color widget click first
            if (colorWidgetX != -1 && (itemSelected || blockSelected || entitySelected) &&
                mouseX >= colorWidgetX && mouseX < colorWidgetX + 75 &&
                mouseY >= colorWidgetY && mouseY < colorWidgetY + 16) {
                
                // Cycle color for all selected types
                int currentColor = getUnifiedColor();
                int newColor = getNextColor(currentColor);
                setUnifiedColor(newColor);
                return true;
            }
            
            // Check item checkbox click
            if (unifiedEntry.hasItem() && itemCheckboxX != -1 &&
                mouseX >= itemCheckboxX && mouseX < itemCheckboxX + checkboxSize &&
                mouseY >= checkboxY && mouseY < checkboxY + checkboxSize) {
                
                Identifier itemId = Registries.ITEM.getId(unifiedEntry.item);
                int currentColor = getUnifiedColor();
                
                ModConfig.INSTANCE.toggleItemSelection(itemId, currentColor);
                this.itemSelected = ModConfig.INSTANCE.isItemSelected(itemId);
                
                // Sync colors when toggling
                if (this.itemSelected) {
                    setUnifiedColor(currentColor);
                }
                return true;
            }
            
            // Check block checkbox click
            if (unifiedEntry.hasBlock() && blockCheckboxX != -1 &&
                mouseX >= blockCheckboxX && mouseX < blockCheckboxX + checkboxSize &&
                mouseY >= checkboxY && mouseY < checkboxY + checkboxSize) {
                
                Identifier blockId = Registries.BLOCK.getId(unifiedEntry.block);
                int currentColor = getUnifiedColor();
                
                ModConfig.INSTANCE.toggleBlockSelection(blockId, currentColor);
                this.blockSelected = ModConfig.INSTANCE.isBlockSelected(blockId);
                
                // Sync colors when toggling
                if (this.blockSelected) {
                    setUnifiedColor(currentColor);
                }
                return true;
            }
            
            // Check entity checkbox click
            if (unifiedEntry.hasEntity() && entityCheckboxX != -1 &&
                mouseX >= entityCheckboxX && mouseX < entityCheckboxX + checkboxSize &&
                mouseY >= checkboxY && mouseY < checkboxY + checkboxSize) {
                
                Identifier entityId = Registries.ENTITY_TYPE.getId(unifiedEntry.entityType);
                int currentColor = getUnifiedColor();
                
                ModConfig.INSTANCE.toggleEntitySelection(entityId, currentColor);
                this.entitySelected = ModConfig.INSTANCE.isEntitySelected(entityId);
                
                // Sync colors when toggling
                if (this.entitySelected) {
                    setUnifiedColor(currentColor);
                }
                return true;
            }
            
            return false;
        }
        
        private void setUnifiedColor(int color) {
            // Set the same color for all selected types
            if (itemSelected && unifiedEntry.hasItem()) {
                ModConfig.INSTANCE.setItemColor(Registries.ITEM.getId(unifiedEntry.item), color);
            }
            if (blockSelected && unifiedEntry.hasBlock()) {
                ModConfig.INSTANCE.setBlockColor(Registries.BLOCK.getId(unifiedEntry.block), color);
            }
            if (entitySelected && unifiedEntry.hasEntity()) {
                ModConfig.INSTANCE.setEntityColor(Registries.ENTITY_TYPE.getId(unifiedEntry.entityType), color);
            }
        }
        
        public void toggleItemSelection(boolean select) {
            if (unifiedEntry.hasItem()) {
                Identifier itemId = Registries.ITEM.getId(unifiedEntry.item);
                if (select != ModConfig.INSTANCE.isItemSelected(itemId)) {
                    int currentColor = getUnifiedColor();
                    ModConfig.INSTANCE.toggleItemSelection(itemId, currentColor);
                }
                this.itemSelected = ModConfig.INSTANCE.isItemSelected(itemId);
            }
        }
        
        public void toggleBlockSelection(boolean select) {
            if (unifiedEntry.hasBlock()) {
                Identifier blockId = Registries.BLOCK.getId(unifiedEntry.block);
                if (select != ModConfig.INSTANCE.isBlockSelected(blockId)) {
                    int currentColor = getUnifiedColor();
                    ModConfig.INSTANCE.toggleBlockSelection(blockId, currentColor);
                }
                this.blockSelected = ModConfig.INSTANCE.isBlockSelected(blockId);
            }
        }
        
        public void toggleEntitySelection(boolean select) {
            if (unifiedEntry.hasEntity()) {
                Identifier entityId = Registries.ENTITY_TYPE.getId(unifiedEntry.entityType);
                if (select != ModConfig.INSTANCE.isEntitySelected(entityId)) {
                    int currentColor = getUnifiedColor();
                    ModConfig.INSTANCE.toggleEntitySelection(entityId, currentColor);
                }
                this.entitySelected = ModConfig.INSTANCE.isEntitySelected(entityId);
            }
        }
        
        @Override
        public void setSelected(boolean selected) {
            int currentColor = getUnifiedColor();
            
            if (unifiedEntry.hasItem()) {
                Identifier itemId = Registries.ITEM.getId(unifiedEntry.item);
                if (selected != ModConfig.INSTANCE.isItemSelected(itemId)) {
                    ModConfig.INSTANCE.toggleItemSelection(itemId, currentColor);
                }
                this.itemSelected = ModConfig.INSTANCE.isItemSelected(itemId);
            }
            
            if (unifiedEntry.hasBlock()) {
                Identifier blockId = Registries.BLOCK.getId(unifiedEntry.block);
                if (selected != ModConfig.INSTANCE.isBlockSelected(blockId)) {
                    ModConfig.INSTANCE.toggleBlockSelection(blockId, currentColor);
                }
                this.blockSelected = ModConfig.INSTANCE.isBlockSelected(blockId);
            }
            
            if (unifiedEntry.hasEntity()) {
                Identifier entityId = Registries.ENTITY_TYPE.getId(unifiedEntry.entityType);
                if (selected != ModConfig.INSTANCE.isEntitySelected(entityId)) {
                    ModConfig.INSTANCE.toggleEntitySelection(entityId, currentColor);
                }
                this.entitySelected = ModConfig.INSTANCE.isEntitySelected(entityId);
            }
            
            // Ensure all selected types have the same color
            if (selected && (itemSelected || blockSelected || entitySelected)) {
                setUnifiedColor(currentColor);
            }
        }
        
        @Override
        public Text getNarration() {
            return Text.literal(unifiedEntry.name);
        }
    }
    
    // Color utility methods
    private static final int[] COLORS = {
        0xFFFF0000, // Red
        0xFF00FF00, // Green  
        0xFF0000FF, // Blue
        0xFFFFFF00, // Yellow
        0xFFFF00FF, // Magenta
        0xFF00FFFF, // Cyan
        0xFFFFFFFF, // White
        0xFFFF8000  // Orange
    };
    
    private static final String[] COLOR_NAMES = {
        "Red", "Green", "Blue", "Yellow", "Magenta", "Cyan", "White", "Orange"
    };
    
    private static String getColorName(int color) {
        for (int i = 0; i < COLORS.length; i++) {
            if (COLORS[i] == color) {
                return COLOR_NAMES[i];
            }
        }
        return "White";
    }
    
    private static int getNextColor(int currentColor) {
        for (int i = 0; i < COLORS.length; i++) {
            if (COLORS[i] == currentColor) {
                return COLORS[(i + 1) % COLORS.length];
            }
        }
        return COLORS[0]; // Default to red
    }
}