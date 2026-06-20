package com.moreoutlines.mixins;

import com.moreoutlines.config.ModConfig;
import com.moreoutlines.renderer.BlockSelectionOutlineRenderer;
import com.moreoutlines.scanner.BlockSelectionScanner;
import com.moreoutlines.util.OutlineColorContext;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderManager;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow
    private ClientWorld world;

    @Shadow
    @org.spongepowered.asm.mixin.Final
    private BufferBuilderStorage bufferBuilders;

    @Shadow
    protected abstract boolean canDrawEntityOutlines();

    /**
     * Checks if block outline rendering is active and has tracked blocks.
     */
    private boolean hasActiveBlockOutlines() {
        return ModConfig.INSTANCE.isOutlinesEnabled()
            && !ModConfig.INSTANCE.selectedBlocks.isEmpty()
            && !BlockSelectionScanner.getInstance().getTrackedBlocksByType().isEmpty();
    }

    /**
     * Force the outline framebuffer to be used when we have block outlines,
     * even if no entity is currently glowing.
     */
    @Inject(method = "fillEntityRenderStates", at = @At("TAIL"))
    private void forceBlockOutlinePass(net.minecraft.client.render.Camera camera,
            net.minecraft.client.render.Frustum frustum,
            net.minecraft.client.render.RenderTickCounter tickCounter,
            WorldRenderState renderStates, CallbackInfo ci) {
        if (hasActiveBlockOutlines() && this.canDrawEntityOutlines()) {
            renderStates.hasOutline = true;
        }
    }

    /**
     * Submit selected block outlines into the render command queue, alongside
     * the regular block entity rendering.
     */
    @Inject(method = "renderBlockEntities", at = @At("TAIL"))
    private void renderCustomOutlines(MatrixStack matrices, WorldRenderState renderStates,
            OrderedRenderCommandQueueImpl queue, CallbackInfo ci) {
        if (hasActiveBlockOutlines()) {
            BlockSelectionScanner scanner = BlockSelectionScanner.getInstance();
            BlockSelectionOutlineRenderer.renderBlockSelectionOutlines(
                matrices,
                renderStates.cameraRenderState.pos,
                this.bufferBuilders.getOutlineVertexConsumers(),
                this.world,
                scanner.getTrackedBlocksByType()
            );
        }
    }

    /**
     * Wrap each block entity render so that, if its block type is selected, the
     * geometry it submits is tagged with our outline color. This makes the
     * single live render also populate the outline framebuffer (correct pose,
     * no duplicate draw).
     */
    @Redirect(method = "renderBlockEntities", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/render/block/entity/BlockEntityRenderManager;render(Lnet/minecraft/client/render/block/entity/state/BlockEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V"))
    private void wrapBlockEntityRender(BlockEntityRenderManager manager,
            BlockEntityRenderState state, MatrixStack matrices,
            OrderedRenderCommandQueue queue, CameraRenderState cameraRenderState) {
        int color = 0;
        if (hasActiveBlockOutlines()) {
            Identifier blockId = Registries.BLOCK.getId(state.blockState.getBlock());
            if (ModConfig.INSTANCE.isBlockSelected(blockId)) {
                color = ColorHelper.fullAlpha(ModConfig.INSTANCE.getBlockColor(blockId));
            }
        }

        if (color != 0) {
            OutlineColorContext.set(color);
            try {
                manager.render(state, matrices, queue, cameraRenderState);
            } finally {
                OutlineColorContext.clear();
            }
        } else {
            manager.render(state, matrices, queue, cameraRenderState);
        }
    }
}
