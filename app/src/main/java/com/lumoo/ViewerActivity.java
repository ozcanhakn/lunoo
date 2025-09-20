package com.lumoo;


import android.animation.Animator;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.lumoo.FCM.FCMNotificationSender;
import com.lumoo.Model.Gift;
import com.lumoo.Model.LiveStreamChat;
import com.lumoo.ViewHolder.GiftAdapter;
import com.lumoo.ViewHolder.LiveStreamMessageViewHolder;
import com.lumoo.util.GlideUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.callback.IZegoRoomLoginCallback;
import im.zego.zegoexpress.constants.ZegoStreamResourceMode;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoPlayerConfig;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;
import io.reactivex.rxjava3.annotations.Nullable;

public class ViewerActivity extends AppCompatActivity {
    private String userID, userName, roomID;
    private boolean isHost;
    private boolean isActivityDestroyed = false;

    private DatabaseReference giftsRef;
    private ChildEventListener giftsChildEventListener;
    RelativeLayout messageStream;
    ImageView exitStream1, exitStream;
    CardView giftStream;
    RecyclerView recyclerLiveStreamChat;
    FirebaseRecyclerAdapter<LiveStreamChat, LiveStreamMessageViewHolder> adapter;


    EditText edtMessageBox;
    ImageButton btnSendMessageLiveStream;
    RecyclerView recyclerViewGifts;
    LinearLayout cardMessageBox;

    String userPhoto;

    private LinearLayout giftContainer;
    private ImageView btnCloseGifts;
    private boolean isGiftContainerVisible = false;
    TextView txtStreamerSurname;
    ImageView streamerPhoto10,giftImageView;


    // New UI elements for viewer count and top gifters
    TextView txtViewerLiveStream;
    CircleImageView profileImageFirstGifter, profileImageSecondGifter;
    ImageView imgFrameFirstGifter, imgFrameSecondGifter;
    CustomDualProgressBar giftCountProgressBar; // ProgressBar olarak değiştirildi
    RelativeLayout cardProfileImageFirstSecondGifter, cardProfileImageSecondGifter;

    // Variables to track viewer count and top gifters
    private int currentViewerCount = 0;
    private Map<String, Integer> userGiftTotals = new HashMap<>();
    private String firstGifterUid = "";
    private String secondGifterUid = "";
    private int firstGifterTotal = 0;
    private int secondGifterTotal = 0;

    // Ses için MediaPlayer
    private MediaPlayer giftSoundPlayer;

    // Yayıncının UID'sini tutmak için değişken
    private String streamerUid = "";

    CardView btnSendFollowReq;

    String userToken;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_viewer);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        isActivityDestroyed = false;


        userID = getIntent().getStringExtra("userID");
        userName = getIntent().getStringExtra("userName");
        roomID = getIntent().getStringExtra("roomID");
        isHost = getIntent().getBooleanExtra("isHost", false);

        Log.d("12313", "onCreate: " + userID + userName + roomID + isHost);


        exitStream = findViewById(R.id.exitStream);
        exitStream1 = findViewById(R.id.exitStream1);
        messageStream = findViewById(R.id.messageStream);
        giftStream = findViewById(R.id.giftStream);
        recyclerLiveStreamChat = findViewById(R.id.recyclerLiveStreamChat);
        cardMessageBox = findViewById(R.id.cardMessageBox);
        edtMessageBox = findViewById(R.id.edtMessageBox);
        btnSendMessageLiveStream = findViewById(R.id.btnSendMessageLiveStream);
        streamerPhoto10 = findViewById(R.id.streamerPhoto);
        txtStreamerSurname = findViewById(R.id.txtStreamerSurname);

        giftContainer = findViewById(R.id.giftContainer);
        btnCloseGifts = findViewById(R.id.btnCloseGifts);


        //recyclerLiveStreamChat.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);

        recyclerLiveStreamChat.setLayoutManager(layoutManager);
        recyclerLiveStreamChat.setItemAnimator(new DefaultItemAnimator());
        recyclerViewGifts = findViewById(R.id.recyclerViewGifts);

        // Initialize new UI elements
        txtViewerLiveStream = findViewById(R.id.txtViewerLiveStream);
        profileImageFirstGifter = findViewById(R.id.profileImageFirstGifter);
        profileImageSecondGifter = findViewById(R.id.profileImageSecondGifter);
        imgFrameFirstGifter = findViewById(R.id.imgFrameFirstGifter);
        imgFrameSecondGifter = findViewById(R.id.imgFrameSecondGifter);
        giftCountProgressBar = findViewById(R.id.giftCountProgressBar); // ProgressBar olarak değiştirildi
        cardProfileImageFirstSecondGifter = findViewById(R.id.cardProfileImageFirstSecondGifter);
        cardProfileImageSecondGifter = findViewById(R.id.cardProfileImageSecondGifter);
        giftImageView = findViewById(R.id.giftImageView);

        btnSendFollowReq = findViewById(R.id.btnSendFollowReq);

        // Viewer count için odaya katıldığımızda +1 ekliyoruz (sadece viewer için)
        if (!isHost) {
            updateViewerCount(1);
        }

        // Initialize viewer count tracking
        initializeViewerCount();
        // Yayıncının UID'sini al (Publishers tablosundan)
        getStreamerUid();
        // Initialize top gifters tracking
        initializeTopGiftersTracking();

        startListenEvent();

        List<Gift> giftList = new ArrayList<>();
        giftList.add(new Gift("Parti", 100, R.drawable.party_gif,R.raw.technodiscoparty ,Gift.GiftType.GIF));
        giftList.add(new Gift("Pizza", 150, R.drawable.pizzagift,R.raw.pizzasound ,Gift.GiftType.GIF));
        giftList.add(new Gift("Unicorn", 200, R.drawable.unicorngift,R.raw.magicwand ,Gift.GiftType.GIF));
        giftList.add(new Gift("Gül", 50, R.drawable.rosegift, Gift.GiftType.PNG));
        giftList.add(new Gift("Kedi", 300, R.drawable.smillingcatgift, Gift.GiftType.PNG));
        giftList.add(new Gift("Dondurma", 300, R.drawable.icecreamgift, Gift.GiftType.PNG));
        giftList.add(new Gift("Milkshake", 300, R.drawable.milkshakegift, Gift.GiftType.PNG));
        giftList.add(new Gift("Kiraz", 300, R.drawable.cherrygift, Gift.GiftType.PNG));
        giftList.add(new Gift("Cookie", 300, R.drawable.cookiegift, Gift.GiftType.PNG));
        giftList.add(new Gift("Elmas", 300, R.drawable.diamondgift, Gift.GiftType.PNG));

        btnCloseGifts.setOnClickListener(v -> {
            Log.d("GiftContainer", "Close button clicked");
            hideGiftContainer();
        });


        // CALLBACK'İ DOĞRU TANIMLA
        // Adapter'ı oluştur - ANONYMOUS INNER CLASS KULLAN
        GiftAdapter giftAdapter = new GiftAdapter(giftList, new GiftAdapter.OnGiftClickListener() {
            @Override
            public void onGiftClick(Gift gift) {
                Log.d("ViewerActivity", "=== CALLBACK RECEIVED ===");
                Log.d("ViewerActivity", "Gift received: " + gift.getName());
                Log.d("ViewerActivity", "Gift cost: " + gift.getCreditCost());

                // DOĞRUDAN onGiftSelected'ı çağır
                Log.d("ViewerActivity", "Calling onGiftSelected...");
                onGiftSelected(gift);
                Log.d("ViewerActivity", "=== CALLBACK PROCESSED ===");
            }
        });

        Log.d("ViewerActivity", "GiftAdapter created successfully");

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        recyclerViewGifts.setLayoutManager(gridLayoutManager);

        recyclerViewGifts.addItemDecoration(new GridSpacingItemDecoration(3,
                dpToPx(8), // 8dp spacing
                true));

        recyclerViewGifts.setAdapter(giftAdapter);
        Log.d("ViewerActivity", "GiftAdapter set to RecyclerView");

        listenForGifts(roomID);

        if (isHost) {
            exitStream.setVisibility(View.GONE);
            messageStream.setVisibility(View.GONE);
            giftStream.setVisibility(View.GONE);
        } else {
            exitStream1.setVisibility(View.GONE);
        }

        exitStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        exitStream1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isHost) {
                    DatabaseReference publisherRef = FirebaseDatabase.getInstance().getReference("Publishers").child(roomID);
                    publisherRef.removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(ViewerActivity.this, "Yayın başarıyla sonlandırıldı.", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(ViewerActivity.this, "Yayın sonlandırılamadı: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    finish();
                }
            }
        });

        messageStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardMessageBox.setVisibility(View.VISIBLE);
            }
        });

        giftStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("GiftStream", "Gift stream button clicked");

                view.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                        .withEndAction(() -> view.animate().scaleX(1f).scaleY(1f).setDuration(100));

                // Toggle gift container visibility
                if (isGiftContainerVisible) {
                    Log.d("GiftStream", "Hiding gift container");
                    hideGiftContainer();
                } else {
                    Log.d("GiftStream", "Showing gift container");
                    showGiftContainer();
                }
            }
        });


        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
        DatabaseReference reference2 = database2.getReference("Publishers").child(roomID).child("message");

        btnSendMessageLiveStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = edtMessageBox.getText().toString().trim();

                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                String userId = currentUser.getUid();

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference reference = database.getReference("Kullanıcılar").child(userId);

                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userPhoto = snapshot.child("profileImage").getValue(String.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


                if (!message.isEmpty()) {
                    reference2.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            int nextMessageIndex = (int) dataSnapshot.getChildrenCount() + 1;
                            String messageId = String.valueOf(nextMessageIndex);

                            Map<String, Object> messageMap = new HashMap<>();
                            messageMap.put("userPhoto", userPhoto);
                            messageMap.put("userMessage", message);
                            messageMap.put("userName", userName);

                            reference2.child(messageId).setValue(messageMap);
                            edtMessageBox.setText("");
                            //cardMessageBox.setVisibility(View.GONE);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }
        });


        startListenEvent();

        ZegoUser user = new ZegoUser(userID, userName);
        ZegoRoomConfig roomConfig = new ZegoRoomConfig();
        roomConfig.isUserStatusNotify = true;
        ZegoExpressEngine.getEngine().loginRoom(roomID, user, roomConfig, new IZegoRoomLoginCallback() {
            @Override
            public void onRoomLoginResult(int errorCode, JSONObject extendedData) {
                if (errorCode == 0) {
                    Toast.makeText(ViewerActivity.this, "Odaya giriş başarılı", Toast.LENGTH_SHORT).show();

                    if (isHost) {
                        startPreview();
                        ZegoExpressEngine.getEngine().startPublishingStream(roomID + "_" + userID + "_call");
                    }
                } else {
                    Toast.makeText(ViewerActivity.this, "Odaya giriş başarısız: " + errorCode, Toast.LENGTH_SHORT).show();
                }
            }
        });


        FirebaseDatabase database3 = FirebaseDatabase.getInstance();
        DatabaseReference reference3 = database3.getReference("Publishers").child(roomID);

        reference3.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String streamerSurname = snapshot.child("userName").getValue(String.class);
                String streamerPhoto = snapshot.child("streamerPhoto").getValue(String.class);

                txtStreamerSurname.setText(streamerSurname);
                GlideUtil.loadOriginalImage(getApplicationContext(), streamerPhoto, streamerPhoto10);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Publishers").child(roomID).child("message");
        FirebaseRecyclerOptions<LiveStreamChat> options = new FirebaseRecyclerOptions.Builder<LiveStreamChat>()
                .setQuery(reference, LiveStreamChat.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<LiveStreamChat, LiveStreamMessageViewHolder>(options) {
            @Override
            protected void onBindViewHolder(LiveStreamMessageViewHolder viewHolder, int position, LiveStreamChat model) {
                viewHolder.liveStreamUserMessage.setText(model.getUserMessage());
                viewHolder.liveStreamUserName.setText(model.getUserName());

                GlideUtil.loadOriginalImage(getApplicationContext(), model.getUserPhoto(), viewHolder.imageUserLiveStream);

                String currentUserChatId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                FirebaseDatabase liveSteamDB = FirebaseDatabase.getInstance();
                DatabaseReference referenceLiveStream = liveSteamDB.getReference("Kullanıcılar").child(currentUserChatId);

                referenceLiveStream.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String frame = snapshot.child("frame").getValue(String.class);

                        if (!frame.equals("")) {
                            if (frame.equals("Melek")) {
                                viewHolder.imgUserLiveStreamFrame.setImageResource(R.drawable.melek_cercevesi);
                            } else if (frame.equals("Goril")) {
                                viewHolder.imgUserLiveStreamFrame.setImageResource(R.drawable.goril_cercevesi);
                            } else if (frame.equals("Aslan")) {
                                viewHolder.imgUserLiveStreamFrame.setImageResource(R.drawable.aslan_cercevesi);
                            } else if (frame.equals("AskoKusko")) {
                                viewHolder.imgUserLiveStreamFrame.setImageResource(R.drawable.askokusko_cercevesi);
                            } else if (frame.equals("Elmas")) {
                                viewHolder.imgUserLiveStreamFrame.setImageResource(R.drawable.elmas_cercevesi);
                            } else if (frame.equals("Sarmasık")) {
                                viewHolder.imgUserLiveStreamFrame.setImageResource(R.drawable.sarmasik_cercevesi);
                            } else if (frame.equals("Hilal")) {
                                viewHolder.imgUserLiveStreamFrame.setImageResource(R.drawable.hilalvekilic);
                            } else if (frame.equals("Kelebek")) {
                                viewHolder.imgUserLiveStreamFrame.setImageResource(R.drawable.kelecekcercevesi);
                            } else if (frame.equals("Miğfer")) {
                                viewHolder.imgUserLiveStreamFrame.setImageResource(R.drawable.migfer_cercevesi);
                            } else if (frame.equals("Ateş")) {
                                viewHolder.imgUserLiveStreamFrame.setImageResource(R.drawable.ates_cercevesi);
                            }
                        } else {
                            viewHolder.imgUserLiveStreamFrame.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public LiveStreamMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.live_streaming_chat_rec_item, parent, false);
                return new LiveStreamMessageViewHolder(itemView);
            }
        };
        adapter.startListening();
        recyclerLiveStreamChat.setAdapter(adapter);

        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                if (recyclerLiveStreamChat.getAdapter().getItemCount() > 6) {
                    recyclerLiveStreamChat.smoothScrollToPosition(recyclerLiveStreamChat.getAdapter().getItemCount() - 1);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        if (adapter.getItemCount() > 0) {
            recyclerLiveStreamChat.smoothScrollToPosition(adapter.getItemCount() - 1);
        }


        checkFirebaseConnection();

        setupSimpleKeyboardHandling();
        setupEditTextListeners();

        btnSendFollowReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference userRef = database.getReference("Kullanıcılar").child(myUid);

                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String name = snapshot.child("ad").getValue(String.class);
                            String surname = snapshot.child("soyad").getValue(String.class);
                            String username = snapshot.child("kullanıcıAdı").getValue(String.class);
                            String profilePhoto = snapshot.child("profileImage").getValue(String.class);

                            // Arkadaşlık isteği objesi oluştur
                            HashMap<String, Object> requestMap = new HashMap<>();
                            requestMap.put("uid", myUid);
                            requestMap.put("name", name);
                            requestMap.put("surname", surname);
                            requestMap.put("username", username);
                            requestMap.put("profileImage", profilePhoto);
                            requestMap.put("status", "pending"); // İstek durumu

                            // İlgili kişinin uid'si altında "FriendRequests" düğümüne ekle
                            DatabaseReference requestRef = database.getReference("Kullanıcılar")
                                    .child(streamerUid) // İstek gönderilen kişinin UID'si
                                    .child("FriendRequests")
                                    .child(myUid); // Gönderenin UID'si

                            requestRef.setValue(requestMap).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(view.getContext(), "Takip isteği gönderildi!", Toast.LENGTH_SHORT).show();
                                    sendNotification(streamerUid,name);
                                } else {
                                    Toast.makeText(view.getContext(), "İstek gönderilemedi!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(view.getContext(), "Veri okunurken hata oluştu!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    private void sendNotification(String receiver_uid, String receiver_name) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens").child(receiver_uid).child("token");


        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userToken = snapshot.getValue(String.class);
                String title = "Arkadaşlık İsteği";
                String message = receiver_name + ", sana arkadaşlık isteği gönderdi";

                FCMNotificationSender notificationSender = new FCMNotificationSender(userToken, title, message, getApplicationContext());
                notificationSender.sendNotification();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void showGiftContainer() {
        if (giftContainer != null) {
            giftContainer.setVisibility(View.VISIBLE);
            isGiftContainerVisible = true;

            // Smooth slide up animation
            giftContainer.setTranslationY(giftContainer.getHeight());
            giftContainer.animate()
                    .translationY(0)
                    .setDuration(300)
                    .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
                    .start();

            Log.d("GiftContainer", "Gift container shown");
        }
    }

    // YENİ - Gift container'ı gizle
    private void hideGiftContainer() {
        if (giftContainer != null && isGiftContainerVisible) {
            // Smooth slide down animation
            giftContainer.animate()
                    .translationY(giftContainer.getHeight())
                    .setDuration(300)
                    .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
                    .withEndAction(() -> {
                        giftContainer.setVisibility(View.GONE);
                        isGiftContainerVisible = false;

                        // Selection'ı reset et
                        GiftAdapter currentAdapter = (GiftAdapter) recyclerViewGifts.getAdapter();
                        if (currentAdapter != null) {
                            currentAdapter.resetSelection();
                        }

                        Log.d("GiftContainer", "Gift container hidden");
                    })
                    .start();
        }
    }

    // GridSpacingItemDecoration class'ını ViewerActivity class'ının içine ekle
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(android.graphics.Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int column = position % spanCount;

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount;
                outRect.right = (column + 1) * spacing / spanCount;

                if (position < spanCount) {
                    outRect.top = spacing;
                }
                outRect.bottom = spacing;
            } else {
                outRect.left = column * spacing / spanCount;
                outRect.right = spacing - (column + 1) * spacing / spanCount;
                if (position >= spanCount) {
                    outRect.top = spacing;
                }
            }
        }
    }

    // DP to PX converter method (ViewerActivity class'ının içine ekle)
    private int dpToPx(int dp) {
        float density = getApplicationContext().getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    // Yayıncının UID'sini almak için method
    private void getStreamerUid() {
        DatabaseReference publisherRef = FirebaseDatabase.getInstance().getReference()
                .child("Publishers").child(roomID);

        publisherRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    streamerUid = snapshot.child("userId").getValue(String.class);
                    if (streamerUid == null) {
                        streamerUid = snapshot.child("userId").getValue(String.class); // Alternatif field adı
                    }
                    Log.d("StreamerUID", "Streamer UID: " + streamerUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("StreamerUID", "Error getting streamer UID", error.toException());
            }
        });
    }


    // GÜNCELLE - onGiftSelected metodunu değiştir
    private void onGiftSelected(Gift gift) {
        Log.d("GiftSelection", "=== onGiftSelected METHOD CALLED ===");
        Log.d("GiftSelection", "Gift: " + gift.getName());
        Log.d("GiftSelection", "Cost: " + gift.getCreditCost());

        try {
            String selectedGiftName = gift.getName();
            int selectedGiftCredit = gift.getCreditCost();

            // ARTIK CONTAINER'I KAPATMIYORUZ - Kullanıcı deneyimi için açık kalıyor
            // hideGiftContainer(); // Bu satırı kaldırdık

            // Seçimi reset et (visual feedback için)
            GiftAdapter currentAdapter = (GiftAdapter) recyclerViewGifts.getAdapter();
            if (currentAdapter != null) {
                currentAdapter.resetSelection();
            }

            // Kullanıcı kontrolü
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Log.e("GiftSelection", "Current user is null");
                Toast.makeText(this, "Lütfen giriş yapın!", Toast.LENGTH_SHORT).show();
                return;
            }

            // RoomID kontrolü
            if (roomID == null || roomID.isEmpty()) {
                Log.e("GiftSelection", "roomID is null or empty");
                Toast.makeText(this, "Oda bilgisi bulunamadı!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Host kontrolü
            if (isHost) {
                Log.w("GiftSelection", "Host cannot send gifts");
                Toast.makeText(this, "Ev sahibi hediye gönderemez!", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("GiftSelection", "All checks passed, showing toast and calling sendGift");

            // Success toast with better styling
            showGiftSuccessToast(selectedGiftName, selectedGiftCredit);

            // SendGift metodunu çağır
            Log.d("GiftSelection", "Calling sendGift...");
            sendGift(roomID, selectedGiftName, selectedGiftCredit);

        } catch (Exception e) {
            Log.e("GiftSelection", "Error in onGiftSelected", e);
            Toast.makeText(this, "Hediye gönderilirken hata oluştu!", Toast.LENGTH_SHORT).show();
        }

        Log.d("GiftSelection", "=== onGiftSelected METHOD COMPLETED ===");
    }
    // YENİ - Custom success toast
    private void showGiftSuccessToast(String giftName, int cost) {
        // Custom toast layout oluştur (isteğe bağlı)
        String message = "🎁 " + giftName + " gönderildi! (-" + cost + " kredi)";
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);

        // Toast position'ı ayarla (hediye container'ının üstünde göster)
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 300);
        toast.show();

        Log.d("GiftToast", "Success toast shown: " + message);
    }

    void startListenEvent() {
        ZegoExpressEngine.getEngine().setEventHandler(new IZegoEventHandler() {
            @Override
            public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList, JSONObject extendedData) {
                super.onRoomStreamUpdate(roomID, updateType, streamList, extendedData);
                if (updateType == ZegoUpdateType.ADD) {
                    startPlayStream(streamList.get(0).streamID);
                } else {
                    stopPlayStream(streamList.get(0).streamID);
                }
            }

            @Override
            public void onRoomUserUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoUser> userList) {
                super.onRoomUserUpdate(roomID, updateType, userList);
                if (updateType == ZegoUpdateType.ADD) {
                    for (ZegoUser user : userList) {
                        String text = user.userID + " Yayına katıldı";
                        Toast.makeText(ViewerActivity.this, text, Toast.LENGTH_SHORT).show();
                    }
                } else if (updateType == ZegoUpdateType.DELETE) {
                    for (ZegoUser user : userList) {
                        String text = user.userID + " Yayından ayrıldı.";
                        Toast.makeText(ViewerActivity.this, text, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    void startPlayStream(String streamID) {
        findViewById(R.id.hostView).setVisibility(View.VISIBLE);
        ZegoCanvas canvas = new ZegoCanvas(findViewById(R.id.hostView));
        ZegoPlayerConfig config = new ZegoPlayerConfig();
        config.resourceMode = ZegoStreamResourceMode.DEFAULT;
        ZegoExpressEngine.getEngine().startPlayingStream(streamID, canvas, config);
    }

    void stopPlayStream(String streamID) {
        ZegoExpressEngine.getEngine().stopPlayingStream(streamID);
        findViewById(R.id.hostView).setVisibility(View.GONE);
    }

    void startPreview() {
        ZegoCanvas canvas = new ZegoCanvas(findViewById(R.id.hostView));
        ZegoExpressEngine.getEngine().startPreview(canvas);
    }

    void stopPreview() {
        ZegoExpressEngine.getEngine().stopPreview();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isFinishing()) {
            stopListenEvent();
            ZegoExpressEngine.getEngine().logoutRoom();
        }
    }

    void stopListenEvent() {
        ZegoExpressEngine.getEngine().setEventHandler(null);
    }

    // Güncellenmiş sendGift metodu (kredi ekleme ile)
    private void sendGift(String streamId, String giftType, int giftCost) {
        Log.d("SendGift", "sendGift method started");
        Log.d("SendGift", "Parameters - StreamID: " + streamId + ", GiftType: " + giftType + ", GiftCost: " + giftCost);

        // Null kontrolü
        if (streamId == null || streamId.isEmpty()) {
            Log.e("SendGift", "StreamID is null or empty");
            Toast.makeText(this, "Oda bilgisi bulunamadı!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e("SendGift", "Current user is null");
            Toast.makeText(this, "Lütfen giriş yapın!", Toast.LENGTH_SHORT).show();
            return;
        }

        String senderUid = currentUser.getUid();
        Log.d("SendGift", "Sender UID: " + senderUid);

        DatabaseReference userCreditRef = FirebaseDatabase.getInstance().getReference()
                .child("Kullanıcılar").child(senderUid).child("credit");

        Log.d("SendGift", "Checking user credit...");

        userCreditRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("SendGift", "Credit check - snapshot exists: " + snapshot.exists());

                if (snapshot.exists()) {
                    Object creditValue = snapshot.getValue();
                    Log.d("SendGift", "Credit value type: " + (creditValue != null ? creditValue.getClass().getSimpleName() : "null"));
                    Log.d("SendGift", "Credit value: " + creditValue);

                    int currentCredit = 0;
                    if (creditValue instanceof Integer) {
                        currentCredit = (Integer) creditValue;
                    } else if (creditValue instanceof Long) {
                        currentCredit = ((Long) creditValue).intValue();
                    } else if (creditValue instanceof String) {
                        try {
                            currentCredit = Integer.parseInt((String) creditValue);
                        } catch (NumberFormatException e) {
                            Log.e("SendGift", "Could not parse credit value", e);
                            Toast.makeText(ViewerActivity.this, "Kredi bilgisi alınamadı!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    Log.d("SendGift", "Current credit: " + currentCredit + ", Required: " + giftCost);

                    if (currentCredit >= giftCost) {
                        Log.d("SendGift", "Credit sufficient, processing gift...");

                        // Gönderenin kredisini azalt
                        userCreditRef.setValue(currentCredit - giftCost)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("SendGift", "User credit updated successfully");

                                    // Yayıncının kredisini artır
                                    increaseStreamerCredit(giftCost);

                                    // Hediyeyi gönder
                                    sendGiftToFirebase(streamId, senderUid, giftType, giftCost);

                                })
                                .addOnFailureListener(e -> {
                                    Log.e("SendGift", "Failed to update user credit", e);
                                    Toast.makeText(ViewerActivity.this, "Kredi güncellenemedi!", Toast.LENGTH_SHORT).show();
                                });

                    } else {
                        Log.d("SendGift", "Insufficient credit");
                        Toast.makeText(ViewerActivity.this, "Yetersiz kredi! Mevcut: " + currentCredit + ", Gerekli: " + giftCost, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("SendGift", "Credit data doesn't exist, assuming 0 credit");
                    Toast.makeText(ViewerActivity.this, "Kredi bilgisi bulunamadı!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("SendGift", "Error checking user credit", error.toException());
                Toast.makeText(ViewerActivity.this, "Kredi kontrolü başarısız!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 3. Yeni method - hediyeyi Firebase'e gönder
    private void sendGiftToFirebase(String streamId, String senderUid, String giftType, int giftCost) {
        DatabaseReference giftRef = FirebaseDatabase.getInstance().getReference()
                .child("Publishers").child(streamId).child("liveGifts").push();

        Map<String, Object> giftData = new HashMap<>();
        giftData.put("senderUid", senderUid);
        giftData.put("giftType", giftType);
        giftData.put("giftCost", giftCost);
        giftData.put("timestamp", ServerValue.TIMESTAMP);

        Log.d("SendGift", "Sending gift data to Firebase: " + giftData.toString());

        giftRef.setValue(giftData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("SendGift", "Gift sent successfully to Firebase");

                    // Gift totals'ı güncelle
                    updateGiftTotals(streamId, senderUid, giftCost);

                    // Container'ı 2 saniye sonra otomatik kapat (isteğe bağlı)
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (isGiftContainerVisible) {
                            hideGiftContainer();
                        }
                    }, 2000); // 2 saniye bekle

                })
                .addOnFailureListener(e -> {
                    Log.e("SendGift", "Failed to send gift to Firebase", e);
                    Toast.makeText(this, "Hediye gönderilemedi: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    // Hata durumunda krediyi geri ver
                    restoreUserCredit(senderUid, giftCost);
                });
    }
    private void restoreUserCredit(String userId, int amount) {
        DatabaseReference userCreditRef = FirebaseDatabase.getInstance().getReference()
                .child("Kullanıcılar").child(userId).child("credit");

        userCreditRef.runTransaction(new com.google.firebase.database.Transaction.Handler() {
            @Override
            public com.google.firebase.database.Transaction.Result doTransaction(com.google.firebase.database.MutableData mutableData) {
                Integer currentCredit = mutableData.getValue(Integer.class);
                if (currentCredit == null) {
                    mutableData.setValue(amount);
                } else {
                    mutableData.setValue(currentCredit + amount);
                }
                return com.google.firebase.database.Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (databaseError == null) {
                    Log.d("RestoreCredit", "User credit restored: " + amount);
                } else {
                    Log.e("RestoreCredit", "Failed to restore credit", databaseError.toException());
                }
            }
        });
    }

    // Yayıncının kredisini artıran method
    private void increaseStreamerCredit(int amount) {
        if (streamerUid == null || streamerUid.isEmpty()) {
            Log.e("StreamerCredit", "Streamer UID is empty");
            return;
        }

        DatabaseReference streamerCreditRef = FirebaseDatabase.getInstance().getReference()
                .child("Kullanıcılar").child(streamerUid).child("credit");

        streamerCreditRef.runTransaction(new com.google.firebase.database.Transaction.Handler() {
            @Override
            public com.google.firebase.database.Transaction.Result doTransaction(com.google.firebase.database.MutableData mutableData) {
                Integer currentCredit = mutableData.getValue(Integer.class);
                if (currentCredit == null) {
                    mutableData.setValue(amount);
                } else {
                    mutableData.setValue(currentCredit + amount);
                }
                return com.google.firebase.database.Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    Log.e("StreamerCredit", "Transaction failed", databaseError.toException());
                } else {
                    Log.d("StreamerCredit", "Streamer credit increased by: " + amount);
                }
            }
        });
    }

    // Update gift totals in Firebase
    private void updateGiftTotals(String streamId, String uid, int giftCost) {
        DatabaseReference giftTotalRef = FirebaseDatabase.getInstance().getReference()
                .child("Publishers").child(streamId).child("giftTotals").child(uid);

        giftTotalRef.runTransaction(new com.google.firebase.database.Transaction.Handler() {
            @Override
            public com.google.firebase.database.Transaction.Result doTransaction(com.google.firebase.database.MutableData mutableData) {
                Integer currentTotal = mutableData.getValue(Integer.class);
                if (currentTotal == null) {
                    mutableData.setValue(giftCost);
                } else {
                    mutableData.setValue(currentTotal + giftCost);
                }
                return com.google.firebase.database.Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    Log.e("GiftTotals", "Transaction failed", databaseError.toException());
                } else {
                    Log.d("GiftTotals", "Gift totals updated successfully");
                }
            }
        });
    }

    // Güncellenmiş listenForGifts metodu (ses ile)
    private void listenForGifts(String streamId) {
        Log.d("ListenForGifts", "Starting to listen for gifts in stream: " + streamId);

        giftsRef = FirebaseDatabase.getInstance().getReference()
                .child("Publishers").child(streamId).child("liveGifts");

        giftsChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Activity destroyed kontrolü ekleyin
                if (isActivityDestroyed || isFinishing()) {
                    Log.d("ListenForGifts", "Activity is destroyed, ignoring gift");
                    return;
                }

                Log.d("ListenForGifts", "New gift received: " + snapshot.getKey());

                String giftType = snapshot.child("giftType").getValue(String.class);
                String senderUid = snapshot.child("senderUid").getValue(String.class);
                String giftKey = snapshot.getKey();

                Log.d("ListenForGifts", "Gift details - Type: " + giftType + ", Sender: " + senderUid);

                if (giftType != null) {
                    // Ses çal
                    playGiftSound(giftType);

                    // Animasyon göster
                    showGiftAnimation(giftType, () -> {
                        // Animasyon bitince gift'i sil - Activity kontrolü ile
                        if (!isActivityDestroyed && !isFinishing() && giftsRef != null) {
                            giftsRef.child(giftKey).removeValue()
                                    .addOnSuccessListener(aVoid -> Log.d("ListenForGifts", "Gift removed from Firebase"))
                                    .addOnFailureListener(e -> Log.e("ListenForGifts", "Failed to remove gift", e));
                        }
                    });
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Log.d("ListenForGifts", "Gift removed: " + snapshot.getKey());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ListenForGifts", "Error listening for gifts", error.toException());
            }
        };

        giftsRef.addChildEventListener(giftsChildEventListener);
    }

    private void playGiftSound(String giftType) {
        if (isActivityDestroyed || isFinishing()) {
            Log.d("GiftSound", "Activity is destroyed, skipping sound");
            return;
        }

        try {
            // Önceki ses varsa durdur
            if (giftSoundPlayer != null) {
                giftSoundPlayer.stop();
                giftSoundPlayer.release();
                giftSoundPlayer = null;
            }

            int soundResId = getSoundResourceByName(giftType);
            if (soundResId != 0) {
                giftSoundPlayer = MediaPlayer.create(this, soundResId);
                if (giftSoundPlayer != null) {
                    giftSoundPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    giftSoundPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mp.release();
                            giftSoundPlayer = null;
                        }
                    });
                    giftSoundPlayer.start();
                    Log.d("GiftSound", "Playing sound for gift: " + giftType);
                }
            }
        } catch (Exception e) {
            Log.e("GiftSound", "Error playing gift sound", e);
        }
    }

    // Gift type'a göre ses resource ID'si döndüren method
    private int getSoundResourceByName(String name) {
        switch (name.toLowerCase()) {
            case "parti":
                return R.raw.technodiscoparty;
            case "pizza":
                return R.raw.pizzasound;
            case "unicorn":
                return R.raw.magicwand;
            case "gül":
                return 0;
            case "kedi":
                return 0;
            case "dondurma":
                return 0;
            case "milkshake":
                return 0;
            case "cookie":
                return 0;
            case "kiraz":
                return 0;
            case "elmas":
                return 0;
            default:
                return 0; // Ses yok
        }
    }

    private void checkFirebaseConnection() {
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                Log.d("FirebaseConnection", "Firebase connected: " + connected);
                if (!connected) {
                    Toast.makeText(ViewerActivity.this, "İnternet bağlantısı zayıf!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseConnection", "Connection check failed", error.toException());
            }
        });
    }
    private void showGiftAnimation(String giftType, Runnable onAnimationEndCallback) {
        // Activity destroyed kontrolü
        if (isActivityDestroyed || isFinishing()) {
            Log.d("GiftAnimation", "Activity is destroyed, skipping animation");
            if (onAnimationEndCallback != null) {
                onAnimationEndCallback.run();
            }
            return;
        }

        ImageView giftImageView = findViewById(R.id.giftImageView);

        if (giftImageView == null) {
            Log.e("GiftAnimation", "giftImageView is null");
            if (onAnimationEndCallback != null) {
                onAnimationEndCallback.run();
            }
            return;
        }

        // Gift type'a göre resource ID'yi al
        int resourceId = getResourceIdByName(giftType);
        Gift.GiftType type = getGiftTypeByName(giftType);

        try {
            // Glide ile güvenli yükleme
            if (type == Gift.GiftType.GIF) {
                // GIF animasyon
                Glide.with(this)
                        .asGif()
                        .load(resourceId)
                        .into(giftImageView);
            } else {
                // PNG static görüntü
                Glide.with(this)
                        .load(resourceId)
                        .into(giftImageView);
            }

            giftImageView.setVisibility(View.VISIBLE);

            // Gift container'ı gizle, animation için hazırla
            if (giftContainer != null && isGiftContainerVisible) {
                hideGiftContainer();
            }

            // Animasyonu 3 saniye göster
            giftImageView.postDelayed(() -> {
                // Activity hala canlı mı kontrol et
                if (!isActivityDestroyed && !isFinishing()) {
                    giftImageView.setVisibility(View.GONE);

                    // Chat'i tekrar göster
                    if (recyclerLiveStreamChat != null) {
                        recyclerLiveStreamChat.setVisibility(View.VISIBLE);
                    }

                    // Animasyon bittiğinde sesi durdur
                    if (giftSoundPlayer != null && giftSoundPlayer.isPlaying()) {
                        giftSoundPlayer.stop();
                        giftSoundPlayer.release();
                        giftSoundPlayer = null;
                        Log.d("GiftSound", "Gift sound stopped when animation ended");
                    }

                    if (onAnimationEndCallback != null) {
                        onAnimationEndCallback.run();
                    }
                }
            }, 3000); // 3 saniye

        } catch (Exception e) {
            Log.e("GiftAnimation", "Error loading gift animation", e);
            giftImageView.setVisibility(View.GONE);

            if (onAnimationEndCallback != null) {
                onAnimationEndCallback.run();
            }
        }
    }

    private int getResourceIdByName(String name) {
        switch (name.toLowerCase()) {
            case "parti":
                return R.drawable.party_gif;
            case "pizza":
                return R.drawable.pizzagift;
            case "unicorn":
                return R.drawable.unicorngift;
            case "gül":
                return R.drawable.rosegift;
            case "kedi":
                return R.drawable.smillingcatgift;
            case "dondurma":
                return R.drawable.icecreamgift;
            case "milkshake":
                return R.drawable.milkshakegift;
            case "cookie":
                return R.drawable.cookiegift;
            case "kiraz":
                return R.drawable.cherrygift;
            case "elmas":
                return R.drawable.diamondgift;
            default:
                return R.drawable.party_gif;
        }
    }

    private Gift.GiftType getGiftTypeByName(String name) {
        switch (name.toLowerCase()) {
            case "parti":
            case "unicorn":
            case "pizza":
                return Gift.GiftType.GIF;
            case "dondurma":
            case "cookie":
            case "gül":
            case "milkshake":
            case "kedi":
            case "kiraz":
            case "elmas":
                return Gift.GiftType.PNG;
            default:
                return Gift.GiftType.GIF;
        }
    }

    private int getAnimationResIdByName(String name) {
        switch (name.toLowerCase()) {
            case "love":
                return R.raw.love_anim;
            case "red car":
                return R.raw.red_card_anim;
            case "butterfly":
                return R.raw.butterfly_anim;
            default:
                return R.raw.love_anim;
        }
    }

    // Initialize viewer count tracking
    private void initializeViewerCount() {
        DatabaseReference viewerCountRef = FirebaseDatabase.getInstance().getReference()
                .child("Publishers").child(roomID).child("viewerCount");

        viewerCountRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentViewerCount = snapshot.getValue(Integer.class);
                } else {
                    currentViewerCount = 0;
                }
                updateViewerCountUI();
                Log.d("ViewerCount", "Current viewer count: " + currentViewerCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ViewerCount", "Error listening to viewer count", error.toException());
            }
        });
    }

    // Update viewer count in Firebase
    private void updateViewerCount(int change) {
        DatabaseReference viewerCountRef = FirebaseDatabase.getInstance().getReference()
                .child("Publishers").child(roomID).child("viewerCount");

        viewerCountRef.runTransaction(new com.google.firebase.database.Transaction.Handler() {
            @Override
            public com.google.firebase.database.Transaction.Result doTransaction(com.google.firebase.database.MutableData mutableData) {
                Integer currentCount = mutableData.getValue(Integer.class);
                if (currentCount == null) {
                    mutableData.setValue(Math.max(0, change));
                } else {
                    mutableData.setValue(Math.max(0, currentCount + change));
                }
                return com.google.firebase.database.Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    Log.e("ViewerCount", "Transaction failed", databaseError.toException());
                } else {
                    Log.d("ViewerCount", "Viewer count updated successfully");
                }
            }
        });
    }

    // Update viewer count UI
    private void updateViewerCountUI() {
        if (txtViewerLiveStream != null) {
            runOnUiThread(() -> {
                txtViewerLiveStream.setText(String.valueOf(currentViewerCount));
            });
        }
    }

    // Initialize top gifters tracking
    private void initializeTopGiftersTracking() {
        DatabaseReference giftTotalsRef = FirebaseDatabase.getInstance().getReference()
                .child("Publishers").child(roomID).child("giftTotals");

        giftTotalsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userGiftTotals.clear();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String uid = userSnapshot.getKey();
                    Integer total = userSnapshot.getValue(Integer.class);
                    if (total != null) {
                        userGiftTotals.put(uid, total);
                    }
                }

                updateTopGifters();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TopGifters", "Error listening to gift totals", error.toException());
            }
        });
    }

    // Update top gifters and competition bar
    private void updateTopGifters() {
        if (userGiftTotals.isEmpty()) {
            hideTopGiftersUI();
            return;
        }

        // Sort users by gift totals
        List<Map.Entry<String, Integer>> sortedGifters = new ArrayList<>(userGiftTotals.entrySet());
        Collections.sort(sortedGifters, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue()); // Descending order
            }
        });

        // Reset values
        firstGifterUid = "";
        secondGifterUid = "";
        firstGifterTotal = 0;
        secondGifterTotal = 0;

        // Get top 2 gifters
        if (sortedGifters.size() >= 1) {
            Map.Entry<String, Integer> firstGifter = sortedGifters.get(0);
            firstGifterUid = firstGifter.getKey();
            firstGifterTotal = firstGifter.getValue();
        }

        if (sortedGifters.size() >= 2) {
            Map.Entry<String, Integer> secondGifter = sortedGifters.get(1);
            secondGifterUid = secondGifter.getKey();
            secondGifterTotal = secondGifter.getValue();
        }

        // Load and display top gifters
        if (!firstGifterUid.isEmpty()) {
            loadTopGifterInfo(firstGifterUid, profileImageFirstGifter, imgFrameFirstGifter, true);
            // Her durumda progress bar'ı güncelle (tek kullanıcı olsa bile)
            updateCompetitionProgressBar();
        }

        if (!secondGifterUid.isEmpty()) {
            loadTopGifterInfo(secondGifterUid, profileImageSecondGifter, imgFrameSecondGifter, false);
        }

        showTopGiftersUI();
        Log.d("TopGifters", "First: " + firstGifterTotal + ", Second: " + secondGifterTotal);
    }

    // Load top gifter information and display
    private void loadTopGifterInfo(String uid, CircleImageView profileImage, ImageView frameImage, boolean isFirst) {
        if (uid.isEmpty()) return;

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child("Kullanıcılar").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String photoBase64 = snapshot.child("profileImage").getValue(String.class);
                    String frame = snapshot.child("frame").getValue(String.class);

                    // Set profile image
                    if (photoBase64 != null && !photoBase64.isEmpty()) {
                        GlideUtil.loadOriginalImage(getApplicationContext(), photoBase64, profileImage);
                    }

                    // Set frame
                    if (frame != null && !frame.isEmpty()) {
                        setFrameImage(frameImage, frame);
                    } else {
                        frameImage.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("LoadGifterInfo", "Error loading gifter info", error.toException());
            }
        });
    }

    // Set frame image based on frame name
    private void setFrameImage(ImageView frameImage, String frame) {
        frameImage.setVisibility(View.VISIBLE);
        switch (frame) {
            case "Melek":
                frameImage.setImageResource(R.drawable.melek_cercevesi);
                break;
            case "Goril":
                frameImage.setImageResource(R.drawable.goril_cercevesi);
                break;
            case "Aslan":
                frameImage.setImageResource(R.drawable.aslan_cercevesi);
                break;
            case "AskoKusko":
                frameImage.setImageResource(R.drawable.askokusko_cercevesi);
                break;
            case "Elmas":
                frameImage.setImageResource(R.drawable.elmas_cercevesi);
                break;
            case "Sarmasık":
                frameImage.setImageResource(R.drawable.sarmasik_cercevesi);
                break;
            case "Hilal":
                frameImage.setImageResource(R.drawable.hilalvekilic);
                break;
            case "Kelebek":
                frameImage.setImageResource(R.drawable.kelecekcercevesi);
                break;
            case "Miğfer":
                frameImage.setImageResource(R.drawable.migfer_cercevesi);
                break;
            case "Ateş":
                frameImage.setImageResource(R.drawable.ates_cercevesi);
                break;
            default:
                frameImage.setVisibility(View.GONE);
                break;
        }
    }

    // Update competition progress bar with gradient and percentage
    // Ana Activity'inizde progress bar güncelleme fonksiyonu
    private void updateCompetitionProgressBar() {
        if (giftCountProgressBar == null) return;

        // Eğer özel progress bar kullanıyorsanız
        if (giftCountProgressBar instanceof CustomDualProgressBar) {
            CustomDualProgressBar dualProgressBar = (CustomDualProgressBar) giftCountProgressBar;

            runOnUiThread(() -> {
                if (firstGifterTotal > 0 && secondGifterTotal > 0) {
                    // İki kullanıcı da hediye göndermiş
                    dualProgressBar.setProgress(firstGifterTotal, secondGifterTotal);
                } else if (firstGifterTotal > 0) {
                    // Sadece birinci kullanıcı hediye göndermiş
                    dualProgressBar.setProgress(firstGifterTotal, 0);
                } else {
                    // Hiç hediye gönderilmemiş
                    dualProgressBar.setProgress(0, 0);
                }
            });
        } else {
            // Alternatif çözüm: İki ayrı progress bar kullan
        }

        Log.d("ProgressBar", "First: " + firstGifterTotal + ", Second: " + secondGifterTotal);
    }

    // Show top gifters UI
    private void showTopGiftersUI() {
        runOnUiThread(() -> {
            if (cardProfileImageFirstSecondGifter != null) {
                cardProfileImageFirstSecondGifter.setVisibility(View.VISIBLE);
            }

            // Progress bar her zaman görünsün (tek kullanıcı olsa bile)
            if (giftCountProgressBar != null) {
                giftCountProgressBar.setVisibility(View.VISIBLE);
            }

            // İkinci kullanıcı varsa göster, yoksa gizle
            if (cardProfileImageSecondGifter != null) {
                if (!secondGifterUid.isEmpty() && secondGifterTotal > 0) {
                    cardProfileImageSecondGifter.setVisibility(View.VISIBLE);
                } else {
                    cardProfileImageSecondGifter.setVisibility(View.INVISIBLE); // Yer kaplar ama görünmez
                }
            }
        });
    }

    // Hide top gifters UI
    private void hideTopGiftersUI() {
        if (cardProfileImageFirstSecondGifter != null) {
            cardProfileImageFirstSecondGifter.setVisibility(View.GONE);
        }
        if (cardProfileImageSecondGifter != null) {
            cardProfileImageSecondGifter.setVisibility(View.GONE);
        }
        if (giftCountProgressBar != null) {
            giftCountProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        isActivityDestroyed = true;

        // Firebase listener'ları temizleyin
        if (giftsRef != null && giftsChildEventListener != null) {
            giftsRef.removeEventListener(giftsChildEventListener);
        }

        // Glide request'leri temizleyin
        if (!isFinishing()) {
            Glide.with(this).clear(giftImageView);
        }

        // Ses player'ı temizle
        if (giftSoundPlayer != null) {
            giftSoundPlayer.stop();
            giftSoundPlayer.release();
            giftSoundPlayer = null;
        }

        if (!isHost) {
            updateViewerCount(-1);
        }

        super.onDestroy();
    }


    @Override
    protected void onPause() {
        super.onPause();

        // Activity pause olduğunda sesi durdur
        if (giftSoundPlayer != null && giftSoundPlayer.isPlaying()) {
            try {
                giftSoundPlayer.pause();
            } catch (Exception e) {
                Log.e("GiftSound", "Error pausing sound", e);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityDestroyed = false; // Activity tekrar aktif

        // Activity resume olduğunda sesi devam ettir (isteğe bağlı)
        if (giftSoundPlayer != null && !giftSoundPlayer.isPlaying()) {
            try {
                giftSoundPlayer.start();
            } catch (Exception e) {
                Log.e("GiftSound", "Error resuming sound", e);
            }
        }
    }


    // PROBLEM 1 ÇÖZÜMÜ: Basitleştirilmiş keyboard handling
    private void setupSimpleKeyboardHandling() {
        View decorView = getWindow().getDecorView();

        decorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                decorView.getWindowVisibleDisplayFrame(rect);

                int screenHeight = decorView.getHeight();
                int keypadHeight = screenHeight - rect.bottom;

                if (keypadHeight > screenHeight * 0.15) {
                    // Klavye açık - sadece bottom margin ekle
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) cardMessageBox.getLayoutParams();
                    params.bottomMargin = keypadHeight - 50; // 50dp daha az boşluk için
                    cardMessageBox.setLayoutParams(params);


                } else {
                    // Klavye kapalı
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) cardMessageBox.getLayoutParams();
                    params.bottomMargin = 0;
                    cardMessageBox.setLayoutParams(params);
                }
            }
        });
    }



    // EditText focus change listener ekleyin
    private void setupEditTextListeners() {
        edtMessageBox.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // Focus aldığında kısa bir gecikme ile scroll yap
                }
            }
        });


    }

}