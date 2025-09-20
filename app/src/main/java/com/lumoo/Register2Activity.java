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
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lumoo.util.SecurityUtils;
import com.lumoo.util.ValidationUtils;

import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;

public class Register2Activity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    EditText edtEmail, edtPasswordReg,edtInvitationReg;
    RelativeLayout btnRegister;
    TextView btnPrivacy,btnLogin;
    String invitationCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }        setContentView(R.layout.activity_register2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtEmail = findViewById(R.id.edtEmail);
        edtPasswordReg = findViewById(R.id.edtPasswordReg);
        edtInvitationReg = findViewById(R.id.edtInvitationReg);
        btnRegister = findViewById(R.id.btnRegister);
        btnPrivacy = findViewById(R.id.btnPrivacy);
        btnLogin = findViewById(R.id.btnLogin);

        firebaseAuth = FirebaseAuth.getInstance();


        init();
    }

    private void init(){
        // Continue butonuna tıklama işlemini dinle
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kullanıcının girdiği e-posta adresini alın
                String email = edtEmail.getText().toString().trim();

                // E-posta adresi boş mu kontrol et
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.pls_enter_mail), Toast.LENGTH_SHORT).show();
                    return;
                }

                // Firebase Authentication ile e-posta adresinin daha önce kaydolup olmadığını sorgula
                FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                            @Override
                            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                if (task.isSuccessful()) {
                                    SignInMethodQueryResult result = task.getResult();
                                    List<String> signInMethods = result.getSignInMethods();

                                    if (signInMethods != null && signInMethods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)) {
                                        // E-posta adresi ile kayıt olunmuşsa, kullanıcıyı uyarmak veya giriş yapmasını sağlamak isterseniz burada gerekli işlemleri yapabilirsiniz.
                                        Toast.makeText(getApplicationContext(), getString(R.string.already_mail), Toast.LENGTH_SHORT).show();
                                    } else {
                                        // E-posta adresi ile daha önce kayıt olunmamışsa, kullanıcıyı şifre belirlemesi için yönlendirin
                                        // Şifre belirleme ekranının görünür hale gelmesi veya yeni bir aktivite başlatılması gibi işlemleri burada yapabilirsiniz.
                                        // İsterseniz bu kısmı kendi uygulamanıza uygun şekilde özelleştirebilirsiniz.
                                        edtPasswordReg.setVisibility(View.VISIBLE);
                                        registerNewUser();
                                    }
                                } else {
                                    // Sorgu başarısız olduysa
                                    Toast.makeText(getApplicationContext(), "Sorgu başarısız. Hata: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    private void registerNewUser() {
        String email, password, invitationCode;
        email = edtEmail.getText().toString();
        password = edtPasswordReg.getText().toString();
        invitationCode = edtInvitationReg.getText().toString().trim();

        // ✅ ValidationUtils ile güvenli form kontrolü
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

        // ✅ SecurityUtils ile güvenlik log'u
        SecurityUtils.logSecurityEvent("Registration Attempt", "New user registration: " + email);

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String newUserId = firebaseAuth.getCurrentUser().getUid();
                            
                            // ✅ SecurityUtils ile başarılı kayıt log'u
                            SecurityUtils.logSecurityEvent("Successful Registration", "User registered successfully: " + email);
                            
                            Toast.makeText(getApplicationContext(), getString(R.string.suc_register), Toast.LENGTH_LONG).show();

                            if (!TextUtils.isEmpty(invitationCode)) {
                                checkInvitationCode(invitationCode, newUserId);
                            }

                            Intent intent = new Intent(Register2Activity.this, PersonalInformationActivity.class);
                            startActivity(intent);
                        } else {
                            // ✅ SecurityUtils ile başarısız kayıt log'u
                            SecurityUtils.logSecurityEvent("Failed Registration", "Registration failed for: " + email);
                            
                            Toast.makeText(getApplicationContext(), getString(R.string.failed_register), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    private void checkInvitationCode(String invitationCode, String newUserId) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Kullanıcılar");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String uid = userSnapshot.getKey();
                    String storedInvitationCode = userSnapshot.child("invitation").getValue(String.class);

                    if (storedInvitationCode != null && storedInvitationCode.equals(invitationCode)) {
                        // Credit değerini al
                        String creditValue = userSnapshot.child("credit").getValue(String.class);
                        int currentCredit = (creditValue != null) ? Integer.parseInt(creditValue) : 0;

                        // 20 kredi ekleyip güncelle
                        int newCredit = currentCredit + 20;
                        usersRef.child(uid).child("credit").setValue(String.valueOf(newCredit));

                        Toast.makeText(getApplicationContext(), "Davet kodu geçerli! Kullanıcının kredisi artırıldı.", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                Toast.makeText(getApplicationContext(), "Geçersiz davet kodu!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Veri okunamadı: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

}