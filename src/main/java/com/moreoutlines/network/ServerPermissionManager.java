package com.moreoutlines.network;

import com.moreoutlines.MoreOutlines;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class ServerPermissionManager {
    private static boolean serverPermissionReceived = false;
    private static boolean serverAllowsMod = false; // Default: assume NOT allowed until told otherwise
    private static String serverMessage = "";
    
    public static void requestPermission() {
        // Use ClientNetworking to avoid compilation issues
        ClientNetworking.requestPermission();
        MoreOutlines.LOGGER.debug("Requested server permission for More Outlines mod");
    }
    
    public static void setServerPermission(boolean allowed) {
        serverPermissionReceived = true;
        serverAllowsMod = allowed;
        
        if (allowed) {
            MoreOutlines.LOGGER.info("Server allows More Outlines mod usage");
        } else {
            MoreOutlines.LOGGER.warn("Server blocks More Outlines mod usage");
            showPermissionMessage();
        }
    }
    
    public static boolean isModAllowed() {
        return serverAllowsMod;
    }
    
    public static boolean hasReceivedPermission() {
        return serverPermissionReceived;
    }
    
    private static void showPermissionMessage() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§c[More Outlines] This server doesn't allow the More Outlines mod"), false);
        }
    }
    
    public static void reset() {
        serverPermissionReceived = false;
        serverAllowsMod = false; // Default back to not allowed
        serverMessage = "";
    }
}