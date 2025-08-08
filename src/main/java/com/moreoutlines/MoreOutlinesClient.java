package com.moreoutlines;

import com.moreoutlines.config.ConfigManager;
import com.moreoutlines.config.ConfigUtil;
import com.moreoutlines.config.ModConfig;
import com.moreoutlines.gui.ModConfigScreen;
import com.moreoutlines.keybinds.ModKeybinds;
import com.moreoutlines.scanner.BlockSelectionScanner;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

/**
 * Client-side initialization for the More Outlines mod.
 * Handles keybind registration and client tick events.
 */
public class MoreOutlinesClient implements ClientModInitializer {
	
	private static MoreOutlinesClient instance;
	
	@Override
	public void onInitializeClient() {
		instance = this;
		
		// Load configuration first
		ConfigManager.loadConfig();
		
		// Validate configuration after loading
		ConfigUtil.validateConfig();
		ConfigUtil.logConfigStats();
		
		MoreOutlines.LOGGER.info("More Outlines client initialized!");
		
		initializeKeybinds();
		registerClientTickEvents();
	}
	
	/**
	 * Initialize keybinds for the mod.
	 */
	private void initializeKeybinds() {
		ModKeybinds.registerKeyBinds();
	}
	
	/**
	 * Register client tick event handlers.
	 */
	private void registerClientTickEvents() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			handleKeybinds(client);
			handleBlockScanning(client);
		});
	}
	
	/**
	 * Handle keybind processing.
	 */
	private void handleKeybinds(MinecraftClient client) {
		ModKeybinds.handleKeyPress();
		
		if (ModKeybinds.openConfigGui.wasPressed()) {
			client.setScreen(new ModConfigScreen(client.currentScreen));
		}
	}
	
	/**
	 * Handle block selection scanning.
	 */
	private void handleBlockScanning(MinecraftClient client) {
		if (ModConfig.INSTANCE.outlinesEnabled && !ModConfig.INSTANCE.selectedBlocks.isEmpty()) {
			BlockSelectionScanner.getInstance().tick(client);
		} else {
			// Clear tracked positions when disabled
			BlockSelectionScanner.getInstance().clearTrackedPositions();
		}
	}
	
	public static MoreOutlinesClient getInstance() {
		return instance;
	}
}