package com.lumoo;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment;
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

import java.util.Collections;

public class DirectCallActivity extends AppCompatActivity {

    private static final long APP_ID = 1199787649;
    private static final String APP_SIGN = "07447c6dd1ded0bd604ebbc510470cefb56e022a6017a4c24d2c214d3b3c7b4b";

    private String targetUserId;
    private String targetUserName;
    private String callerUserId;
    private boolean isVideoCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_direct_call);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Intent'ten parametreleri al
        getIntentExtras();

        // Doğrudan aramayı başlat
        startDirectCall();
    }

    private void getIntentExtras() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            targetUserId = bundle.getString("targetUserId");
            targetUserName = bundle.getString("targetUserName");
            callerUserId = bundle.getString("callerUserId");
            isVideoCall = bundle.getBoolean("isVideoCall", false);

            Log.d("DirectCall", "Target User: " + targetUserId + ", Caller: " + callerUserId + ", Video: " + isVideoCall);
        } else {
            Toast.makeText(this, "Arama bilgileri eksik", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void startDirectCall() {
        try {
            // Call ID oluştur (unique olmalı)
            String callID = callerUserId + "_" + targetUserId + "_" + System.currentTimeMillis();

            Log.d("DirectCall", "Starting call with ID: " + callID);

            // Prebuilt call config oluştur
            ZegoUIKitPrebuiltCallConfig config;
            if (isVideoCall) {
                config = ZegoUIKitPrebuiltCallConfig.oneOnOneVideoCall();
            } else {
                config = ZegoUIKitPrebuiltCallConfig.oneOnOneVoiceCall();
            }

            // Call fragment oluştur ve ekle
            ZegoUIKitPrebuiltCallFragment fragment = ZegoUIKitPrebuiltCallFragment.newInstance(
                    APP_ID, APP_SIGN, callerUserId, callerUserId, callID, config);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commitNow();

            // Hedef kullanıcıya arama davetiyesi gönder
            sendCallInvitation();

        } catch (Exception e) {
            Log.e("DirectCall", "Call başlatma hatası: " + e.getMessage());
            Toast.makeText(this, "Arama başlatılamadı: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void sendCallInvitation() {
        try {
            // Bu kısım arama davetiyesi göndermek için
            // ZEGO SDK'nın invitation özelliklerini kullanır
            ZegoSendCallInvitationButton inviteButton = new ZegoSendCallInvitationButton(this);
            inviteButton.setIsVideoCall(isVideoCall);
            inviteButton.setResourceID("zego_uikit_call");
            inviteButton.setInvitees(Collections.singletonList(new ZegoUIKitUser(targetUserId, targetUserName)));

            // Programmatik olarak davetiye gönder
            inviteButton.performClick();

            Log.d("DirectCall", "Invitation sent to: " + targetUserId);

        } catch (Exception e) {
            Log.e("DirectCall", "Invitation gönderme hatası: " + e.getMessage());
            Toast.makeText(this, "Davetiye gönderilemedi", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("DirectCall", "DirectCallActivity destroyed");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Geri tuşuna basıldığında activity'yi kapat
        finish();
    }
}