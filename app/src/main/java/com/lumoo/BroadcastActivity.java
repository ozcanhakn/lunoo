package com.lumoo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.Objects;
import java.util.Random;


import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.entity.ZegoEngineProfile;

import androidx.activity.result.ActivityResultCallback;



public class BroadcastActivity extends AppCompatActivity {


    private long appID = 1894509662;
    private String appSign = "76423ffc215ab783f16feb49f04a220227c3aaae15d6e92801fc1d7cbabb49d8";

    FirebaseDatabase database;
    DatabaseReference reference;

    String name,userName,photo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_broadcast);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Kullanıcılar").child(userId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                name = snapshot.child("ad").getValue(String.class);
                userName = snapshot.child("kullanıcıAdı").getValue(String.class);
                photo = snapshot.child("profileImage").getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        MaterialButton hostStream = findViewById(R.id.hostStream);
        MaterialButton joinStream = findViewById(R.id.joinStream);

        TextInputLayout roomIDLayout = findViewById(R.id.roomIDLayout);
        TextInputEditText roomIDET = findViewById(R.id.roomIDET);

        hostStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(BroadcastActivity.this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    activityResultLauncher.launch(android.Manifest.permission.RECORD_AUDIO);
                } else if (ActivityCompat.checkSelfPermission(BroadcastActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    activityResultLauncher.launch(android.Manifest.permission.CAMERA);
                } else {
                    String randomRoomID = generateRandomID(); // Rastgele oda ID oluştur

                    // Firebase veritabanına yayıncı bilgilerini ekle
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    String userId = currentUser.getUid();

                    // Kullanıcı bilgilerini Firebase'e ekle
                    DatabaseReference publishersRef = FirebaseDatabase.getInstance().getReference("Publishers").child(randomRoomID);
                    publishersRef.child("userId").setValue(userId);
                    publishersRef.child("userName").setValue(userName);
                    publishersRef.child("roomId").setValue(randomRoomID);
                    publishersRef.child("streamerPhoto").setValue(photo);

                    Intent intent = new Intent(BroadcastActivity.this, ViewerActivity.class);
                    intent.putExtra("userID", name);
                    intent.putExtra("userName", userName);
                    intent.putExtra("roomID", randomRoomID); // Rastgele oluşturulan oda ID'sini ekle
                    intent.putExtra("isHost", true);
                    startActivity(intent);
                }
            }
        });



        joinStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(BroadcastActivity.this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    activityResultLauncher.launch(android.Manifest.permission.RECORD_AUDIO);
                } else if (ActivityCompat.checkSelfPermission(BroadcastActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    activityResultLauncher.launch(android.Manifest.permission.CAMERA);
                } else {
                    if (Objects.requireNonNull(roomIDET.getText()).toString().isEmpty()) {
                        roomIDLayout.setError("Oda numarası girin");
                    } else {
                        Intent intent = new Intent(BroadcastActivity.this, ViewerActivity.class);
                        intent.putExtra("userID", generateRandomID());
                        intent.putExtra("userName", userName);
                        intent.putExtra("roomID", roomIDET.getText().toString());
                        intent.putExtra("isHost", false);
                        startActivity(intent);
                    }
                }
            }
        });

        ZegoEngineProfile profile = new ZegoEngineProfile();
        profile.appID = appID;
        profile.appSign = appSign;
        profile.scenario = ZegoScenario.BROADCAST;
        profile.application = getApplication();
        ZegoExpressEngine.createEngine(profile, null);
    }

    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean o) {

        }
    });

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ZegoExpressEngine.destroyEngine(null);
    }

    private String generateRandomID() {
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        while (builder.length() < 6) {
            int nextInt = random.nextInt(10);
            if (builder.length() == 0 && nextInt ==0) {
                continue;
            }
            builder.append(nextInt);
        }
        return builder.toString();
    }
}