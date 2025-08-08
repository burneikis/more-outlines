package com.moreoutlines.gui;

/**
 * More Outlines configuration screen inspired by ReEntityOutliner's design.
 * Clean, list-based interface with search functionality.
 * 
 * GUI design based on ReEntityOutliner by Globox_Z
 * https://github.com/Globox-Z/ReEntityOutliner
 * Used with inspiration for clean, user-friendly interface design.
 */

import com.moreoutlines.gui.widgets.OutlineListWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class ModConfigScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget searchField;
    private OutlineListWidget list;
    private String searchText = "";
    
    // Tab management
    public enum Tab { ITEMS, ENTITIES, BLOCKS }
    private Tab currentTab = Tab.ITEMS;
    
    public ModConfigScreen(Screen parent) {
        super(Text.literal("More Outlines Configuration"));
        this.parent = parent;
    }
    
    @Override
    protected void init() {
        super.init();
        
        final int margin = 35;
        
        // Search field at top
        this.searchField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 6, 200, 20, Text.literal("Search..."));
        this.searchField.setText(searchText);
        this.searchField.setChangedListener(this::onSearchChanged);
        this.addDrawableChild(this.searchField);
        
        // Outlines enabled toggle button at top right
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Outlines " + (com.moreoutlines.config.ModConfig.INSTANCE.outlinesEnabled ? "ON" : "OFF")),
                button -> {
                    com.moreoutlines.config.ModConfig.INSTANCE.toggleOutlinesEnabled();
                    button.setMessage(Text.literal("Outlines " + (com.moreoutlines.config.ModConfig.INSTANCE.outlinesEnabled ? "ON" : "OFF")));
                })
            .dimensions(this.width - 85, 6, 80, 20)
            .build());
        
        // Scrollable list
        this.list = new OutlineListWidget(this.client, this.width, this.height - margin * 2, margin, 25, convertTab(currentTab));
        this.addDrawableChild(this.list);
        
        // Bottom buttons
        int buttonWidth = 80;
        int buttonHeight = 20;
        int numberOfButtons = 6; // Items, Entities, Blocks, Deselect, Select, Done
        int buttonInterval = (this.width - numberOfButtons * buttonWidth) / (numberOfButtons + 1);
        int buttonY = this.height - 16 - (buttonHeight / 2);
        
        // Tab selection buttons
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal(currentTab == Tab.ITEMS ? "Items*" : "Items"),
                button -> switchToTab(Tab.ITEMS))
            .dimensions(buttonInterval, buttonY, buttonWidth, buttonHeight)
            .build());
            
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal(currentTab == Tab.ENTITIES ? "Entities*" : "Entities"),
                button -> switchToTab(Tab.ENTITIES))
            .dimensions(buttonInterval + (buttonWidth + buttonInterval), buttonY, buttonWidth, buttonHeight)
            .build());
            
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal(currentTab == Tab.BLOCKS ? "Blocks*" : "Blocks"),
                button -> switchToTab(Tab.BLOCKS))
            .dimensions(buttonInterval + (buttonWidth + buttonInterval) * 2, buttonY, buttonWidth, buttonHeight)
            .build());
        
        // Deselect all button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("All Off"),
                button -> deselectAllVisible())
            .dimensions(buttonInterval + (buttonWidth + buttonInterval) * 3, buttonY, buttonWidth, buttonHeight)
            .build());
        
        // Select all button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("All On"),
                button -> selectAllVisible())
            .dimensions(buttonInterval + (buttonWidth + buttonInterval) * 4, buttonY, buttonWidth, buttonHeight)
            .build());
            
        // Done button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Done"),
                button -> this.close())
            .dimensions(buttonInterval + (buttonWidth + buttonInterval) * 5, buttonY, buttonWidth, buttonHeight)
            .build());
        
        this.setInitialFocus(this.searchField);
        this.onSearchChanged(this.searchField.getText());
    }
    
    private OutlineListWidget.Tab convertTab(Tab tab) {
        switch (tab) {
            case ITEMS: return OutlineListWidget.Tab.ITEMS;
            case ENTITIES: return OutlineListWidget.Tab.ENTITIES;
            case BLOCKS: return OutlineListWidget.Tab.BLOCKS;
            default: return OutlineListWidget.Tab.ITEMS;
        }
    }
    
    private void switchToTab(Tab tab) {
        this.currentTab = tab;
        // Reinitialize to update display
        this.clearChildren();
        this.init();
    }
    
    private void onSearchChanged(String searchText) {
        this.searchText = searchText.toLowerCase().trim();
        if (list != null) {
            list.updateSearchResults(this.searchText);
        }
    }
    
    private void selectAllVisible() {
        if (list != null) {
            list.selectAllVisible();
        }
    }
    
    private void deselectAllVisible() {
        if (list != null) {
            list.deselectAllVisible();
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (list != null) {
            list.render(context, mouseX, mouseY, delta);
        }
        this.setFocused(this.searchField);
        if (searchField != null) {
            this.searchField.render(context, mouseX, mouseY, delta);
        }
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (list != null) {
            return list.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}