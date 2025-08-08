package com.moreoutlines.mixins;

import com.moreoutlines.config.ModConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    
    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    private void onIsGlowing(CallbackInfoReturnable<Boolean> cir) {
        if (!ModConfig.INSTANCE.outlinesEnabled) {
            return;
        }
        
        Entity entity = (Entity) (Object) this;
        
        if (ModConfig.INSTANCE.itemOutlines && entity instanceof ItemEntity) {
            cir.setReturnValue(true);
        }
        
        if (ModConfig.INSTANCE.entityOutlines) {
            if (entity instanceof LivingEntity) {
                cir.setReturnValue(true);
            } else if (!(entity instanceof ItemEntity)) {
                cir.setReturnValue(true);
            }
        }
    }
}