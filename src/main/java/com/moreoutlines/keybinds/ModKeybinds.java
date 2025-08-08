package com.moreoutlines.keybinds;

import com.moreoutlines.config.ModConfig;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ModKeybinds {
    public static final String KEY_CATEGORY_MORE_OUTLINES = "key.category.more-outlines";
    
    public static KeyBinding openConfigGui;
    public static KeyBinding toggleAllOutlines;
    public static KeyBinding toggleEntityOutlines;
    
    public static void registerKeyBinds() {
        openConfigGui = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.more-outlines.open_config",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            KEY_CATEGORY_MORE_OUTLINES
        ));
        
        toggleAllOutlines = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.more-outlines.toggle_all_outlines",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            KEY_CATEGORY_MORE_OUTLINES
        ));
        
        toggleEntityOutlines = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.more-outlines.toggle_entity_outlines",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_E,
            KEY_CATEGORY_MORE_OUTLINES
        ));
    }
    
    public static void handleKeyPress() {
        if (toggleAllOutlines.wasPressed()) {
            ModConfig.INSTANCE.toggleOutlinesEnabled();
        }
        
        if (toggleEntityOutlines.wasPressed()) {
            ModConfig.INSTANCE.toggleEntityOutlines();
        }
    }
}