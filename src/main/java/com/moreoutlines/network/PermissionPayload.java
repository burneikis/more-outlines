package com.moreoutlines.network;

import com.moreoutlines.MoreOutlines;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record PermissionPayload(boolean allowed) implements CustomPayload {
    public static final CustomPayload.Id<PermissionPayload> ID = new CustomPayload.Id<>(Identifier.of(MoreOutlines.MOD_ID, "permission"));
    public static final PacketCodec<RegistryByteBuf, PermissionPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.BOOLEAN, PermissionPayload::allowed,
        PermissionPayload::new
    );
    
    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}