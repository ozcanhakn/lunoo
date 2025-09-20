package com.lumoo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import io.reactivex.rxjava3.annotations.NonNull;

public class ForgotPasswordActivity extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forget_password);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        final FirebaseAuth auth = FirebaseAuth.getInstance();

        final EditText emailEditText = findViewById(R.id.emailEdittextForgotPass); // EditText alanı için id'yi ayarlayın
        RelativeLayout resetPasswordButton = findViewById(R.id.continueButtonForgot); // Şifre sıfırlama butonu iç


        ((View) resetPasswordButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailAddress = emailEditText.getText().toString();

                if (!TextUtils.isEmpty(emailAddress)) {
                    auth.sendPasswordResetEmail(emailAddress)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Şifre sıfırlama e-postası başarıyla gönderildi
                                        Toast.makeText(ForgotPasswordActivity.this, "Şifre sıfırlama e-postası gönderildi.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Şifre sıfırlama e-postası gönderilemedi
                                    }
                                }
                            });
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, getString(R.string.pls_enter_mail), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}