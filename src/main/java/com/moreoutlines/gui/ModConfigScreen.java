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
        
        // Scrollable list (leave extra space for column headers)
        this.list = new OutlineListWidget(this.client, this.width, this.height - margin * 2 - 15, margin + 15, 25);
        this.addDrawableChild(this.list);
        
        // Bottom buttons
        int buttonWidth = 80;
        int buttonHeight = 20;
        int numberOfButtons = 3; // All Items, All Entities, Done
        int buttonInterval = (this.width - numberOfButtons * buttonWidth) / (numberOfButtons + 1);
        int buttonY = this.height - 16 - (buttonHeight / 2);
        
        // All Items button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("All Items"),
                button -> toggleAllItems())
            .dimensions(buttonInterval, buttonY, buttonWidth, buttonHeight)
            .build());
        
        // All Entities button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("All Entities"),
                button -> toggleAllEntities())
            .dimensions(buttonInterval + (buttonWidth + buttonInterval), buttonY, buttonWidth, buttonHeight)
            .build());
            
        // Done button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Done"),
                button -> this.close())
            .dimensions(buttonInterval + (buttonWidth + buttonInterval) * 2, buttonY, buttonWidth, buttonHeight)
            .build());
        
        this.setInitialFocus(this.searchField);
        this.onSearchChanged(this.searchField.getText());
    }
    
    
    private void onSearchChanged(String searchText) {
        this.searchText = searchText.toLowerCase().trim();
        if (list != null) {
            list.updateSearchResults(this.searchText);
        }
    }
    
    private void toggleAllItems() {
        if (list != null) {
            list.toggleAllItems();
        }
    }
    
    private void toggleAllEntities() {
        if (list != null) {
            list.toggleAllEntities();
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