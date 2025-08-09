package com.moreoutlines.network;

import com.moreoutlines.MoreOutlines;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class ModNetworking {
    public static void registerServerPackets() {
        ServerPlayConnectionEvents.JOIN.register(ModNetworking::onPlayerJoin);
        
        ServerPlayNetworking.registerGlobalReceiver(PermissionRequestPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> {
                sendPermissionResponse(player);
            });
        });
    }

    public static void registerClientPackets() {
        // This method will be called from client-side code
        // Implementation moved to client-specific class to avoid compile issues
    }

    private static void onPlayerJoin(ServerPlayNetworkHandler handler, net.fabricmc.fabric.api.networking.v1.PacketSender sender, MinecraftServer server) {
        sendPermissionResponse(handler.getPlayer());
    }

    private static void sendPermissionResponse(ServerPlayerEntity player) {
        boolean allowed = ServerPermissionConfig.isModAllowed();
        ServerPlayNetworking.send(player, new PermissionPayload(allowed));
    }
}