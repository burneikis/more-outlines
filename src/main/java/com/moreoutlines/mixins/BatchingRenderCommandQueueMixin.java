package com.moreoutlines.mixins;

import com.moreoutlines.util.OutlineColorContext;
import net.minecraft.client.render.command.BatchingRenderCommandQueue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Substitutes the outline color of model / model-part render commands with the
 * color from {@link OutlineColorContext} while a tracked block entity is being
 * rendered. This causes the block entity's normal (single) render to also feed
 * the outline framebuffer, in its correct live pose, with no duplicate draw.
 */
@Mixin(BatchingRenderCommandQueue.class)
public class BatchingRenderCommandQueueMixin {

    @ModifyVariable(method = "submitModel", at = @At("HEAD"), argsOnly = true, ordinal = 3)
    private int moreoutlines$modelOutline(int outlineColor) {
        int ctx = OutlineColorContext.get();
        return ctx != 0 ? ctx : outlineColor;
    }

    @ModifyVariable(method = "submitModelPart", at = @At("HEAD"), argsOnly = true, ordinal = 3)
    private int moreoutlines$modelPartOutline(int outlineColor) {
        int ctx = OutlineColorContext.get();
        return ctx != 0 ? ctx : outlineColor;
    }
}
