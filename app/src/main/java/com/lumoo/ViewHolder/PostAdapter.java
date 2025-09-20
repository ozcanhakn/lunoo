package com.lumoo.ViewHolder;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lumoo.CommentsActivity;
import com.lumoo.Model.Post;
import com.lumoo.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.annotations.NonNull;

public class PostAdapter extends RecyclerView.Adapter<PostFriendViewHolder> {
    private List<Post> postList;
    private Context context;
    private String currentUserId;

    public PostAdapter(List<Post> postList) {
        this.postList = postList;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public PostFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.post_item, parent, false);
        return new PostFriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostFriendViewHolder holder, int position) {
        Post model = postList.get(position);

        // Resmi yükle
        if (model.getImage() != null && !model.getImage().isEmpty()) {
            holder.imgPost.setImageBitmap(decodeBase64(model.getImage()));
        }

        // Post bilgilerini set et
        holder.txtPostDesc.setText(model.getDescription());
        holder.txtUsernamePost.setVisibility(View.VISIBLE);
        holder.txtUsernamePost.setText(model.getUsername());
        holder.txtPostDate.setText(formatDate(model.getDate()));

        // Beğeni durumunu kontrol et ve set et
        boolean isLiked = model.isLikedByUser(currentUserId);
        updateLikeButton(holder.btnLike, isLiked);

        // Beğeni sayısını set et
        long likeCount = model.getLikeCount();
        holder.txtLikeCount.setText(likeCount + " beğeni");

        // Yorum sayısını kontrol et
        long commentCount = model.getCommentCount();
        if (commentCount > 0) {
            holder.txtViewComments.setVisibility(View.VISIBLE);
            holder.txtViewComments.setText(commentCount + " yorumu görüntüle");
        } else {
            holder.txtViewComments.setVisibility(View.GONE);
        }

        // Beğeni butonuna tıklama
        holder.btnLike.setOnClickListener(v -> toggleLike(model, holder, position));

        // Yorum butonuna tıklama
        holder.btnComment.setOnClickListener(v -> openComments(model));

        // Yorumları görüntüle tıklama
        holder.txtViewComments.setOnClickListener(v -> openComments(model));
    }

    private void toggleLike(Post post, PostFriendViewHolder holder, int position) {
        // Post sahibinin UID'sini kullanarak doğru yolu oluştur
        String postOwnerId = post.getUid();
        if (postOwnerId == null) {
            Toast.makeText(context, "Post sahibi bilgisi bulunamadı", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Post key'ini bulmak için tüm postları kontrol et
        DatabaseReference ownerPostsRef = database.getReference("Kullanıcılar")
                .child(postOwnerId).child("Post");

        ownerPostsRef.orderByChild("date").equalTo(post.getDate()).get()
                .addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String postKey = snapshot.getKey();
                            DatabaseReference postRef = ownerPostsRef.child(postKey);

                            boolean isCurrentlyLiked = post.isLikedByUser(currentUserId);
                            HashMap<String, Object> updates = new HashMap<>();

                            if (isCurrentlyLiked) {
                                // Beğeniyi kaldır
                                updates.put("likes/" + currentUserId, null);
                                updates.put("likeCount", Math.max(0, post.getLikeCount() - 1));
                            } else {
                                // Beğeni ekle
                                updates.put("likes/" + currentUserId, true);
                                updates.put("likeCount", post.getLikeCount() + 1);
                            }

                            postRef.updateChildren(updates)
                                    .addOnSuccessListener(aVoid -> {
                                        // Local datayı güncelle
                                        if (isCurrentlyLiked) {
                                            if (post.getLikes() != null) {
                                                post.getLikes().remove(currentUserId);
                                            }
                                            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
                                        } else {
                                            if (post.getLikes() == null) {
                                                post.setLikes(new HashMap<>());
                                            }
                                            post.getLikes().put(currentUserId, true);
                                            post.setLikeCount(post.getLikeCount() + 1);
                                        }

                                        // UI'ı güncelle
                                        updateLikeButton(holder.btnLike, !isCurrentlyLiked);
                                        holder.txtLikeCount.setText(post.getLikeCount() + " beğeni");
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Beğeni güncellenirken hata oluştu", Toast.LENGTH_SHORT).show();
                                    });
                            break; // İlk eşleşmeyi bulduktan sonra çık
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Post bulunamadı", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateLikeButton(android.widget.ImageView btnLike, boolean isLiked) {
        if (isLiked) {
            btnLike.setImageResource(R.drawable.ic_heart_filled);
            btnLike.setColorFilter(context.getResources().getColor(android.R.color.holo_red_dark));
        } else {
            btnLike.setImageResource(R.drawable.ic_heart_outline);
            btnLike.setColorFilter(context.getResources().getColor(android.R.color.black));
        }
    }

    private void openComments(Post post) {
        // Post key'ini bulmak için tarihi kullan
        String postOwnerId = post.getUid();
        if (postOwnerId == null) {
            Toast.makeText(context, "Post sahibi bilgisi bulunamadı", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ownerPostsRef = database.getReference("Kullanıcılar")
                .child(postOwnerId).child("Post");

        ownerPostsRef.orderByChild("date").equalTo(post.getDate()).get()
                .addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String postKey = snapshot.getKey();

                            Intent intent = new Intent(context, CommentsActivity.class);
                            intent.putExtra("postKey", postKey);
                            intent.putExtra("postOwnerId", postOwnerId);
                            context.startActivity(intent);
                            break; // İlk eşleşmeyi bulduktan sonra çık
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Post bulunamadı", Toast.LENGTH_SHORT).show();
                });
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MMM-yyyy:HH:mm:ss", Locale.ENGLISH);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("tr", "TR"));

            Calendar date = Calendar.getInstance();
            date.setTime(inputFormat.parse(dateString));

            return outputFormat.format(date.getTime());
        } catch (Exception e) {
            return dateString;
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    // Adapter'a yeni postları eklemek için method
    public void updatePosts(List<Post> newPosts) {
        this.postList.clear();
        this.postList.addAll(newPosts);
        notifyDataSetChanged();
    }
    public static Bitmap decodeBase64(String input) {
        byte[] decodedBytes = Base64.decode(input, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

}