package com.moreoutlines.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Environment(EnvType.CLIENT)
public class ClientNetworking {
    public static void registerClientPackets() {
        ClientPlayNetworking.registerGlobalReceiver(PermissionPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                ServerPermissionManager.setServerPermission(payload.allowed());
            });
        });
    }
    
    public static void requestPermission() {
        if (ClientPlayNetworking.canSend(PermissionRequestPayload.ID)) {
            ClientPlayNetworking.send(new PermissionRequestPayload());
        } else {
            // Server doesn't have the mod, assume not allowed by default
            ServerPermissionManager.setServerPermission(false);
            com.moreoutlines.MoreOutlines.LOGGER.debug("Server doesn't support mod permission system, assuming not allowed");
        }
    }
}