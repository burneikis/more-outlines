package com.moreoutlines.config;

public class ModConfig {
    public static final ModConfig INSTANCE = new ModConfig();
    
    public boolean outlinesEnabled = false;
    public boolean itemOutlines = false;
    public boolean entityOutlines = false;
    public boolean blockEntityOutlines = false;
    public boolean blockOutlines = false;
    
    public float outlineWidth = 2.0f;
    public int itemOutlineColor = 0xFF00FF00;
    public int entityOutlineColor = 0xFF0000FF;
    public int blockEntityOutlineColor = 0xFFFF0000;
    public int blockOutlineColor = 0xFFFFFF00;
    
    private ModConfig() {}
    
    public void toggleOutlinesEnabled() {
        outlinesEnabled = !outlinesEnabled;
    }
    
    public void toggleItemOutlines() {
        itemOutlines = !itemOutlines;
    }
    
    public void toggleEntityOutlines() {
        entityOutlines = !entityOutlines;
    }
    
    public void toggleBlockEntityOutlines() {
        blockEntityOutlines = !blockEntityOutlines;
    }
    
    public void toggleBlockOutlines() {
        blockOutlines = !blockOutlines;
    }
}