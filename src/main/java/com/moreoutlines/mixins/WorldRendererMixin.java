package com.moreoutlines.mixins;

import com.moreoutlines.config.ModConfig;
import com.moreoutlines.network.ServerPermissionManager;
import com.moreoutlines.renderer.BlockSelectionOutlineRenderer;
import com.moreoutlines.scanner.BlockSelectionScanner;
import com.moreoutlines.util.ColorUtil;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
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
    
    /**
     * Checks if block outline rendering is active and has tracked blocks.
     */
    private boolean hasActiveBlockOutlines() {
        return ModConfig.INSTANCE.isOutlinesEnabled() 
            && !ModConfig.INSTANCE.selectedBlocks.isEmpty() 
            && !BlockSelectionScanner.getInstance().getTrackedBlocksByType().isEmpty();
    }

    @Redirect(method = "renderBlockEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/entity/BlockEntityRenderDispatcher;render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V"))
    private void redirectBlockEntityRender(BlockEntityRenderDispatcher dispatcher, BlockEntity blockEntity,
            float tickProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        
        // Always render normally first
        dispatcher.render(blockEntity, tickProgress, matrices, vertexConsumers);
        
        // Only render outlines if enabled AND the specific block entity type is selected
        if (ModConfig.INSTANCE.isOutlinesEnabled() && blockEntity != null) {
            // Get the block entity's block type
            Identifier blockId = Registries.BLOCK.getId(blockEntity.getCachedState().getBlock());
            
            // Only render outline if this specific block type is selected
            if (ModConfig.INSTANCE.isBlockSelected(blockId)) {
                OutlineVertexConsumerProvider outlineProvider = this.bufferBuilders.getOutlineVertexConsumers();
                
                // Set the outline color for this specific block type
                int color = ModConfig.INSTANCE.getBlockColor(blockId);
                ColorUtil.setOutlineColor(outlineProvider, color);
                
                dispatcher.render(blockEntity, tickProgress, matrices, outlineProvider);
            }
        }
    }

    // Inject after block entities are rendered to add our custom outlines
    @Inject(
        method = "renderBlockEntities", 
        at = @At("TAIL")
    )
    private void renderCustomOutlines(
        MatrixStack matrices,
        VertexConsumerProvider.Immediate entityVertexConsumers,
        VertexConsumerProvider.Immediate effectVertexConsumers,
        Camera camera,
        float tickProgress,
        CallbackInfo ci
    ) {
        // Render block selection outlines
        if (hasActiveBlockOutlines()) {
            BlockSelectionScanner scanner = BlockSelectionScanner.getInstance();
            
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

        @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderMain(Lnet/minecraft/client/render/FrameGraphBuilder;Lnet/minecraft/client/render/Frustum;Lnet/minecraft/client/render/Camera;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;ZZLnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/util/profiler/Profiler;)V"), index = 6)
    private boolean forceEntityOutline(boolean renderEntityOutline) {
        // Force entity outline rendering if any outlines are enabled
        return renderEntityOutline || hasActiveBlockOutlines();
    }

    @Inject(method = "getEntitiesToRender", at = @At("RETURN"), cancellable = true)
    private void forceEntityOutlineReturn(Camera camera, Frustum frustum, List<Entity> output,
            CallbackInfoReturnable<Boolean> cir) {
        // Force return true if any outlines are enabled to ensure proper rendering
        if (hasActiveBlockOutlines()) {
            cir.setReturnValue(true);
        }
    }
}