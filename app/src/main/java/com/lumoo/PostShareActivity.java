package com.lumoo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PostShareActivity extends AppCompatActivity {
    EditText postDescription;
    RelativeLayout btnSharePost;
    ImageView imgSharePost, btnBackShare;

    // Supabase configuration
    private static final String SUPABASE_URL = "https://iauuehrfhmzhnfsnsjdx.supabase.co";
    private static final String SUPABASE_APIKEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlhdXVlaHJmaG16aG5mc25zamR4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTc4NjQ2NzAsImV4cCI6MjA3MzQ0MDY3MH0.GnwTJFqC_cLAuKt7dAlSjlVIBfy4O9nTVWyn3d2wzRM";
    private static final String SUPABASE_STORAGE_ENDPOINT = SUPABASE_URL + "/storage/v1/object/post/";
    private final OkHttpClient httpClient = new OkHttpClient();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post_share);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        postDescription = findViewById(R.id.postDescription);
        btnSharePost = findViewById(R.id.btnSharePost);
        imgSharePost = findViewById(R.id.imgSharePost);
        btnBackShare = findViewById(R.id.btnBackShare);

        // Seçilen görselin URI'sini al
        String imageUriString = getIntent().getStringExtra("imageUri");
        if (imageUriString != null) {
            Uri imageUri = Uri.parse(imageUriString);
            imgSharePost.setImageURI(imageUri);
        }

        btnSharePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharePost();
            }
        });

        btnBackShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void sharePost() {
        String description = postDescription.getText().toString().trim();
        String imageUriString = getIntent().getStringExtra("imageUri");

        if (imageUriString == null) {
            Toast.makeText(this, "Lütfen bir fotoğraf seçin", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri imageUri = Uri.parse(imageUriString);

        // Önce resmi Supabase'e yükle, sonra paylaş
        uploadImageToSupabase(imageUri, description);
    }

    private void uploadImageToSupabase(Uri imageUri, String description) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // Bitmap'i optimize et
            bitmap = resizeBitmap(bitmap, 1200, 1200);

            // Bitmap'i byte array'e dönüştür
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos); // %90 kalite
            byte[] imageData = baos.toByteArray();

            // Benzersiz dosya adı oluştur
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
                        Toast.makeText(PostShareActivity.this, "Fotoğraf yüklenemedi", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String imageUrl = SUPABASE_STORAGE_ENDPOINT.replace("/object/", "/object/public/") + fileName;
                        runOnUiThread(() -> {
                            savePostToFirebase(description, imageUrl);
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(PostShareActivity.this, "Upload hatası: " + response.code(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Resim işlenirken hata oluştu", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap resizeBitmap(Bitmap originalBitmap, int maxWidth, int maxHeight) {
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

        return Bitmap.createScaledBitmap(originalBitmap, finalWidth, finalHeight, true);
    }

    private void savePostToFirebase(String description, String imageUrl) {
        Calendar cdate = Calendar.getInstance();
        SimpleDateFormat currentdate = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
        String savedate = currentdate.format(cdate.getTime());

        Calendar ctime = Calendar.getInstance();
        SimpleDateFormat currenttime = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
        String savetime = currenttime.format(ctime.getTime());

        String time = savedate + ":" + savetime;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("Kullanıcılar").child(uid);

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String username = snapshot.child("kullanıcıAdı").getValue(String.class);
                String profileImageUrl = snapshot.child("profileImage").getValue(String.class);

                // Post objesini oluştur
                HashMap<String, Object> post = new HashMap<>();
                post.put("description", description);
                post.put("image", imageUrl); // URL olarak kaydediyoruz
                post.put("uid", uid);
                post.put("username", username);
                post.put("date", time);
                post.put("timestamp", ServerValue.TIMESTAMP);
                post.put("likes", new HashMap<String, Boolean>());
                post.put("comments", new HashMap<String, Object>());
                post.put("likeCount", 0);
                post.put("commentCount", 0);
                post.put("url", profileImageUrl);

                DatabaseReference userPostRef = database.getReference("Kullanıcılar")
                        .child(uid)
                        .child("Post");
                String userPostKey = userPostRef.push().getKey();

                DatabaseReference globalPostRef = database.getReference("GlobalPosts");
                String globalPostKey = globalPostRef.push().getKey();

                post.put("userPostKey", userPostKey);

                userPostRef.child(userPostKey).setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        globalPostRef.child(globalPostKey).setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused2) {
                                Toast.makeText(PostShareActivity.this, "Paylaşım başarılı", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(PostShareActivity.this, HomeActivity.class);
                                intent.putExtra("goToFragment", "profile");
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(PostShareActivity.this, "Global paylaşım hatası", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PostShareActivity.this, "Paylaşım yapılamadı", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PostShareActivity.this, "Kullanıcı bilgileri alınamadı", Toast.LENGTH_SHORT).show();
            }
        });
    }
}