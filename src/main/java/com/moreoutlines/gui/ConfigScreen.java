package com.moreoutlines.gui;

import com.moreoutlines.config.ModConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ConfigScreen extends Screen {
    private final Screen parent;
    
    public ConfigScreen(Screen parent) {
        super(Text.literal("More Outlines Configuration"));
        this.parent = parent;
    }
    
    @Override
    protected void init() {
        super.init();
        
        int buttonWidth = 200;
        int buttonHeight = 20;
        int spacing = 25;
        int startY = this.height / 2 - (spacing * 4);
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Outlines Enabled: " + (ModConfig.INSTANCE.outlinesEnabled ? "ON" : "OFF")),
            button -> {
                ModConfig.INSTANCE.toggleOutlinesEnabled();
                button.setMessage(Text.literal("Outlines Enabled: " + (ModConfig.INSTANCE.outlinesEnabled ? "ON" : "OFF")));
            })
            .dimensions(this.width / 2 - buttonWidth / 2, startY, buttonWidth, buttonHeight)
            .build());
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Item Outlines: " + (ModConfig.INSTANCE.itemOutlines ? "ON" : "OFF")),
            button -> {
                ModConfig.INSTANCE.toggleItemOutlines();
                button.setMessage(Text.literal("Item Outlines: " + (ModConfig.INSTANCE.itemOutlines ? "ON" : "OFF")));
            })
            .dimensions(this.width / 2 - buttonWidth / 2, startY + spacing, buttonWidth, buttonHeight)
            .build());
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Entity Outlines: " + (ModConfig.INSTANCE.entityOutlines ? "ON" : "OFF")),
            button -> {
                ModConfig.INSTANCE.toggleEntityOutlines();
                button.setMessage(Text.literal("Entity Outlines: " + (ModConfig.INSTANCE.entityOutlines ? "ON" : "OFF")));
            })
            .dimensions(this.width / 2 - buttonWidth / 2, startY + spacing * 2, buttonWidth, buttonHeight)
            .build());
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Block Entity Outlines: " + (ModConfig.INSTANCE.blockEntityOutlines ? "ON" : "OFF")),
            button -> {
                ModConfig.INSTANCE.toggleBlockEntityOutlines();
                button.setMessage(Text.literal("Block Entity Outlines: " + (ModConfig.INSTANCE.blockEntityOutlines ? "ON" : "OFF")));
            })
            .dimensions(this.width / 2 - buttonWidth / 2, startY + spacing * 3, buttonWidth, buttonHeight)
            .build());
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Diamond Block Outlines: " + (ModConfig.INSTANCE.diamondBlockOutlines ? "ON" : "OFF")),
            button -> {
                ModConfig.INSTANCE.toggleDiamondBlockOutlines();
                button.setMessage(Text.literal("Diamond Block Outlines: " + (ModConfig.INSTANCE.diamondBlockOutlines ? "ON" : "OFF")));
            })
            .dimensions(this.width / 2 - buttonWidth / 2, startY + spacing * 4, buttonWidth, buttonHeight)
            .build());
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Done"),
            button -> this.close())
            .dimensions(this.width / 2 - buttonWidth / 2, startY + spacing * 6, buttonWidth, buttonHeight)
            .build());
    }
    
    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}