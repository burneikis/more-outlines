package com.moreoutlines.scanner;

import com.moreoutlines.config.ModConfig;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.Registries;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BlockSelectionScanner {
    private static final BlockSelectionScanner INSTANCE = new BlockSelectionScanner();
    private final Map<Identifier, Set<BlockPos>> trackedBlocksByType = new HashMap<>();
    private final int SCAN_RADIUS = 32;
    
    public static BlockSelectionScanner getInstance() {
        return INSTANCE;
    }
    
    public void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return;
        }
        
        // Clear previous scan results
        trackedBlocksByType.clear();
        
        // Get player position
        BlockPos playerPos = client.player.getBlockPos();
        ClientWorld world = client.world;
        
        // Scan for selected blocks
        for (int x = -SCAN_RADIUS; x <= SCAN_RADIUS; x++) {
            for (int y = -SCAN_RADIUS; y <= SCAN_RADIUS; y++) {
                for (int z = -SCAN_RADIUS; z <= SCAN_RADIUS; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    BlockState state = world.getBlockState(pos);
                    
                    if (!state.isAir()) {
                        Identifier blockId = Registries.BLOCK.getId(state.getBlock());
                        
                        // Check if this block type is selected
                        if (ModConfig.INSTANCE.isBlockSelected(blockId)) {
                            trackedBlocksByType
                                .computeIfAbsent(blockId, k -> new HashSet<>())
                                .add(pos);
                        }
                    }
                }
            }
        }
    }
    
    public Map<Identifier, Set<BlockPos>> getTrackedBlocksByType() {
        return trackedBlocksByType;
    }
    
    public void clearTrackedPositions() {
        trackedBlocksByType.clear();
    }
}