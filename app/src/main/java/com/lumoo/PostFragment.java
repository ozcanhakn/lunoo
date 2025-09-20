package com.lumoo;



import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lumoo.Model.Post;
import com.lumoo.ViewHolder.PostViewHolder;
import com.lumoo.util.GlideUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import io.reactivex.rxjava3.annotations.NonNull;

public class PostFragment extends Fragment {

    RecyclerView recyclerView;
    FirebaseRecyclerAdapter<Post, PostViewHolder> adapter;
    String currentUserId,url;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public PostFragment() {
        // Required empty public constructor
    }

    public static PostFragment newInstance(String param1, String param2) {
        PostFragment fragment = new PostFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);

        recyclerView = view.findViewById(R.id.recyclerProfilePost);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Kullanıcılar").child(currentUserId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                url = snapshot.child("profileImage").getValue(String.class);
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });

        setupRecyclerView();
        return view;
    }

    private void setupRecyclerView() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        // Database yolunu düzelt
        DatabaseReference reference = database.getReference("Kullanıcılar").child(currentUserId).child("Post");

        // Timestamp'e göre sıralama (en yeni önce)
        Query query = reference.orderByChild("timestamp");

        FirebaseRecyclerOptions<Post> options = new FirebaseRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull Post model) {
                try {
                    // Resmi yükle - Bitmap boyutunu optimize et
                    if (model.getImage() != null && !model.getImage().isEmpty()) {
                        GlideUtil.loadOriginalImage(requireContext(), model.getImage(), holder.imgPost);
                    }

                    // Post bilgilerini set et
                    holder.txtPostDesc.setText(model.getDescription() != null ? model.getDescription() : "");
                    holder.txtUsernamePost.setText(model.getUsername() != null ? model.getUsername() : "");
                    holder.txtPostDate.setText(model.getDate() != null ? formatDate(model.getDate()) : "");

                    // Menü butonunu göster (sadece kendi postları için)
                    if (model.getUid() != null && model.getUid().equals(currentUserId)) {
                        holder.btnPostMenu.setVisibility(View.VISIBLE);
                    } else {
                        holder.btnPostMenu.setVisibility(View.GONE);
                    }

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



                    GlideUtil.loadOriginalImage(requireContext(), url, holder.userImage);

                    // Beğeni butonuna tıklama
                    String postKey = getRef(position).getKey();
                    holder.btnLike.setOnClickListener(v -> toggleLike(postKey, model, holder));

                    // Yorum butonuna tıklama
                    holder.btnComment.setOnClickListener(v -> openComments(postKey, model));

                    // Yorumları görüntüle tıklama
                    holder.txtViewComments.setOnClickListener(v -> openComments(postKey, model));

                    // Menü butonuna tıklama
                    holder.btnPostMenu.setOnClickListener(v -> showPostMenu(v, postKey, model));

                } catch (Exception e) {
                    Log.e("PostFragment", "Error binding view holder", e);
                    Toast.makeText(getContext(), "Post yüklenirken hata oluştu", Toast.LENGTH_SHORT).show();
                }
            }

            @NonNull
            @Override
            public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.post_item, parent, false);
                return new PostViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
    }

    private void showPostMenu(View view, String postKey, Post post) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.post_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_delete_post) {
                showDeleteConfirmDialog(postKey, post);
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    private void showDeleteConfirmDialog(String postKey, Post post) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Postu Sil");
        builder.setMessage("Bu postu silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.");
        builder.setPositiveButton("Sil", (dialog, which) -> deletePost(postKey, post));
        builder.setNegativeButton("İptal", null);
        builder.show();
    }

    private void deletePost(String postKey, Post post) {
        if (postKey == null) {
            Toast.makeText(getContext(), "Post anahtarı bulunamadı", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Kullanıcının postunu sil
        DatabaseReference userPostRef = database.getReference("Kullanıcılar")
                .child(currentUserId).child("Post").child(postKey);

        // Global posttan da sil (eğer userPostKey varsa)
        DatabaseReference globalPostsRef = database.getReference("GlobalPosts");

        userPostRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Global posts'tan da sil
                    if (post.getUserPostKey() != null) {
                        // Global posts'ta bu post'u bul ve sil
                        globalPostsRef.orderByChild("userPostKey").equalTo(postKey)
                                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                                    @Override
                                    public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                                        for (com.google.firebase.database.DataSnapshot childSnapshot : snapshot.getChildren()) {
                                            childSnapshot.getRef().removeValue();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                                        Log.e("PostFragment", "Global post delete failed", error.toException());
                                    }
                                });
                    }

                    Toast.makeText(getContext(), "Post silindi", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("PostFragment", "Post delete failed", e);
                    Toast.makeText(getContext(), "Post silinirken hata oluştu", Toast.LENGTH_SHORT).show();
                });
    }

    private void toggleLike(String postKey, Post post, PostViewHolder holder) {
        if (postKey == null) {
            Toast.makeText(getContext(), "Post anahtarı bulunamadı", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference postRef = database.getReference("Kullanıcılar")
                .child(currentUserId).child("Post").child(postKey);

        boolean isCurrentlyLiked = post.isLikedByUser(currentUserId);

        // Beğeni durumunu güncelle
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
                    // UI'ı güncelle
                    updateLikeButton(holder.btnLike, !isCurrentlyLiked);
                    long newLikeCount = !isCurrentlyLiked ?
                            post.getLikeCount() + 1 : Math.max(0, post.getLikeCount() - 1);
                    holder.txtLikeCount.setText(newLikeCount + " beğeni");
                })
                .addOnFailureListener(e -> {
                    Log.e("PostFragment", "Like update failed", e);
                    Toast.makeText(getContext(), "Beğeni güncellenirken hata oluştu", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateLikeButton(android.widget.ImageView btnLike, boolean isLiked) {
        try {
            if (isLiked) {
                btnLike.setImageResource(R.drawable.ic_heart_filled);
                btnLike.setColorFilter(getResources().getColor(android.R.color.holo_red_dark));
            } else {
                btnLike.setImageResource(R.drawable.ic_heart_outline);
                btnLike.setColorFilter(getResources().getColor(android.R.color.white));
            }
        } catch (Exception e) {
            Log.e("PostFragment", "Error updating like button", e);
        }
    }

    private void openComments(String postKey, Post post) {
        if (postKey == null) {
            Toast.makeText(getContext(), "Post anahtarı bulunamadı", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(getContext(), CommentsActivity.class);
        intent.putExtra("postKey", postKey);
        intent.putExtra("postOwnerId", post.getUid());
        startActivity(intent);
    }

    private String formatDate(String dateString) {
        try {
            // Mevcut format: "dd-MMM-yyyy:HH:mm:ss"
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MMM-yyyy:HH:mm:ss", Locale.ENGLISH);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("tr", "TR"));

            Calendar date = Calendar.getInstance();
            date.setTime(inputFormat.parse(dateString));

            return outputFormat.format(date.getTime());
        } catch (Exception e) {
            Log.e("PostFragment", "Error formatting date: " + dateString, e);
            return dateString; // Hata durumunda orijinal string'i döndür
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            recyclerView.setAdapter(adapter);
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
            recyclerView.setAdapter(null);
        }
    }


}