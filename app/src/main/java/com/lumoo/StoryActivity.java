package com.lumoo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import com.lumoo.util.ImageUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StoryActivity extends AppCompatActivity {

    // UI Elements
    private ImageView imageView, closeButton, filterIndicator;
    private CardView uploadButton, cameraButtonContainer, galleryButtonContainer;
    private LinearLayout placeholderContent;
    private CircularProgressIndicator progressIndicator;

    // Data
    private String currentUid;
    private Bitmap selectedBitmap;
    private boolean isImageSelected = false;
    private String currentPhotoPath;

    // Constants
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_SELECT = 2;
    private static final int ANIMATION_DURATION = 300;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        // Hide action bar and set window insets
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.hide();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.story), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupFirebaseAuth();
        setupClickListeners();
        startInitialAnimations();
    }

    private void initializeViews() {
        // Initialize all UI elements
        uploadButton = findViewById(R.id.btn_story_upload);
        cameraButtonContainer = findViewById(R.id.btn_camera_container);
        galleryButtonContainer = findViewById(R.id.btn_gallery_container);
        imageView = findViewById(R.id.iv_storyUp);
        closeButton = findViewById(R.id.iv_close);
        filterIndicator = findViewById(R.id.iv_filter_indicator);
        placeholderContent = findViewById(R.id.ll_placeholder);
        progressIndicator = findViewById(R.id.pb_storyUp);
    }

    private void setupFirebaseAuth() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUid = user.getUid();
        } else {
            showStyledToast("Kullanıcı girişi yapılmamış");
            finish();
        }
    }

    private void setupClickListeners() {
        // Close button
        closeButton.setOnClickListener(v -> {
            animateButtonClick(v, this::finish);
        });

        // Camera button with animation
        cameraButtonContainer.setOnClickListener(v -> {
            animateButtonClick(v, this::dispatchTakePictureIntent);
        });

        // Gallery button with animation
        galleryButtonContainer.setOnClickListener(v -> {
            animateButtonClick(v, this::openGallery);
        });

        // Upload button with enhanced animation
        uploadButton.setOnClickListener(v -> {
            if (selectedBitmap != null) {
                animateUploadProcess();
            } else {
                showStyledToast("Önce bir görsel seçin");
                shakeAnimation(uploadButton);
            }
        });

        // Filter indicator (for future features)
        filterIndicator.setOnClickListener(v -> {
            if (isImageSelected) {
                animateButtonClick(v, () -> {
                    // Future: Open filter selection
                    showStyledToast("Filtreler yakında geliyor!");
                });
            }
        });
    }

    private void startInitialAnimations() {
        // Fade in animation for the main container
        View mainContainer = findViewById(R.id.cv_preview_container);
        mainContainer.setAlpha(0f);
        mainContainer.setScaleX(0.9f);
        mainContainer.setScaleY(0.9f);

        mainContainer.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(500)
                .setInterpolator(new OvershootInterpolator(0.5f))
                .start();

        // Animate buttons with staggered delay
        animateButtonsEntrance();
    }

    private void animateButtonsEntrance() {
        View[] buttons = {cameraButtonContainer, galleryButtonContainer, uploadButton};

        for (int i = 0; i < buttons.length; i++) {
            final View button = buttons[i];
            button.setTranslationY(100f);
            button.setAlpha(0f);

            button.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setDuration(400)
                    .setStartDelay(i * 100L)
                    .setInterpolator(new OvershootInterpolator(0.8f))
                    .start();
        }
    }

    private void animateButtonClick(View view, Runnable action) {
        // Scale animation for button press
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .withEndAction(action)
                            .start();
                })
                .start();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Geçici dosya oluştur
            File photoFile = createImageFile();
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        getPackageName() + ".fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() {
        // Geçici bir görüntü dosyası oluştur
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try {
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );

            currentPhotoPath = image.getAbsolutePath();
            return image;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_SELECT);
    }

    private void animateUploadProcess() {
        // Show progress and animate upload button
        progressIndicator.setVisibility(View.VISIBLE);
        uploadButton.setEnabled(false);

        // Pulse animation for upload button
        ValueAnimator pulseAnimator = ValueAnimator.ofFloat(1f, 1.05f);
        pulseAnimator.setDuration(800);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.setRepeatCount(1);
        pulseAnimator.addUpdateListener(animation -> {
            float scale = (Float) animation.getAnimatedValue();
            uploadButton.setScaleX(scale);
            uploadButton.setScaleY(scale);
        });

        pulseAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                proceedToEditor();
            }
        });

        pulseAnimator.start();
    }

    private void proceedToEditor() {
        if (selectedBitmap != null) {
            try {
                // Geçici dosyaya kaydet
                File tempFile = createTempImageFile();
                if (tempFile != null) {
                    FileOutputStream fos = new FileOutputStream(tempFile);
                    selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                    fos.close();

                    Intent intent = new Intent(StoryActivity.this, StoryEditorActivity.class);
                    intent.putExtra("image_path", tempFile.getAbsolutePath());

                    // Slide transition animation
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                }
            } catch (IOException e) {
                e.printStackTrace();
                showStyledToast("Dosya oluşturulamadı");
            }
        }
    }

    private File createTempImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TEMP_" + timeStamp + ".jpg";
        File storageDir = getCacheDir(); // Cache dizinini kullan
        return new File(storageDir, imageFileName);
    }

    private void shakeAnimation(View view) {
        ObjectAnimator shakeAnimator = ObjectAnimator.ofFloat(view, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
        shakeAnimator.setDuration(500);
        shakeAnimator.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            showProgressWithDelay(() -> {
                if (requestCode == REQUEST_IMAGE_CAPTURE) {
                    handleCameraResult();
                } else if (requestCode == REQUEST_IMAGE_SELECT && data != null) {
                    handleGalleryResult(data);
                }
            });
        }
    }

    private void showProgressWithDelay(Runnable action) {
        progressIndicator.setVisibility(View.VISIBLE);

        new Handler().postDelayed(() -> {
            action.run();
            progressIndicator.setVisibility(View.GONE);
        }, 800); // Realistic loading time
    }

    private void handleCameraResult() {
        if (currentPhotoPath != null) {
            try {
                // Dosyadan bitmap yükle
                selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),
                        Uri.fromFile(new File(currentPhotoPath)));
                processSelectedImage(selectedBitmap);
            } catch (IOException e) {
                e.printStackTrace();
                showStyledToast("Görsel yüklenirken hata oluştu");
            }
        }
    }

    private void handleGalleryResult(Intent data) {
        Uri imageUri = data.getData();
        if (imageUri != null) {
            try {
                Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                processSelectedImage(originalBitmap);
            } catch (IOException e) {
                e.printStackTrace();
                showStyledToast("Görsel yüklenirken hata oluştu");
            }
        }
    }

    private void processSelectedImage(Bitmap originalBitmap) {
        // Scale bitmap for optimal performance
        selectedBitmap = scaleBitmap(originalBitmap, 2000, 2000);

        // Animate image appearance
        animateImageTransition();

        // Clean up original bitmap
        if (originalBitmap != selectedBitmap) {
            originalBitmap.recycle();
        }

        isImageSelected = true;

        // Show filter indicator with animation
        showFilterIndicator();
    }

    private void animateImageTransition() {
        // Hide placeholder
        placeholderContent.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> placeholderContent.setVisibility(View.GONE))
                .start();

        // Set and animate new image
        imageView.setImageBitmap(selectedBitmap);
        imageView.setAlpha(0f);
        imageView.setScaleX(0.8f);
        imageView.setScaleY(0.8f);

        imageView.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .setInterpolator(new BounceInterpolator())
                .start();

        // Animate upload button to be more prominent
        uploadButton.animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(200)
                .withEndAction(() -> {
                    uploadButton.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(200)
                            .start();
                })
                .start();
    }

    private void showFilterIndicator() {
        filterIndicator.setVisibility(View.VISIBLE);
        filterIndicator.setAlpha(0f);
        filterIndicator.setScaleX(0f);
        filterIndicator.setScaleY(0f);

        filterIndicator.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .start();
    }

    private Bitmap scaleBitmap(Bitmap originalBitmap, int maxWidth, int maxHeight) {
        if (originalBitmap == null) return null;

        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        // Don't scale if already smaller than max dimensions
        if (width <= maxWidth && height <= maxHeight) {
            return originalBitmap;
        }

        // Maintain aspect ratio while scaling
        float ratio = (float) width / (float) height;

        if (width > height) {
            width = maxWidth;
            height = (int) (width / ratio);
        } else {
            height = maxHeight;
            width = (int) (height * ratio);
        }

        // High quality scaling
        return Bitmap.createScaledBitmap(originalBitmap, width, height, true);
    }

    private void showStyledToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        // Custom back animation
        View mainContainer = findViewById(R.id.cv_preview_container);
        mainContainer.animate()
                .alpha(0f)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(200)
                .withEndAction(super::onBackPressed)
                .start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Bitmap güvenli temizleme - ImageUtils kullan
        ImageUtils.recycleBitmap(selectedBitmap);
        selectedBitmap = null;
    }
}