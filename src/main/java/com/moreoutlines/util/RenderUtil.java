package com.moreoutlines.util;

import net.minecraft.client.gui.DrawContext;

/**
 * Rendering helpers for compatibility across Minecraft versions.
 */
public final class RenderUtil {

    private RenderUtil() {
    }

    /**
     * Draws a 1px border rectangle. Replacement for the removed
     * {@code DrawContext.drawBorder} method.
     *
     * @param context the draw context
     * @param x       left coordinate
     * @param y       top coordinate
     * @param width   width of the bordered region
     * @param height  height of the bordered region
     * @param color   ARGB color
     */
    public static void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        // Top
        context.fill(x, y, x + width, y + 1, color);
        // Bottom
        context.fill(x, y + height - 1, x + width, y + height, color);
        // Left
        context.fill(x, y + 1, x + 1, y + height - 1, color);
        // Right
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }
}
