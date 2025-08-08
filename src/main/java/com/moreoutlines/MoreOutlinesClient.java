package com.moreoutlines;

import net.fabricmc.api.ClientModInitializer;

public class MoreOutlinesClient implements ClientModInitializer {
	
	@Override
	public void onInitializeClient() {
		MoreOutlines.LOGGER.info("More Outlines client initialized!");
	}
}