package com.lumoo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lumoo.FCM.FCMNotificationSender;
import com.lumoo.databinding.ActivityConnectingBinding;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class ConnectingActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;

    ActivityConnectingBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    boolean isOkay = false;
    String targetUserId;
    String targetUserName;
    String callType;
    ValueEventListener callStatusListener;
    private Handler timeoutHandler;
    private Runnable timeoutRunnable;

    // Camera variables
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private CameraSelector cameraSelector;
    private Preview preview;
    private boolean isBackCamera = false;
    private ValueAnimator progressAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConnectingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        String profile = getIntent().getStringExtra("profile");
        targetUserId = getIntent().getStringExtra("targetUserId");
        targetUserName = getIntent().getStringExtra("targetUserName");
        callType = getIntent().getStringExtra("callType");

        if ("audio".equals(callType)) {
            binding.previewView.setVisibility(View.GONE);
            binding.switchCameraButton.setVisibility(View.GONE);
        } else {
            setupCameraPermission();
        }

        setupUI(profile);
        setupCameraPermission();
        setupClickListeners();

        String username = auth.getUid();
        Log.d("ConnectingActivity", "Starting " + callType + " call to: " + targetUserId);

        setupCallTimeout();
        checkUserAvailabilityAndStartCall(username, profile);
    }

    private void setupUI(String profile) {
        // Profil resmi yükle
        if (profile != null) {
            Glide.with(this)
                    .load(profile)
                    .circleCrop()
                    .into(binding.profile);
        }

        // Kullanıcı adını ayarla
        binding.name.setText(targetUserName);

        // Arama tipine göre metni güncelle
        String callTypeText = "video".equals(callType) ? "Video Arama" : "Sesli Arama";
        binding.callStatusText.setText(callTypeText + " Yapılıyor...");

        // Progress animasyonunu başlat
        setupProgressAnimation();

        // Profil resmi pulse animasyonu
        setupPulseAnimation();
    }

    private void setupProgressAnimation() {
        progressAnimator = ValueAnimator.ofInt(0, 30);
        progressAnimator.setDuration(30000); // 30 saniye
        progressAnimator.setInterpolator(new LinearInterpolator());
        progressAnimator.addUpdateListener(animation -> {
            int progress = (int) animation.getAnimatedValue();
            binding.progressBar.setProgress(progress);
        });
        progressAnimator.start();
    }

    private void setupPulseAnimation() {
        ObjectAnimator pulseAnimator = ObjectAnimator.ofFloat(binding.profile, "scaleX", 1.0f, 1.1f, 1.0f);
        pulseAnimator.setDuration(2000);
        pulseAnimator.setRepeatCount(ObjectAnimator.INFINITE);

        ObjectAnimator pulseAnimatorY = ObjectAnimator.ofFloat(binding.profile, "scaleY", 1.0f, 1.1f, 1.0f);
        pulseAnimatorY.setDuration(2000);
        pulseAnimatorY.setRepeatCount(ObjectAnimator.INFINITE);

        pulseAnimator.start();
        pulseAnimatorY.start();
    }

    private void setupCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            // Kamera izni varsa, önce CameraX'in hazır olup olmadığını kontrol et
            checkCameraXAvailability();
        }
    }

    private void checkCameraXAvailability() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                // CameraX hazır, kamerayı başlat
                startCamera();
            } catch (ExecutionException | InterruptedException e) {
                Log.e("ConnectingActivity", "CameraX not available", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Kamera başlatılamadı", Toast.LENGTH_SHORT).show();
                    // Kamera olmadan devam et (sesli arama için)
                    binding.previewView.setVisibility(View.GONE);
                    binding.switchCameraButton.setVisibility(View.GONE);
                });
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e("ConnectingActivity", "Camera initialization failed", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Kamera başlatılamadı", Toast.LENGTH_SHORT).show();
                    binding.previewView.setVisibility(View.GONE);
                    binding.switchCameraButton.setVisibility(View.GONE);
                });
            }
        }, ContextCompat.getMainExecutor(this));
    }
    private void bindPreview(ProcessCameraProvider cameraProvider) {
        preview = new Preview.Builder().build();

        cameraSelector = isBackCamera ? CameraSelector.DEFAULT_BACK_CAMERA
                : CameraSelector.DEFAULT_FRONT_CAMERA;

        preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, preview);
        } catch (Exception e) {
            Log.e("ConnectingActivity", "Camera binding failed", e);
        }
    }

    private void setupClickListeners() {
        // İptal butonu
        binding.cancelCallButton.setOnClickListener(v -> {
            cleanupCall();
            finish();
        });

        // Kamera değiştirme butonu
        binding.switchCameraButton.setOnClickListener(v -> {
            isBackCamera = !isBackCamera;
            if (cameraProviderFuture != null) {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);
                } catch (ExecutionException | InterruptedException e) {
                    Log.e("ConnectingActivity", "Camera switch failed", e);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkCameraXAvailability();
            } else {
                Toast.makeText(this, "Kamera izni gerekli", Toast.LENGTH_SHORT).show();
                // Kamera izni yoksa, video arama yerine sesli aramaya geç
                if ("video".equals(callType)) {
                    callType = "audio";
                    updateCallTypeUI();
                }
                binding.previewView.setVisibility(View.GONE);
                binding.switchCameraButton.setVisibility(View.GONE);
            }
        }
    }

    private void updateCallTypeUI() {
        String callTypeText = "video".equals(callType) ? "Video Arama" : "Sesli Arama";
        binding.callStatusText.setText(callTypeText + " Yapılıyor...");
    }

    private void checkUserAvailabilityAndStartCall(String username, String profile) {
        database.getReference().child("calls")
                .child(targetUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Integer status = snapshot.child("status").getValue(Integer.class);

                            if (status != null && status == 0) {
                                createCall(username, profile);
                            } else {
                                showUserBusyAndFinish();
                            }
                        } else {
                            createCall(username, profile);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        Log.e("ConnectingActivity", "Database error: " + error.getMessage());
                        createCall(username, profile);
                    }
                });
    }

    private void showUserBusyAndFinish() {
        runOnUiThread(() -> {
            binding.callStatusText.setText("Kullanıcı Meşgul");
            binding.waitingText.setText("Kullanıcı şu anda başka bir aramada");

            new Handler().postDelayed(() -> {
                Toast.makeText(ConnectingActivity.this, "Kullanıcı şu anda meşgul", Toast.LENGTH_SHORT).show();
                finish();
            }, 2000);
        });
    }

    private void setupCallTimeout() {
        timeoutHandler = new Handler();
        timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isOkay) {
                    runOnUiThread(() -> {
                        binding.callStatusText.setText("Zaman Aşımı");
                        binding.waitingText.setText("Arama yanıtlanmadı");
                    });

                    new Handler().postDelayed(() -> {
                        Toast.makeText(ConnectingActivity.this, "Arama zaman aşımına uğradı", Toast.LENGTH_SHORT).show();
                        cleanupCall();
                        finish();
                    }, 1500);
                }
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, 30000);
    }

    private void createCall(String username, String profile) {
        HashMap<String, Object> callData = new HashMap<>();
        callData.put("incoming", username);
        callData.put("createdBy", username);
        callData.put("isAvailable", true);
        callData.put("status", 0);
        callData.put("targetUser", targetUserId);
        callData.put("profileImage", profile);
        callData.put("timestamp", System.currentTimeMillis());
        callData.put("callType", callType);

        Log.d("ConnectingActivity", "Creating call data: " + callData.toString());

        database.getReference()
                .child("calls")
                .child(targetUserId)
                .setValue(callData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("ConnectingActivity", "Call data created successfully");
                        sendCallNotification(targetUserId, username, callType);
                        waitForResponse(username);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ConnectingActivity", "Failed to create call: " + e.getMessage());
                    Toast.makeText(ConnectingActivity.this, "Arama başlatılamadı", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void sendCallNotification(String targetUserId, String callerId, String callType) {
        database.getReference().child("Tokens").child(targetUserId).child("token")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String token = snapshot.getValue(String.class);
                            if (token != null) {
                                String title = "Yeni " + ("video".equals(callType) ? "video arama" : "sesli arama");
                                String message = "Bir arama geliyor";

                                FCMNotificationSender notificationSender = new FCMNotificationSender(token, title, message, getApplicationContext());
                                notificationSender.sendNotification();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("ConnectingActivity", "FCM token alınamadı: " + error.getMessage());
                    }
                });
    }

    private void waitForResponse(String username) {
        callStatusListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer status = snapshot.child("status").getValue(Integer.class);
                    Log.d("ConnectingActivity", "Status changed: " + status);

                    if (status != null) {
                        if (status == 1) {
                            handleCallAccepted(username);
                        } else if (status == 2) {
                            handleCallRejected();
                        }
                    }
                } else {
                    handleCallEnded();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Log.e("ConnectingActivity", "Call status listener error: " + error.getMessage());
            }
        };

        database.getReference()
                .child("calls")
                .child(targetUserId)
                .addValueEventListener(callStatusListener);
    }

    private void handleCallAccepted(String username) {
        if (isOkay) return;

        isOkay = true;
        cancelTimeout();

        runOnUiThread(() -> {
            binding.callStatusText.setText("Arama Kabul Edildi");
            binding.waitingText.setText("Bağlanıyor...");
        });

        new Handler().postDelayed(() -> {
            if ("video".equals(callType)) {
                Intent intent = new Intent(ConnectingActivity.this, CallActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("incoming", targetUserId);
                intent.putExtra("createdBy", username);
                intent.putExtra("isAvailable", true);
                startActivity(intent);
            } else {
                Intent intent = new Intent(ConnectingActivity.this, AudioCallActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("incoming", targetUserId);
                intent.putExtra("createdBy", username);
                intent.putExtra("isAvailable", true);
                startActivity(intent);
            }
            finish();
        }, 1000);
    }

    private void handleCallRejected() {
        runOnUiThread(() -> {
            binding.callStatusText.setText("Arama Reddedildi");
            binding.waitingText.setText("Kullanıcı aramayı reddetti");
        });

        new Handler().postDelayed(() -> {
            Toast.makeText(ConnectingActivity.this, "Arama reddedildi", Toast.LENGTH_SHORT).show();
            finish();
        }, 1500);
    }

    private void handleCallEnded() {
        if (!isOkay) {
            runOnUiThread(() -> {
                binding.callStatusText.setText("Arama Sonlandırıldı");
                binding.waitingText.setText("Arama iptal edildi");
            });

            new Handler().postDelayed(() -> {
                Toast.makeText(ConnectingActivity.this, "Arama sonlandırıldı", Toast.LENGTH_SHORT).show();
                finish();
            }, 1500);
        }
    }

    private void cancelTimeout() {
        if (timeoutHandler != null && timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }
        if (progressAnimator != null) {
            progressAnimator.cancel();
        }
    }

    private void cleanupCall() {
        if (targetUserId != null) {
            database.getReference().child("calls").child(targetUserId).removeValue();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        cancelTimeout();

        if (callStatusListener != null && targetUserId != null) {
            database.getReference().child("calls").child(targetUserId)
                    .removeEventListener(callStatusListener);
        }

        if (!isOkay) {
            cleanupCall();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        cleanupCall();
    }
}