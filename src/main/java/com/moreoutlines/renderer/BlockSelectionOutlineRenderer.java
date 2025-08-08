package com.moreoutlines.renderer;

import com.moreoutlines.config.ModConfig;
import com.moreoutlines.util.ColorUtil;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class BlockSelectionOutlineRenderer {
    
    public static void renderBlockSelectionOutlines(
        MatrixStack matrices,
        Camera camera,
        OutlineVertexConsumerProvider outlineProvider,
        World world,
        Map<Identifier, Set<BlockPos>> blocksByType
    ) {
        if (blocksByType.isEmpty()) {
            return;
        }
        
        // Get camera position for relative positioning
        Vec3d cameraPos = camera.getPos();
        
        // Get block render manager and block entity render dispatcher
        BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
        BlockEntityRenderDispatcher blockEntityRenderDispatcher = MinecraftClient.getInstance().getBlockEntityRenderDispatcher();
        
        // Render each block type with its specific color
        for (Map.Entry<Identifier, Set<BlockPos>> entry : blocksByType.entrySet()) {
            Identifier blockId = entry.getKey();
            Set<BlockPos> positions = entry.getValue();
            
            if (positions.isEmpty()) {
                continue;
            }
            
            // Get the color for this block type and set it
            int color = ModConfig.INSTANCE.getBlockColor(blockId);
            ColorUtil.setOutlineColor(outlineProvider, color);
            
            // Render each block of this type
            for (BlockPos pos : positions) {
                BlockState state = world.getBlockState(pos);
                if (!state.isAir()) {
                    
                    matrices.push();
                    
                    // Translate to block position relative to camera
                    matrices.translate(
                        pos.getX() - cameraPos.x,
                        pos.getY() - cameraPos.y,
                        pos.getZ() - cameraPos.z
                    );
                    
                    // Check if this is a block entity
                    BlockEntity blockEntity = world.getBlockEntity(pos);
                    if (blockEntity != null) {
                        // Render block entity using proper block entity methods
                        renderBlockEntityOutline(blockEntity, matrices, outlineProvider, blockEntityRenderDispatcher);
                    } else {
                        // Render regular block with invisible faces but preserve outline capability
                        renderInvisibleBlock(state, pos, matrices, outlineProvider, blockRenderManager, world);
                    }
                    
                    matrices.pop();
                }
            }
            
            // Draw all buffered outline vertices for this color
            outlineProvider.draw();
        }
    }
    
    // Render block entity outline using proper block entity methods
    private static void renderBlockEntityOutline(
        BlockEntity blockEntity,
        MatrixStack matrices,
        OutlineVertexConsumerProvider outlineProvider,
        BlockEntityRenderDispatcher blockEntityRenderDispatcher
    ) {
        // Render the block entity using the dispatcher - this will properly handle
        // block entity rendering and outline generation
        blockEntityRenderDispatcher.render(blockEntity, 0.0f, matrices, outlineProvider);
    }
    
    // Render invisible block method for outline rendering
    private static void renderInvisibleBlock(
        BlockState blockState, 
        BlockPos blockPos, 
        MatrixStack matrixStack, 
        OutlineVertexConsumerProvider outlineProvider,
        BlockRenderManager blockRenderManager,
        World world
    ) {
        if (blockState.getRenderType() != BlockRenderType.MODEL) {
            return;
        }

        // Generate the block model parts using the block's render seed
        List<BlockModelPart> modelParts = blockRenderManager
                .getModel(blockState)
                .getParts(Random.create(blockState.getRenderingSeed(blockPos)));

        // Use EntityTranslucent render layer - supports outlines and alpha blending
        RenderLayer renderLayer = RenderLayer.getEntityTranslucent(
            Identifier.of("minecraft", "textures/atlas/blocks.png"), 
            true
        );
        VertexConsumer vertexConsumer = outlineProvider.getBuffer(renderLayer);

        // Wrap with InvisibleVertexConsumer to make faces transparent
        InvisibleVertexConsumer invisibleConsumer = new InvisibleVertexConsumer(vertexConsumer);

        // Render using model renderer directly
        blockRenderManager.getModelRenderer().render(
            world,
            modelParts,
            blockState,
            blockPos,
            matrixStack,
            invisibleConsumer,
            false,
            OverlayTexture.DEFAULT_UV
        );
    }
    
    // InvisibleVertexConsumer that makes block faces transparent
    private static class InvisibleVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;

        public InvisibleVertexConsumer(VertexConsumer delegate) {
            this.delegate = delegate;
        }

        @Override
        public VertexConsumer vertex(float x, float y, float z) {
            return this.delegate.vertex(x, y, z);
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            // Set alpha to 0 to make it completely transparent
            return this.delegate.color(red, green, blue, 0);
        }

        @Override
        public VertexConsumer texture(float u, float v) {
            return this.delegate.texture(u, v);
        }

        @Override
        public VertexConsumer overlay(int u, int v) {
            return this.delegate.overlay(u, v);
        }

        @Override
        public VertexConsumer light(int u, int v) {
            return this.delegate.light(u, v);
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            return this.delegate.normal(x, y, z);
        }
    }
}