package com.lumoo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.lumoo.util.SecurityUtils;
import com.lumoo.util.ValidationUtils;

import io.reactivex.rxjava3.annotations.NonNull;

public class LoginActivity extends AppCompatActivity {
    EditText edtMail, edtPassword;
    RelativeLayout btnLogin;
    FirebaseAuth mAuth;
    TextView forgotPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();
        initViews();
        setupClickListeners();

    }
    
    private void initViews() {
        edtMail = findViewById(R.id.edtMail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        forgotPass = findViewById(R.id.txtLoginText);
    }
    
    private void setupClickListeners() {
        btnLogin.setOnClickListener(view -> loginUserAccount());
        
        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loginUserAccount() {
        String email, password;
        email = edtMail.getText().toString().trim();
        password = edtPassword.getText().toString().trim();

        // Güvenlik kontrolleri - ValidationUtils kullan
        ValidationResult emailResult = ValidationUtils.validateEmail(email);
        if (!emailResult.isValid()) {
            Toast.makeText(getApplicationContext(), emailResult.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        ValidationResult passwordResult = ValidationUtils.validatePassword(password);
        if (!passwordResult.isValid()) {
            Toast.makeText(getApplicationContext(), passwordResult.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        // Güvenli giriş işlemi
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Güvenlik log'u
                            SecurityUtils.logSecurityEvent("User Login", "Successful login for: " + email);
                            
                            Toast.makeText(getApplicationContext(), getString(R.string.succlogin), Toast.LENGTH_LONG).show();
                            
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            // Güvenlik log'u - başarısız giriş
                            SecurityUtils.logSecurityEvent("Failed Login Attempt", "Failed login for: " + email);
                            
                            Toast.makeText(getApplicationContext(), getString(R.string.failedlogin), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}