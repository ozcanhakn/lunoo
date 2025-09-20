package com.lumoo;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.reactivex.rxjava3.annotations.NonNull;

public class MyApp extends Application implements Application.ActivityLifecycleCallbacks {


    private DatabaseReference userStatusRef;
    private int activityCount = 0;


    private ValueEventListener callListener;
    private DatabaseReference callsRef;

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            userStatusRef = FirebaseDatabase.getInstance()
                    .getReference("Kullanıcılar")
                    .child(uid)
                    .child("online");

            // Bağlantı koparsa offline
            userStatusRef.onDisconnect().setValue("offline");
        }

        setupCallListener();
    }

    @Override
    public void onActivityStarted(Activity activity) {
        activityCount++;
        if (userStatusRef != null) {
            userStatusRef.setValue("online");
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        activityCount--;
        if (activityCount == 0 && userStatusRef != null) {
            userStatusRef.setValue("offline");
        }
    }

    // Diğer lifecycle metodları boş bırakılabilir
    @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}
    @Override public void onActivityResumed(Activity activity) {}
    @Override public void onActivityPaused(Activity activity) {}
    @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
    @Override public void onActivityDestroyed(Activity activity) {}


    // MainActivity içine ekleyin
    private void setupCallListener() {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Kullanıcı giriş yapmış mı kontrol et
        if (auth.getCurrentUser() == null) {
            return;
        }

        String currentUserId = auth.getCurrentUser().getUid();
        callsRef = FirebaseDatabase.getInstance().getReference("calls");

        callListener = callsRef.orderByChild("targetUser").equalTo(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot callSnapshot : snapshot.getChildren()) {
                            Integer status = callSnapshot.child("status").getValue(Integer.class);
                            String callerId = callSnapshot.child("createdBy").getValue(String.class);

                            if (status != null && status == 0) {
                                // Yeni gelen çağrı
                                String callId = callSnapshot.getKey();

                                Intent intent = new Intent(MyApp.this, IncomingCallActivity.class);
                                intent.putExtra("callerId", callerId);
                                intent.putExtra("callId", callId);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("MyApp", "Çağrı dinleyici hatası: " + error.getMessage());
                    }
                });
    }

    public void restartCallListener() {
        // Önceki dinleyiciyi temizle
        if (callListener != null && callsRef != null) {
            callsRef.removeEventListener(callListener);
        }

        // Yeni dinleyici kur
        setupCallListener();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        // Uygulama kapanırken dinleyiciyi temizle
        if (callListener != null && callsRef != null) {
            callsRef.removeEventListener(callListener);
        }
    }
}
