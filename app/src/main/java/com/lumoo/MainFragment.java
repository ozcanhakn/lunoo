package com.lumoo;

import static com.lumoo.R.*;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lumoo.Model.Publisher;
import com.lumoo.Model.StoryMember;
import com.lumoo.ViewHolder.PublisherAdapter;
import com.lumoo.ViewHolder.StoryViewHolder;
import com.lumoo.ViewHolder.TabPagerAdapter;
import com.lumoo.ViewHolder.TabPagerMainAdapter;
import com.lumoo.util.GlideUtil;
import com.lumoo.util.ImageUtils;

import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.annotations.Nullable;

public class MainFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final org.apache.commons.logging.Log log = LogFactory.getLog(MainFragment.class);

    // Maksimum bitmap boyutları (Android'in sınırları içinde)
    private static final int MAX_BITMAP_WIDTH = 2048;
    private static final int MAX_BITMAP_HEIGHT = 2048;
    private static final int MAX_BITMAP_SIZE = MAX_BITMAP_WIDTH * MAX_BITMAP_HEIGHT * 4; // 4 bytes per pixel (ARGB)

    private TabLayout tabLayout;
    ViewPager2 viewPager;
    RecyclerView stories_recycler;
    RelativeLayout relativeStoryMain;
    CircleImageView imageStory;

    // Custom adapter for grouped stories
    private GroupedStoryAdapter groupedStoryAdapter;
    private List<GroupedStory> groupedStoriesList;

    private static final int PICK_IMAGE = 1;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        groupedStoriesList = new ArrayList<>();
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        viewPager = view.findViewById(R.id.pagerMain);
        tabLayout = view.findViewById(R.id.tabLayoutMain);

        relativeStoryMain = view.findViewById(R.id.relativeStoryMain);
        relativeStoryMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), StoryActivity.class);
                startActivity(intent);
            }
        });

        imageStory = view.findViewById(R.id.imageStory);

        // Profil resmini yükle
        loadProfileImage();

        // Stories RecyclerView'ı hazırla
        setupStoriesRecyclerView(view);

        // ViewPager'ı ayarla
        setupViewPager();

        return view;
    }

    private void loadProfileImage() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || !isAdded() || getActivity() == null) return;

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Kullanıcılar").child(currentUser.getUid());

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                // Fragment hala attached mi kontrol et
                if (!isAdded() || getActivity() == null) return;

                String url = snapshot.child("profileImage").getValue(String.class);
                
                // ✅ ImageUtils ile güvenli resim yükleme
                ImageUtils.loadImageSafely(getActivity(), url, imageStory);
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {
                Log.e("MainFragment", "Error loading profile: " + error.getMessage());
            }
        });
    }

    private void setupStoriesRecyclerView(View view) {
        stories_recycler = view.findViewById(R.id.stories_recycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        stories_recycler.setLayoutManager(linearLayoutManager);
        stories_recycler.setItemAnimator(new DefaultItemAnimator());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e("MainFragment", "User is null");
            return;
        }

        final String currentUserid = user.getUid();
        Log.d("StoryDebug", "Current user ID: " + currentUserid);

        FirebaseDatabase database1 = FirebaseDatabase.getInstance();
        DatabaseReference reference1 = database1.getReference("Kullanıcılar").child(currentUserid).child("Story");

        // Önce story'lerin olup olmadığını kontrol et
        reference1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("StoryDebug", "Story count: " + snapshot.getChildrenCount());

                if (snapshot.getChildrenCount() > 0) {
                    // Story'leri gruplayarak göster
                    groupStoriesAndSetupAdapter(snapshot);
                } else {
                    Log.d("StoryDebug", "No stories found for user: " + currentUserid);
                    if (isAdded()) {
                        Log.d("TAG", "onDataChange: ");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("StoryDebug", "Database error: " + error.getMessage());
            }
        });
    }

    private void groupStoriesAndSetupAdapter(DataSnapshot snapshot) {
        Map<String, List<StoryMember>> userStoriesMap = new HashMap<>();

        // Story'leri kullanıcı ID'sine göre grupla
        for (DataSnapshot child : snapshot.getChildren()) {
            StoryMember story = child.getValue(StoryMember.class);
            if (story != null && story.getUid() != null) {
                String userId = story.getUid();
                if (!userStoriesMap.containsKey(userId)) {
                    userStoriesMap.put(userId, new ArrayList<>());
                }
                userStoriesMap.get(userId).add(story);
            }
        }

        // Gruplanmış story'leri yeni bir liste yapısına dönüştür
        groupedStoriesList.clear();
        for (Map.Entry<String, List<StoryMember>> entry : userStoriesMap.entrySet()) {
            String userId = entry.getKey();
            List<StoryMember> stories = entry.getValue();

            // Aynı gün içindeki story'leri kontrol et
            Map<String, List<StoryMember>> dailyStoriesMap = new HashMap<>();
            for (StoryMember story : stories) {
                String date = extractDateFromTimeUpload(story.getTimeUpload());
                if (!dailyStoriesMap.containsKey(date)) {
                    dailyStoriesMap.put(date, new ArrayList<>());
                }
                dailyStoriesMap.get(date).add(story);
            }

            // Her gün için bir grup oluştur
            for (Map.Entry<String, List<StoryMember>> dailyEntry : dailyStoriesMap.entrySet()) {
                List<StoryMember> dailyStories = dailyEntry.getValue();
                // İlk story'nin bilgilerini kullan
                StoryMember firstStory = dailyStories.get(0);
                groupedStoriesList.add(new GroupedStory(firstStory, dailyStories.size()));
            }
        }

        // Adapter'ı oluştur veya güncelle
        if (groupedStoryAdapter == null) {
            groupedStoryAdapter = new GroupedStoryAdapter(groupedStoriesList);
            stories_recycler.setAdapter(groupedStoryAdapter);
        } else {
            groupedStoryAdapter.notifyDataSetChanged();
        }
    }

    // TimeUpload'tan tarihi çıkar (format: "dd-MMM-yyyy:HH:mm:ss")
    private String extractDateFromTimeUpload(String timeUpload) {
        if (timeUpload == null || timeUpload.isEmpty()) {
            return "";
        }

        // Tarih kısmını ayır (ilk ':' karakterine kadar olan kısım)
        int colonIndex = timeUpload.indexOf(':');
        if (colonIndex > 0) {
            return timeUpload.substring(0, colonIndex);
        }
        return timeUpload;
    }

    private void setupViewPager() {
        viewPager.setAdapter(new TabPagerMainAdapter(getActivity()));
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Paylaşımlar");
                            break;
                        case 1:
                            tab.setText("Canlı Yayınlar");
                            break;
                    }
                }
        ).attach();
    }




    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Memory leak'leri önlemek için adapter'ı temizle
        if (groupedStoryAdapter != null) {
            groupedStoryAdapter = null;
        }
    }

    // GÜVENLI bitmap boyut kontrolü - ImageUtils kullan
    private boolean isBitmapSafe(Bitmap bitmap) {
        return ImageUtils.isBitmapSafe(bitmap);
    }

    // GÜVENLI bitmap boyut küçültme fonksiyonu - ImageUtils kullan
    public static Bitmap resizeBitmapSafely(Bitmap originalBitmap, int maxWidth, int maxHeight) {
        return ImageUtils.resizeBitmapSafely(originalBitmap, maxWidth, maxHeight);
    }

    // Eski resizeBitmap fonksiyonu (geriye uyumluluk için)
    public static Bitmap resizeBitmap(Bitmap originalBitmap, int maxWidth, int maxHeight) {
        return resizeBitmapSafely(originalBitmap, maxWidth, maxHeight);
    }

    // İyileştirilmiş Base64 kodlama fonksiyonu - ImageUtils kullan
    public static String encodeBase64Compressed(Bitmap bitmap, int quality) {
        return ImageUtils.encodeBase64Compressed(bitmap, quality);
    }

    // GÜVENLI decodeBase64 fonksiyonu - ImageUtils kullan
    public static Bitmap decodeBase64Safe(String encodedImage) {
        return ImageUtils.decodeBase64Safe(encodedImage);
    }

    // Eski decodeBase64 fonksiyonu (geriye uyumluluk için)
    public static Bitmap decodeBase64(String encodedImage) {
        return decodeBase64Safe(encodedImage);
    }

    // Gruplanmış story'ler için model sınıfı
    private static class GroupedStory {
        private StoryMember firstStory;
        private int storyCount;

        public GroupedStory(StoryMember firstStory, int storyCount) {
            this.firstStory = firstStory;
            this.storyCount = storyCount;
        }

        public StoryMember getFirstStory() {
            return firstStory;
        }

        public int getStoryCount() {
            return storyCount;
        }
    }

    // Gruplanmış story'ler için özel adapter
    private class GroupedStoryAdapter extends RecyclerView.Adapter<StoryViewHolder> {
        private List<GroupedStory> groupedStories;

        public GroupedStoryAdapter(List<GroupedStory> groupedStories) {
            this.groupedStories = groupedStories;
        }

        @NonNull
        @Override
        public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.story_layout, parent, false);
            return new StoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
            GroupedStory groupedStory = groupedStories.get(position);
            StoryMember story = groupedStory.getFirstStory();

            Log.d("GroupedStoryAdapter", "Binding story at position: " + position);

            // Null kontrolleri
            String postUri = story.getPostUri() != null ? story.getPostUri() : "";
            String name = story.getName() != null ? story.getName() : "Bilinmeyen Kullanıcı";
            String url = story.getUrl() != null ? story.getUrl() : "";
            String caption = story.getCaption() != null ? story.getCaption() : "";
            String uid = story.getUid() != null ? story.getUid() : "";
            String type = story.getType() != null ? story.getType() : "image";
            long timeEnd = story.getTimeEnd();
            String timeUpload = story.getTimeUpload() != null ? story.getTimeUpload() : "";

            Log.d("GroupedStoryAdapter", "Story data - Name: " + name + ", URL length: " + url.length());

            // Story count'u caption'a ekleyelim
            String displayCaption = caption;
            if (groupedStory.getStoryCount() > 1) {
                displayCaption = caption;
            }

            holder.setStory(getActivity(), postUri, name, timeEnd, timeUpload, type, displayCaption, url, uid);

            // Tıklama eventi
            final String userid = story.getUid();
            if (userid != null && !userid.isEmpty()) {
                holder.itemView.setOnClickListener(view -> {
                    Log.d("GroupedStoryAdapter", "Story clicked, opening ShowStory for user: " + userid);
                    Intent intent = new Intent(getActivity(), ShowStory.class);
                    intent.putExtra("u", userid);
                    startActivity(intent);
                });
            }
        }

        @Override
        public int getItemCount() {
            return groupedStories.size();
        }

        @Override
        public void onViewRecycled(@NonNull StoryViewHolder holder) {
            super.onViewRecycled(holder);
            // Memory leak'leri önlemek için bitmap'i recycle et
            if (holder.imageView.getDrawable() != null) {
                holder.imageView.setImageDrawable(null);
            }
        }
    }

}