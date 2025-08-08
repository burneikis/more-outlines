package com.moreoutlines.gui;

/**
 * Complete GUI implementation for More Outlines mod configuration.
 * 
 * Features:
 * - Tabbed interface (Items, Entities, Blocks)
 * - Search functionality with live filtering
 * - Grid/list display of items/entities/blocks
 * - Color picker with 8 preset colors
 * - Toggle selection for individual items
 * - Scrolling support for large lists
 * - Real-time configuration updates
 * 
 * Usage:
 * - Use tabs to switch between Items, Entities, and Blocks
 * - Type in search bar to filter results
 * - Click on items to select them
 * - Use "Outline: ON/OFF" button to toggle outlines
 * - Click color swatches to change outline color
 * - Mouse wheel to scroll through lists
 */

import com.moreoutlines.config.ModConfig;
import com.moreoutlines.gui.widgets.ColorPickerWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.EntityType;
import net.minecraft.block.Block;

import java.util.*;
import java.util.stream.Collectors;

public class ModConfigScreen extends Screen {
    private static final int GUI_WIDTH = 480;
    private static final int GUI_HEIGHT = 300;
    private static final int TAB_HEIGHT = 20;
    private static final int HEADER_HEIGHT = 40;
    
    private final Screen parent;
    private int guiLeft;
    private int guiTop;
    
    // Tab management
    private enum Tab { ITEMS, ENTITIES, BLOCKS }
    private Tab currentTab = Tab.ITEMS;
    
    // Search functionality
    private TextFieldWidget searchField;
    private String searchText = "";
    
    // Selection state
    private Identifier selectedItem = null;
    private Identifier selectedEntity = null;
    private Identifier selectedBlock = null;
    
    // Scroll positions
    private int itemScrollPos = 0;
    private int entityScrollPos = 0;
    private int blockScrollPos = 0;
    
    // Cached lists
    private List<Item> filteredItems = new ArrayList<>();
    private List<EntityType<?>> filteredEntities = new ArrayList<>();
    private List<Block> filteredBlocks = new ArrayList<>();
    
    // Color picker
    private ColorPickerWidget colorPicker;
    
    // Toggle buttons
    private ButtonWidget toggleButton;
    private ButtonWidget toggleItemsButton;
    private ButtonWidget toggleEntitiesButton;
    private ButtonWidget toggleBlocksButton;
    
    public ModConfigScreen(Screen parent) {
        super(Text.literal("More Outlines Configuration"));
        this.parent = parent;
    }
    
    @Override
    protected void init() {
        super.init();
        
        this.guiLeft = (this.width - GUI_WIDTH) / 2;
        this.guiTop = (this.height - GUI_HEIGHT) / 2;
        
        // Initialize search field
        this.searchField = new TextFieldWidget(this.textRenderer, guiLeft + 10, guiTop + HEADER_HEIGHT + 5, GUI_WIDTH - 20, 20, Text.literal("Search..."));
        this.searchField.setPlaceholder(Text.literal("Search items, entities, or blocks..."));
        this.searchField.setChangedListener(this::onSearchChanged);
        this.searchField.setEditableColor(0xFFFFFFFF); // White text
        this.searchField.setUneditableColor(0xFFCCCCCC); // Light gray when not focused
        this.addSelectableChild(this.searchField);
        
        // Initialize color picker - positioned below the individual selection display
        this.colorPicker = new ColorPickerWidget(guiLeft + 255, guiTop + HEADER_HEIGHT + 125, this::onColorChanged);
        
        // Initialize toggle button (positioned at top right of selection panel)
        int panelX = guiLeft + 250;
        int panelY = guiTop + HEADER_HEIGHT + 30;
        this.toggleButton = ButtonWidget.builder(
            Text.literal("Outline: OFF"),
            button -> toggleCurrentSelection())
            .dimensions(panelX + 120, panelY + 5, 90, 18)
            .build();
        this.addDrawableChild(this.toggleButton);
        
        // Add tab-specific toggle buttons
        this.toggleItemsButton = ButtonWidget.builder(
            Text.literal("Items: " + (ModConfig.INSTANCE.itemOutlines ? "ON" : "OFF")),
            button -> toggleItemsOverlay())
            .dimensions(panelX + 5, panelY + 30, 80, 18)
            .build();
        this.addDrawableChild(this.toggleItemsButton);
        
        this.toggleEntitiesButton = ButtonWidget.builder(
            Text.literal("Entities: " + (ModConfig.INSTANCE.entityOutlines ? "ON" : "OFF")),
            button -> toggleEntitiesOverlay())
            .dimensions(panelX + 90, panelY + 30, 80, 18)
            .build();
        this.addDrawableChild(this.toggleEntitiesButton);
        
        this.toggleBlocksButton = ButtonWidget.builder(
            Text.literal("Blocks: " + (ModConfig.INSTANCE.blockOutlines ? "ON" : "OFF")),
            button -> toggleBlocksOverlay())
            .dimensions(panelX + 5, panelY + 55, 80, 18)
            .build();
        this.addDrawableChild(this.toggleBlocksButton);
        
        // Initialize tabs
        initializeTabs();
        
        // Initialize item lists
        updateFilteredLists();
        
        // Update button texts to reflect current config state
        updateToggleButtonTexts();
        
        // Add close button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Done"),
            button -> this.close())
            .dimensions(guiLeft + GUI_WIDTH - 60, guiTop + GUI_HEIGHT - 25, 50, 20)
            .build());
    }
    
    private void initializeTabs() {
        int tabWidth = 160;
        int startX = guiLeft;
        
        // Items tab
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Items" + (currentTab == Tab.ITEMS ? " [ACTIVE]" : "")),
            button -> switchToTab(Tab.ITEMS))
            .dimensions(startX, guiTop + 10, tabWidth, TAB_HEIGHT)
            .build());
            
        // Entities tab
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Entities" + (currentTab == Tab.ENTITIES ? " [ACTIVE]" : "")),
            button -> switchToTab(Tab.ENTITIES))
            .dimensions(startX + tabWidth, guiTop + 10, tabWidth, TAB_HEIGHT)
            .build());
            
        // Blocks tab
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Blocks" + (currentTab == Tab.BLOCKS ? " [ACTIVE]" : "")),
            button -> switchToTab(Tab.BLOCKS))
            .dimensions(startX + tabWidth * 2, guiTop + 10, tabWidth, TAB_HEIGHT)
            .build());
    }
    
    private void switchToTab(Tab tab) {
        this.currentTab = tab;
        updateFilteredLists();
        // Reinitialize to update tab display
        this.clearChildren();
        this.init();
        updateToggleButtonTexts();
    }
    
    private void onSearchChanged(String searchText) {
        this.searchText = searchText.toLowerCase();
        updateFilteredLists();
    }
    
    private void updateFilteredLists() {
        switch (currentTab) {
            case ITEMS:
                filteredItems = Registries.ITEM.stream()
                    .filter(item -> searchText.isEmpty() || 
                        Registries.ITEM.getId(item).toString().toLowerCase().contains(searchText))
                    .sorted((a, b) -> Registries.ITEM.getId(a).toString().compareTo(Registries.ITEM.getId(b).toString()))
                    .collect(Collectors.toList());
                break;
            case ENTITIES:
                filteredEntities = Registries.ENTITY_TYPE.stream()
                    .filter(entityType -> searchText.isEmpty() || 
                        Registries.ENTITY_TYPE.getId(entityType).toString().toLowerCase().contains(searchText))
                    .sorted((a, b) -> Registries.ENTITY_TYPE.getId(a).toString().compareTo(Registries.ENTITY_TYPE.getId(b).toString()))
                    .collect(Collectors.toList());
                break;
            case BLOCKS:
                filteredBlocks = Registries.BLOCK.stream()
                    .filter(block -> searchText.isEmpty() || 
                        Registries.BLOCK.getId(block).toString().toLowerCase().contains(searchText))
                    .sorted((a, b) -> Registries.BLOCK.getId(a).toString().compareTo(Registries.BLOCK.getId(b).toString()))
                    .collect(Collectors.toList());
                break;
        }
    }
    
    private void onColorChanged(int color) {
        switch (currentTab) {
            case ITEMS:
                if (selectedItem != null) {
                    ModConfig.INSTANCE.setItemColor(selectedItem, color);
                }
                break;
            case ENTITIES:
                if (selectedEntity != null) {
                    ModConfig.INSTANCE.setEntityColor(selectedEntity, color);
                }
                break;
            case BLOCKS:
                if (selectedBlock != null) {
                    ModConfig.INSTANCE.setBlockColor(selectedBlock, color);
                }
                break;
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render background
        context.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
        
        // Render main GUI background
        context.fill(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, 0xFF2D2D30);
        
        // Render header
        context.fill(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + HEADER_HEIGHT, 0xFF3C3C3C);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, guiLeft + GUI_WIDTH / 2, guiTop + 15, 0xFFFFFFFF);
        
        // Render tab background
        context.fill(guiLeft, guiTop + HEADER_HEIGHT, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, 0xFF1E1E1E);
        
        // Render search field background
        context.fill(guiLeft + 9, guiTop + HEADER_HEIGHT + 4, guiLeft + GUI_WIDTH - 9, guiTop + HEADER_HEIGHT + 26, 0xFF3C3C3C);
        
        // Render widgets
        super.render(context, mouseX, mouseY, delta);
        
        // Force render search field text and placeholder
        if (searchField != null) {
            if (!searchField.getText().isEmpty()) {
                context.drawText(this.textRenderer, searchField.getText(), 
                    searchField.getX() + 4, searchField.getY() + 6, 0xFFFFFFFF, false);
            } else if (!searchField.isFocused()) {
                // Show placeholder when empty and not focused
                context.drawText(this.textRenderer, "Search items, entities, or blocks...", 
                    searchField.getX() + 4, searchField.getY() + 6, 0xFF888888, false);
            }
        }
        
        // Render current tab content
        renderTabContent(context, mouseX, mouseY, delta);
        
        // Render selection panel
        renderSelectionPanel(context, mouseX, mouseY, delta);
    }
    
    private void renderTabContent(DrawContext context, int mouseX, int mouseY, float delta) {
        int contentX = guiLeft + 10;
        int contentY = guiTop + HEADER_HEIGHT + 30;
        int contentWidth = 230;
        int contentHeight = 200;
        
        // Render content background
        context.fill(contentX, contentY, contentX + contentWidth, contentY + contentHeight, 0xFF2D2D30);
        
        switch (currentTab) {
            case ITEMS:
                renderItemsGrid(context, contentX, contentY, contentWidth, contentHeight, mouseX, mouseY);
                break;
            case ENTITIES:
                renderEntitiesList(context, contentX, contentY, contentWidth, contentHeight, mouseX, mouseY);
                break;
            case BLOCKS:
                renderBlocksGrid(context, contentX, contentY, contentWidth, contentHeight, mouseX, mouseY);
                break;
        }
    }
    
    private void renderItemsGrid(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY) {
        int slotsPerRow = 10;
        int slotSize = 20;
        int rows = height / slotSize;
        int startIndex = itemScrollPos * slotsPerRow;
        
        for (int row = 0; row < rows && startIndex + row * slotsPerRow < filteredItems.size(); row++) {
            for (int col = 0; col < slotsPerRow && startIndex + row * slotsPerRow + col < filteredItems.size(); col++) {
                int index = startIndex + row * slotsPerRow + col;
                Item item = filteredItems.get(index);
                Identifier itemId = Registries.ITEM.getId(item);
                
                int slotX = x + col * slotSize + 2;
                int slotY = y + row * slotSize + 2;
                
                // Render slot background
                int bgColor = selectedItem != null && selectedItem.equals(itemId) ? 0xFF555555 : 0xFF3C3C3C;
                context.fill(slotX, slotY, slotX + 16, slotY + 16, bgColor);
                
                // Render item
                context.drawItem(new ItemStack(item), slotX, slotY);
                
                // Render enabled indicator
                if (ModConfig.INSTANCE.isItemSelected(itemId)) {
                    context.fill(slotX + 12, slotY, slotX + 16, slotY + 4, 0xFF00FF00);
                }
                
                // Handle hover
                if (mouseX >= slotX && mouseX < slotX + 16 && mouseY >= slotY && mouseY < slotY + 16) {
                    context.drawBorder(slotX - 1, slotY - 1, 18, 18, 0xFFFFFFFF);
                }
            }
        }
    }
    
    private void renderEntitiesList(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY) {
        int itemHeight = 20;
        int visibleItems = height / itemHeight;
        
        for (int i = 0; i < visibleItems && entityScrollPos + i < filteredEntities.size(); i++) {
            EntityType<?> entityType = filteredEntities.get(entityScrollPos + i);
            Identifier entityId = Registries.ENTITY_TYPE.getId(entityType);
            
            int itemY = y + i * itemHeight;
            
            // Render item background
            int bgColor = selectedEntity != null && selectedEntity.equals(entityId) ? 0xFF555555 : 0xFF3C3C3C;
            context.fill(x, itemY, x + width, itemY + itemHeight, bgColor);
            
            // Render entity name
            String name = entityId.toString();
            context.drawText(this.textRenderer, name, x + 5, itemY + 6, 0xFFFFFFFF, false);
            
            // Render enabled indicator
            if (ModConfig.INSTANCE.isEntitySelected(entityId)) {
                context.fill(x + width - 20, itemY + 2, x + width - 2, itemY + 6, 0xFF00FF00);
            }
            
            // Handle hover
            if (mouseX >= x && mouseX < x + width && mouseY >= itemY && mouseY < itemY + itemHeight) {
                context.drawBorder(x - 1, itemY - 1, width + 2, itemHeight + 2, 0xFFFFFFFF);
            }
        }
    }
    
    private void renderBlocksGrid(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY) {
        int slotsPerRow = 10;
        int slotSize = 20;
        int rows = height / slotSize;
        int startIndex = blockScrollPos * slotsPerRow;
        
        for (int row = 0; row < rows && startIndex + row * slotsPerRow < filteredBlocks.size(); row++) {
            for (int col = 0; col < slotsPerRow && startIndex + row * slotsPerRow + col < filteredBlocks.size(); col++) {
                int index = startIndex + row * slotsPerRow + col;
                Block block = filteredBlocks.get(index);
                Identifier blockId = Registries.BLOCK.getId(block);
                
                int slotX = x + col * slotSize + 2;
                int slotY = y + row * slotSize + 2;
                
                // Render slot background
                int bgColor = selectedBlock != null && selectedBlock.equals(blockId) ? 0xFF555555 : 0xFF3C3C3C;
                context.fill(slotX, slotY, slotX + 16, slotY + 16, bgColor);
                
                // Render block as item
                context.drawItem(new ItemStack(block), slotX, slotY);
                
                // Render enabled indicator
                if (ModConfig.INSTANCE.isBlockSelected(blockId)) {
                    context.fill(slotX + 12, slotY, slotX + 16, slotY + 4, 0xFF00FF00);
                }
                
                // Handle hover
                if (mouseX >= slotX && mouseX < slotX + 16 && mouseY >= slotY && mouseY < slotY + 16) {
                    context.drawBorder(slotX - 1, slotY - 1, 18, 18, 0xFFFFFFFF);
                }
            }
        }
    }
    
    private void renderSelectionPanel(DrawContext context, int mouseX, int mouseY, float delta) {
        int panelX = guiLeft + 250;
        int panelY = guiTop + HEADER_HEIGHT + 30;
        int panelWidth = 220;
        int panelHeight = 200;
        
        // Render panel background with border
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xFF2D2D30);
        context.drawBorder(panelX, panelY, panelWidth, panelHeight, 0xFF555555);
        
        // Render panel title
        context.drawText(this.textRenderer, "Selection Panel", panelX + 5, panelY + 5, 0xFFFFFFFF, false);
        
        // Add section label
        context.drawText(this.textRenderer, "Category Toggles:", panelX + 5, panelY + 18, 0xFFCCCCCC, false);
        
        Identifier currentSelection = getCurrentSelection();
        if (currentSelection != null) {
            String name = currentSelection.toString();
            context.drawText(this.textRenderer, "Selected: " + name, panelX + 5, panelY + 80, 0xFFFFFFFF, false);
            
            // Update toggle button text
            boolean isEnabled = isCurrentSelectionEnabled();
            toggleButton.setMessage(Text.literal("Outline: " + (isEnabled ? "ON" : "OFF")));
        } else {
            toggleButton.setMessage(Text.literal("Outline: OFF"));
            context.drawText(this.textRenderer, "Click an item", panelX + 5, panelY + 80, 0xFF888888, false);
            context.drawText(this.textRenderer, "to configure", panelX + 5, panelY + 95, 0xFF888888, false);
            context.drawText(this.textRenderer, "its outline", panelX + 5, panelY + 110, 0xFF888888, false);
        }
        
        // Force render toggle buttons manually
        if (toggleButton != null) {
            toggleButton.render(context, mouseX, mouseY, delta);
        }
        if (toggleItemsButton != null) {
            toggleItemsButton.render(context, mouseX, mouseY, delta);
        }
        if (toggleEntitiesButton != null) {
            toggleEntitiesButton.render(context, mouseX, mouseY, delta);
        }
        if (toggleBlocksButton != null) {
            toggleBlocksButton.render(context, mouseX, mouseY, delta);
        }
        
        // Render color picker
        if (colorPicker != null) {
            colorPicker.render(context, mouseX, mouseY, delta);
        }
    }
    
    private Identifier getCurrentSelection() {
        switch (currentTab) {
            case ITEMS: return selectedItem;
            case ENTITIES: return selectedEntity;
            case BLOCKS: return selectedBlock;
            default: return null;
        }
    }
    
    private boolean isCurrentSelectionEnabled() {
        switch (currentTab) {
            case ITEMS: return selectedItem != null && ModConfig.INSTANCE.isItemSelected(selectedItem);
            case ENTITIES: return selectedEntity != null && ModConfig.INSTANCE.isEntitySelected(selectedEntity);
            case BLOCKS: return selectedBlock != null && ModConfig.INSTANCE.isBlockSelected(selectedBlock);
            default: return false;
        }
    }
    
    private void toggleCurrentSelection() {
        switch (currentTab) {
            case ITEMS:
                if (selectedItem != null) {
                    // Use current color picker color instead of default color
                    int currentColor = colorPicker != null ? colorPicker.getCurrentColor() : ModConfig.INSTANCE.itemOutlineColor;
                    ModConfig.INSTANCE.toggleItemSelection(selectedItem, currentColor);
                }
                break;
            case ENTITIES:
                if (selectedEntity != null) {
                    // Use current color picker color instead of default color
                    int currentColor = colorPicker != null ? colorPicker.getCurrentColor() : ModConfig.INSTANCE.entityOutlineColor;
                    ModConfig.INSTANCE.toggleEntitySelection(selectedEntity, currentColor);
                }
                break;
            case BLOCKS:
                if (selectedBlock != null) {
                    // Use current color picker color instead of default color
                    int currentColor = colorPicker != null ? colorPicker.getCurrentColor() : ModConfig.INSTANCE.blockOutlineColor;
                    ModConfig.INSTANCE.toggleBlockSelection(selectedBlock, currentColor);
                }
                break;
        }
    }
    
    private void toggleItemsOverlay() {
        ModConfig.INSTANCE.toggleItemOutlines();
        updateToggleButtonTexts();
    }
    
    private void toggleEntitiesOverlay() {
        ModConfig.INSTANCE.toggleEntityOutlines();
        updateToggleButtonTexts();
    }
    
    private void toggleBlocksOverlay() {
        ModConfig.INSTANCE.toggleBlockOutlines();
        updateToggleButtonTexts();
    }
    
    private void updateToggleButtonTexts() {
        if (toggleItemsButton != null) {
            toggleItemsButton.setMessage(Text.literal("Items: " + (ModConfig.INSTANCE.itemOutlines ? "ON" : "OFF")));
        }
        if (toggleEntitiesButton != null) {
            toggleEntitiesButton.setMessage(Text.literal("Entities: " + (ModConfig.INSTANCE.entityOutlines ? "ON" : "OFF")));
        }
        if (toggleBlocksButton != null) {
            toggleBlocksButton.setMessage(Text.literal("Blocks: " + (ModConfig.INSTANCE.blockOutlines ? "ON" : "OFF")));
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Check toggle buttons first
        if (toggleButton != null && toggleButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (toggleItemsButton != null && toggleItemsButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (toggleEntitiesButton != null && toggleEntitiesButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (toggleBlocksButton != null && toggleBlocksButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        
        // Check color picker
        if (colorPicker != null && colorPicker.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        
        if (button == 0) { // Left click
            // Check if click is in content area
            int contentX = guiLeft + 10;
            int contentY = guiTop + HEADER_HEIGHT + 30;
            int contentWidth = 230;
            int contentHeight = 200;
            
            if (mouseX >= contentX && mouseX < contentX + contentWidth && 
                mouseY >= contentY && mouseY < contentY + contentHeight) {
                
                switch (currentTab) {
                    case ITEMS:
                        return handleItemClick(mouseX, mouseY, contentX, contentY);
                    case ENTITIES:
                        return handleEntityClick(mouseX, mouseY, contentX, contentY);
                    case BLOCKS:
                        return handleBlockClick(mouseX, mouseY, contentX, contentY);
                }
            }
        }
        
        return false;
    }
    
    private boolean handleItemClick(double mouseX, double mouseY, int contentX, int contentY) {
        int slotsPerRow = 10;
        int slotSize = 20;
        int startIndex = itemScrollPos * slotsPerRow;
        
        int relX = (int)(mouseX - contentX - 2);
        int relY = (int)(mouseY - contentY - 2);
        
        int col = relX / slotSize;
        int row = relY / slotSize;
        
        if (col >= 0 && col < slotsPerRow && row >= 0) {
            int index = startIndex + row * slotsPerRow + col;
            if (index < filteredItems.size()) {
                Item item = filteredItems.get(index);
                Identifier itemId = Registries.ITEM.getId(item);
                selectedItem = itemId;
                colorPicker.setColor(ModConfig.INSTANCE.getItemColor(itemId));
                return true;
            }
        }
        
        return false;
    }
    
    private boolean handleEntityClick(double mouseX, double mouseY, int contentX, int contentY) {
        int itemHeight = 20;
        int relY = (int)(mouseY - contentY);
        int index = entityScrollPos + (relY / itemHeight);
        
        if (index >= 0 && index < filteredEntities.size()) {
            EntityType<?> entityType = filteredEntities.get(index);
            Identifier entityId = Registries.ENTITY_TYPE.getId(entityType);
            selectedEntity = entityId;
            colorPicker.setColor(ModConfig.INSTANCE.getEntityColor(entityId));
            return true;
        }
        
        return false;
    }
    
    private boolean handleBlockClick(double mouseX, double mouseY, int contentX, int contentY) {
        int slotsPerRow = 10;
        int slotSize = 20;
        int startIndex = blockScrollPos * slotsPerRow;
        
        int relX = (int)(mouseX - contentX - 2);
        int relY = (int)(mouseY - contentY - 2);
        
        int col = relX / slotSize;
        int row = relY / slotSize;
        
        if (col >= 0 && col < slotsPerRow && row >= 0) {
            int index = startIndex + row * slotsPerRow + col;
            if (index < filteredBlocks.size()) {
                Block block = filteredBlocks.get(index);
                Identifier blockId = Registries.BLOCK.getId(block);
                selectedBlock = blockId;
                colorPicker.setColor(ModConfig.INSTANCE.getBlockColor(blockId));
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // Handle scrolling for current tab
        switch (currentTab) {
            case ITEMS:
                if (verticalAmount > 0 && itemScrollPos > 0) {
                    itemScrollPos--;
                } else if (verticalAmount < 0) {
                    itemScrollPos++;
                }
                break;
            case ENTITIES:
                if (verticalAmount > 0 && entityScrollPos > 0) {
                    entityScrollPos--;
                } else if (verticalAmount < 0 && entityScrollPos < filteredEntities.size() - 1) {
                    entityScrollPos++;
                }
                break;
            case BLOCKS:
                if (verticalAmount > 0 && blockScrollPos > 0) {
                    blockScrollPos--;
                } else if (verticalAmount < 0) {
                    blockScrollPos++;
                }
                break;
        }
        return true;
    }
    
    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}