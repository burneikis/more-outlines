package com.moreoutlines;

import com.moreoutlines.gui.ConfigScreen;
import com.moreoutlines.keybinds.ModKeybinds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

public class MoreOutlinesClient implements ClientModInitializer {
	
	@Override
	public void onInitializeClient() {
		MoreOutlines.LOGGER.info("More Outlines client initialized!");
		
		ModKeybinds.registerKeyBinds();
		
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			ModKeybinds.handleKeyPress();
			
			if (ModKeybinds.openConfigGui.wasPressed()) {
				MinecraftClient.getInstance().setScreen(new ConfigScreen(MinecraftClient.getInstance().currentScreen));
			}
		});
	}
}