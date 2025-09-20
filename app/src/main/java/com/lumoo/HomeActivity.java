package com.lumoo;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.lumoo.util.GlideUtil;
import com.lumoo.util.ImageUtils;
import com.lumoo.util.SecurityUtils;

import io.reactivex.rxjava3.annotations.NonNull;

public class HomeActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    FragmentTransaction fragmentTransaction;
    DatabaseReference userStatusRef;

    //Rastgele tanışma ekle, profilde rastgele tanışma özelliği açık veya kapalı olsun bu duruma göre
    //rastgele tanışma açılabilsin.

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // PROBLEM 3 ÇÖZÜMÜ: Sistem barını tam transparan yap
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        // Durum çubuğunu transparan yap
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        EdgeToEdge.enable(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        setupStatusBar();
        setContentView(R.layout.activity_home);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.homee), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });






        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, new MainFragment());
        fragmentTransaction.commit();

        saveFcmToken();


        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.main) {
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frame_layout, new MainFragment());
                fragmentTransaction.commit();
                return true;
            } else if (itemId == R.id.message) {
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frame_layout, new MessageFragment());
                fragmentTransaction.commit();
                return true;
            } else if (itemId == R.id.match){
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frame_layout, new RandomFragment());
                fragmentTransaction.commit();
            }else if (itemId == R.id.profile) {
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frame_layout, new ProfileFragment());
                fragmentTransaction.commit();
                return true;
            } else if (itemId == R.id.online) {
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frame_layout, new OnlineFragment());
                fragmentTransaction.commit();
                return true;
            }
            return false;
        });

        bottomNavigationView.setSelectedItemId(R.id.main);

        if (getIntent().hasExtra("goToFragment")) {
            String fragmentName = getIntent().getStringExtra("goToFragment");
            if ("profile".equals(fragmentName)) {
                loadFragment(new ProfileFragment()); // ProfileFragment'ı aç
            }
        }
    }
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_layout, fragment); // Ana fragment container ID'sini kontrol et
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Status bar'ı transparan yap
            getWindow().setStatusBarColor(Color.TRANSPARENT);

            // System UI flag'lerini ayarla
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );

            // Alternatif olarak fragment'ınızın rengiyle eşleştirmek için:
            // getWindow().setStatusBarColor(Color.parseColor("#0A1014"));
        }

        // API 23+ için status bar icon rengini ayarla (dark theme için)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Dark background olduğu için light status bar icons kullan
            getWindow().getDecorView().setSystemUiVisibility(
                    getWindow().getDecorView().getSystemUiVisibility() &
                            ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }
    }
    private void saveFcmToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Kullanıcının UID’si
                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        // FCM cihaz tokenı
                        String token = task.getResult();
                        Log.d("FCM", "Cihaz tokenı: " + token);

                        // Realtime Database’e kaydet
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
                        reference.child(uid).child("token").setValue(token)
                                .addOnSuccessListener(aVoid -> Log.d("FCM", "Token başarıyla kaydedildi"))
                                .addOnFailureListener(e -> Log.e("FCM", "Token kaydedilemedi", e));
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ✅ Memory temizleme - Utils ile güvenli temizleme
        GlideUtil.clearImageCache(this);
        ImageUtils.clearImageCacheAsync(this);
        
        // ✅ SecurityUtils ile güvenlik log'u
        SecurityUtils.logSecurityEvent("App Destroy", "HomeActivity destroyed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Kullanıcı durumunu offline yap
        if (userStatusRef != null) {
            userStatusRef.setValue("offline");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Kullanıcı durumunu online yap
        if (userStatusRef != null) {
            userStatusRef.setValue("online");
        }
    }
}