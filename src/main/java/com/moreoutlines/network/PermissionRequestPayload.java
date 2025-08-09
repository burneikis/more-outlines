package com.moreoutlines.network;

import com.moreoutlines.MoreOutlines;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record PermissionRequestPayload() implements CustomPayload {
    public static final CustomPayload.Id<PermissionRequestPayload> ID = new CustomPayload.Id<>(Identifier.of(MoreOutlines.MOD_ID, "permission_request"));
    public static final PacketCodec<RegistryByteBuf, PermissionRequestPayload> CODEC = PacketCodec.unit(new PermissionRequestPayload());
    
    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}