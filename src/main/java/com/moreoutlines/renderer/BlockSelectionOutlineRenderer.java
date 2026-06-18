package com.moreoutlines.renderer;

import com.moreoutlines.config.ModConfig;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Set;

/**
 * Renders outlines for selected blocks (and block entities) using the
 * 1.21.9+ render command queue. {@code submitBlock} re-submits the block's
 * model with an outline color, which feeds the entity-outline framebuffer.
 */
public class BlockSelectionOutlineRenderer {

    public static void renderBlockSelectionOutlines(
        MatrixStack matrices,
        Vec3d cameraPos,
        OrderedRenderCommandQueue queue,
        World world,
        Map<Identifier, Set<BlockPos>> blocksByType
    ) {
        if (blocksByType.isEmpty()) {
            return;
        }

        for (Map.Entry<Identifier, Set<BlockPos>> entry : blocksByType.entrySet()) {
            Identifier blockId = entry.getKey();
            Set<BlockPos> positions = entry.getValue();

            if (positions.isEmpty()) {
                continue;
            }

            int outlineColor = ColorHelper.fullAlpha(ModConfig.INSTANCE.getBlockColor(blockId));

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

                queue.submitBlock(
                    matrices,
                    state,
                    LightmapTextureManager.MAX_LIGHT_COORDINATE,
                    OverlayTexture.DEFAULT_UV,
                    outlineColor
                );

                matrices.pop();
            }
        }
    }
}
