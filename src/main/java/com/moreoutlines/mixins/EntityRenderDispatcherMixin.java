package com.moreoutlines.mixins;

import com.moreoutlines.config.ModConfig;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
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
        if (!ModConfig.INSTANCE.outlinesEnabled || !entity.isGlowing()) {
            return;
        }
        
        // Check if we have an outline vertex consumer provider
        if (!(vertexConsumers instanceof OutlineVertexConsumerProvider outlineProvider)) {
            return;
        }
        
        int color = getEntityOutlineColor(entity);
        if (color != -1) {
            // Extract color components
            int red = (color >> 16) & 0xFF;
            int green = (color >> 8) & 0xFF;
            int blue = color & 0xFF;
            int alpha = (color >> 24) & 0xFF;
            
            // Set the outline color
            outlineProvider.setColor(red, green, blue, alpha);
        }
    }
    
    private int getEntityOutlineColor(Entity entity) {
        // Handle item entities
        if (entity instanceof ItemEntity itemEntity) {
            Identifier itemId = Registries.ITEM.getId(itemEntity.getStack().getItem());
            
            // Check for specific item selection
            if (ModConfig.INSTANCE.isItemSelected(itemId)) {
                return ModConfig.INSTANCE.getItemColor(itemId);
            }
            
            // Fall back to general item outline color if general item outlines are enabled
            if (ModConfig.INSTANCE.itemOutlines) {
                return ModConfig.INSTANCE.defaultColor;
            }
        }
        
        // Handle other entities
        Identifier entityId = Registries.ENTITY_TYPE.getId(entity.getType());
        
        // Check for specific entity selection
        if (ModConfig.INSTANCE.isEntitySelected(entityId)) {
            return ModConfig.INSTANCE.getEntityColor(entityId);
        }
        
        // Fall back to general entity outline color if general entity outlines are enabled
        if (ModConfig.INSTANCE.entityOutlines) {
            return ModConfig.INSTANCE.defaultColor;
        }
        
        return -1; // No color to set
    }
}
