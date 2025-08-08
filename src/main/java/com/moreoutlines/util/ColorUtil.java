package com.moreoutlines.util;

import net.minecraft.client.render.OutlineVertexConsumerProvider;

/**
 * Utility class for handling color operations.
 */
public final class ColorUtil {
    
    private ColorUtil() {
        // Utility class - no instantiation
    }
    
    /**
     * Sets the outline color from a color integer value.
     * 
     * @param outlineProvider The outline vertex consumer provider
     * @param color The color as an integer (ARGB format)
     */
    public static void setOutlineColor(OutlineVertexConsumerProvider outlineProvider, int color) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        int alpha = (color >> 24) & 0xFF;
        
        // Use full alpha if alpha is 0 (common case for RGB colors)
        if (alpha == 0) {
            alpha = 255;
        }
        
        outlineProvider.setColor(red, green, blue, alpha);
    }
    
    /**
     * Extracts the red component from a color integer.
     */
    public static int getRed(int color) {
        return (color >> 16) & 0xFF;
    }
    
    /**
     * Extracts the green component from a color integer.
     */
    public static int getGreen(int color) {
        return (color >> 8) & 0xFF;
    }
    
    /**
     * Extracts the blue component from a color integer.
     */
    public static int getBlue(int color) {
        return color & 0xFF;
    }
    
    /**
     * Extracts the alpha component from a color integer.
     */
    public static int getAlpha(int color) {
        return (color >> 24) & 0xFF;
    }
    
    /**
     * Creates a color from RGBA components.
     */
    public static int createColor(int red, int green, int blue, int alpha) {
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
    
    /**
     * Creates a color from RGB components with full alpha.
     */
    public static int createColor(int red, int green, int blue) {
        return createColor(red, green, blue, 255);
    }
}
