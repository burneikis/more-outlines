package com.moreoutlines.mixins;

import com.moreoutlines.config.ModConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
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
        
        // Handle item entities with specific item selection
        if (entity instanceof ItemEntity itemEntity) {
            // Check specific item selection first
            Identifier itemId = Registries.ITEM.getId(itemEntity.getStack().getItem());
            if (ModConfig.INSTANCE.isItemSelected(itemId)) {
                cir.setReturnValue(true);
                return;
            }
        }
        
        // Handle other entities with specific entity selection
        Identifier entityId = Registries.ENTITY_TYPE.getId(entity.getType());
        if (ModConfig.INSTANCE.isEntitySelected(entityId)) {
            cir.setReturnValue(true);
            return;
        }
    }
}