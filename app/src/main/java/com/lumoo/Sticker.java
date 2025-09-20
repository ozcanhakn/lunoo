package com.lumoo;

import android.graphics.Bitmap;

public class Sticker {
    private Bitmap bitmap;
    public float x;
    public float y;
    public float size = 80f;
    public float scale = 1.0f;
    public float rotation = 0.0f;

    public Sticker(Bitmap bitmap, float x, float y) {
        this.bitmap = bitmap;
        this.x = x;
        this.y = y;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}