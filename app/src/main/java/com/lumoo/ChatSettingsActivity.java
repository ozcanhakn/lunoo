package com.lumoo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lumoo.Model.BackgroundTheme;
import com.lumoo.ViewHolder.BackgroundThemeAdapter;
import com.lumoo.util.GlideUtil;

import java.util.ArrayList;
import java.util.List;


public class ChatSettingsActivity extends AppCompatActivity {

    private String receiverUid, receiverName, receiverProfileUrl;
    private ImageView backButton, userProfileImage;
    private TextView userName;
    private CardView viewProfileBtn, reportUserBtn;
    private RecyclerView themeRecyclerView;
    private BackgroundThemeAdapter themeAdapter;
    private List<BackgroundTheme> themeList;

    private FirebaseDatabase database;
    private DatabaseReference userRef, chatSettingsRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_settings);

        // Intent'ten verileri al
        Intent intent = getIntent();
        receiverUid = intent.getStringExtra("uid");
        receiverName = intent.getStringExtra("name");
        receiverProfileUrl = intent.getStringExtra("profileUrl");

        // Firebase init
        database = FirebaseDatabase.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = currentUser.getUid();

        userRef = database.getReference("Kullanıcılar").child(receiverUid);
        chatSettingsRef = database.getReference("ChatSettings").child(currentUserId).child(receiverUid);

        initViews();
        setupThemes();
        loadUserData();
        setupClickListeners();
    }

    private void initViews() {
        backButton = findViewById(R.id.backButton);
        userProfileImage = findViewById(R.id.userProfileImage);
        userName = findViewById(R.id.userName);
        viewProfileBtn = findViewById(R.id.viewProfileBtn);
        reportUserBtn = findViewById(R.id.reportUserBtn);
        themeRecyclerView = findViewById(R.id.themeRecyclerView);
    }

    private void setupThemes() {
        themeList = new ArrayList<>();
        themeList.add(new BackgroundTheme("Varsayılan", "#0A1014", true));
        themeList.add(new BackgroundTheme("Gece Mavisi", "#1a237e", false));
        themeList.add(new BackgroundTheme("Koyu Yeşil", "#1b5e20", false));
        themeList.add(new BackgroundTheme("Mor", "#4a148c", false));
        themeList.add(new BackgroundTheme("Kahverengi", "#3e2723", false));

        themeAdapter = new BackgroundThemeAdapter(themeList, new BackgroundThemeAdapter.OnThemeClickListener() {
            @Override
            public void onThemeClick(BackgroundTheme theme) {
                saveSelectedTheme(theme);
            }
        });

        themeRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        themeRecyclerView.setAdapter(themeAdapter);

        // Mevcut temayı yükle
        loadSelectedTheme();
    }

    private void loadUserData() {
        userName.setText(receiverName);
        if (receiverProfileUrl != null && !receiverProfileUrl.isEmpty()) {
            GlideUtil.loadOriginalImage(getApplicationContext(), receiverProfileUrl, userProfileImage);
        }
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        viewProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatSettingsActivity.this, PublicProfileActivity.class);
                intent.putExtra("uid", receiverUid);
                intent.putExtra("url", receiverProfileUrl);
                startActivity(intent);
            }
        });

        reportUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportUser();
            }
        });
    }

    private void saveSelectedTheme(BackgroundTheme theme) {
        // Seçili temayı güncelle
        for (BackgroundTheme t : themeList) {
            t.setSelected(t.equals(theme));
        }
        themeAdapter.notifyDataSetChanged();

        // Firebase'e kaydet
        chatSettingsRef.child("backgroundColor").setValue(theme.getColorCode())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ChatSettingsActivity.this, "Tema değiştirildi", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ChatSettingsActivity.this, "Tema kaydedilemedi", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadSelectedTheme() {
        chatSettingsRef.child("backgroundColor").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String selectedColor = snapshot.getValue(String.class);
                    for (BackgroundTheme theme : themeList) {
                        theme.setSelected(theme.getColorCode().equals(selectedColor));
                    }
                    themeAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void reportUser() {
        DatabaseReference reportsRef = database.getReference("Reports");
        String reportId = reportsRef.push().getKey();

        long timestamp = System.currentTimeMillis();

        // Report objesi oluştur
        ReportModel report = new ReportModel();
        report.setReporterId(currentUserId);
        report.setReportedUserId(receiverUid);
        report.setReportedUserName(receiverName);
        report.setTimestamp(timestamp);
        report.setReason("Uygunsuz mesaj/fotoğraf");
        report.setStatus("pending");

        reportsRef.child(reportId).setValue(report)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ChatSettingsActivity.this, "Şikayetiniz alındı", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ChatSettingsActivity.this, "Şikayet gönderilemedi", Toast.LENGTH_SHORT).show();
                });
    }

    // Report model sınıfı
    private static class ReportModel {
        private String reporterId, reportedUserId, reportedUserName, reason, status;
        private long timestamp;

        public ReportModel() {}

        // Getters and Setters
        public String getReporterId() { return reporterId; }
        public void setReporterId(String reporterId) { this.reporterId = reporterId; }

        public String getReportedUserId() { return reportedUserId; }
        public void setReportedUserId(String reportedUserId) { this.reportedUserId = reportedUserId; }

        public String getReportedUserName() { return reportedUserName; }
        public void setReportedUserName(String reportedUserName) { this.reportedUserName = reportedUserName; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}