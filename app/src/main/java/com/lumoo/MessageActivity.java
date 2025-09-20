package com.lumoo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.lumoo.FCM.FCMNotificationSender;
import com.lumoo.Model.AllUser;
import com.lumoo.Model.MessageMember;
import com.lumoo.ViewHolder.MessageViewHolder;
import com.lumoo.util.GlideUtil;
import com.lumoo.util.SecurityUtils;
import com.lumoo.util.ValidationUtils;
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MessageActivity extends AppCompatActivity {
    private static final long APP_ID = 1199787649;
    private static final String APP_SIGN = "07447c6dd1ded0bd604ebbc510470cefb56e022a6017a4c24d2c214d3b3c7b4b";

    private static final String SUPABASE_URL = "https://iauuehrfhmzhnfsnsjdx.supabase.co";
    private static final String SUPABASE_APIKEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlhdXVlaHJmaG16aG5mc25zamR4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTc4NjQ2NzAsImV4cCI6MjA3MzQ0MDY3MH0.GnwTJFqC_cLAuKt7dAlSjlVIBfy4O9nTVWyn3d2wzRM";
    private static final String SUPABASE_STORAGE_ENDPOINT = SUPABASE_URL + "/storage/v1/object/message-images/";

    private final OkHttpClient httpClient = new OkHttpClient();

    EditText messageEt;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference rootref1, rootref2, chatSettingsRef;
    MessageMember messageMember;
    String receiver_name, receiver_uid, sender_uid, url;
    String userid;
    RecyclerView recyclerView;
    ImageView imageView, arrowBack, menuButton;
    ImageButton sendbtn, cambtn, emojiBtn;
    TextView username, userStatus;
    LinearLayout messageInputContainer;
    CardView messageCardImageContainer;

    String userToken;
    String currentBackgroundColor = "#0A1014";

    boolean notify = false;
    boolean isClicked = false;

    Uri selectedImageUri;
    private static final int PICK_IMAGE = 1;

    ConstraintLayout constTouchProfile, mainLayout;

    private FirebaseRecyclerAdapter<MessageMember, MessageViewHolder> firebaseRecyclerAdapter1;

    ImageView videoCallBtn, callBtn;
    String usernameForCall, myUid;

    // Zego service durumunu takip etmek için
    private boolean isZegoInitialized = false;
    private boolean isActivityActive = false;
    AllUser userDeneme;

    String[] permissions = new String[] {android.Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    private int requestCode = 1;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setContentView(R.layout.activity_message);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String uid = currentUser.getUid();

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    return;
                }
                String token = task.getResult();
                FirebaseDatabase.getInstance().getReference("Tokens")
                        .child(uid).child("token").setValue(token);
            }
        });

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            url = bundle.getString("u");
            receiver_uid = bundle.getString("uid");
            receiver_name = bundle.getString("n");
            Log.d("MessageActivity", "onCreate: " + url + receiver_uid + receiver_name);
        } else {
            Toast.makeText(this, "user missing", Toast.LENGTH_SHORT).show();
        }

        userid = receiver_uid;

        messageMember = new MessageMember();
        recyclerView = findViewById(R.id.rv_message);
        recyclerView.setHasFixedSize(true);

        // RecyclerView için LinearLayoutManager düzeltmesi
        LinearLayoutManager layoutManager = new LinearLayoutManager(MessageActivity.this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(false); // Explicitly set to false for consistency

        // ÖNEMLİ: Bu satırı ekleyin - performans ve tutarlılık için
        recyclerView.setItemAnimator(null);

        recyclerView.setLayoutManager(layoutManager);

        // RecyclerView optimizasyonu
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        mainLayout = findViewById(R.id.main);
        imageView = findViewById(R.id.messageUserImage);
        messageEt = findViewById(R.id.messageet);
        sendbtn = findViewById(R.id.imageButtonsend);
        username = findViewById(R.id.messageUserName);
        userStatus = findViewById(R.id.userStatus);
        cambtn = findViewById(R.id.cam_sendMessage);
        emojiBtn = findViewById(R.id.btn_emoji);
        arrowBack = findViewById(R.id.btnBackButton);
        menuButton = findViewById(R.id.menuButton);
        messageInputContainer = findViewById(R.id.messageInputContainer);
        constTouchProfile = findViewById(R.id.constTouchProfile);
        messageCardImageContainer = findViewById(R.id.messageCardImageContainer);

        init();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        sender_uid = user.getUid();

        // GlideUtil ile profil resmini yükle
        GlideUtil.loadOriginalImage(this, url, imageView);
        username.setText(receiver_name);

        chatSettingsRef = database.getReference("ChatSettings").child(sender_uid).child(receiver_uid);
        loadBackgroundTheme();

        Log.d("TAG", "onCreate: " + sender_uid + receiver_uid);
        rootref1 = database.getReference("Message").child(sender_uid).child(receiver_uid);
        rootref2 = database.getReference("Message").child(receiver_uid).child(sender_uid);

        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                SendMessage();
            }
        });

        cambtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        loadUserStatus();
        setupSimpleKeyboardHandling();
        setupEditTextListeners();
        initializeMessageAdapter();

        callBtn = findViewById(R.id.callButton);
        videoCallBtn = findViewById(R.id.videocallButton);

        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Önce kullanıcının online olup olmadığını kontrol et
                DatabaseReference statusRef = FirebaseDatabase.getInstance()
                        .getReference("Kullanıcılar").child(receiver_uid);

                statusRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot statusSnapshot) {
                        boolean isOnline = false;
                        if (statusSnapshot.exists()) {
                            Boolean online = statusSnapshot.child("isOnline").getValue(Boolean.class);
                            isOnline = online != null && online;
                        }

                        // Çevrimdışı olsa bile FCM bildirimi göndereceğiz
                        // Bu yüzden aramayı başlatmaya devam ediyoruz

                        // Kullanıcı profil bilgilerini al
                        DatabaseReference userRef = FirebaseDatabase.getInstance()
                                .getReference("Kullanıcılar").child(receiver_uid);

                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    AllUser user = snapshot.getValue(AllUser.class);
                                    if (user != null) {
                                        // ConnectingActivity'yi başlat - sesli arama için
                                        Intent intent = new Intent(MessageActivity.this, ConnectingActivity.class);
                                        intent.putExtra("profile", user.getProfileImage());
                                        intent.putExtra("targetUserId", receiver_uid);
                                        intent.putExtra("targetUserName", receiver_name);
                                        intent.putExtra("callType", "audio"); // Sesli arama olduğunu belirt
                                        startActivity(intent);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(MessageActivity.this, "Kullanıcı bilgileri alınamadı", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MessageActivity.this, "Kullanıcı durumu kontrol edilemedi", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        videoCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Önce kullanıcının online olup olmadığını kontrol et
                DatabaseReference statusRef = FirebaseDatabase.getInstance()
                        .getReference("Kullanıcılar").child(receiver_uid);

                statusRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot statusSnapshot) {
                        boolean isOnline = false;
                        if (statusSnapshot.exists()) {
                            Boolean online = statusSnapshot.child("isOnline").getValue(Boolean.class);
                            isOnline = online != null && online;
                        }

                        // Çevrimdışı olsa bile FCM bildirimi göndereceğiz
                        // Bu yüzden aramayı başlatmaya devam ediyoruz

                        // Kullanıcı profil bilgilerini al
                        DatabaseReference userRef = FirebaseDatabase.getInstance()
                                .getReference("Kullanıcılar").child(receiver_uid);

                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    AllUser user = snapshot.getValue(AllUser.class);
                                    if (user != null) {
                                        // ConnectingActivity'yi başlat - video arama için
                                        Intent intent = new Intent(MessageActivity.this, ConnectingActivity.class);
                                        intent.putExtra("profile", user.getProfileImage());
                                        intent.putExtra("targetUserId", receiver_uid);
                                        intent.putExtra("targetUserName", receiver_name);
                                        intent.putExtra("callType", "video"); // Video arama olduğunu belirt
                                        startActivity(intent);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(MessageActivity.this, "Kullanıcı bilgileri alınamadı", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MessageActivity.this, "Kullanıcı durumu kontrol edilemedi", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    private void uploadImageToSupabase(Uri imageUri) {
        if (imageUri == null) {
            Toast.makeText(this, "Önce bir fotoğraf seçin", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Kullanıcı girişi yapılmamış", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Resim gönderiliyor...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // DAHA İYİ KALİTE İÇİN OPTİMİZASYON
            // Orijinal boyutları koru ama maksimum limitler koy
            int originalWidth = bitmap.getWidth();
            int originalHeight = bitmap.getHeight();

            // Maksimum boyut limitleri (daha yüksek çözünürlük için)
            int maxWidth = 1200;
            int maxHeight = 1200;

            // Sadece gerektiğinde yeniden boyutlandır
            if (originalWidth > maxWidth || originalHeight > maxHeight) {
                bitmap = resizeBitmapWithQuality(bitmap, maxWidth, maxHeight);
            }

            // DAHA YÜKSEK KALİTELİ SIKIŞTIRMA
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos); // 90% kalite (80 yerine)
            byte[] imageData = baos.toByteArray();

            // Dosya boyutu kontrolü
            long fileSize = imageData.length;
            if (fileSize > 5 * 1024 * 1024) { // 5MB'den büyükse
                // Tekrar sıkıştır (daha düşük boyut için)
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);
                imageData = baos.toByteArray();
            }

            String fileName = UUID.randomUUID().toString() + ".jpg";

            MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");
            RequestBody body = RequestBody.create(imageData, MEDIA_TYPE_JPEG);

            String uploadUrl = SUPABASE_STORAGE_ENDPOINT + fileName;

            Request request = new Request.Builder()
                    .url(uploadUrl)
                    .put(body)
                    .addHeader("apikey", SUPABASE_APIKEY)
                    .addHeader("Authorization", "Bearer " + SUPABASE_APIKEY)
                    .addHeader("Content-Type", "image/jpeg")
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(MessageActivity.this, "Fotoğraf yüklenemedi", Toast.LENGTH_SHORT).show();
                        Log.e("Supabase", "Upload failed: " + e.getMessage());
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String imageUrl = SUPABASE_STORAGE_ENDPOINT.replace("/object/", "/object/public/") + fileName;
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(MessageActivity.this, "Fotoğraf yüklendi", Toast.LENGTH_SHORT).show();
                            sendImageMessage(imageUrl);
                        });
                    } else {
                        String errorBody = response.body() != null ? response.body().string() : "No error body";
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(MessageActivity.this, "Upload hatası: " + response.code(), Toast.LENGTH_SHORT).show();
                            Log.e("Supabase", "Upload error: " + errorBody);
                        });
                    }
                }
            });

        } catch (Exception e) {
            progressDialog.dismiss();
            Log.e("MessageActivity", "Resim işleme hatası: " + e.getMessage());
            Toast.makeText(this, "Resim işlenirken hata oluştu", Toast.LENGTH_SHORT).show();
        }
    }

    // YENİ: DAHA KALİTELİ YENİDEN BOYUTLANDIRMA METODU
    private Bitmap resizeBitmapWithQuality(Bitmap originalBitmap, int maxWidth, int maxHeight) {
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        float ratioBitmap = (float) width / (float) height;
        float ratioMax = (float) maxWidth / (float) maxHeight;

        int finalWidth = maxWidth;
        int finalHeight = maxHeight;

        if (ratioMax > ratioBitmap) {
            finalWidth = (int) ((float) maxHeight * ratioBitmap);
        } else {
            finalHeight = (int) ((float) maxWidth / ratioBitmap);
        }

        // Yüksek kaliteli yeniden boyutlandırma
        return Bitmap.createScaledBitmap(originalBitmap, finalWidth, finalHeight, true);
    }

    // ZEGOCLOUD Call Service'i başlatma - DÜZELTİLMİŞ
    private void initializeZegoCallService() {
        if (isZegoInitialized) {
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userID = user.getUid();

            // Önce varsayılan değerlerle başlat
            initializeWithDefaultName(userID);

            // Kullanıcı adını arka planda güncelle
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Kullanıcılar").child(userID);
            userRef.child("kullanıcıAdı").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && isActivityActive) {
                        String userName = snapshot.getValue(String.class);
                        if (userName != null && !userName.isEmpty()) {
                            // Servisi güvenli bir şekilde yeniden başlat
                            reinitializeZegoService(userID, userName);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("ZegoInit", "Kullanıcı adı alınamadı: " + error.getMessage());
                }
            });
        }
    }

    private void reinitializeZegoService(String userID, String userName) {
        try {
            // Eski servisi durdur
            ZegoUIKitPrebuiltCallService.unInit();
            isZegoInitialized = false;

            // Kısa bir bekleme süresi
            new Handler().postDelayed(() -> {
                if (isActivityActive) {
                    // Yeni servisi başlat
                    ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig = new ZegoUIKitPrebuiltCallInvitationConfig();
                    ZegoUIKitPrebuiltCallService.init(
                            getApplication(),
                            APP_ID,
                            APP_SIGN,
                            userID,
                            userName,
                            callInvitationConfig
                    );
                    isZegoInitialized = true;
                    Log.d("ZegoInit", "Zego servisi başarıyla yeniden başlatıldı: " + userName);
                }
            }, 500);

        } catch (Exception e) {
            Log.e("ZegoInit", "Zego servisi yeniden başlatılamadı: " + e.getMessage());
        }
    }

    private void initializeWithDefaultName(String userID) {
        try {
            ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig = new ZegoUIKitPrebuiltCallInvitationConfig();
            ZegoUIKitPrebuiltCallService.init(
                    getApplication(),
                    APP_ID,
                    APP_SIGN,
                    userID,
                    "User",
                    callInvitationConfig
            );
            isZegoInitialized = true;
            Log.d("ZegoInit", "Zego servisi varsayılan adla başlatıldı");
        } catch (Exception e) {
            Log.e("ZegoInit", "Zego servisi başlatılamadı: " + e.getMessage());
            isZegoInitialized = false;
        }
    }

    private void startVoiceCall() {
        if (!isZegoInitialized) {
            Toast.makeText(this, "Arama servisi henüz hazır değil", Toast.LENGTH_SHORT).show();
            return;
        }
        sendZegoInvitation(false);
    }

    private void startVideoCall() {
        if (!isZegoInitialized) {
            Toast.makeText(this, "Arama servisi henüz hazır değil", Toast.LENGTH_SHORT).show();
            return;
        }
        sendZegoInvitation(true);
    }

    private void sendZegoInvitation(boolean isVideoCall) {
        try {
            List<ZegoUIKitUser> invitees = Collections.singletonList(
                    new ZegoUIKitUser(receiver_uid, receiver_name)
            );

            ZegoInvitationType invitationType = isVideoCall ?
                    ZegoInvitationType.VIDEO_CALL : ZegoInvitationType.VOICE_CALL;

            ZegoUIKitPrebuiltCallService.sendInvitation(
                    invitees,
                    invitationType,
                    "",
                    60,
                    null,
                    null,
                    new PluginCallbackListener() {
                        @Override
                        public void callback(Map<String, Object> result) {
                            int code = (int) result.get("code");
                            if (code != 0) {
                                String message = (String) result.get("message");
                                Log.e("CallError", "Invitation error: " + message);

                                runOnUiThread(() -> {
                                    Toast.makeText(MessageActivity.this, "Arama hatası: " + message, Toast.LENGTH_SHORT).show();
                                });
                            } else {
                                Log.d("CallSuccess", "Arama başarıyla başlatıldı");
                                runOnUiThread(() -> {
                                    Toast.makeText(MessageActivity.this, "Arama başlatıldı", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    }
            );
        } catch (Exception e) {
            Log.e("CallError", "Arama başlatılamadı: " + e.getMessage());
            Toast.makeText(this, "Arama başlatılamadı", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectImage() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE);
        } catch (Exception e) {
            Log.e("MessageActivity", "Fotoğraf seçme hatası: " + e.getMessage());
            Toast.makeText(this, "Galeri açılırken hata oluştu", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSimpleKeyboardHandling() {
        View decorView = getWindow().getDecorView();

        decorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                decorView.getWindowVisibleDisplayFrame(rect);

                int screenHeight = decorView.getHeight();
                int keypadHeight = screenHeight - rect.bottom;

                if (keypadHeight > screenHeight * 0.15) {
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) messageInputContainer.getLayoutParams();
                    params.bottomMargin = keypadHeight - 50;
                    messageInputContainer.setLayoutParams(params);

                    messageInputContainer.postDelayed(() -> scrollToBottom(), 100);
                } else {
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) messageInputContainer.getLayoutParams();
                    params.bottomMargin = 0;
                    messageInputContainer.setLayoutParams(params);
                }
            }
        });
    }

    private void scrollToBottom() {
        if (recyclerView != null && recyclerView.getAdapter() != null) {
            int itemCount = recyclerView.getAdapter().getItemCount();
            if (itemCount > 0) {
                try {
                    // Smooth scroll yerine direkt scroll - daha güvenli
                    recyclerView.scrollToPosition(itemCount - 1);
                } catch (Exception e) {
                    Log.e("ScrollError", "Scroll hatası: " + e.getMessage());
                }
            }
        }
    }
    private void setupEditTextListeners() {
        messageEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    v.postDelayed(() -> scrollToBottom(), 200);
                }
            }
        });

        messageEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.postDelayed(() -> scrollToBottom(), 200);
            }
        });
    }

    private void loadBackgroundTheme() {
        chatSettingsRef.child("backgroundColor").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentBackgroundColor = snapshot.getValue(String.class);
                } else {
                    currentBackgroundColor = "#0A1014";
                }
                mainLayout.setBackgroundColor(Color.parseColor(currentBackgroundColor));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadUserStatus() {
        DatabaseReference userStatusRef = database.getReference("Kullanıcılar").child(receiver_uid);
        userStatusRef.child("lastSeen").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Long lastSeen = snapshot.getValue(Long.class);
                    if (lastSeen != null) {
                        String status = formatLastSeen(lastSeen);
                        userStatus.setText(status);
                    }
                } else {
                    userStatus.setText("son görülme bilgisi yok");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                userStatus.setText("");
            }
        });
    }

    private String formatLastSeen(long lastSeen) {
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - lastSeen;

        if (timeDiff < 60000) {
            return "çevrimiçi";
        } else if (timeDiff < 3600000) {
            int minutes = (int) (timeDiff / 60000);
            return minutes + " dakika önce";
        } else if (timeDiff < 86400000) {
            int hours = (int) (timeDiff / 3600000);
            return hours + " saat önce";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            return "son görülme: " + sdf.format(lastSeen);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                selectedImageUri = data.getData();
                Log.d("MessageActivity", "Seçilen resim URI: " + selectedImageUri.toString());

                // Resmi Supabase'e yükle
                uploadImageToSupabase(selectedImageUri);

            } catch (Exception e) {
                Log.e("MessageActivity", "Resim işleme hatası: " + e.getMessage());
                Toast.makeText(this, "Resim işlenirken hata oluştu", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void SendMessage() {
        final String message = messageEt.getText().toString().trim();

        // Güvenlik kontrolleri - ValidationUtils kullan
        ValidationResult messageResult = ValidationUtils.validateMessage(message);
        if (!messageResult.isValid()) {
            Toast.makeText(MessageActivity.this, messageResult.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Güvenli mesaj temizleme
        String sanitizedMessage = SecurityUtils.sanitizeInput(message);
        if (sanitizedMessage.isEmpty()) {
            Toast.makeText(MessageActivity.this, "Geçersiz mesaj içeriği", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar cdate = Calendar.getInstance();
        SimpleDateFormat currentdate = new SimpleDateFormat("dd-MMM-yyyy");
        final String savedate = currentdate.format(cdate.getTime());

        Calendar ctime = Calendar.getInstance();
        SimpleDateFormat currenttime = new SimpleDateFormat("HH:mm");
        final String savetime = currenttime.format(ctime.getTime());

        long timestamp = System.currentTimeMillis();

        messageMember = new MessageMember();
        messageMember.setDate(savedate);
        messageMember.setTime(savetime);
        messageMember.setMessage(sanitizedMessage);
        messageMember.setReceiveruid(receiver_uid);
        messageMember.setSenderuid(sender_uid);
        messageMember.setType("text");
        messageMember.setRead(false);
        messageMember.setTimestamp(timestamp);

        String id = rootref1.push().getKey();
        rootref1.child(id).setValue(messageMember);

        String id1 = rootref2.push().getKey();
        rootref2.child(id1).setValue(messageMember);

        updateLastMessage(sender_uid, receiver_uid, message, savetime, timestamp);
        updateLastMessage(receiver_uid, sender_uid, message, savetime, timestamp);

        messageEt.setText("");
        sendNotification(receiver_uid, receiver_name, message);
    }

    private void updateLastMessage(String currentUserId, String chatUserId, String lastMessage, String lastTime, long timestamp) {
        DatabaseReference friendRef = FirebaseDatabase.getInstance()
                .getReference("Kullanıcılar")
                .child(currentUserId)
                .child("Friends")
                .child(chatUserId);

        friendRef.child("lastMessage").setValue(lastMessage);
        friendRef.child("lastMessageTime").setValue(lastTime);
        friendRef.child("lastMessageTimestamp").setValue(timestamp);
    }

    private void sendNotification(String receiver_uid, String receiver_name, String message1) {
        DatabaseReference reference1 = FirebaseDatabase.getInstance()
                .getReference("Kullanıcılar").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myUid = snapshot.child("kullanıcıAdı").getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens")
                .child(receiver_uid).child("token");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userToken = snapshot.getValue(String.class);
                String title = myUid;
                String message = message1;

                FCMNotificationSender notificationSender = new FCMNotificationSender(userToken, title, message, getApplicationContext());
                notificationSender.sendNotification();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    public void init(){
        arrowBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        constTouchProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MessageActivity.this, PublicProfileActivity.class);
                intent.putExtra("url", url);
                intent.putExtra("uid", receiver_uid);
                startActivity(intent);
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MessageActivity.this, ChatSettingsActivity.class);
                intent.putExtra("uid", receiver_uid);
                intent.putExtra("name", receiver_name);
                intent.putExtra("profileUrl", url);
                startActivity(intent);
            }
        });

        messageCardImageContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MessageActivity.this, PublicProfileActivity.class);
                Log.d("Logg", "onClick: "+receiver_uid);
                intent.putExtra("url",url);
                intent.putExtra("uid",receiver_uid);
                startActivity(intent);
            }
        });
    }

    private void initializeMessageAdapter() {
        try {
            FirebaseRecyclerOptions<MessageMember> options1 =
                    new FirebaseRecyclerOptions.Builder<MessageMember>()
                            .setQuery(rootref1.orderByChild("timestamp"), MessageMember.class)
                            .build();

            firebaseRecyclerAdapter1 = new FirebaseRecyclerAdapter<MessageMember, MessageViewHolder>(options1) {
                @Override
                protected void onBindViewHolder(final MessageViewHolder holder, int position, MessageMember model) {
                    try {
                        String messageId = getRef(position).getKey();
                        holder.Setmessage(getApplication(), model.getMessage(), model.getTime(), model.getDate(),
                                model.getType(), model.getSenderuid(), model.getReceiveruid(),
                                model.isRead(), model.getTimestamp(), messageId);
                    } catch (Exception e) {
                        Log.e("AdapterError", "ViewHolder bind hatası: " + e.getMessage());
                    }
                }

                @Override
                public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.message_layout, parent, false);
                    return new MessageViewHolder(view);
                }

                @Override
                public void onDataChanged() {
                    super.onDataChanged();
                    // Veri değiştiğinde scroll pozisyonunu güncelle - UI thread'de yap
                    runOnUiThread(() -> {
                        try {
                            scrollToBottom();
                        } catch (Exception e) {
                            Log.e("AdapterError", "Scroll hatası: " + e.getMessage());
                        }
                    });
                }

                // ÖNEMLİ: getItemId metodunu override edin - bu tutarlılık için kritik
                @Override
                public long getItemId(int position) {
                    // Her mesaj için benzersiz bir ID döndür
                    if (getItem(position) != null) {
                        return getItem(position).getTimestamp();
                    }
                    return super.getItemId(position);
                }
            };

            // ÖNEMLİ: Adapter'ı set etmeden önce bu satırı ekleyin
            recyclerView.setAdapter(null);
            recyclerView.setAdapter(firebaseRecyclerAdapter1);

            firebaseRecyclerAdapter1.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    super.onItemRangeInserted(positionStart, itemCount);
                    // UI thread'de scroll işlemi yap
                    runOnUiThread(() -> {
                        try {
                            int newPosition = firebaseRecyclerAdapter1.getItemCount() - 1;
                            if (newPosition >= 0) {
                                // Smooth scroll yerine direkt scroll - daha güvenli
                                recyclerView.scrollToPosition(newPosition);
                            }
                        } catch (Exception e) {
                            Log.e("AdapterError", "Scroll hatası: " + e.getMessage());
                        }
                    });
                }

                @Override
                public void onChanged() {
                    super.onChanged();
                    // UI thread'de scroll işlemi yap
                    runOnUiThread(() -> {
                        try {
                            scrollToBottom();
                        } catch (Exception e) {
                            Log.e("AdapterError", "Adapter değişiklik hatası: " + e.getMessage());
                        }
                    });
                }

                @Override
                public void onItemRangeChanged(int positionStart, int itemCount) {
                    super.onItemRangeChanged(positionStart, itemCount);
                    // Veri değişikliklerini UI thread'de işle
                    runOnUiThread(() -> {
                        try {
                            firebaseRecyclerAdapter1.notifyItemRangeChanged(positionStart, itemCount);
                        } catch (Exception e) {
                            Log.e("AdapterError", "Item range change error: " + e.getMessage());
                        }
                    });
                }
            });

        } catch (Exception e) {
            Log.e("AdapterError", "Adapter başlatma hatası: " + e.getMessage());
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        isActivityActive = true;

        // Zego servisini başlat
        initializeZegoCallService();

        if (firebaseRecyclerAdapter1 != null) {
            // Adapter'ı başlatmadan önce RecyclerView'ı temizle
            recyclerView.setAdapter(null);
            recyclerView.setAdapter(firebaseRecyclerAdapter1);
            firebaseRecyclerAdapter1.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActivityActive = false;

        if (firebaseRecyclerAdapter1 != null) {
            firebaseRecyclerAdapter1.stopListening();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityActive = true;

        DatabaseReference userStatusRef = database.getReference("Kullanıcılar").child(sender_uid);
        userStatusRef.child("isOnline").setValue(true);
        userStatusRef.child("lastSeen").setValue(System.currentTimeMillis());
    }

    @Override
    protected void onPause() {
        super.onPause();
        DatabaseReference userStatusRef = database.getReference("Kullanıcılar").child(sender_uid);
        userStatusRef.child("isOnline").setValue(false);
        userStatusRef.child("lastSeen").setValue(System.currentTimeMillis());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActivityActive = false;

        // Zego servisini güvenli bir şekilde temizle
        // NOT: Zego servisini tamamen kapatmayın, diğer aktivitelerde kullanılabilir
        // ZegoUIKitPrebuiltCallService.unInit();

        if (firebaseRecyclerAdapter1 != null) {
            try {
                firebaseRecyclerAdapter1.stopListening();
                firebaseRecyclerAdapter1 = null;
            } catch (Exception e) {
                Log.e("DestroyError", "Adapter temizleme hatası: " + e.getMessage());
            }
        }
    }

    private void sendImageMessage(String imageUrl) {
        // Güvenlik kontrolleri - ValidationUtils kullan
        if (imageUrl == null || imageUrl.isEmpty()) {
            Toast.makeText(this, "Resim işlenemedi", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Supabase URL güvenlik kontrolü
        ValidationResult urlResult = ValidationUtils.validateSupabaseUrl(imageUrl);
        if (!urlResult.isValid()) {
            SecurityUtils.logSecurityEvent("Invalid Image URL", "Suspicious image URL detected: " + imageUrl);
            Toast.makeText(this, "Geçersiz resim URL'i", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar cdate = Calendar.getInstance();
        SimpleDateFormat currentdate = new SimpleDateFormat("dd-MMM-yyyy");
        final String savedate = currentdate.format(cdate.getTime());

        Calendar ctime = Calendar.getInstance();
        SimpleDateFormat currenttime = new SimpleDateFormat("HH:mm");
        final String savetime = currenttime.format(ctime.getTime());

        long timestamp = System.currentTimeMillis();

        messageMember = new MessageMember();
        messageMember.setDate(savedate);
        messageMember.setTime(savetime);
        messageMember.setMessage(imageUrl);
        messageMember.setReceiveruid(receiver_uid);
        messageMember.setSenderuid(sender_uid);
        messageMember.setType("iv");
        messageMember.setRead(false);
        messageMember.setTimestamp(timestamp);

        String id = rootref1.push().getKey();
        rootref1.child(id).setValue(messageMember);

        String id1 = rootref2.push().getKey();
        rootref2.child(id1).setValue(messageMember);

        updateLastMessage(sender_uid, receiver_uid, "📷 Fotoğraf", savetime, timestamp);
        updateLastMessage(receiver_uid, sender_uid, "📷 Fotoğraf", savetime, timestamp);

        sendNotification(receiver_uid, receiver_name, "📷 Fotoğraf gönderdi");

        recyclerView.postDelayed(() -> scrollToBottom(), 100);
    }

    void askPermissions(){
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    private boolean isPermissionsGranted() {
        for(String permission : permissions ){
            if(ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }

        return true;
    }

}