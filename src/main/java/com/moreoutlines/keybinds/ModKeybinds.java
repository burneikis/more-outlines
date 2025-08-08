package com.moreoutlines.keybinds;

import com.moreoutlines.config.ModConfig;
import com.moreoutlines.gui.ToggleNotificationHud;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ModKeybinds {
    public static final String KEY_CATEGORY_MORE_OUTLINES = "key.category.more-outlines";
    
    public static KeyBinding openConfigGui;
    public static KeyBinding toggleAllOutlines;
    public static KeyBinding toggleItemOutlines;
    public static KeyBinding toggleEntityOutlines;
    public static KeyBinding toggleBlockOutlines;
    
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
        
        toggleItemOutlines = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.more-outlines.toggle_item_outlines",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_I,
            KEY_CATEGORY_MORE_OUTLINES
        ));
        
        toggleEntityOutlines = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.more-outlines.toggle_entity_outlines",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_E,
            KEY_CATEGORY_MORE_OUTLINES
        ));
        
        toggleBlockOutlines = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.more-outlines.toggle_block_outlines",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            KEY_CATEGORY_MORE_OUTLINES
        ));
    }
    
    public static void handleKeyPress() {
        if (toggleAllOutlines.wasPressed()) {
            ModConfig.INSTANCE.toggleOutlinesEnabled();
            String status = ModConfig.INSTANCE.outlinesEnabled ? "ON" : "OFF";
            ToggleNotificationHud.getInstance().showNotification("All Outlines: " + status);
        }
        
        if (toggleItemOutlines.wasPressed()) {
            ModConfig.INSTANCE.toggleItemOutlines();
            String status = ModConfig.INSTANCE.itemOutlines ? "ON" : "OFF";
            ToggleNotificationHud.getInstance().showNotification("Item Outlines: " + status);
        }
        
        if (toggleEntityOutlines.wasPressed()) {
            ModConfig.INSTANCE.toggleEntityOutlines();
            String status = ModConfig.INSTANCE.entityOutlines ? "ON" : "OFF";
            ToggleNotificationHud.getInstance().showNotification("Entity Outlines: " + status);
        }
        
        if (toggleBlockOutlines.wasPressed()) {
            ModConfig.INSTANCE.toggleBlockOutlines();
            String status = ModConfig.INSTANCE.blockOutlines ? "ON" : "OFF";
            ToggleNotificationHud.getInstance().showNotification("Block Outlines: " + status);
        }
    }
}