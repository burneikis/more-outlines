package com.moreoutlines;

import com.moreoutlines.config.ModConfig;
import com.moreoutlines.gui.ModConfigScreen;
import com.moreoutlines.keybinds.ModKeybinds;
import com.moreoutlines.scanner.DiamondBlockScanner;
import com.moreoutlines.scanner.BlockSelectionScanner;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

public class MoreOutlinesClient implements ClientModInitializer {
	
	private static MoreOutlinesClient instance;
	
	@Override
	public void onInitializeClient() {
		instance = this;
		
		MoreOutlines.LOGGER.info("More Outlines client initialized!");
		
		// Add default selections for demonstration
		ModConfig.INSTANCE.addDefaultSelections();
		
		ModKeybinds.registerKeyBinds();
		
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			ModKeybinds.handleKeyPress();
			
			if (ModKeybinds.openConfigGui.wasPressed()) {
				MinecraftClient.getInstance().setScreen(new ModConfigScreen(MinecraftClient.getInstance().currentScreen));
			}
			
			// Handle diamond block scanning if enabled
			if (ModConfig.INSTANCE.outlinesEnabled && ModConfig.INSTANCE.diamondBlockOutlines) {
				DiamondBlockScanner.getInstance().tick(client);
			} else {
				// Clear tracked positions when disabled
				DiamondBlockScanner.getInstance().clearTrackedPositions();
			}
			
			// Handle block selection scanning if enabled and there are selections
			if (ModConfig.INSTANCE.outlinesEnabled && !ModConfig.INSTANCE.selectedBlocks.isEmpty()) {
				BlockSelectionScanner.getInstance().tick(client);
			} else {
				// Clear tracked positions when disabled
				BlockSelectionScanner.getInstance().clearTrackedPositions();
			}
		});
	}
	
	public static MoreOutlinesClient getInstance() {
		return instance;
	}
}