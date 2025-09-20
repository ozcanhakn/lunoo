package com.lumoo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

public class GradientTextView extends AppCompatTextView {

    private int startColor = 0xFF6B9D; // Default pink
    private int endColor = 0xC44CEA;   // Default purple
    private float angle = 0f;

    public GradientTextView(Context context) {
        super(context);
        init();
    }

    public GradientTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
        init();
    }

    public GradientTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        init();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GradientTextView);
        try {
            startColor = a.getColor(R.styleable.GradientTextView_startColor, 0xFF6B9D);
            endColor = a.getColor(R.styleable.GradientTextView_endColor, 0xC44CEA);
            angle = a.getFloat(R.styleable.GradientTextView_gradientAngle, 0f);
        } finally {
            a.recycle();
        }
    }

    private void init() {
        applyGradient();
        animateGradient();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            applyGradient();
        }
    }

    private void applyGradient() {
        if (getWidth() > 0 && getHeight() > 0) {
            float x0, y0, x1, y1;

            // Calculate gradient coordinates based on angle
            double radians = Math.toRadians(angle);
            float centerX = getWidth() / 2f;
            float centerY = getHeight() / 2f;
            float radius = Math.max(getWidth(), getHeight()) / 2f;

            x0 = (float) (centerX - radius * Math.cos(radians));
            y0 = (float) (centerY - radius * Math.sin(radians));
            x1 = (float) (centerX + radius * Math.cos(radians));
            y1 = (float) (centerY + radius * Math.sin(radians));

            LinearGradient gradient = new LinearGradient(
                    x0, y0, x1, y1,
                    startColor, endColor,
                    Shader.TileMode.CLAMP
            );

            getPaint().setShader(gradient);
        }
    }

    public void setGradientColors(int startColor, int endColor) {
        this.startColor = startColor;
        this.endColor = endColor;
        applyGradient();
        invalidate();
    }

    public void setGradientAngle(float angle) {
        this.angle = angle;
        applyGradient();
        invalidate();
    }

    // Animated gradient effect
    public void animateGradient() {
        android.animation.ValueAnimator animator = android.animation.ValueAnimator.ofFloat(0f, 360f);
        animator.setDuration(3000);
        animator.setRepeatCount(android.animation.ValueAnimator.INFINITE);
        animator.addUpdateListener(animation -> {
            float animatedAngle = (float) animation.getAnimatedValue();
            setGradientAngle(animatedAngle);
        });
        animator.start();
    }
}

