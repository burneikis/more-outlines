package com.moreoutlines.renderer;

import com.moreoutlines.config.ModConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.LoadedBlockEntityModels;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Set;

/**
 * Renders outlines for selected blocks (and block entities).
 *
 * <p>Regular (chunk-baked) blocks are rendered directly into the
 * {@link OutlineVertexConsumerProvider} only, so they contribute a silhouette
 * to the outline framebuffer without producing a flickering visible duplicate
 * on top of the terrain.
 *
 * <p>Block entities (chests, signs, beds, shulker boxes, ...) have empty
 * block-state models; their visuals come from special model renderers which can
 * only be driven through the render command queue. For those we re-submit the
 * special model with an outline color. Because this re-uses the exact same
 * geometry the vanilla block-entity pass draws, there is no z-fighting.
 */
public class BlockSelectionOutlineRenderer {

    public static void renderBlockSelectionOutlines(
        MatrixStack matrices,
        Vec3d cameraPos,
        OutlineVertexConsumerProvider outlineConsumers,
        OrderedRenderCommandQueue queue,
        World world,
        Map<Identifier, Set<BlockPos>> blocksByType
    ) {
        if (blocksByType.isEmpty()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        BlockRenderManager blockRenderManager = client.getBlockRenderManager();
        LoadedBlockEntityModels blockEntityModels = client.getBakedModelManager().getBlockEntityModelsSupplier();

        for (Map.Entry<Identifier, Set<BlockPos>> entry : blocksByType.entrySet()) {
            Identifier blockId = entry.getKey();
            Set<BlockPos> positions = entry.getValue();

            if (positions.isEmpty()) {
                continue;
            }

            int outlineColor = ColorHelper.fullAlpha(ModConfig.INSTANCE.getBlockColor(blockId));
            outlineConsumers.setColor(outlineColor);

            for (BlockPos pos : positions) {
                BlockState state = world.getBlockState(pos);
                if (state.isAir()) {
                    continue;
                }

                matrices.push();
                matrices.translate(
                    pos.getX() - cameraPos.x,
                    pos.getY() - cameraPos.y,
                    pos.getZ() - cameraPos.z
                );

                // Block-state model silhouette (outline buffer only).
                blockRenderManager.renderBlockAsEntity(
                    state,
                    matrices,
                    outlineConsumers,
                    LightmapTextureManager.MAX_LIGHT_COORDINATE,
                    OverlayTexture.DEFAULT_UV
                );

                // Block entity special model (e.g. chest, sign) -> via the queue
                // with an outline color so it reaches the outline framebuffer.
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity != null) {
                    blockEntityModels.render(
                        state.getBlock(),
                        ItemDisplayContext.NONE,
                        matrices,
                        queue,
                        LightmapTextureManager.MAX_LIGHT_COORDINATE,
                        OverlayTexture.DEFAULT_UV,
                        outlineColor
                    );
                }

                matrices.pop();
            }
        }
    }
}
