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
                Text.literal("Outlines " + (com.moreoutlines.config.ModConfig.INSTANCE.isOutlinesEnabled() ? "ON" : "OFF")),
                button -> {
                    com.moreoutlines.config.ModConfig.INSTANCE.toggleOutlinesEnabled();
                    button.setMessage(Text.literal("Outlines " + (com.moreoutlines.config.ModConfig.INSTANCE.isOutlinesEnabled() ? "ON" : "OFF")));
                })
            .dimensions(this.width - 85, 6, 80, 20)
            .build());
        
        // Scrollable list (leave extra space for column headers)
        this.list = new OutlineListWidget(this.client, this.width, this.height - margin * 2 - 15, margin + 15, 25);
        this.addDrawableChild(this.list);
        
        // Bottom buttons
        int buttonWidth = 80;
        int buttonHeight = 20;
        int numberOfButtons = 4; // All Items, All Blocks, All Entities, Done
        int buttonInterval = (this.width - numberOfButtons * buttonWidth) / (numberOfButtons + 1);
        int buttonY = this.height - 16 - (buttonHeight / 2);
        
        // All Items button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("All Items"),
                button -> toggleAllItems())
            .dimensions(buttonInterval, buttonY, buttonWidth, buttonHeight)
            .build());
        
        // All Blocks button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("All Blocks"),
                button -> toggleAllBlocks())
            .dimensions(buttonInterval + (buttonWidth + buttonInterval), buttonY, buttonWidth, buttonHeight)
            .build());
        
        // All Entities button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("All Entities"),
                button -> toggleAllEntities())
            .dimensions(buttonInterval + (buttonWidth + buttonInterval) * 2, buttonY, buttonWidth, buttonHeight)
            .build());
            
        // Done button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Done"),
                button -> this.close())
            .dimensions(buttonInterval + (buttonWidth + buttonInterval) * 3, buttonY, buttonWidth, buttonHeight)
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
    
    private void toggleAllBlocks() {
        if (list != null) {
            list.toggleAllBlocks();
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
        if (searchField != null) {
            this.searchField.render(context, mouseX, mouseY, delta);
        }
    }
    
    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput input) {
        // Let the list handle arrow keys first
        if (list != null && list.keyPressed(input)) {
            return true;
        }
        // Then let the search field handle other keys
        if (searchField != null && searchField.isFocused() && searchField.keyPressed(input)) {
            return true;
        }
        return super.keyPressed(input);
    }
    
    @Override
    public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        // Check if clicking on the list area to focus it for keyboard navigation
        if (list != null && mouseX >= list.getX() && mouseX < list.getX() + list.getWidth() && 
            mouseY >= list.getY() && mouseY < list.getY() + list.getHeight()) {
            this.setFocused(list);
            return list.mouseClicked(click, doubled);
        }
        return super.mouseClicked(click, doubled);
    }
    
    @Override
    public boolean mouseDragged(net.minecraft.client.gui.Click click, double offsetX, double offsetY) {
        if (list != null) {
            return list.mouseDragged(click, offsetX, offsetY);
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }
    
    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}