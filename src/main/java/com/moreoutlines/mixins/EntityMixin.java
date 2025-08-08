package com.moreoutlines.mixins;

import com.moreoutlines.util.EntityGlowUtil;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    
    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    private void onIsGlowing(CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;
        
        Boolean shouldGlow = EntityGlowUtil.shouldEntityGlow(entity);
        if (shouldGlow != null) {
            cir.setReturnValue(shouldGlow);
        }
    }
}