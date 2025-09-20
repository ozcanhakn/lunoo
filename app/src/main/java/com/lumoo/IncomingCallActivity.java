package com.lumoo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class IncomingCallActivity extends AppCompatActivity {

    private String callerId;
    private String callId;
    private DatabaseReference callRef;
    private ValueEventListener callStatusListener;

    // Animation views
    private View pulseRing1, pulseRing2;
    private View particle1, particle2;
    private CircleImageView profileImage;
    private TextView callerName, callStatus;
    private ImageView btnAccept, btnReject;

    // Animation objects
    private AnimatorSet pulseAnimator;
    private AnimatorSet particleAnimator;
    private Handler animationHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Full screen and keep screen on
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );

        setContentView(R.layout.activity_incoming_call);

        // Get data from intent
        callerId = getIntent().getStringExtra("callerId");
        callId = getIntent().getStringExtra("callId");

        if (callerId == null || callId == null) {
            Toast.makeText(this, "Çağrı bilgileri alınamadı", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        loadCallerInfo();
        setupClickListeners();
        setupCallStatusListener();
        startAnimations();
    }

    private void initializeViews() {
        pulseRing1 = findViewById(R.id.pulseRing1);
        pulseRing2 = findViewById(R.id.pulseRing2);
        particle1 = findViewById(R.id.particle1);
        particle2 = findViewById(R.id.particle2);
        profileImage = findViewById(R.id.profileImage);
        callerName = findViewById(R.id.callerName);
        callStatus = findViewById(R.id.callStatus);
        btnAccept = findViewById(R.id.btnAccept);
        btnReject = findViewById(R.id.btnReject);

        // Initial setup for animations
        pulseRing1.setScaleX(0.8f);
        pulseRing1.setScaleY(0.8f);
        pulseRing1.setAlpha(0f);

        pulseRing2.setScaleX(0.9f);
        pulseRing2.setScaleY(0.9f);
        pulseRing2.setAlpha(0f);

        // Entrance animation for main content
        profileImage.setScaleX(0f);
        profileImage.setScaleY(0f);
        callerName.setAlpha(0f);
        callerName.setTranslationY(50f);
        callStatus.setAlpha(0f);
        callStatus.setTranslationY(30f);

        btnAccept.setScaleX(0f);
        btnAccept.setScaleY(0f);
        btnReject.setScaleX(0f);
        btnReject.setScaleY(0f);
    }

    private void loadCallerInfo() {
        FirebaseDatabase.getInstance().getReference()
                .child("Kullanıcılar")
                .child(callerId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String profileImageUrl = snapshot.child("profileImage").getValue(String.class);
                            String kullaniciAdi = snapshot.child("kullanıcıAdı").getValue(String.class);

                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                Glide.with(IncomingCallActivity.this)
                                        .load(profileImageUrl)
                                        .apply(RequestOptions.circleCropTransform())
                                        .placeholder(R.drawable.default_avatar)
                                        .error(R.drawable.default_avatar)
                                        .into(profileImage);
                            }

                            if (kullaniciAdi != null) {
                                callerName.setText(kullaniciAdi);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("IncomingCall", "Kullanıcı bilgileri yüklenemedi: " + error.getMessage());
                    }
                });
    }

    private void setupClickListeners() {
        btnAccept.setOnClickListener(v -> {
            animateButtonClick(btnAccept, this::acceptCall);
        });

        btnReject.setOnClickListener(v -> {
            animateButtonClick(btnReject, this::rejectCall);
        });
    }

    private void animateButtonClick(View button, Runnable action) {
        ObjectAnimator scaleDown = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.8f);
        scaleDown.setDuration(100);
        scaleDown.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator scaleUp = ObjectAnimator.ofFloat(button, "scaleX", 0.8f, 1f);
        scaleUp.setDuration(100);
        scaleUp.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.8f);
        scaleDownY.setDuration(100);
        scaleDownY.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(button, "scaleY", 0.8f, 1f);
        scaleUpY.setDuration(100);
        scaleUpY.setInterpolator(new AccelerateDecelerateInterpolator());

        AnimatorSet clickAnimation = new AnimatorSet();
        clickAnimation.playTogether(scaleDown, scaleDownY);
        clickAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                AnimatorSet restoreAnimation = new AnimatorSet();
                restoreAnimation.playTogether(scaleUp, scaleUpY);
                restoreAnimation.start();

                // Execute the action after animation
                animationHandler.postDelayed(action, 50);
            }
        });
        clickAnimation.start();
    }

    private void startAnimations() {
        // Entrance animation
        animationHandler.postDelayed(() -> {
            // Profile image entrance
            ObjectAnimator profileScaleX = ObjectAnimator.ofFloat(profileImage, "scaleX", 0f, 1f);
            ObjectAnimator profileScaleY = ObjectAnimator.ofFloat(profileImage, "scaleY", 0f, 1f);
            profileScaleX.setDuration(800);
            profileScaleY.setDuration(800);
            profileScaleX.setInterpolator(new AccelerateDecelerateInterpolator());
            profileScaleY.setInterpolator(new AccelerateDecelerateInterpolator());

            AnimatorSet profileAnimation = new AnimatorSet();
            profileAnimation.playTogether(profileScaleX, profileScaleY);
            profileAnimation.start();

            // Text animations
            animationHandler.postDelayed(() -> {
                ObjectAnimator nameAlpha = ObjectAnimator.ofFloat(callerName, "alpha", 0f, 1f);
                ObjectAnimator nameTranslation = ObjectAnimator.ofFloat(callerName, "translationY", 50f, 0f);
                nameAlpha.setDuration(600);
                nameTranslation.setDuration(600);

                AnimatorSet nameAnimation = new AnimatorSet();
                nameAnimation.playTogether(nameAlpha, nameTranslation);
                nameAnimation.start();

                ObjectAnimator statusAlpha = ObjectAnimator.ofFloat(callStatus, "alpha", 0f, 1f);
                ObjectAnimator statusTranslation = ObjectAnimator.ofFloat(callStatus, "translationY", 30f, 0f);
                statusAlpha.setDuration(600);
                statusTranslation.setDuration(600);
                statusAlpha.setStartDelay(200);
                statusTranslation.setStartDelay(200);

                AnimatorSet statusAnimation = new AnimatorSet();
                statusAnimation.playTogether(statusAlpha, statusTranslation);
                statusAnimation.start();
            }, 300);

            // Button animations
            animationHandler.postDelayed(() -> {
                ObjectAnimator rejectScaleX = ObjectAnimator.ofFloat(btnReject, "scaleX", 0f, 1f);
                ObjectAnimator rejectScaleY = ObjectAnimator.ofFloat(btnReject, "scaleY", 0f, 1f);
                rejectScaleX.setDuration(500);
                rejectScaleY.setDuration(500);
                rejectScaleX.setInterpolator(new AccelerateDecelerateInterpolator());
                rejectScaleY.setInterpolator(new AccelerateDecelerateInterpolator());

                AnimatorSet rejectAnimation = new AnimatorSet();
                rejectAnimation.playTogether(rejectScaleX, rejectScaleY);
                rejectAnimation.start();

                ObjectAnimator acceptScaleX = ObjectAnimator.ofFloat(btnAccept, "scaleX", 0f, 1f);
                ObjectAnimator acceptScaleY = ObjectAnimator.ofFloat(btnAccept, "scaleY", 0f, 1f);
                acceptScaleX.setDuration(500);
                acceptScaleY.setDuration(500);
                acceptScaleX.setStartDelay(100);
                acceptScaleY.setStartDelay(100);
                acceptScaleX.setInterpolator(new AccelerateDecelerateInterpolator());
                acceptScaleY.setInterpolator(new AccelerateDecelerateInterpolator());

                AnimatorSet acceptAnimation = new AnimatorSet();
                acceptAnimation.playTogether(acceptScaleX, acceptScaleY);
                acceptAnimation.start();
            }, 800);
        }, 200);

        // Start continuous animations
        startPulseAnimation();
        startParticleAnimation();
    }

    private void startPulseAnimation() {
        // Outer ring pulse
        ObjectAnimator pulseScale1X = ObjectAnimator.ofFloat(pulseRing1, "scaleX", 0.8f, 1.2f);
        ObjectAnimator pulseScale1Y = ObjectAnimator.ofFloat(pulseRing1, "scaleY", 0.8f, 1.2f);
        ObjectAnimator pulseAlpha1 = ObjectAnimator.ofFloat(pulseRing1, "alpha", 0.8f, 0f);

        pulseScale1X.setDuration(2000);
        pulseScale1Y.setDuration(2000);
        pulseAlpha1.setDuration(2000);
        pulseScale1X.setRepeatCount(ValueAnimator.INFINITE);
        pulseScale1Y.setRepeatCount(ValueAnimator.INFINITE);
        pulseAlpha1.setRepeatCount(ValueAnimator.INFINITE);
        pulseScale1X.setInterpolator(new LinearInterpolator());
        pulseScale1Y.setInterpolator(new LinearInterpolator());
        pulseAlpha1.setInterpolator(new LinearInterpolator());

        // Inner ring pulse
        ObjectAnimator pulseScale2X = ObjectAnimator.ofFloat(pulseRing2, "scaleX", 0.9f, 1.1f);
        ObjectAnimator pulseScale2Y = ObjectAnimator.ofFloat(pulseRing2, "scaleY", 0.9f, 1.1f);
        ObjectAnimator pulseAlpha2 = ObjectAnimator.ofFloat(pulseRing2, "alpha", 1f, 0f);

        pulseScale2X.setDuration(1500);
        pulseScale2Y.setDuration(1500);
        pulseAlpha2.setDuration(1500);
        pulseScale2X.setRepeatCount(ValueAnimator.INFINITE);
        pulseScale2Y.setRepeatCount(ValueAnimator.INFINITE);
        pulseAlpha2.setRepeatCount(ValueAnimator.INFINITE);
        pulseScale2X.setStartDelay(500);
        pulseScale2Y.setStartDelay(500);
        pulseAlpha2.setStartDelay(500);
        pulseScale2X.setInterpolator(new LinearInterpolator());
        pulseScale2Y.setInterpolator(new LinearInterpolator());
        pulseAlpha2.setInterpolator(new LinearInterpolator());

        pulseAnimator = new AnimatorSet();
        pulseAnimator.playTogether(
                pulseScale1X, pulseScale1Y, pulseAlpha1,
                pulseScale2X, pulseScale2Y, pulseAlpha2
        );
        pulseAnimator.start();
    }

    private void startParticleAnimation() {
        // Particle floating animations
        ObjectAnimator particle1Y = ObjectAnimator.ofFloat(particle1, "translationY", 0f, -50f, 0f);
        ObjectAnimator particle1Alpha = ObjectAnimator.ofFloat(particle1, "alpha", 0.6f, 0.2f, 0.6f);
        particle1Y.setDuration(3000);
        particle1Alpha.setDuration(3000);
        particle1Y.setRepeatCount(ValueAnimator.INFINITE);
        particle1Alpha.setRepeatCount(ValueAnimator.INFINITE);

        ObjectAnimator particle2Y = ObjectAnimator.ofFloat(particle2, "translationY", 0f, 30f, 0f);
        ObjectAnimator particle2Alpha = ObjectAnimator.ofFloat(particle2, "alpha", 0.4f, 0.8f, 0.4f);
        particle2Y.setDuration(2500);
        particle2Alpha.setDuration(2500);
        particle2Y.setRepeatCount(ValueAnimator.INFINITE);
        particle2Alpha.setRepeatCount(ValueAnimator.INFINITE);
        particle2Y.setStartDelay(1000);
        particle2Alpha.setStartDelay(1000);

        particleAnimator = new AnimatorSet();
        particleAnimator.playTogether(particle1Y, particle1Alpha, particle2Y, particle2Alpha);
        particleAnimator.start();
    }

    private void setupCallStatusListener() {
        callRef = FirebaseDatabase.getInstance().getReference("calls").child(callId);

        callStatusListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(IncomingCallActivity.this, "Çağrı iptal edildi", Toast.LENGTH_SHORT).show();
                    finishWithAnimation();
                } else {
                    Integer status = snapshot.child("status").getValue(Integer.class);
                    if (status != null && status == 2) {
                        Toast.makeText(IncomingCallActivity.this, "Çağrı sonlandırıldı", Toast.LENGTH_SHORT).show();
                        finishWithAnimation();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("IncomingCall", "Çağrı durumu dinlenemedi: " + error.getMessage());
            }
        };

        callRef.addValueEventListener(callStatusListener);
    }

    private void acceptCall() {
        // Disable buttons to prevent multiple clicks
        btnAccept.setEnabled(false);
        btnReject.setEnabled(false);

        HashMap<String, Object> update = new HashMap<>();
        update.put("status", 1); // Kabul edildi

        FirebaseDatabase.getInstance().getReference("calls")
                .child(callId)
                .updateChildren(update)
                .addOnSuccessListener(aVoid -> {
                    // Start CallActivity
                    Intent intent = new Intent(IncomingCallActivity.this, CallActivity.class);
                    intent.putExtra("username", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    intent.putExtra("incoming", callerId);
                    intent.putExtra("createdBy", callerId);
                    intent.putExtra("isAvailable", true);
                    startActivity(intent);
                    finishWithAnimation();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(IncomingCallActivity.this, "Çağrı kabul edilemedi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // Re-enable buttons
                    btnAccept.setEnabled(true);
                    btnReject.setEnabled(true);
                });
    }

    private void rejectCall() {
        // Disable buttons to prevent multiple clicks
        btnAccept.setEnabled(false);
        btnReject.setEnabled(false);

        FirebaseDatabase.getInstance().getReference("calls")
                .child(callId)
                .removeValue()
                .addOnSuccessListener(aVoid -> finishWithAnimation())
                .addOnFailureListener(e -> {
                    Toast.makeText(IncomingCallActivity.this, "Çağrı reddedilemedi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // Re-enable buttons
                    btnAccept.setEnabled(true);
                    btnReject.setEnabled(true);
                });
    }

    private void finishWithAnimation() {
        // Stop all animations
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
        }
        if (particleAnimator != null) {
            particleAnimator.cancel();
        }

        // Exit animation
        ObjectAnimator profileScaleX = ObjectAnimator.ofFloat(profileImage, "scaleX", 1f, 0f);
        ObjectAnimator profileScaleY = ObjectAnimator.ofFloat(profileImage, "scaleY", 1f, 0f);
        ObjectAnimator profileAlpha = ObjectAnimator.ofFloat(profileImage, "alpha", 1f, 0f);

        ObjectAnimator nameAlpha = ObjectAnimator.ofFloat(callerName, "alpha", 1f, 0f);
        ObjectAnimator statusAlpha = ObjectAnimator.ofFloat(callStatus, "alpha", 1f, 0f);

        ObjectAnimator acceptScale = ObjectAnimator.ofFloat(btnAccept, "scaleX", 1f, 0f);
        ObjectAnimator rejectScale = ObjectAnimator.ofFloat(btnReject, "scaleX", 1f, 0f);

        AnimatorSet exitAnimation = new AnimatorSet();
        exitAnimation.playTogether(
                profileScaleX, profileScaleY, profileAlpha,
                nameAlpha, statusAlpha, acceptScale, rejectScale
        );
        exitAnimation.setDuration(300);
        exitAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                finish();
                overridePendingTransition(0, 0);
            }
        });
        exitAnimation.start();
    }

    @Override
    public void onBackPressed() {
        rejectCall();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Clean up animations
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
        }
        if (particleAnimator != null) {
            particleAnimator.cancel();
        }
        if (animationHandler != null) {
            animationHandler.removeCallbacksAndMessages(null);
        }

        // Remove Firebase listener
        if (callRef != null && callStatusListener != null) {
            callRef.removeEventListener(callStatusListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause animations to save battery
        if (pulseAnimator != null && pulseAnimator.isRunning()) {
            pulseAnimator.pause();
        }
        if (particleAnimator != null && particleAnimator.isRunning()) {
            particleAnimator.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume animations
        if (pulseAnimator != null && pulseAnimator.isPaused()) {
            pulseAnimator.resume();
        }
        if (particleAnimator != null && particleAnimator.isPaused()) {
            particleAnimator.resume();
        }
    }
}