package com.moreoutlines.mixins;

import com.moreoutlines.config.ModConfig;
import com.moreoutlines.renderer.DiamondBlockOutlineRenderer;
import com.moreoutlines.renderer.BlockSelectionOutlineRenderer;
import com.moreoutlines.scanner.DiamondBlockScanner;
import com.moreoutlines.scanner.BlockSelectionScanner;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.Shadow;
import java.util.List;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Shadow
    private BufferBuilderStorage bufferBuilders;
    
    @Shadow
    private ClientWorld world;

    @Redirect(method = "renderBlockEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/entity/BlockEntityRenderDispatcher;render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V"))
    private void redirectBlockEntityRender(BlockEntityRenderDispatcher dispatcher, BlockEntity blockEntity,
            float tickProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        
        // Always render normally first
        dispatcher.render(blockEntity, tickProgress, matrices, vertexConsumers);
        
        // Only render outlines if enabled
        if (ModConfig.INSTANCE.outlinesEnabled && ModConfig.INSTANCE.blockEntityOutlines) {
            OutlineVertexConsumerProvider outlineProvider = this.bufferBuilders.getOutlineVertexConsumers();
            
            // Extract color components from config
            int color = ModConfig.INSTANCE.blockOutlineColor;
            int red = (color >> 16) & 0xFF;
            int green = (color >> 8) & 0xFF;
            int blue = color & 0xFF;
            int alpha = (color >> 24) & 0xFF;
            
            outlineProvider.setColor(red, green, blue, alpha);
            dispatcher.render(blockEntity, tickProgress, matrices, outlineProvider);
        }
    }

    // Inject after block entities are rendered to add our custom diamond block outlines
    @Inject(
        method = "renderBlockEntities", 
        at = @At("TAIL")
    )
    private void renderDiamondBlockOutlines(
        MatrixStack matrices,
        VertexConsumerProvider.Immediate entityVertexConsumers,
        VertexConsumerProvider.Immediate effectVertexConsumers,
        Camera camera,
        float tickProgress,
        CallbackInfo ci
    ) {
        // Only render if diamond block outlines are enabled and we have blocks to render
        if (ModConfig.INSTANCE.outlinesEnabled && ModConfig.INSTANCE.diamondBlockOutlines) {
            DiamondBlockScanner scanner = DiamondBlockScanner.getInstance();
            if (!scanner.getTrackedBlockPositions().isEmpty()) {
                
                // Get the outline vertex consumer provider from the buffer builders
                OutlineVertexConsumerProvider outlineProvider = this.bufferBuilders.getOutlineVertexConsumers();
                
                // Render our custom diamond block outlines
                DiamondBlockOutlineRenderer.renderDiamondBlockOutlines(
                    matrices,
                    camera,
                    outlineProvider,
                    this.world,
                    scanner.getTrackedBlockPositions(),
                    ModConfig.INSTANCE.blockOutlineColor & 0xFFFFFF // Remove alpha for color extraction
                );
            }
        }
        
        // Render block selection outlines
        if (ModConfig.INSTANCE.outlinesEnabled && !ModConfig.INSTANCE.selectedBlocks.isEmpty()) {
            BlockSelectionScanner scanner = BlockSelectionScanner.getInstance();
            if (!scanner.getTrackedBlocksByType().isEmpty()) {
                
                // Get the outline vertex consumer provider from the buffer builders
                OutlineVertexConsumerProvider outlineProvider = this.bufferBuilders.getOutlineVertexConsumers();
                
                // Render our custom block selection outlines
                BlockSelectionOutlineRenderer.renderBlockSelectionOutlines(
                    matrices,
                    camera,
                    outlineProvider,
                    this.world,
                    scanner.getTrackedBlocksByType()
                );
            }
        }
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderMain(Lnet/minecraft/client/render/FrameGraphBuilder;Lnet/minecraft/client/render/Frustum;Lnet/minecraft/client/render/Camera;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;ZZLnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/util/profiler/Profiler;)V"), index = 6)
    private boolean forceEntityOutline(boolean renderEntityOutline) {
        // Force entity outline rendering if any outlines are enabled
        return renderEntityOutline || 
               (ModConfig.INSTANCE.outlinesEnabled && ModConfig.INSTANCE.blockEntityOutlines) ||
               (ModConfig.INSTANCE.outlinesEnabled && ModConfig.INSTANCE.diamondBlockOutlines && 
                !DiamondBlockScanner.getInstance().getTrackedBlockPositions().isEmpty()) ||
               (ModConfig.INSTANCE.outlinesEnabled && !ModConfig.INSTANCE.selectedBlocks.isEmpty() && 
                !BlockSelectionScanner.getInstance().getTrackedBlocksByType().isEmpty());
    }

    @Inject(method = "getEntitiesToRender", at = @At("RETURN"), cancellable = true)
    private void forceEntityOutlineReturn(Camera camera, Frustum frustum, List<Entity> output,
            CallbackInfoReturnable<Boolean> cir) {
        // Force return true if any outlines are enabled to ensure proper rendering
        if (ModConfig.INSTANCE.outlinesEnabled && 
            (ModConfig.INSTANCE.blockEntityOutlines || 
             (ModConfig.INSTANCE.diamondBlockOutlines && !DiamondBlockScanner.getInstance().getTrackedBlockPositions().isEmpty()) ||
             (!ModConfig.INSTANCE.selectedBlocks.isEmpty() && !BlockSelectionScanner.getInstance().getTrackedBlocksByType().isEmpty()))) {
            cir.setReturnValue(true);
        }
    }
}