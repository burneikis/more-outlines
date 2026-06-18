package com.moreoutlines.mixins;

import com.moreoutlines.config.ModConfig;
import com.moreoutlines.util.EntityGlowUtil;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.ColorHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * In 1.21.9+ entity rendering is driven by render states. The outline color
 * is stored on {@link EntityRenderState#outlineColor} during
 * {@code updateRenderState}. We hook the tail of that method to apply our
 * custom per-entity / per-item outline colors.
 */
@Mixin(EntityRenderer.class)
public class EntityRenderDispatcherMixin {

    @Inject(method = "updateRenderState", at = @At("TAIL"))
    private void onUpdateRenderState(Entity entity, EntityRenderState state, float tickProgress, CallbackInfo ci) {
        if (!ModConfig.INSTANCE.isOutlinesEnabled()) {
            return;
        }

        // Only override when the entity is actually outlined (glowing).
        if (state.outlineColor == 0) {
            return;
        }

        int color = EntityGlowUtil.getEntityOutlineColor(entity);
        if (color != -1) {
            state.outlineColor = ColorHelper.fullAlpha(color);
        }
    }
}
