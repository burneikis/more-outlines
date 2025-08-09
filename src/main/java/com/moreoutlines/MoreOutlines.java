package com.moreoutlines;

import com.moreoutlines.network.ModNetworking;
import com.moreoutlines.network.ServerPermissionConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoreOutlines implements ModInitializer {
	public static final String MOD_ID = "more-outlines";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("More Outlines mod initialized!");
		
		// Register payload types first
		registerPayloadTypes();
		
		// Load server permission config and register networking
		ServerPermissionConfig.loadConfig();
		ModNetworking.registerServerPackets();
	}
	
	private void registerPayloadTypes() {
		net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(
			com.moreoutlines.network.PermissionPayload.ID, 
			com.moreoutlines.network.PermissionPayload.CODEC
		);
		net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playC2S().register(
			com.moreoutlines.network.PermissionRequestPayload.ID, 
			com.moreoutlines.network.PermissionRequestPayload.CODEC
		);
	}
}