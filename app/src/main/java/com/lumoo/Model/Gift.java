package com.lumoo.Model;

public class Gift {
    private String name;
    private int creditCost;
    private int rawResId;
    private int soundResId; // Hediye sesi için
    private GiftType giftType;

    public enum GiftType {
        GIF,
        PNG
    }

    public Gift() {
        // Firebase için boş constructor
    }

    public Gift(String name, int creditCost, int rawResId, int soundResId, GiftType giftType) {
        this.name = name;
        this.creditCost = creditCost;
        this.rawResId = rawResId;
        this.soundResId = soundResId;
        this.giftType = giftType;
    }

    // Ses olmayan constructor (backward compatibility)
    public Gift(String name, int creditCost, int rawResId, GiftType giftType) {
        this.name = name;
        this.creditCost = creditCost;
        this.rawResId = rawResId;
        this.soundResId = 0; // Ses yok
        this.giftType = giftType;
    }

    // Eski constructor'ı koruyalım
    public Gift(String name, int creditCost, int rawResId) {
        this.name = name;
        this.creditCost = creditCost;
        this.rawResId = rawResId;
        this.soundResId = 0; // Ses yok
        this.giftType = GiftType.PNG; // Default olarak PNG
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCreditCost() {
        return creditCost;
    }

    public void setCreditCost(int creditCost) {
        this.creditCost = creditCost;
    }

    public int getRawResId() {
        return rawResId;
    }

    public void setRawResId(int rawResId) {
        this.rawResId = rawResId;
    }

    public int getSoundResId() {
        return soundResId;
    }

    public void setSoundResId(int soundResId) {
        this.soundResId = soundResId;
    }

    public GiftType getGiftType() {
        return giftType;
    }

    public void setGiftType(GiftType giftType) {
        this.giftType = giftType;
    }
}