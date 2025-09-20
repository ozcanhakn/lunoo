package com.lumoo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.lumoo.util.SecurityUtils;
import com.lumoo.util.ValidationUtils;

public class RegisterActivity extends AppCompatActivity {
    RelativeLayout btnStartingRegister;
    private TextView sliderTextView;
    private String[] sliderTexts = {
            "Yeni insanlarla tanış, anlamlı bağlantılar kur.",
            "Canlı yayınlarda eğlenceye katıl, sohbetlerde keyifli vakit geçir.",
            "Hayatına renk katacak yeni deneyimler seni bekliyor."
    };
    private int currentTextIndex = 0;
    private Animation fadeInAnimation, fadeOutAnimation;

    private Handler handler = new Handler();




    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sliderTextView = findViewById(R.id.sliderTextview); // TextView'ı initialize etme

        fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);

        fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                sliderTextView.setVisibility(View.VISIBLE); // Animasyon başladığında görünür yapma
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        sliderTextView.setAnimation(fadeInAnimation); // Başlangıçta fade-in animasyonu

        handler.postDelayed(runnable, 3000);

        btnStartingRegister = findViewById(R.id.btnStartingRegister);
        btnStartingRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, Register2Activity.class);
                startActivity(intent);
            }
        });
    }
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            sliderTextView.startAnimation(fadeOutAnimation); // Önce fade-out

            fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    sliderTextView.setText(sliderTexts[currentTextIndex]);
                    currentTextIndex = (currentTextIndex + 1) % sliderTexts.length;
                    sliderTextView.startAnimation(fadeInAnimation); // Sonra fade-in
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            handler.postDelayed(this, 3000);
        }
    };
}