package com.lumoo;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lumoo.ViewHolder.FrameAdapter;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;

public class FramesChooseActivity extends AppCompatActivity {
    private List<String> userFrames = new ArrayList<>();
    FrameAdapter frameAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        setContentView(R.layout.activity_frames_choose);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerViewFrames);
        FrameAdapter frameAdapter = new FrameAdapter(userFrames, this::onFrameSelected);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(frameAdapter);



        fetchUserFrames();


    }
    private void onFrameSelected(String selectedFrame) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child("Kullanıcılar").child(uid).child("frames");

        userRef.setValue(selectedFrame).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getApplicationContext(), "Seçilen Çerçeve: " + selectedFrame, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Çerçeve seçimi başarısız!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserFrames() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference framesRef = FirebaseDatabase.getInstance().getReference()
                .child("Kullanıcılar").child(uid).child("frameList");

        framesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userFrames.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot frameSnapshot : snapshot.getChildren()) {
                        String frameName = frameSnapshot.getValue(String.class);
                        userFrames.add(frameName);
                    }

                    if (userFrames.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Hiç çerçeven yok.", Toast.LENGTH_SHORT).show();
                    } else {
                        frameAdapter.notifyDataSetChanged();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Hiç çerçeven yok.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Çerçeveler yüklenemedi.", Toast.LENGTH_SHORT).show();
            }
        });
    }

}