package com.moreoutlines.mixins;

import com.moreoutlines.config.ModConfig;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.Shadow;
import java.util.List;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Shadow
    private BufferBuilderStorage bufferBuilders;

    @Redirect(method = "renderBlockEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/entity/BlockEntityRenderDispatcher;render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V"))
    private void redirectBlockEntityRender(BlockEntityRenderDispatcher dispatcher, BlockEntity blockEntity,
            float tickProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        
        // Always render normally first
        dispatcher.render(blockEntity, tickProgress, matrices, vertexConsumers);
        
        // Only render outlines if enabled
        if (ModConfig.INSTANCE.outlinesEnabled && ModConfig.INSTANCE.blockEntityOutlines) {
            OutlineVertexConsumerProvider outlineProvider = this.bufferBuilders.getOutlineVertexConsumers();
            
            // Extract color components from config
            int color = ModConfig.INSTANCE.blockEntityOutlineColor;
            int red = (color >> 16) & 0xFF;
            int green = (color >> 8) & 0xFF;
            int blue = color & 0xFF;
            int alpha = (color >> 24) & 0xFF;
            
            outlineProvider.setColor(red, green, blue, alpha);
            dispatcher.render(blockEntity, tickProgress, matrices, outlineProvider);
        }
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderMain(Lnet/minecraft/client/render/FrameGraphBuilder;Lnet/minecraft/client/render/Frustum;Lnet/minecraft/client/render/Camera;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;ZZLnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/util/profiler/Profiler;)V"), index = 6)
    private boolean forceEntityOutline(boolean renderEntityOutline) {
        // Only force entity outline rendering if block entity outlines are enabled
        return renderEntityOutline || (ModConfig.INSTANCE.outlinesEnabled && ModConfig.INSTANCE.blockEntityOutlines);
    }

    @Inject(method = "getEntitiesToRender", at = @At("RETURN"), cancellable = true)
    private void forceEntityOutlineReturn(Camera camera, Frustum frustum, List<Entity> output,
            CallbackInfoReturnable<Boolean> cir) {
        // Force return true if block entity outlines are enabled to ensure proper rendering
        if (ModConfig.INSTANCE.outlinesEnabled && ModConfig.INSTANCE.blockEntityOutlines) {
            cir.setReturnValue(true);
        }
    }
}