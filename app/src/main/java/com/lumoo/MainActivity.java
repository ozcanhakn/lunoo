package com.lumoo;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

// Typing animation views (kendi kütüphanen)
// import your.typinganimationlibrary.*;

public class MainActivity extends AppCompatActivity {
    RelativeLayout register;
    TextView signIn;
    FirebaseAuth mAuth;

    SequentialTypingTextView txtHeader1; // kendi kütüphanen
    TypingAnimationTextView txtClickJoinExplain;

    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 100;

    private ImageView logoImage, heart1, heart2, heart3, heart4;
    private TextView appName, subtitle;
    private CardView joinButton, loginButton, googleLoginButton;
    private View pulseRing1, pulseRing2, shimmerEffect1, logoGlow;
    private ImageView floatingIcon1, floatingIcon2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Google Sign-In ayarları
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth.addAuthStateListener(firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            } else {
                initViews();
                startAnimations();
                setupClickListeners();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (txtHeader1 != null) {
            txtHeader1.restartAnimation();
        }
    }

    private void initViews() {
        logoImage = findViewById(R.id.logoImage);
        appName = findViewById(R.id.appName);
        subtitle = findViewById(R.id.subtitle);
        joinButton = findViewById(R.id.joinButton);
        loginButton = findViewById(R.id.loginButton);
        googleLoginButton = findViewById(R.id.googleLoginButton);

        heart1 = findViewById(R.id.heart1);
        heart2 = findViewById(R.id.heart2);
        heart3 = findViewById(R.id.heart3);
        heart4 = findViewById(R.id.heart4);

        pulseRing1 = findViewById(R.id.pulseRing1);
        pulseRing2 = findViewById(R.id.pulseRing2);
        shimmerEffect1 = findViewById(R.id.shimmerEffect1);
        logoGlow = findViewById(R.id.logoGlow);

        floatingIcon1 = findViewById(R.id.floatingIcon1);
        floatingIcon2 = findViewById(R.id.floatingIcon2);
    }

    private void startAnimations() {
        logoImage.setScaleX(0f);
        logoImage.setScaleY(0f);
        logoImage.setRotation(-180f);
        logoImage.animate()
                .scaleX(1f)
                .scaleY(1f)
                .rotation(0f)
                .setDuration(1000)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .start();

        animateLogoGlow();

        appName.setTranslationY(-200f);
        appName.setAlpha(0f);
        appName.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(800)
                .setStartDelay(300)
                .setInterpolator(new BounceInterpolator())
                .start();

        animateSubtitleTypewriter();
        animateButtonsEntrance();
        animateFloatingHearts();
        animatePulseRings();
        animateShimmerEffect();
        animateFloatingIcons();
    }

    private void animateLogoGlow() {
        ObjectAnimator glowAnimator = ObjectAnimator.ofFloat(logoGlow, "alpha", 0.3f, 0.7f);
        glowAnimator.setDuration(1500);
        glowAnimator.setRepeatMode(ValueAnimator.REVERSE);
        glowAnimator.setRepeatCount(ValueAnimator.INFINITE);
        glowAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        glowAnimator.start();
    }

    private void animateSubtitleTypewriter() {
        String fullText = "Aşkı Keşfet, Anıları Paylaş";
        subtitle.setText("");
        subtitle.setAlpha(1f);

        Handler handler = new Handler();
        for (int i = 0; i <= fullText.length(); i++) {
            final int index = i;
            handler.postDelayed(() -> subtitle.setText(fullText.substring(0, index)), i * 50 + 800);
        }
    }

    private void animateButtonsEntrance() {
        joinButton.setTranslationY(300f);
        loginButton.setTranslationY(350f);
        googleLoginButton.setTranslationY(400f);

        joinButton.setAlpha(0f);
        loginButton.setAlpha(0f);
        googleLoginButton.setAlpha(0f);

        joinButton.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(1200)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        loginButton.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(1400)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        googleLoginButton.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(1600)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void animateFloatingHearts() {
        animateFloatingElement(heart1, 2000, 0f, -30f);
        animateFloatingElement(heart2, 2500, 0f, -25f);
        animateFloatingElement(heart3, 1800, 0f, -35f);
        animateFloatingElement(heart4, 2200, 0f, -28f);
    }

    private void animateFloatingElement(View element, int duration, float startY, float endY) {
        ObjectAnimator floatAnimator = ObjectAnimator.ofFloat(element, "translationY", startY, endY);
        floatAnimator.setDuration(duration);
        floatAnimator.setRepeatMode(ValueAnimator.REVERSE);
        floatAnimator.setRepeatCount(ValueAnimator.INFINITE);
        floatAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        floatAnimator.start();

        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(element, "rotation", 0f, 360f);
        rotateAnimator.setDuration(duration * 2);
        rotateAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rotateAnimator.setInterpolator(new LinearInterpolator());
        rotateAnimator.start();
    }

    private void animatePulseRings() {
        ObjectAnimator scaleX1 = ObjectAnimator.ofFloat(pulseRing1, "scaleX", 0.8f, 1.2f);
        scaleX1.setRepeatCount(ValueAnimator.INFINITE);
        scaleX1.setRepeatMode(ValueAnimator.RESTART);

        ObjectAnimator scaleY1 = ObjectAnimator.ofFloat(pulseRing1, "scaleY", 0.8f, 1.2f);
        scaleY1.setRepeatCount(ValueAnimator.INFINITE);
        scaleY1.setRepeatMode(ValueAnimator.RESTART);

        ObjectAnimator alpha1 = ObjectAnimator.ofFloat(pulseRing1, "alpha", 0.1f, 0f);
        alpha1.setRepeatCount(ValueAnimator.INFINITE);
        alpha1.setRepeatMode(ValueAnimator.RESTART);

        AnimatorSet pulseSet1 = new AnimatorSet();
        pulseSet1.playTogether(scaleX1, scaleY1, alpha1);
        pulseSet1.setDuration(2000);
        pulseSet1.setInterpolator(new AccelerateInterpolator());
        pulseSet1.start();

        ObjectAnimator scaleX2 = ObjectAnimator.ofFloat(pulseRing2, "scaleX", 0.6f, 1.4f);
        scaleX2.setRepeatCount(ValueAnimator.INFINITE);
        scaleX2.setRepeatMode(ValueAnimator.RESTART);

        ObjectAnimator scaleY2 = ObjectAnimator.ofFloat(pulseRing2, "scaleY", 0.6f, 1.4f);
        scaleY2.setRepeatCount(ValueAnimator.INFINITE);
        scaleY2.setRepeatMode(ValueAnimator.RESTART);

        ObjectAnimator alpha2 = ObjectAnimator.ofFloat(pulseRing2, "alpha", 0.05f, 0f);
        alpha2.setRepeatCount(ValueAnimator.INFINITE);
        alpha2.setRepeatMode(ValueAnimator.RESTART);

        AnimatorSet pulseSet2 = new AnimatorSet();
        pulseSet2.playTogether(scaleX2, scaleY2, alpha2);
        pulseSet2.setDuration(2500);
        pulseSet2.setStartDelay(500);
        pulseSet2.setInterpolator(new AccelerateInterpolator());
        pulseSet2.start();
    }

    private void animateShimmerEffect() {
        ObjectAnimator shimmer = ObjectAnimator.ofFloat(shimmerEffect1, "translationX", -100f, 400f);
        shimmer.setDuration(1500);
        shimmer.setRepeatCount(ValueAnimator.INFINITE);
        shimmer.setRepeatMode(ValueAnimator.RESTART);
        shimmer.setStartDelay(2000);
        shimmer.setInterpolator(new AccelerateDecelerateInterpolator());
        shimmer.start();
    }

    private void animateFloatingIcons() {
        ObjectAnimator rotate1 = ObjectAnimator.ofFloat(floatingIcon1, "rotation", 0f, 360f);
        rotate1.setDuration(4000);
        rotate1.setRepeatCount(ValueAnimator.INFINITE);
        rotate1.setInterpolator(new LinearInterpolator());
        rotate1.start();

        ObjectAnimator float1 = ObjectAnimator.ofFloat(floatingIcon1, "translationY", 0f, -15f);
        float1.setDuration(2000);
        float1.setRepeatMode(ValueAnimator.REVERSE);
        float1.setRepeatCount(ValueAnimator.INFINITE);
        float1.setInterpolator(new AccelerateDecelerateInterpolator());
        float1.start();

        ObjectAnimator rotate2 = ObjectAnimator.ofFloat(floatingIcon2, "rotation", 0f, -360f);
        rotate2.setDuration(3000);
        rotate2.setRepeatCount(ValueAnimator.INFINITE);
        rotate2.setInterpolator(new LinearInterpolator());
        rotate2.start();

        ObjectAnimator float2 = ObjectAnimator.ofFloat(floatingIcon2, "translationY", 0f, -20f);
        float2.setDuration(2500);
        float2.setRepeatMode(ValueAnimator.REVERSE);
        float2.setRepeatCount(ValueAnimator.INFINITE);
        float2.setInterpolator(new AccelerateDecelerateInterpolator());
        float2.start();
    }

    private void setupClickListeners() {
        setupButtonClickAnimation(joinButton, () -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
            Toast.makeText(this, "LUNOO'ya Katılın!", Toast.LENGTH_SHORT).show();
        });

        setupButtonClickAnimation(loginButton, () -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            Toast.makeText(this, "Oturum Açılıyor...", Toast.LENGTH_SHORT).show();
        });

        setupButtonClickAnimation(googleLoginButton, () -> {
            signInWithGoogle();
            Toast.makeText(this, "yönlendiriliyor...", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupButtonClickAnimation(View button, Runnable action) {
        button.setOnClickListener(v -> {
            // Animasyonu başlat
            v.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(100).start())
                    .start();

            // Action'ı **hemen çalıştır**
            action.run();
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Google giriş başarısız!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                Toast.makeText(this, "Hoşgeldin: " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, PersonalInformationActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Firebase giriş başarısız!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
