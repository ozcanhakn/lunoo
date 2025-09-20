package com.lumoo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.lumoo.Model.AllUser;
import com.lumoo.Model.InterfaceJava;
import com.lumoo.R;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lumoo.databinding.ActivityCallBinding;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class CallActivity extends AppCompatActivity {

    ActivityCallBinding binding;
    String uniqueId = "";
    FirebaseAuth auth;
    String username = "";
    String friendsUsername = "";


    private boolean isAudioEnabled = true;
    private boolean isVideoEnabled = true;


    boolean isPeerConnected = false;

    DatabaseReference firebaseRef;

    boolean isAudio = true;
    boolean isVideo = true;
    String createdBy;

    boolean pageExit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firebaseRef = FirebaseDatabase.getInstance().getReference().child("calls");

        username = getIntent().getStringExtra("username");
        String incoming = getIntent().getStringExtra("incoming");
        createdBy = getIntent().getStringExtra("createdBy");

        updateAudioButton();
        updateVideoButton();


        friendsUsername = incoming;

        Log.d("CallActivity", "Username: " + username + ", Incoming: " + incoming + ", CreatedBy: " + createdBy);

        setupWebView();

        binding.micBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAudioEnabled = !isAudioEnabled;
                callJavaScriptFunction("javascript:toggleAudio(" + isAudioEnabled + ")");
                updateAudioButton();

                // Kısa feedback
                Toast.makeText(CallActivity.this,
                        isAudioEnabled ? "Mikrofon açıldı" : "Mikrofon kapatıldı",
                        Toast.LENGTH_SHORT).show();
            }
        });

        binding.videoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isVideoEnabled = !isVideoEnabled;
                callJavaScriptFunction("javascript:toggleVideo(" + isVideoEnabled + ")");
                updateVideoButton();

                // Kısa feedback
                Toast.makeText(CallActivity.this,
                        isVideoEnabled ? "Kamera açıldı" : "Kamera kapatıldı",
                        Toast.LENGTH_SHORT).show();
            }
        });


        binding.endCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endCall();
                finish();
            }
        });
    }

    void setupWebView() {
        binding.webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request.grant(request.getResources());
                }
            }
        });

        WebSettings webSettings = binding.webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);

        // Hata ayıklama için
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        binding.webView.addJavascriptInterface(new InterfaceJava(this), "Android");
        loadVideoCall();
    }
    public void loadVideoCall() {
        String filePath = "file:android_asset/call.html";
        binding.webView.loadUrl(filePath);

        binding.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                initializePeer();
            }
        });
    }

    void initializePeer() {
        uniqueId = getUniqueId();
        Log.d("CallActivity", "Initializing peer with ID: " + uniqueId);


        callJavaScriptFunction("javascript:init(\"" + uniqueId + "\")");

        if(createdBy.equalsIgnoreCase(username)) {
            Log.d("CallActivity", "Caller side - waiting for receiver");

            // Arama başlatıcısı
            if(pageExit) return;

            firebaseRef.child(username).child("connId").setValue(uniqueId);
            Log.d("CallActivity", "connId saved: " + uniqueId);
            firebaseRef.child(username).child("isAvailable").setValue(true);

            binding.loadingGroup.setVisibility(View.GONE);
            binding.controls.setVisibility(View.VISIBLE);

            // Profil bilgilerini al
            loadUserProfile(friendsUsername);

        } else {
            // Arama alıcısı
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    friendsUsername = createdBy;

                    // Profil bilgilerini al
                    loadUserProfile(friendsUsername);

                    // Arama başlatıcısının connection ID'sini bekle
                    FirebaseDatabase.getInstance().getReference()
                            .child("calls")
                            .child(friendsUsername)
                            .child("connId")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                    if(snapshot.getValue() != null) {
                                        sendCallRequest();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                                    Log.e("CallActivity", "Error getting connId: " + error.getMessage());
                                }
                            });
                }
            }, 3000);
        }
    }

    void loadUserProfile(String userId) {
        FirebaseDatabase.getInstance().getReference()
                .child("Kullanıcılar")
                .child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if(snapshot.exists()) {
                            String profileImage = snapshot.child("profileImage").getValue(String.class);
                            String kullaniciAdi = snapshot.child("kullanıcıAdı").getValue(String.class);

                            if (profileImage != null) {
                                Glide.with(CallActivity.this).load(profileImage)
                                        .circleCrop()
                                        .into(binding.profile);
                            }

                            if (kullaniciAdi != null) {
                                binding.name.setText(kullaniciAdi);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        Log.e("CallActivity", "Error loading user profile: " + error.getMessage());
                    }
                });
    }



    public void onPeerConnected(){
        isPeerConnected = true;
        Log.d("CallActivity", "Peer connected, isPeerConnected: " + isPeerConnected);

        // Arama başlatıcısı değilse (çağrıyı alan taraf) hemen arama yap
        if (!createdBy.equalsIgnoreCase(username)) {
            Log.d("CallActivity", "Receiver - sending call request");
            sendCallRequest();
        }
    }

    void sendCallRequest(){
        if(!isPeerConnected) {
            Toast.makeText(this, "Bağlantı kurulamadı. İnternet bağlantınızı kontrol edin.", Toast.LENGTH_SHORT).show();
            return;
        }

        listenConnId();
    }

    void listenConnId() {
        firebaseRef.child(friendsUsername).child("connId").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.getValue() == null)
                    return;

                binding.loadingGroup.setVisibility(View.GONE);
                binding.controls.setVisibility(View.VISIBLE);
                String connId = snapshot.getValue(String.class);
                callJavaScriptFunction("javascript:startCall(\""+connId+"\")");

                Log.d("CallActivity", "Starting call with connId: " + connId);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Log.e("CallActivity", "Error listening connId: " + error.getMessage());
            }
        });
    }

    void callJavaScriptFunction(String function){
        binding.webView.post(new Runnable() {
            @Override
            public void run() {
                try {
                    binding.webView.evaluateJavascript(function, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            Log.d("CallActivity", "JS function executed: " + function + " -> " + value);
                        }
                    });
                } catch (Exception e) {
                    Log.e("CallActivity", "Error executing JS function: " + e.getMessage());

                    // Fallback: Buton durumlarını manuel güncelle
                    if (function.contains("toggleAudio")) {
                        updateAudioButton();
                    } else if (function.contains("toggleVideo")) {
                        updateVideoButton();
                    }
                }
            }
        });
    }
    String getUniqueId(){
        return UUID.randomUUID().toString();
    }

    private void endCall() {
        // Çağrı kayıtlarını temizle
        if (username != null) {
            firebaseRef.child(username).removeValue();
        }
        if (friendsUsername != null) {
            firebaseRef.child(friendsUsername).removeValue();
        }

        callJavaScriptFunction("javascript:endCall()");

        Log.d("CallActivity", "Call ended");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pageExit = true;
        endCall();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        endCall();
    }
    public void onAudioStateChanged(boolean isEnabled) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isAudioEnabled = isEnabled;
                updateAudioButton();
                Log.d("CallActivity", "Audio state changed: " + isEnabled);
            }
        });
    }

    public void onVideoStateChanged(boolean isEnabled) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isVideoEnabled = isEnabled;
                updateVideoButton();
                Log.d("CallActivity", "Video state changed: " + isEnabled);
            }
        });
    }

    private void updateAudioButton() {
        if (isAudioEnabled) {
            binding.micBtn.setImageResource(R.drawable.btn_unmute_normal);
            binding.micBtn.setColorFilter(ContextCompat.getColor(this, android.R.color.white));
        } else {
            binding.micBtn.setImageResource(R.drawable.btn_mute_normal);
            binding.micBtn.setColorFilter(ContextCompat.getColor(this, R.color.red));
        }
    }

    private void updateVideoButton() {
        if (isVideoEnabled) {
            binding.videoBtn.setImageResource(R.drawable.btn_video_normal);
            binding.videoBtn.setColorFilter(ContextCompat.getColor(this, android.R.color.white));
        } else {
            binding.videoBtn.setImageResource(R.drawable.btn_video_muted);
            binding.videoBtn.setColorFilter(ContextCompat.getColor(this, R.color.red));
        }
    }
}