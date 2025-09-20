package com.lumoo;


import static java.security.AccessController.getContext;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lumoo.FCM.FCMNotificationSender;
import com.lumoo.Model.Post;
import com.lumoo.ViewHolder.PostViewHolder;
import com.lumoo.ViewHolder.TabPagerAdapter;
import com.lumoo.util.GlideUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class PublicProfileActivity extends AppCompatActivity {
    RelativeLayout btnAddFriendRequest;
    TextView publicProfileName,publicProfileSurname,txtCountryPP;
    ImageView imgPublicProfile,btnBackk,imgCountryFlagPublicProfile,imgGenderFlagPP;

    String name, uid, url,surname;
    String userToken;
    String userId;
    String interest1, interest2, interest3;

    TextView txtFollowerCount, txtPostCount, txtFollowCount;
    private TabLayout tabLayout;
    ViewPager2 viewPager;

    RecyclerView recyclerPostsPublicUser;
    RelativeLayout btnMessagePublicProfile, btnReportUser;

    String receiver_name;

    FirebaseRecyclerAdapter<Post, PostViewHolder> adapter;

    FirebaseDatabase database;
    DatabaseReference reference;

    TextView txtSurnamePublicProfile,txtUserNamePublicProfile,txtUserPublicCountry,txtHoroscopePublic
            ,txtUserAgePublic;
    // Görebiliyorsunuz değil mi ekranı, whatsapp'a yazmanız yeterli

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_public_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });





        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            url = bundle.getString("url");
            uid = bundle.getString("uid");


        } else {
            Toast.makeText(this, "user missing", Toast.LENGTH_SHORT).show();
        }

        Log.d("LOG101", "onCreate: "+uid);
        userId = uid;





        btnAddFriendRequest = findViewById(R.id.btnAddFriendRequest);
        publicProfileName = findViewById(R.id.publicProfileName);
        imgPublicProfile = findViewById(R.id.imgPublicProfile);
        btnMessagePublicProfile = findViewById(R.id.btnMessagePublicProfile);

        txtFollowCount = findViewById(R.id.userFollowCount);
        txtFollowerCount = findViewById(R.id.userFollowerCount);
        txtPostCount = findViewById(R.id.userPostCount);

        recyclerPostsPublicUser = findViewById(R.id.recyclerPostsPublicUser);
        btnReportUser = findViewById(R.id.btnPublicReport);

        txtSurnamePublicProfile = findViewById(R.id.txtSurnamePublicProfile);
        txtUserNamePublicProfile = findViewById(R.id.txtUserNamePublicProfile);
        txtUserPublicCountry = findViewById(R.id.txtUserPublicCountry);
        txtHoroscopePublic = findViewById(R.id.txtHoroscopePublic);
        txtUserAgePublic = findViewById(R.id.txtUserAgePublic);
        txtCountryPP = findViewById(R.id.txtCountryPP);
        imgGenderFlagPP = findViewById(R.id.imgGenderFlagPP);

        btnBackk = findViewById(R.id.btnBackk);

        btnBackk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PublicProfileActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        recyclerPostsPublicUser.setLayoutManager(new LinearLayoutManager(this));

        setupRecyclerView();




         /*
            url = bundle.getString("u");
            receiver_uid = bundle.getString("uid");
            receiver_name = bundle.getString("n");*/


        btnMessagePublicProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference reference = database.getReference("Kullanıcılar").child(uid);
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        receiver_name = snapshot.child("ad").getValue(String.class);
                        Intent intent = new Intent(PublicProfileActivity.this, MessageActivity.class);
                        intent.putExtra("u", url);
                        intent.putExtra("n", receiver_name);
                        intent.putExtra("uid", uid);

                        android.util.Log.d("(ppa)HATA LOGU : ", "onCreate: "+url+uid+receiver_name);

                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });



//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference reference = database.getReference("Kullanıcılar").child(userId);
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                name = snapshot.child("ad").getValue(String.class);
//                String frame = snapshot.child("frame").getValue(String.class);
//
//
//                if (!frame.equals("")) {
//                    if (frame.equals("Melek")) {
//                        imgPublicProfileFrame.setImageResource(R.drawable.melek_cercevesi);
//                    } else if (frame.equals("Goril")) {
//                        imgPublicProfileFrame.setImageResource(R.drawable.goril_cercevesi);
//                    } else if (frame.equals("Aslan")) {
//                        imgPublicProfileFrame.setImageResource(R.drawable.aslan_cercevesi);
//                    } else if (frame.equals("AskoKusko")) {
//                        imgPublicProfileFrame.setImageResource(R.drawable.askokusko_cercevesi);
//                    } else if (frame.equals("Elmas")) {
//                        imgPublicProfileFrame.setImageResource(R.drawable.elmas_cercevesi);
//                    } else if (frame.equals("Sarmasık")) {
//                        imgPublicProfileFrame.setImageResource(R.drawable.sarmasik_cercevesi);
//                    } else if (frame.equals("Hilal")) {
//                        imgPublicProfileFrame.setImageResource(R.drawable.hilalvekilic);
//                    } else if (frame.equals("Kelebek")) {
//                        imgPublicProfileFrame.setImageResource(R.drawable.kelecekcercevesi);
//                    } else if (frame.equals("Miğfer")) {
//                        imgPublicProfileFrame.setImageResource(R.drawable.migfer_cercevesi);
//                    } else if (frame.equals("Ateş")) {
//                        imgPublicProfileFrame.setImageResource(R.drawable.ates_cercevesi);
//                    }
//                }else {
//                    imgPublicProfileFrame.setVisibility(View.GONE);
//                }
//
//                initSetText(name);
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });


        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Kullanıcılar").child(userId);

        FirebaseDatabase database1 = FirebaseDatabase.getInstance();
        DatabaseReference reference1 = database1.getReference("Kullanıcılar").child(userId);

        Log.d("idd", "onCreate: "+userId);

        reference1.child("Friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                String countText = String.valueOf(count);
                txtFollowerCount.setText(countText);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // 2. Post sayısı (Posts altında kaç ID varsa)
        reference.child("Posts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                txtPostCount.setText("" + count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // 3. Kaç kişiyle arkadaş olmuşsun (başkalarının Friends'inde sen var mısın?)
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int addedByCount = 0;
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    if (userSnapshot.child("Friends").hasChild(userId)) {
                        addedByCount++;
                    }
                }
                txtFollowCount.setText("" + addedByCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        init();
        initSetText(userId);
    }


    private void init() {
        btnAddFriendRequest.setOnClickListener(view -> {
            String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance();

            // Önce kendi bilgilerini al
            DatabaseReference userRef = database.getReference("Kullanıcılar").child(myUid);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("ad").getValue(String.class);
                        String surname = snapshot.child("soyad").getValue(String.class);
                        String username = snapshot.child("kullanıcıAdı").getValue(String.class);
                        String profilePhoto = snapshot.child("profileImage").getValue(String.class);

                        // Arkadaş objesi
                        HashMap<String, Object> friendMap = new HashMap<>();
                        friendMap.put("uid", myUid);
                        friendMap.put("name", name);
                        friendMap.put("surname", surname);
                        friendMap.put("username", username);
                        friendMap.put("profileImage", profilePhoto);

                        // 1. Taraf: benim Friends altına ekle
                        DatabaseReference myFriendsRef = database.getReference("Kullanıcılar")
                                .child(myUid)
                                .child("Friends")
                                .child(uid); // karşı tarafın UID'si
                        myFriendsRef.setValue(friendMap);

                        // 2. Taraf: karşı tarafın Friends altına ekle
                        DatabaseReference otherFriendsRef = database.getReference("Kullanıcılar")
                                .child(uid)
                                .child("Friends")
                                .child(myUid); // kendi UID'm
                        otherFriendsRef.setValue(friendMap).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(view.getContext(), "Artık arkadaşsınız!", Toast.LENGTH_SHORT).show();
                                sendNotification(uid, name);
                            } else {
                                Toast.makeText(view.getContext(), "Arkadaş ekleme başarısız!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(view.getContext(), "Veri okunurken hata oluştu!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnReportUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportUser();
            }
        });
    }

    private void sendNotification(String receiver_uid, String receiver_name) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens").child(receiver_uid).child("token");


        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userToken = snapshot.getValue(String.class);
                String title = "Arkadaş Ekledi";
                String message = receiver_name + ", seni arkadaş ekledi";

                FCMNotificationSender notificationSender = new FCMNotificationSender(userToken, title, message, getApplicationContext());
                notificationSender.sendNotification();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initSetText(String uid) {
        GlideUtil.loadOriginalImage(getApplicationContext(), url, imgPublicProfile);
        FirebaseDatabase database1 = FirebaseDatabase.getInstance();
        DatabaseReference reference1 = database1.getReference("Kullanıcılar").child(userId);
        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("ad").getValue(String.class);
                String surname = snapshot.child("soyad").getValue(String.class);
                String country = snapshot.child("country").getValue(String.class);
                String horoscope = snapshot.child("burç").getValue(String.class);
                String age = snapshot.child("doğumTarihi").getValue(String.class);
                String username = snapshot.child("kullanıcıAdı").getValue(String.class);
                String countryCode = snapshot.child("countryCode").getValue(String.class);
                String gender = snapshot.child("gender").getValue(String.class);

                publicProfileName.setText(name);
                txtUserAgePublic.setText(age);
                txtHoroscopePublic.setText(horoscope);
                
                // Ülke kısaltmalarını uygula
                String displayCountry;
                if ("Amerika Birleşik Devletleri".equals(country)) {
                    displayCountry = "ABD";
                } else if ("Dominik Cumhuriyeti".equals(country)) {
                    displayCountry = "Dominik";
                } else if ("Birleşik Arap Emirlikleri".equals(country)) {
                    displayCountry = "BAE";
                } else {
                    displayCountry = country;
                }
                
                txtUserPublicCountry.setText(displayCountry);
                txtSurnamePublicProfile.setText(surname);
                txtUserNamePublicProfile.setText(username);
                txtCountryPP.setText(displayCountry);

                if (gender != null && !gender.isEmpty()){
                    if (gender.equals("Erkek")){
                        imgGenderFlagPP.setImageResource(R.drawable.maleicon);
                    } else if (gender.equals("Kadın")) {
                        imgGenderFlagPP.setImageResource(R.drawable.femaleicon);
                    }
                }else {
                    imgGenderFlagPP.setImageResource(R.drawable.ic_launcher_background);
                }

                if (countryCode != null && !countryCode.isEmpty()) {
                    // countryCode ile aynı isimde drawable kaynağını buluyoruz
                    int resId = getResources().getIdentifier(countryCode.toLowerCase(), "drawable", getPackageName());

                    if (resId != 0) {
                        // Drawable bulunduysa imgGenderFlag ImageView'a set ediyoruz
                        imgCountryFlagPublicProfile = findViewById(R.id.imgCountryFlagPublicProfile);
                        imgCountryFlagPublicProfile.setImageResource(resId);
                    } else {
                        // Eğer drawable yoksa istersen varsayılan bir görsel atayabilirsin
                        imgCountryFlagPublicProfile = findViewById(R.id.imgCountryFlagProfile);
                        imgCountryFlagPublicProfile.setImageResource(R.drawable.tr); // default_flag senin varsayılan bayrak resmin olur
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void setupRecyclerView() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        // Database yolunu düzelt
        DatabaseReference reference = database.getReference("Kullanıcılar").child(userId).child("Post");

        // Timestamp'e göre sıralama (en yeni önce)
        Query query = reference.orderByChild("timestamp");

        FirebaseRecyclerOptions<Post> options = new FirebaseRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@io.reactivex.rxjava3.annotations.NonNull PostViewHolder holder, int position, @io.reactivex.rxjava3.annotations.NonNull Post model) {
                try {
                    // Resmi yükle
                    if (model.getImage() != null && !model.getImage().isEmpty()) {
                        GlideUtil.loadOriginalImage(getApplicationContext(), model.getImage(), holder.imgPost);
                    }

                    // Post bilgilerini set et
                    holder.txtPostDesc.setText(model.getDescription() != null ? model.getDescription() : "");
                    holder.txtUsernamePost.setText(model.getUsername() != null ? model.getUsername() : "");
                    holder.txtPostDate.setText(model.getDate() != null ? formatDate(model.getDate()) : "");

                    // Beğeni durumunu kontrol et ve set et
                    boolean isLiked = model.isLikedByUser(userId);
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
                    String postKey = getRef(position).getKey();
                    holder.btnLike.setOnClickListener(v -> toggleLike(postKey, model, holder));

                    // Yorum butonuna tıklama
                    holder.btnComment.setOnClickListener(v -> openComments(postKey, model));

                    // Yorumları görüntüle tıklama
                    holder.txtViewComments.setOnClickListener(v -> openComments(postKey, model));

                } catch (Exception e) {
                    Log.e("PostFragment", "Error binding view holder", e);
                    Toast.makeText(PublicProfileActivity.this, "Post yüklenirken hata oluştu", Toast.LENGTH_SHORT).show();
                }
            }

            @io.reactivex.rxjava3.annotations.NonNull
            @Override
            public PostViewHolder onCreateViewHolder(@io.reactivex.rxjava3.annotations.NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.post_item, parent, false);
                return new PostViewHolder(view);
            }
        };

        recyclerPostsPublicUser.setAdapter(adapter);
    }

    private void toggleLike(String postKey, Post post, PostViewHolder holder) {
        if (postKey == null) {
            Toast.makeText(PublicProfileActivity.this, "Post anahtarı bulunamadı", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference postRef = database.getReference("Kullanıcılar")
                .child(userId).child("Post").child(postKey);

        boolean isCurrentlyLiked = post.isLikedByUser(userId);

        // Beğeni durumunu güncelle
        HashMap<String, Object> updates = new HashMap<>();

        if (isCurrentlyLiked) {
            // Beğeniyi kaldır
            updates.put("likes/" + userId, null);
            updates.put("likeCount", Math.max(0, post.getLikeCount() - 1));
        } else {
            // Beğeni ekle
            updates.put("likes/" + userId, true);
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
                    Toast.makeText(PublicProfileActivity.this, "Beğeni güncellenirken hata oluştu", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateLikeButton(android.widget.ImageView btnLike, boolean isLiked) {
        try {
            if (isLiked) {
                btnLike.setImageResource(R.drawable.ic_heart_filled);
                btnLike.setColorFilter(getResources().getColor(android.R.color.holo_red_dark));
            } else {
                btnLike.setImageResource(R.drawable.ic_heart_outline);
                btnLike.setColorFilter(getResources().getColor(android.R.color.black));
            }
        } catch (Exception e) {
            Log.e("PostFragment", "Error updating like button", e);
        }
    }

    private void openComments(String postKey, Post post) {
        if (postKey == null) {
            Toast.makeText(PublicProfileActivity.this, "Post anahtarı bulunamadı", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(PublicProfileActivity.this, CommentsActivity.class);
        intent.putExtra("postKey", postKey);
        intent.putExtra("postOwnerId", post.getUid());
        startActivity(intent);
    }

    private void reportUser() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reportsRef = database.getReference("Reports");
        String reportId = reportsRef.push().getKey();

        long timestamp = System.currentTimeMillis();

        // Report objesi oluştur
        PublicProfileActivity.ReportModel report = new ReportModel();
        report.setReporterId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        report.setReportedUserId(userId);
        report.setReportedUserName("");
        report.setTimestamp(timestamp);
        report.setReason("Uygunsuz mesaj/fotoğraf");
        report.setStatus("pending");

        reportsRef.child(reportId).setValue(report)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(PublicProfileActivity.this, "Şikayetiniz alındı", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(PublicProfileActivity.this, "Şikayet gönderilemedi", Toast.LENGTH_SHORT).show();
                });
    }

    // Report model sınıfı
    public static class ReportModel {
        private String reporterId, reportedUserId, reportedUserName, reason, status;
        private long timestamp;

        public ReportModel() {}

        // Getters and Setters
        public String getReporterId() { return reporterId; }
        public void setReporterId(String reporterId) { this.reporterId = reporterId; }

        public String getReportedUserId() { return reportedUserId; }
        public void setReportedUserId(String reportedUserId) { this.reportedUserId = reportedUserId; }

        public String getReportedUserName() { return reportedUserName; }
        public void setReportedUserName(String reportedUserName) { this.reportedUserName = reportedUserName; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
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
            recyclerPostsPublicUser.setAdapter(adapter);
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
            recyclerPostsPublicUser.setAdapter(null);
        }
    }


}