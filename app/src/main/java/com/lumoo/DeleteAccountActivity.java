package com.lumoo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DeleteAccountActivity extends AppCompatActivity {
    RelativeLayout btnDeleteAccountA;
    ImageView btnBackDelete;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_delete_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnDeleteAccountA = findViewById(R.id.btnDeleteAccountA);
        btnBackDelete = findViewById(R.id.btnBackDelete);

        btnBackDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DeleteAccountActivity.this, HomeActivity.class);
                intent.putExtra("goToFragment", "settings");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });

        btnDeleteAccountA.setOnClickListener(view -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Kullanıcılar");

            if (user != null) {
                String userId = user.getUid();

                // Kullanıcıyı veritabanından sil
                databaseRef.child(userId).removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Firebase Authentication'dan kullanıcıyı sil
                        user.delete().addOnCompleteListener(deleteTask -> {
                            if (deleteTask.isSuccessful()) {
                                // Kullanıcıyı çıkış yaptır ve giriş ekranına yönlendir
                                auth.signOut();
                                Intent intent = new Intent(DeleteAccountActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(DeleteAccountActivity.this, "Hesap silinirken hata oluştu", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(DeleteAccountActivity.this, "Veritabanından silinirken hata oluştu", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}