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

/**
 * Scans the world around the player for selected block types.
 * Uses a singleton pattern to maintain state across ticks.
 */
public class BlockSelectionScanner {
    private static final BlockSelectionScanner INSTANCE = new BlockSelectionScanner();
    private static final int SCAN_RADIUS = 32;
    
    private final Map<Identifier, Set<BlockPos>> trackedBlocksByType = new HashMap<>();
    
    private BlockSelectionScanner() {}
    
    public static BlockSelectionScanner getInstance() {
        return INSTANCE;
    }
    
    public void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return;
        }
        
        // Clear previous scan results
        trackedBlocksByType.clear();
        
        // Get player position and world
        BlockPos playerPos = client.player.getBlockPos();
        ClientWorld world = client.world;
        
        // Calculate Y bounds to avoid unnecessary scanning
        int minY = Math.max(playerPos.getY() - SCAN_RADIUS, world.getBottomY());
        int maxY = Math.min(playerPos.getY() + SCAN_RADIUS, world.getHeight() + world.getBottomY() - 1);
        
        // Scan for selected blocks within radius
        for (int x = playerPos.getX() - SCAN_RADIUS; x <= playerPos.getX() + SCAN_RADIUS; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = playerPos.getZ() - SCAN_RADIUS; z <= playerPos.getZ() + SCAN_RADIUS; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = world.getBlockState(pos);
                    
                    // Skip air blocks early
                    if (state.isAir()) {
                        continue;
                    }
                    
                    Identifier blockId = Registries.BLOCK.getId(state.getBlock());
                    
                    // Check if this block type is selected
                    if (ModConfig.INSTANCE.isBlockSelected(blockId)) {
                        trackedBlocksByType
                            .computeIfAbsent(blockId, k -> new HashSet<>())
                            .add(pos.toImmutable()); // Use immutable position for better memory efficiency
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