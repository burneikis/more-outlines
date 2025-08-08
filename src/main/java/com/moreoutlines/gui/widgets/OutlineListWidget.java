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

import java.util.*;
import java.util.stream.Collectors;

public class OutlineListWidget extends AlwaysSelectedEntryListWidget<OutlineListWidget.Entry> {
    public enum Tab { ITEMS, ENTITIES, BLOCKS }
    
    private final Tab currentTab;
    private Map<String, List<Object>> searchCache = new HashMap<>();
    
    public OutlineListWidget(net.minecraft.client.MinecraftClient client, int width, int height, int top, int itemHeight, Tab currentTab) {
        super(client, width, height, top, itemHeight);
        this.currentTab = currentTab;
        initializeSearchCache();
        updateSearchResults("");
    }
    
    public void updateSearchResults(String searchText) {
        this.clearEntries();
        
        if (searchCache.containsKey(searchText)) {
            List<Object> results = searchCache.get(searchText);
            
            for (Object obj : results) {
                switch (currentTab) {
                    case ITEMS:
                        if (obj instanceof Item) {
                            this.addEntry(new ItemEntry((Item) obj));
                        }
                        break;
                    case ENTITIES:
                        if (obj instanceof EntityType) {
                            this.addEntry(new EntityEntry((EntityType<?>) obj));
                        }
                        break;
                    case BLOCKS:
                        if (obj instanceof Block) {
                            this.addEntry(new BlockEntry((Block) obj));
                        }
                        break;
                }
            }
        }
        
        if (this.children().isEmpty()) {
            this.addEntry(new HeaderEntry("No results found"));
        }
    }
    
    private void initializeSearchCache() {
        searchCache.clear();
        
        List<Object> allResults = new ArrayList<>();
        searchCache.put("", allResults);
        
        switch (currentTab) {
            case ITEMS:
                List<Item> items = Registries.ITEM.stream()
                    .sorted((a, b) -> Registries.ITEM.getId(a).toString().compareTo(Registries.ITEM.getId(b).toString()))
                    .collect(Collectors.toList());
                
                for (Item item : items) {
                    String name = Registries.ITEM.getId(item).toString().toLowerCase();
                    allResults.add(item);
                    addToPrefixTree(name, item);
                }
                break;
                
            case ENTITIES:
                List<EntityType<?>> entities = Registries.ENTITY_TYPE.stream()
                    .sorted((a, b) -> Registries.ENTITY_TYPE.getId(a).toString().compareTo(Registries.ENTITY_TYPE.getId(b).toString()))
                    .collect(Collectors.toList());
                
                for (EntityType<?> entity : entities) {
                    String name = Registries.ENTITY_TYPE.getId(entity).toString().toLowerCase();
                    allResults.add(entity);
                    addToPrefixTree(name, entity);
                }
                break;
                
            case BLOCKS:
                List<Block> blocks = Registries.BLOCK.stream()
                    .sorted((a, b) -> Registries.BLOCK.getId(a).toString().compareTo(Registries.BLOCK.getId(b).toString()))
                    .collect(Collectors.toList());
                
                for (Block block : blocks) {
                    String name = Registries.BLOCK.getId(block).toString().toLowerCase();
                    allResults.add(block);
                    addToPrefixTree(name, block);
                }
                break;
        }
    }
    
    private void addToPrefixTree(String name, Object obj) {
        List<String> prefixes = new ArrayList<>();
        prefixes.add("");
        
        for (int i = 0; i < name.length(); i++) {
            char character = name.charAt(i);
            for (int p = 0; p < prefixes.size(); p++) {
                String prefix = prefixes.get(p) + character;
                prefixes.set(p, prefix);
                
                List<Object> results = searchCache.computeIfAbsent(prefix, k -> new ArrayList<>());
                results.add(obj);
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
    
    @Override
    public int getRowWidth() {
        return 350;
    }
    
    protected int getScrollbarX() {
        return this.width - 6;
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
                // Draw checkmark
                context.drawText(minecraft.textRenderer, "✓", checkboxX + 3, checkboxY + 4, 0xFFFFFFFF, false);
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
                context.drawText(minecraft.textRenderer, "✓", checkboxX + 3, checkboxY + 4, 0xFFFFFFFF, false);
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
                context.drawText(minecraft.textRenderer, "✓", checkboxX + 3, checkboxY + 4, 0xFFFFFFFF, false);
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