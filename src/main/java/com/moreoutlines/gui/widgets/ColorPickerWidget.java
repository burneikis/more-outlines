package com.moreoutlines.gui.widgets;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import java.util.function.IntConsumer;

public class ColorPickerWidget extends ClickableWidget {
    private static final int[] PRESET_COLORS = {
        0xFF0000, // Red
        0x00FF00, // Green  
        0x0000FF, // Blue
        0xFF00FF, // Magenta
        0x00FFFF, // Cyan
        0xFFFF00, // Yellow
        0xFFFFFF, // White
        0x000000  // Black
    };
    
    private final IntConsumer colorCallback;
    private int selectedColor = 0xFF0000;
    
    public ColorPickerWidget(int x, int y, IntConsumer colorCallback) {
        super(x, y, 150, 120, Text.literal("Color Picker"));
        this.colorCallback = colorCallback;
    }
    
    public void setColor(int color) {
        this.selectedColor = color;
    }
    
    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render color picker background
        context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xFF2D2D30);
        context.drawBorder(this.getX(), this.getY(), this.width, this.height, 0xFF555555);
        
        // Render title
        context.drawText(MinecraftClient.getInstance().textRenderer, "Colors:", this.getX() + 5, this.getY() + 3, 0xFFFFFFFF, false);
        
        // Render preset colors in a 2x3 + 1x2 layout
        int colorSize = 16;
        int spacing = 20;
        int startX = this.getX() + 5;
        int startY = this.getY() + 20; // Moved down to account for title
        
        // First two rows (3 colors each)
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                int index = row * 3 + col;
                if (index < PRESET_COLORS.length) {
                    int colorX = startX + col * spacing;
                    int colorY = startY + row * spacing;
                    
                    int color = PRESET_COLORS[index] | 0xFF000000; // Ensure alpha
                    context.fill(colorX, colorY, colorX + colorSize, colorY + colorSize, color);
                    
                    // Render selection border
                    if ((selectedColor & 0xFFFFFF) == (PRESET_COLORS[index] & 0xFFFFFF)) {
                        context.drawBorder(colorX - 1, colorY - 1, colorSize + 2, colorSize + 2, 0xFFFFFFFF);
                    }
                    
                    // Handle click
                    if (mouseX >= colorX && mouseX < colorX + colorSize && 
                        mouseY >= colorY && mouseY < colorY + colorSize) {
                        if (this.isHovered() && this.isMouseOver(mouseX, mouseY)) {
                            context.drawBorder(colorX - 1, colorY - 1, colorSize + 2, colorSize + 2, 0xFFCCCCCC);
                        }
                    }
                }
            }
        }
        
        // Third row (2 colors)
        for (int col = 0; col < 2; col++) {
            int index = 6 + col;
            if (index < PRESET_COLORS.length) {
                int colorX = startX + col * spacing;
                int colorY = startY + 2 * spacing;
                
                int color = PRESET_COLORS[index] | 0xFF000000; // Ensure alpha
                context.fill(colorX, colorY, colorX + colorSize, colorY + colorSize, color);
                
                // Render selection border
                if ((selectedColor & 0xFFFFFF) == (PRESET_COLORS[index] & 0xFFFFFF)) {
                    context.drawBorder(colorX - 1, colorY - 1, colorSize + 2, colorSize + 2, 0xFFFFFFFF);
                }
                
                // Handle click
                if (mouseX >= colorX && mouseX < colorX + colorSize && 
                    mouseY >= colorY && mouseY < colorY + colorSize) {
                    if (this.isHovered() && this.isMouseOver(mouseX, mouseY)) {
                        context.drawBorder(colorX - 1, colorY - 1, colorSize + 2, colorSize + 2, 0xFFCCCCCC);
                    }
                }
            }
        }
        
        // Show current color
        int currentColorX = startX + 80;
        int currentColorY = startY;
        context.drawText(MinecraftClient.getInstance().textRenderer, "Current:", currentColorX, startY - 15, 0xFFFFFFFF, false);
        context.fill(currentColorX, currentColorY, currentColorX + 32, currentColorY + 32, selectedColor | 0xFF000000);
        context.drawBorder(currentColorX - 1, currentColorY - 1, 34, 34, 0xFFFFFFFF);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left click
            int colorSize = 16;
            int spacing = 20;
            int startX = this.getX() + 5;
            int startY = this.getY() + 20; // Match the rendering startY
            
            // Check if click is anywhere within widget bounds first
            if (mouseX >= this.getX() && mouseX < this.getX() + this.width && 
                mouseY >= this.getY() && mouseY < this.getY() + this.height) {
                
                // Check preset colors
                for (int row = 0; row < 3; row++) {
                    int maxCols = (row < 2) ? 3 : 2;
                    for (int col = 0; col < maxCols; col++) {
                        int index = (row < 2) ? row * 3 + col : 6 + col;
                        if (index < PRESET_COLORS.length) {
                            int colorX = startX + col * spacing;
                            int colorY = startY + row * spacing;
                            
                            if (mouseX >= colorX && mouseX < colorX + colorSize && 
                                mouseY >= colorY && mouseY < colorY + colorSize) {
                                this.selectedColor = PRESET_COLORS[index] | 0xFF000000;
                                this.colorCallback.accept(this.selectedColor);
                                return true;
                            }
                        }
                    }
                }
                
                // If clicked within widget but not on a color, still consume the click
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    protected void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {
        builder.put(net.minecraft.client.gui.screen.narration.NarrationPart.TITLE, "Color Picker");
    }
}