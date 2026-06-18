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
 * <p>In 1.21.9+ the deferred render command queue's {@code submitBlock} draws
 * the model into <em>both</em> the visible buffer and the outline buffer,
 * producing a flickering duplicate on top of the real terrain block. To avoid
 * that, we render the block model directly into the
 * {@link OutlineVertexConsumerProvider} only, so it contributes the silhouette
 * to the outline framebuffer without any visible re-draw.
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

        BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();

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
