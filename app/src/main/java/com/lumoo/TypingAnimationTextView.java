package com.lumoo;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import androidx.appcompat.widget.AppCompatTextView;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.graphics.Color;

public class TypingAnimationTextView extends AppCompatTextView {
    private CharSequence sequence;
    private int index;
    private long delay = 150; // milliseconds
    private Handler handler = new Handler();

    public TypingAnimationTextView(Context context) {
        super(context);
    }

    public TypingAnimationTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            setText(sequence.subSequence(0, index++));
            if (index <= sequence.length()) {
                handler.postDelayed(runnable, delay);
            }
        }
    };

    public void animateText(CharSequence txt) {
        sequence = txt;
        index = 0;

        setText("");
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, delay);
    }

    public void setCharacterDelay(long millis) {
        delay = millis;
    }
    
    // Add gradient effect to typing animation text using the specified colors
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        
        // Apply gradient only after layout is complete
        if (getWidth() > 0) {
            android.graphics.LinearGradient gradient = new android.graphics.LinearGradient(
                    0, 0, getWidth(), getHeight(),
                    new int[]{Color.parseColor("#0f0d48"), Color.parseColor("#00cfff")},
                    new float[]{0, 1},
                    android.graphics.Shader.TileMode.CLAMP
            );
            getPaint().setShader(gradient);
        }
    }
    
    // Clean up resources
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
    }
}