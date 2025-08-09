package com.moreoutlines.mixins;

import com.moreoutlines.config.ModConfig;
import com.moreoutlines.util.ColorUtil;
import com.moreoutlines.util.EntityGlowUtil;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderEntityHead(Entity entity, double x, double y, double z, 
                                   float tickProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, 
                                   int light, CallbackInfo ci) {
        // Only set colors if outlines are enabled and the entity is glowing
        if (!ModConfig.INSTANCE.isOutlinesEnabled() || !entity.isGlowing()) {
            return;
        }

        // Check if we have an outline vertex consumer provider
        if (!(vertexConsumers instanceof OutlineVertexConsumerProvider outlineProvider)) {
            return;
        }

        int color = getEntityOutlineColor(entity);
        if (color != -1) {
            // Use ColorUtil for extraction and setting
            ColorUtil.setOutlineColor(outlineProvider, color);
        }
    }
    
    private int getEntityOutlineColor(Entity entity) {
        return EntityGlowUtil.getEntityOutlineColor(entity);
    }
}
