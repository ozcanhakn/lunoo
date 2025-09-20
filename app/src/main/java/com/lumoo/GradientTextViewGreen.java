package com.lumoo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.text.BoringLayout;
import android.text.Layout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

public class GradientTextViewGreen extends View {

    private String text = "Online Kullanıcılar";
    private float textSize = 64f;
    private TextPaint textPaint; // <-- Paint yerine TextPaint
    private BoringLayout boringLayout;

    public GradientTextViewGreen(Context context) {
        super(context);
        init();
    }

    public GradientTextViewGreen(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GradientTextViewGreen(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        textPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG); // <-- TextPaint
        textPaint.setTextSize(textSize);

        // Gradient tanımla (üstten alta)
        Shader textShader = new LinearGradient(
                0, 0, 0, textSize,
                new int[]{Color.parseColor("#A7F3D0"), Color.parseColor("#15803D")},
                new float[]{0, 1},
                Shader.TileMode.CLAMP
        );
        textPaint.setShader(textShader);

        BoringLayout.Metrics boringMetrics = BoringLayout.isBoring(text, textPaint);
        boringLayout = new BoringLayout(
                text,
                textPaint,
                (int) textPaint.measureText(text),
                Layout.Alignment.ALIGN_CENTER,
                1.0f,
                0.0f,
                boringMetrics,
                false
        );
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = (int) textPaint.measureText(text);
        int height = (int) textPaint.getFontSpacing();
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        boringLayout.draw(canvas);
    }

    // Dışarıdan text değiştirme fonksiyonu
    public void setText(String newText) {
        this.text = newText;
        BoringLayout.Metrics boringMetrics = BoringLayout.isBoring(text, textPaint);
        boringLayout = new BoringLayout(
                text,
                textPaint,
                (int) textPaint.measureText(text),
                Layout.Alignment.ALIGN_CENTER,
                1.0f,
                0.0f,
                boringMetrics,
                false
        );
        requestLayout();
        invalidate();
    }
}
