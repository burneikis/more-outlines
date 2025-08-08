package com.moreoutlines.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class ToggleNotificationHud {
    private static ToggleNotificationHud instance;
    
    private String notificationText = "";
    private long notificationStartTime = 0;
    private static final long NOTIFICATION_DURATION = 3000; // 3 seconds
    
    public static ToggleNotificationHud getInstance() {
        if (instance == null) {
            instance = new ToggleNotificationHud();
        }
        return instance;
    }
    
    public void showNotification(String text) {
        this.notificationText = text;
        this.notificationStartTime = System.currentTimeMillis();
    }
    
    public void render(DrawContext context) {
        if (notificationText.isEmpty()) return;
        
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - notificationStartTime;
        
        if (elapsed >= NOTIFICATION_DURATION) {
            notificationText = "";
            return;
        }
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) return;
        
        // Calculate alpha for fade out effect
        float progress = (float) elapsed / NOTIFICATION_DURATION;
        float alpha = 1.0f;
        if (progress > 0.7f) {
            alpha = MathHelper.lerp((progress - 0.7f) / 0.3f, 1.0f, 0.0f);
        }
        
        int color = (int) (alpha * 255) << 24 | 0xFFFFFF; // White text with fade
        
        // Position in top-right area
        int screenWidth = context.getScaledWindowWidth();
        int textWidth = client.textRenderer.getWidth(notificationText);
        int x = screenWidth - textWidth - 10;
        int y = 10;
        
        // Draw background
        context.fill(x - 5, y - 2, x + textWidth + 5, y + client.textRenderer.fontHeight + 2, 
                     (int) (alpha * 128) << 24 | 0x000000); // Semi-transparent black background
        
        // Draw text
        context.drawText(client.textRenderer, Text.literal(notificationText), x, y, color, false);
    }
    
    public boolean hasActiveNotification() {
        return !notificationText.isEmpty() && 
               (System.currentTimeMillis() - notificationStartTime) < NOTIFICATION_DURATION;
    }
}
