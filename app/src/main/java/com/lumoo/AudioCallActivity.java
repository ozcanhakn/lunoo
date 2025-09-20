package com.lumoo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lumoo.Model.AudioInterfaceJava;
import com.lumoo.databinding.ActivityAudioCallBinding;

public class AudioCallActivity extends AppCompatActivity {

    ActivityAudioCallBinding binding;
    String uniqueId = "";
    FirebaseAuth auth;
    String username = "";
    String friendsUsername = "";
    boolean isPeerConnected = false;
    DatabaseReference firebaseRef;
    boolean isAudio = true;
    String createdBy;
    boolean pageExit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAudioCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firebaseRef = FirebaseDatabase.getInstance().getReference().child("calls");

        username = getIntent().getStringExtra("username");
        String incoming = getIntent().getStringExtra("incoming");
        createdBy = getIntent().getStringExtra("createdBy");

        friendsUsername = incoming;

        setupWebView();

        binding.micBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAudio = !isAudio;
                callJavaScriptFunction("javascript:toggleAudio(" + isAudio + ")");
                updateAudioButton();

                Toast.makeText(AudioCallActivity.this,
                        isAudio ? "Mikrofon açıldı" : "Mikrofon kapatıldı",
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
        binding.webView.setWebChromeClient(new WebChromeClient());

        WebSettings webSettings = binding.webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);

        binding.webView.addJavascriptInterface(new AudioInterfaceJava(this), "Android");
        loadAudioCall();
    }

    public void loadAudioCall() {
        String filePath = "file:android_asset/audio_call.html";
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
        callJavaScriptFunction("javascript:init(\"" + uniqueId + "\")");

        if(createdBy.equalsIgnoreCase(username)) {
            if(pageExit) return;

            firebaseRef.child(username).child("connId").setValue(uniqueId);
            firebaseRef.child(username).child("isAvailable").setValue(true);

            binding.loadingGroup.setVisibility(View.GONE);
            binding.controls.setVisibility(View.VISIBLE);

            loadUserProfile(friendsUsername);

        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    friendsUsername = createdBy;
                    loadUserProfile(friendsUsername);

                    FirebaseDatabase.getInstance().getReference()
                            .child("calls")
                            .child(friendsUsername)
                            .child("connId")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.getValue() != null) {
                                        sendCallRequest();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("AudioCallActivity", "Error getting connId: " + error.getMessage());
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
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()) {
                            String profileImage = snapshot.child("profileImage").getValue(String.class);
                            String kullaniciAdi = snapshot.child("kullanıcıAdı").getValue(String.class);

                            // JavaScript'e kullanıcı bilgilerini gönder
                            if (kullaniciAdi != null && profileImage != null) {
                                callJavaScriptFunction("javascript:setCallerInfo('" + kullaniciAdi + "', '" + profileImage + "')");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("AudioCallActivity", "Error loading user profile: " + error.getMessage());
                    }
                });
    }

    public void onPeerConnected(){
        isPeerConnected = true;
        Log.d("AudioCallActivity", "Audio peer connected");
    }

    public void onAudioStateChanged(boolean isEnabled) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isAudio = isEnabled;
                updateAudioButton();
            }
        });
    }

    private void updateAudioButton() {
        if (isAudio) {
            binding.micBtn.setImageResource(R.drawable.btn_unmute_normal);
        } else {
            binding.micBtn.setImageResource(R.drawable.btn_mute_normal);
        }
    }

    void sendCallRequest(){
        if(!isPeerConnected) {
            Toast.makeText(this, "Ses bağlantısı kurulamadı", Toast.LENGTH_SHORT).show();
            return;
        }
        listenConnId();
    }

    void listenConnId() {
        firebaseRef.child(friendsUsername).child("connId").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() == null) return;

                binding.loadingGroup.setVisibility(View.GONE);
                binding.controls.setVisibility(View.VISIBLE);
                String connId = snapshot.getValue(String.class);
                callJavaScriptFunction("javascript:startCall(\""+connId+"\")");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AudioCallActivity", "Error listening connId: " + error.getMessage());
            }
        });
    }

    void callJavaScriptFunction(String function){
        binding.webView.post(new Runnable() {
            @Override
            public void run() {
                binding.webView.evaluateJavascript(function, null);
            }
        });
    }

    String getUniqueId(){
        return java.util.UUID.randomUUID().toString();
    }

    private void endCall() {
        if (username != null) {
            firebaseRef.child(username).removeValue();
        }
        if (friendsUsername != null) {
            firebaseRef.child(friendsUsername).removeValue();
        }
        callJavaScriptFunction("javascript:endCall()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pageExit = true;
        endCall();
    }
}