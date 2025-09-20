package com.lumoo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CustomDualProgressBar extends View {
    private Paint firstGifterPaint;
    private Paint secondGifterPaint;
    private Paint backgroundPaint;
    private float firstGifterPercentage = 0f;
    private float secondGifterPercentage = 0f;
    private int cornerRadius = 8;

    public CustomDualProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        firstGifterPaint = new Paint();
        firstGifterPaint.setColor(0xFF4CAF50); // Yeşil - birinci için
        firstGifterPaint.setAntiAlias(true);

        secondGifterPaint = new Paint();
        secondGifterPaint.setColor(0xFF2196F3); // Mavi - ikinci için
        secondGifterPaint.setAntiAlias(true);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(0xFFE0E0E0); // Gri background
        backgroundPaint.setAntiAlias(true);
    }

    public void setProgress(float firstPercentage, float secondPercentage) {
        this.firstGifterPercentage = firstPercentage;
        this.secondGifterPercentage = secondPercentage;
        invalidate(); // View'i yeniden çiz
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        if (width == 0 || height == 0) return;

        // Background çiz
        RectF backgroundRect = new RectF(0, 0, width, height);
        canvas.drawRoundRect(backgroundRect, cornerRadius, cornerRadius, backgroundPaint);

        // Toplam yüzdeyi hesapla
        float totalPercentage = firstGifterPercentage + secondGifterPercentage;

        if (totalPercentage > 0) {
            // Birinci gifter'ın çubuğunu çiz (soldan başla)
            float firstWidth = (firstGifterPercentage / totalPercentage) * width;
            if (firstWidth > 0) {
                RectF firstRect = new RectF(0, 0, firstWidth, height);
                canvas.drawRoundRect(firstRect, cornerRadius, cornerRadius, firstGifterPaint);
            }

            // İkinci gifter'ın çubuğunu çiz (birincinin devamından)
            float secondWidth = (secondGifterPercentage / totalPercentage) * width;
            if (secondWidth > 0) {
                RectF secondRect = new RectF(firstWidth, 0, width, height);
                canvas.drawRoundRect(secondRect, cornerRadius, cornerRadius, secondGifterPaint);
            }
        }
    }
}