package com.moreoutlines.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

/**
 * Manages temporary notification messages displayed in the HUD.
 * Uses a singleton pattern to ensure consistent state.
 */
public class ToggleNotificationHud {
    private static final ToggleNotificationHud INSTANCE = new ToggleNotificationHud();
    private static final long NOTIFICATION_DURATION = 3000; // 3 seconds
    private static final float FADE_START_THRESHOLD = 0.7f; // Start fading at 70% of duration
    
    private String notificationText = "";
    private long notificationStartTime = 0;
    
    private ToggleNotificationHud() {}
    
    public static ToggleNotificationHud getInstance() {
        return INSTANCE;
    }
    
    /**
     * Shows a notification message for the specified duration.
     */
    public void showNotification(String text) {
        this.notificationText = text;
        this.notificationStartTime = System.currentTimeMillis();
    }
    
    /**
     * Renders the notification if one is active.
     */
    public void render(DrawContext context) {
        if (notificationText.isEmpty()) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - notificationStartTime;
        
        // Hide notification after duration
        if (elapsed >= NOTIFICATION_DURATION) {
            notificationText = "";
            return;
        }
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) {
            return;
        }
        
        // Calculate fade-out alpha
        float alpha = calculateFadeAlpha(elapsed);
        int color = ((int) (alpha * 255) << 24) | 0xFFFFFF; // White text with fade
        
        renderNotificationText(context, client, alpha, color);
    }
    
    /**
     * Calculates the fade-out alpha based on elapsed time.
     */
    private float calculateFadeAlpha(long elapsed) {
        float progress = (float) elapsed / NOTIFICATION_DURATION;
        if (progress > FADE_START_THRESHOLD) {
            return MathHelper.lerp((progress - FADE_START_THRESHOLD) / (1.0f - FADE_START_THRESHOLD), 1.0f, 0.0f);
        }
        return 1.0f;
    }
    
    /**
     * Renders the notification text with background.
     */
    private void renderNotificationText(DrawContext context, MinecraftClient client, float alpha, int color) {
        // Position in top-right area
        int screenWidth = context.getScaledWindowWidth();
        int textWidth = client.textRenderer.getWidth(notificationText);
        int x = screenWidth - textWidth - 10;
        int y = 10;
        
        // Draw background
        context.fill(x - 5, y - 2, x + textWidth + 5, y + client.textRenderer.fontHeight + 2, 
                     ((int) (alpha * 128) << 24) | 0x000000); // Semi-transparent black background
        
        // Draw text
        context.drawText(client.textRenderer, Text.literal(notificationText), x, y, color, false);
    }
    
    /**
     * Checks if there is an active notification being displayed.
     */
    public boolean hasActiveNotification() {
        return !notificationText.isEmpty() && 
               (System.currentTimeMillis() - notificationStartTime) < NOTIFICATION_DURATION;
    }
}
