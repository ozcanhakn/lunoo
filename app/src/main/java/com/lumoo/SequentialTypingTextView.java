package com.lumoo;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.TextView;
import androidx.appcompat.widget.AppCompatTextView;

public class SequentialTypingTextView extends AppCompatTextView {
    private String[] texts = {"ARKADAŞ BUL", "SOHBET ET", "EĞLEN"};
    private int currentTextIndex = 0;
    private int currentCharIndex = 0;
    private boolean isDeleting = false;
    private Handler handler = new Handler();
    private long typingSpeed = 150; // milliseconds
    private long deletingSpeed = 100; // milliseconds
    private long pauseBetweenTexts = 1000; // milliseconds
    private boolean animationCompleted = false;

    public SequentialTypingTextView(Context context) {
        super(context);
        init();
    }

    public SequentialTypingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Set initial text
        setText("");
        
        // Apply gradient
        setGradient();
        
        // Start the animation
        startAnimation();
    }

    private void setGradient() {
        // Apply gradient only after layout is complete
        if (getWidth() > 0) {
            LinearGradient gradient = new LinearGradient(
                    0, 0, getWidth(), getHeight(),
                    new int[]{Color.parseColor("#A7F3D0"), Color.parseColor("#15803D")},
                    new float[]{0, 1},
                    Shader.TileMode.CLAMP
            );
            getPaint().setShader(gradient);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        setGradient();
    }

    private void startAnimation() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!animationCompleted) {
                    updateText();
                    handler.postDelayed(this, isDeleting ? deletingSpeed : typingSpeed);
                }
            }
        }, typingSpeed);
    }

    private void updateText() {
        // If animation is completed, show the full text
        if (animationCompleted) {
            setText("ARKADAŞ BUL, SOHBET ET & EĞLEN");
            return;
        }
        
        String currentText = texts[currentTextIndex];
        
        if (!isDeleting) {
            // Typing
            if (currentCharIndex <= currentText.length()) {
                setText(currentText.substring(0, currentCharIndex));
                currentCharIndex++;
            } else {
                // Finished typing, pause before deleting
                isDeleting = true;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isDeleting = true;
                    }
                }, pauseBetweenTexts);
            }
        } else {
            // Deleting
            if (currentCharIndex > 0) {
                currentCharIndex--;
                setText(currentText.substring(0, currentCharIndex));
            } else {
                // Finished deleting
                isDeleting = false;
                currentTextIndex = (currentTextIndex + 1) % texts.length;
                
                // If we've completed all texts, show the full text
                if (currentTextIndex == 0 && !isDeleting) {
                    animationCompleted = true;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setText("ARKADAŞ BUL, SOHBET ET & EĞLEN");
                        }
                    }, pauseBetweenTexts);
                }
            }
        }
    }

    // Clean up resources
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    public void setTypingSpeed(long speed) {
        this.typingSpeed = speed;
    }

    public void setDeletingSpeed(long speed) {
        this.deletingSpeed = speed;
    }

    public void setPauseBetweenTexts(long pause) {
        this.pauseBetweenTexts = pause;
    }
    
    // Method to restart the animation
    public void restartAnimation() {
        animationCompleted = false;
        currentTextIndex = 0;
        currentCharIndex = 0;
        isDeleting = false;
        setText("");
        startAnimation();
    }
}