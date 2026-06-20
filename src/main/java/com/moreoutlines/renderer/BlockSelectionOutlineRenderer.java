package com.moreoutlines.renderer;

import com.moreoutlines.config.ModConfig;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
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
 * <p>Block entities (chests, signs, beds, ...) have empty block-state models;
 * their outlines are handled separately by tagging their normal render with an
 * outline color (see {@code OutlineColorContext} and the command-queue mixin),
 * so they are not handled here.
 */
public class BlockSelectionOutlineRenderer {

    public static void renderBlockSelectionOutlines(
        MatrixStack matrices,
        Vec3d cameraPos,
        OutlineVertexConsumerProvider outlineConsumers,
        World world,
        Map<Identifier, Set<BlockPos>> blocksByType
    ) {
        if (blocksByType.isEmpty()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        BlockRenderManager blockRenderManager = client.getBlockRenderManager();

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

                matrices.pop();
            }
        }
    }
}
