package com.lumoo;



import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.lumoo.Model.Post;
import com.lumoo.ViewHolder.PostViewHolder;
import com.lumoo.util.GlideUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import io.reactivex.rxjava3.annotations.NonNull;

public class FriendsSharesFragment extends Fragment {
    RecyclerView recyclerView;
    LottieAnimationView lottieGhostShare;
    FirebaseRecyclerAdapter<Post, PostViewHolder> adapter;
    String currentUserId;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public FriendsSharesFragment() {
        // Required empty public constructor
    }

    public static FriendsSharesFragment newInstance(String param1, String param2) {
        FriendsSharesFragment fragment = new FriendsSharesFragment();
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
        View view = inflater.inflate(R.layout.fragment_friends_shares, container, false);

        recyclerView = view.findViewById(R.id.recyclerPostFriend);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setSmoothScrollbarEnabled(true); // Smooth scroll
        layoutManager.setItemPrefetchEnabled(true);     // Ön yükleme ile akıcı scroll
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setNestedScrollingEnabled(false); // Eğer NestedScrollView içindeyse
        recyclerView.setMotionEventSplittingEnabled(false); // Daha pürüzsüz dokunma

        LinearSnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        lottieGhostShare = view.findViewById(R.id.lottieGhostShare);

        setupGlobalPostsRecyclerView();
        return view;
    }

    private void setupGlobalPostsRecyclerView() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference globalPostsRef = database.getReference("GlobalPosts");

        // Son 30 postu al, timestamp'e göre sıralama (en yeni post üstte olacak)
        Query query = globalPostsRef.orderByChild("timestamp").limitToLast(30);

        FirebaseRecyclerOptions<Post> options = new FirebaseRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull Post model) {
                try {
                    // Pozisyonu ters çevir: en yeni post üstte olacak
                    int reversedPosition = getItemCount() - 1 - position;
                    Post reversedModel = getItem(reversedPosition);

                    // Null kontrolleri
                    if (reversedModel.getLikes() == null) {
                        reversedModel.setLikes(new HashMap<>());
                    }
                    if (reversedModel.getComments() == null) {
                        reversedModel.setComments(new HashMap<>());
                    }

                    // Post resmi
                    if (reversedModel.getImage() != null && !reversedModel.getImage().isEmpty()) {
                        String imageUrl = reversedModel.getImage();
                        GlideUtil.loadOriginalImage(requireContext(), imageUrl, holder.imgPost);
                    }

                    // Kullanıcı bilgisi
                    holder.txtPostDesc.setText(reversedModel.getDescription() != null ? reversedModel.getDescription() : "");
                    holder.txtUsernamePost.setText(reversedModel.getUsername() != null ? reversedModel.getUsername() : "");
                    holder.txtPostDate.setText(reversedModel.getDate() != null ? formatDate(reversedModel.getDate()) : "");

                    if (!isAdded() || getContext() == null) return;
                    String imageUrl = reversedModel.getUrl();
                    GlideUtil.loadOriginalImage(getContext(), imageUrl, holder.userImage);

                    // Beğeni durumu
                    boolean isLiked = reversedModel.isLikedByUser(currentUserId);
                    updateLikeButton(holder.btnLike, isLiked);

                    // Beğeni sayısı
                    long likeCount = reversedModel.getLikeCount();
                    holder.txtLikeCount.setText(likeCount + " beğeni");

                    // Yorum sayısı
                    long commentCount = reversedModel.getCommentCount();
                    if (commentCount > 0) {
                        holder.txtViewComments.setVisibility(View.VISIBLE);
                        holder.txtViewComments.setText(commentCount + " yorumu görüntüle");
                    } else {
                        holder.txtViewComments.setVisibility(View.GONE);
                    }

                    // Beğeni butonuna tıklama
                    String globalPostKey = getRef(reversedPosition).getKey();
                    holder.btnLike.setOnClickListener(v -> toggleLikeGlobal(globalPostKey, reversedModel, holder));

                    // Yorum butonuna tıklama
                    holder.btnComment.setOnClickListener(v -> openCommentsGlobal(globalPostKey, reversedModel));

                    // Yorumları görüntüle tıklama
                    holder.txtViewComments.setOnClickListener(v -> openCommentsGlobal(globalPostKey, reversedModel));

                } catch (Exception e) {
                    //Log.e("FriendsSharesFragment", "Error binding view holder", e);
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

        // RecyclerView boşsa Lottie göster
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                checkEmpty();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                checkEmpty();
            }
        });
    }

    private void checkEmpty() {
        if (adapter.getItemCount() == 0) {
            lottieGhostShare.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            lottieGhostShare.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void toggleLikeGlobal(String globalPostKey, Post post, PostViewHolder holder) {
        if (globalPostKey == null) {
            Toast.makeText(getContext(), "Post anahtarı bulunamadı", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // 1. Global posts'ta güncelle
        DatabaseReference globalPostRef = database.getReference("GlobalPosts").child(globalPostKey);

        // 2. Kullanıcının kendi postunda da güncelle (eğer kendi postuysa)
        DatabaseReference userPostRef = null;
        if (post.getUid().equals(currentUserId)) {
            // Kendi postuysa kullanıcı profilinde de güncelle
            String userPostKey = post.getUserPostKey(); // Bu field'ı Post modeline eklememiz gerekiyor
            if (userPostKey != null) {
                userPostRef = database.getReference("Kullanıcılar").child(currentUserId).child("Post").child(userPostKey);
            }
        }

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

        // Global post'u güncelle
        globalPostRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    // UI'ı güncelle
                    updateLikeButton(holder.btnLike, !isCurrentlyLiked);
                    long newLikeCount = !isCurrentlyLiked ?
                            post.getLikeCount() + 1 : Math.max(0, post.getLikeCount() - 1);
                    holder.txtLikeCount.setText(newLikeCount + " beğeni");
                })
                .addOnFailureListener(e -> {
                    Log.e("GlobalPostsFragment", "Like update failed", e);
                    Toast.makeText(getContext(), "Beğeni güncellenirken hata oluştu", Toast.LENGTH_SHORT).show();
                });

        // Eğer kendi postuysa kullanıcı profilinde de güncelle
        if (userPostRef != null) {
            userPostRef.updateChildren(updates);
        }
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
            Log.e("GlobalPostsFragment", "Error updating like button", e);
        }
    }

    private void openCommentsGlobal(String globalPostKey, Post post) {
        if (globalPostKey == null) {
            Toast.makeText(getContext(), "Post anahtarı bulunamadı", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(getContext(), CommentsActivity.class);
        intent.putExtra("postKey", globalPostKey);
        intent.putExtra("postOwnerId", post.getUid());
        intent.putExtra("isGlobalPost", true); // Global post olduğunu belirt
        startActivity(intent);
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MMM-yyyy:HH:mm:ss", Locale.ENGLISH);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("tr", "TR"));

            Calendar date = Calendar.getInstance();
            date.setTime(inputFormat.parse(dateString));

            return outputFormat.format(date.getTime());
        } catch (Exception e) {
            Log.e("GlobalPostsFragment", "Error formatting date: " + dateString, e);
            return dateString;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            recyclerView.setAdapter(adapter); // 🔑 Adapter yeniden bağlansın
            adapter.startListening();
            checkEmpty();
        }
    }



    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
            recyclerView.setAdapter(null); // 🔑 detach et
        }
    }

}