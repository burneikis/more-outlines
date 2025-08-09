package com.moreoutlines.gui.widgets;

import com.moreoutlines.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.function.IntConsumer;

public class ColorPickerWidget extends ClickableWidget {
    private static final int[] PRESET_COLORS = {
        0xFFFFFF, // White
        0xFF0000, // Red
        0x00FF00, // Green  
        0x0000FF, // Blue
        0xFF00FF, // Magenta
        0x00FFFF, // Cyan
        0xFFFF00, // Yellow
        0x000000  // Black
    };
    
    private final IntConsumer colorCallback;
    private int selectedColor = 0xFFFFFF;
    
    public ColorPickerWidget(int x, int y, IntConsumer colorCallback) {
        super(x, y, 130, 60, Text.literal("Color Picker"));
        this.colorCallback = colorCallback;
    }
    
    public void setColor(int color) {
        this.selectedColor = color;
    }
    
    /**
     * Gets the currently selected color from the color picker.
     * @return The selected color as an integer (ARGB format)
     */
    public int getCurrentColor() {
        return this.selectedColor;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left click
            int colorSize = 12;
            int spacing = 16;
            int startX = this.getX() + 5;
            int startY = this.getY() + 20; // Match the rendering startY
            
            // Check if click is anywhere within widget bounds first
            if (mouseX >= this.getX() && mouseX < this.getX() + this.width && 
                mouseY >= this.getY() && mouseY < this.getY() + this.height) {
                
                // Check preset colors in 2x4 layout
                for (int row = 0; row < 2; row++) {
                    for (int col = 0; col < 4; col++) {
                        int index = row * 4 + col;
                        if (index < PRESET_COLORS.length) {
                            int colorX = startX + col * spacing;
                            int colorY = startY + row * spacing;
                            
                            if (mouseX >= colorX && mouseX < colorX + colorSize && 
                                mouseY >= colorY && mouseY < colorY + colorSize) {
                                this.selectedColor = ColorUtil.createColor(
                                    ColorUtil.getRed(PRESET_COLORS[index]),
                                    ColorUtil.getGreen(PRESET_COLORS[index]),
                                    ColorUtil.getBlue(PRESET_COLORS[index]),
                                    255
                                );
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
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render color picker background
        context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xFF2D2D30);
        context.drawBorder(this.getX(), this.getY(), this.width, this.height, 0xFF555555);
        
        // Render title
        context.drawText(MinecraftClient.getInstance().textRenderer, "Colors:", this.getX() + 5, this.getY() + 3, 0xFFFFFFFF, false);
        
        // Render preset colors in a 2x4 layout (more compact)
        int colorSize = 12;
        int spacing = 16;
        int startX = this.getX() + 5;
        int startY = this.getY() + 20; // Moved down to account for title
        
        // Two rows (4 colors each)
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 4; col++) {
                int index = row * 4 + col;
                if (index < PRESET_COLORS.length) {
                    int colorX = startX + col * spacing;
                    int colorY = startY + row * spacing;
                    int color = ColorUtil.createColor(
                        ColorUtil.getRed(PRESET_COLORS[index]),
                        ColorUtil.getGreen(PRESET_COLORS[index]),
                        ColorUtil.getBlue(PRESET_COLORS[index]),
                        255
                    );
                    context.fill(colorX, colorY, colorX + colorSize, colorY + colorSize, color);
                    
                    // Render selection border
                    if ((selectedColor & 0xFFFFFF) == (PRESET_COLORS[index] & 0xFFFFFF)) {
                        context.drawBorder(colorX - 1, colorY - 1, colorSize + 2, colorSize + 2, 0xFFFFFFFF);
                    }
                    
                    // Handle hover
                    if (mouseX >= colorX && mouseX < colorX + colorSize && 
                        mouseY >= colorY && mouseY < colorY + colorSize) {
                        if (this.isHovered() && this.isMouseOver(mouseX, mouseY)) {
                            context.drawBorder(colorX - 1, colorY - 1, colorSize + 2, colorSize + 2, 0xFFCCCCCC);
                        }
                    }
                }
            }
        }
        
        // Show current color
        int currentColorX = startX + 70;
        int currentColorY = startY;
        context.drawText(MinecraftClient.getInstance().textRenderer, "Current:", currentColorX, startY - 15, 0xFFFFFFFF, false);
        context.fill(currentColorX, currentColorY, currentColorX + 24, currentColorY + 24, selectedColor);
        context.drawBorder(currentColorX - 1, currentColorY - 1, 26, 26, 0xFFFFFFFF);
    }
    
    @Override
    protected void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {
        builder.put(net.minecraft.client.gui.screen.narration.NarrationPart.TITLE, "Color Picker");
    }
}