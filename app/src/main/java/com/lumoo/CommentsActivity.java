package com.lumoo;



import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lumoo.Model.Comment;
import com.lumoo.util.GlideUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class CommentsActivity extends AppCompatActivity {

    RecyclerView recyclerComments;
    EditText etComment;
    ImageButton btnSendComment;
    ImageView btnBack;
    TextView tvTitle;

    String postKey, postOwnerId, currentUserId, currentUsername,url;
    DatabaseReference commentsRef, userRef;
    FirebaseRecyclerAdapter<Comment, CommentViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        initViews();
        getIntentData();
        getCurrentUserData();
        setupRecyclerView();
        setupClickListeners();
    }

    private void initViews() {
        recyclerComments = findViewById(R.id.recyclerComments);
        etComment = findViewById(R.id.etComment);
        btnSendComment = findViewById(R.id.btnSendComment);
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);

        recyclerComments.setLayoutManager(new LinearLayoutManager(this));
    }

    private void getIntentData() {
        postKey = getIntent().getStringExtra("postKey");
        postOwnerId = getIntent().getStringExtra("postOwnerId");
        boolean isGlobalPost = getIntent().getBooleanExtra("isGlobalPost", false);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        if (isGlobalPost) {
            // Global post ise GlobalPosts'tan al
            commentsRef = database.getReference("GlobalPosts")
                    .child(postKey).child("comments");
        } else {
            // Normal post ise kullanıcının profilinden al
            commentsRef = database.getReference("Kullanıcılar")
                    .child(postOwnerId).child("Post").child(postKey).child("comments");
        }

        userRef = database.getReference("Kullanıcılar").child(currentUserId);
    }

    private void getCurrentUserData() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUsername = snapshot.child("kullanıcıAdı").getValue(String.class);
                url = snapshot.child("profileImage").getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CommentsActivity.this, "Kullanıcı bilgileri alınamadı", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        FirebaseRecyclerOptions<Comment> options = new FirebaseRecyclerOptions.Builder<Comment>()
                .setQuery(commentsRef, Comment.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Comment, CommentViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CommentViewHolder holder, int position, @NonNull Comment model) {
                holder.tvUsername.setText(model.getUsername());
                holder.tvComment.setText(model.getComment());
                holder.tvDate.setText(formatDate(model.getDate()));
                String imageUrl = model.getUrl();
                GlideUtil.loadOriginalImage(getApplicationContext(), imageUrl, holder.commentPhoto);


            }

            @NonNull
            @Override
            public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.comment_item, parent, false);
                return new CommentViewHolder(view);
            }
        };

        recyclerComments.setAdapter(adapter);
        adapter.startListening();
    }

    private void setupClickListeners() {
        btnSendComment.setOnClickListener(v -> sendComment());
        btnBack.setOnClickListener(v -> finish());
    }

    private void sendComment() {
        String commentText = etComment.getText().toString().trim();

        if (commentText.isEmpty()) {
            Toast.makeText(this, "Yorum boş olamaz", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUsername == null) {
            Toast.makeText(this, "Kullanıcı bilgileri yüklenmeyi bekleyin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tarih oluştur
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy:HH:mm:ss", Locale.ENGLISH);
        String currentDate = dateFormat.format(calendar.getTime());

        // Yorum objesi oluştur
        String commentId = commentsRef.push().getKey();
        Comment comment = new Comment(commentId, currentUserId, currentUsername, commentText, currentDate,url);

        // Yorumu kaydet
        commentsRef.child(commentId).setValue(comment)
                .addOnSuccessListener(aVoid -> {
                    etComment.setText("");
                    updateCommentCount(1);
                    Toast.makeText(CommentsActivity.this, "Yorum eklendi", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CommentsActivity.this, "Yorum eklenirken hata oluştu", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateCommentCount(int increment) {
        boolean isGlobalPost = getIntent().getBooleanExtra("isGlobalPost", false);
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        if (isGlobalPost) {
            // Global post ise GlobalPosts'ta güncelle
            DatabaseReference globalPostRef = database.getReference("GlobalPosts").child(postKey);
            globalPostRef.child("commentCount").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    long currentCount = 0;
                    if (task.getResult().getValue() != null) {
                        currentCount = (Long) task.getResult().getValue();
                    }
                    globalPostRef.child("commentCount").setValue(currentCount + increment);
                }
            });
        } else {
            // Normal post ise kullanıcı profilinde güncelle
            DatabaseReference postRef = database.getReference("Kullanıcılar")
                    .child(postOwnerId).child("Post").child(postKey);
            postRef.child("commentCount").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    long currentCount = 0;
                    if (task.getResult().getValue() != null) {
                        currentCount = (Long) task.getResult().getValue();
                    }
                    postRef.child("commentCount").setValue(currentCount + increment);
                }
            });
        }
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MMM-yyyy:HH:mm:ss", Locale.ENGLISH);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM, HH:mm", new Locale("tr", "TR"));

            Calendar date = Calendar.getInstance();
            date.setTime(inputFormat.parse(dateString));

            return outputFormat.format(date.getTime());
        } catch (Exception e) {
            return dateString;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    // ViewHolder sınıfı
    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvComment, tvDate;
        ImageView commentPhoto;
        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvCommentUsername);
            tvComment = itemView.findViewById(R.id.tvCommentText);
            tvDate = itemView.findViewById(R.id.tvCommentDate);
            commentPhoto = itemView.findViewById(R.id.commentUrl);
        }
    }



}