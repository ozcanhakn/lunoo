package com.lumoo.Model;

public class BackgroundTheme {
    private String name;
    private String colorCode;
    private String imageUrl; // Gradient veya resim için
    private boolean isSelected;
    private ThemeType type; // BACKGROUND veya BUBBLE

    public enum ThemeType {
        BACKGROUND, BUBBLE
    }

    public BackgroundTheme() {}

    public BackgroundTheme(String name, String colorCode, boolean isSelected) {
        this.name = name;
        this.colorCode = colorCode;
        this.isSelected = isSelected;
        this.type = ThemeType.BACKGROUND;
    }

    public BackgroundTheme(String name, String colorCode, String imageUrl, boolean isSelected, ThemeType type) {
        this.name = name;
        this.colorCode = colorCode;
        this.imageUrl = imageUrl;
        this.isSelected = isSelected;
        this.type = type;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColorCode() { return colorCode; }
    public void setColorCode(String colorCode) { this.colorCode = colorCode; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }

    public ThemeType getType() { return type; }
    public void setType(ThemeType type) { this.type = type; }
}