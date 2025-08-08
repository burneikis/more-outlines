package com.moreoutlines.scanner;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DiamondBlockScanner {
    
    private static DiamondBlockScanner instance;
    
    private int scanRadius = 32; // Blocks to scan around player
    private int scanRate = 20; // Ticks between scans (20 = 1 second)
    private int tickCounter = 0;
    private final Set<BlockPos> trackedBlockPositions = ConcurrentHashMap.newKeySet();
    private final Set<BlockPos> previousPositions = new HashSet<>();
    
    public static DiamondBlockScanner getInstance() {
        if (instance == null) {
            instance = new DiamondBlockScanner();
        }
        return instance;
    }
    
    public void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return;
        }
        
        tickCounter++;
        if (tickCounter % scanRate == 0) {
            scanForDiamondBlocks(client);
            tickCounter = 0; // Reset counter to prevent overflow
        }
    }
    
    private void scanForDiamondBlocks(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return;
        }
        
        ClientWorld world = client.world;
        BlockPos playerPos = client.player.getBlockPos();
        Set<BlockPos> currentPositions = new HashSet<>();
        
        // Scan in a cube around the player
        for (int x = -scanRadius; x <= scanRadius; x++) {
            for (int y = -scanRadius; y <= scanRadius; y++) {
                for (int z = -scanRadius; z <= scanRadius; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    
                    // Check if this position has a diamond block
                    BlockState blockState = world.getBlockState(pos);
                    if (blockState.isOf(Blocks.DIAMOND_BLOCK)) {
                        currentPositions.add(pos.toImmutable());
                    }
                }
            }
        }
        
        // Update tracked positions
        synchronized (trackedBlockPositions) {
            trackedBlockPositions.clear();
            trackedBlockPositions.addAll(currentPositions);
        }
        
        // Update previous positions for change detection
        previousPositions.clear();
        previousPositions.addAll(currentPositions);
    }
    
    public Set<BlockPos> getTrackedBlockPositions() {
        return new HashSet<>(trackedBlockPositions);
    }
    
    public void clearTrackedPositions() {
        trackedBlockPositions.clear();
        previousPositions.clear();
    }
    
    public int getScanRadius() {
        return scanRadius;
    }
    
    public void setScanRadius(int radius) {
        this.scanRadius = Math.max(8, Math.min(64, radius));
    }
    
    public int getScanRate() {
        return scanRate;
    }
    
    public void setScanRate(int rate) {
        this.scanRate = Math.max(5, Math.min(100, rate));
    }
}