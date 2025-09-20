package com.lumoo;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lumoo.ViewHolder.TabPagerAdapter;
import com.lumoo.ViewHolder.TabPagerProfileAdapter;
import com.lumoo.util.GlideUtil;
import com.lumoo.util.SecurityUtils;
import com.lumoo.util.ValidationUtils;

import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProfileFragment extends Fragment {
    FirebaseDatabase database;
    DatabaseReference reference;
    TextView name, surname;
    private ImageView backgroundImage; // Arka plan için
    private CircleImageView profileImage; // Profil fotoğrafı için
    private ImageView btnEditProfile; // Profil düzenleme butonu
    private Bitmap selectedImageBitmap;
    ConstraintLayout btnCardProfile;
    private static final int REQUEST_CODE_PROFILE_IMAGE = 101;
    private static final int REQUEST_CODE_BACKGROUND_IMAGE = 102;

    ImageView btnFriendRequests;

    TextView hostStream;
    TextView txtFollowerCount, txtPostCount, txtFollowCount;

    String txtName;

    FirebaseDatabase database1;
    DatabaseReference reference1;

    private TabLayout tabLayout;
    ViewPager2 viewPager;

    private long appID = 1771662561;
    private String appSign = "83ab84a07b219d583fee959f8fa5f913fd213c96b601f891af3ea0a3ea8deb40";
    String namee, userName, photo;

    TextView txtHoroscope, txtCountry, usernameProfile, txtUserAge, txtUserCountry;
    ImageView imgCountryFlag, imgGenderFlag;

    private static final String SUPABASE_URL = "https://iauuehrfhmzhnfsnsjdx.supabase.co";
    private static final String SUPABASE_APIKEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlhdXVlaHJmaG16aG5mc25zamR4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTc4NjQ2NzAsImV4cCI6MjA3MzQ0MDY3MH0.GnwTJFqC_cLAuKt7dAlSjlVIBfy4O9nTVWyn3d2wzRM";
    private static final String SUPABASE_STORAGE_ENDPOINT = SUPABASE_URL + "/storage/v1/object/profile-images/";
    private static final String SUPABASE_BACKGROUND_ENDPOINT = SUPABASE_URL + "/storage/v1/object/background-images/";

    RelativeLayout btnLive;

    // Listener'ları saklamak için değişkenler
    private ValueEventListener userValueEventListener;
    private ValueEventListener friendsValueEventListener;
    private ValueEventListener postsValueEventListener;
    private ValueEventListener followersValueEventListener;
    private ValueEventListener profileImageValueEventListener;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews(view);
        setupViewPager();
        setupClickListeners(view);
        setupFirebaseListeners();
        loadProfileImages();
        setupZegoEngine();

        return view;
    }

    private void initViews(View view) {
        name = view.findViewById(R.id.txtName);
        surname = view.findViewById(R.id.txtSurname);
        backgroundImage = view.findViewById(R.id.backgroundImage);
        profileImage = view.findViewById(R.id.profileImage);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnCardProfile = view.findViewById(R.id.btnCardProfile);
        btnLive = view.findViewById(R.id.btnLive);
        viewPager = view.findViewById(R.id.pagerProfile);
        tabLayout = view.findViewById(R.id.tabLayoutProfile);
        txtHoroscope = view.findViewById(R.id.txtHoroscope);
        usernameProfile = view.findViewById(R.id.txtUserNameProfile);
        txtCountry = view.findViewById(R.id.txtCountry);
        imgGenderFlag = view.findViewById(R.id.imgGenderFlag);
        txtUserCountry = view.findViewById(R.id.txtUserCountry);
        txtUserAge = view.findViewById(R.id.txtUserAge);
        txtFollowCount = view.findViewById(R.id.userFollowCount1);
        txtFollowerCount = view.findViewById(R.id.userFollowerCount1);
        txtPostCount = view.findViewById(R.id.userPostCount1);
        btnFriendRequests = view.findViewById(R.id.btnFriendRequests);
    }

    private void setupViewPager() {
        viewPager.setAdapter(new TabPagerProfileAdapter(getActivity()));
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Paylaşımlar");
                            break;
                        case 1:
                            tab.setText("Kredim");
                            break;
                        case 2:
                            tab.setText("Ayarlar");
                            break;
                    }
                }
        ).attach();
    }

    private void setupClickListeners(View view) {
        RelativeLayout btnShare = view.findViewById(R.id.btnShare);
        btnShare.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ShareActivity.class);
            startActivity(intent);
        });

        btnFriendRequests.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FriendRequestActivity.class);
            startActivity(intent);
        });

        // Arka plan fotoğrafına tıklama
        backgroundImage.setOnClickListener(v -> {
            if (isAdded() && getContext() != null) {
                showImageSelectionDialog(REQUEST_CODE_BACKGROUND_IMAGE);
            }
        });

        // Profil fotoğrafı düzenleme butonu
        btnEditProfile.setOnClickListener(v -> {
            if (isAdded() && getContext() != null) {
                showImageSelectionDialog(REQUEST_CODE_PROFILE_IMAGE);
            }
        });

        btnLive.setOnClickListener(v -> {
            if (!isAdded() || getContext() == null) return;

            if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                activityResultLauncher.launch(android.Manifest.permission.RECORD_AUDIO);
            } else if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                activityResultLauncher.launch(android.Manifest.permission.CAMERA);
            } else {
                startLiveStream();
            }
        });
    }

    private void showImageSelectionDialog(int requestCode) {
        // Basit bir seçim diyalogu veya direkt galeriyi aç
        pickImageFromGallery(requestCode);
    }

    private void startLiveStream() {
        String randomRoomID = generateRandomID();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();

        DatabaseReference publishersRef = FirebaseDatabase.getInstance().getReference("Publishers").child(randomRoomID);
        publishersRef.child("userId").setValue(userId);
        publishersRef.child("userName").setValue(userName);
        publishersRef.child("roomId").setValue(randomRoomID);
        publishersRef.child("streamerPhoto").setValue(photo);

        Intent intent = new Intent(getContext(), ViewerActivity.class);
        intent.putExtra("userID", txtName);
        intent.putExtra("userName", userName);
        intent.putExtra("roomID", randomRoomID);
        intent.putExtra("isHost", true);
        startActivity(intent);
    }

    private void setupFirebaseListeners() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Kullanıcılar").child(userId);

        DatabaseReference reference2 = database.getReference("Kullanıcılar").child(userId);

        // 1. Arkadaş sayısı
        friendsValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || getContext() == null) return;
                long count = snapshot.getChildrenCount();
                txtFollowerCount.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        reference2.child("Friends").addValueEventListener(friendsValueEventListener);

        // 2. Post sayısı
        postsValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || getContext() == null) return;
                long count = snapshot.getChildrenCount();
                txtPostCount.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        reference2.child("Posts").addValueEventListener(postsValueEventListener);

        // 3. Takipçi sayısı
        followersValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || getContext() == null) return;
                int addedByCount = 0;
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    if (userSnapshot.child("Friends").hasChild(userId)) {
                        addedByCount++;
                    }
                }
                txtFollowCount.setText(String.valueOf(addedByCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        reference2.addValueEventListener(followersValueEventListener);

        // Kullanıcı bilgilerini yükle
        userValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || getContext() == null) return;

                txtName = snapshot.child("ad").getValue(String.class);
                String txtSurname = snapshot.child("soyad").getValue(String.class);
                String txtUsername = snapshot.child("kullanıcıAdı").getValue(String.class);
                String countryStr = snapshot.child("country").getValue(String.class);
                String countryCode = snapshot.child("countryCode").getValue(String.class);
                String horoscope = snapshot.child("burç").getValue(String.class);
                String gender = snapshot.child("gender").getValue(String.class);
                String age = snapshot.child("doğumTarihi").getValue(String.class);

                name.setText(txtName);
                surname.setText(txtSurname);
                usernameProfile.setText(txtUsername);

                // Ülke kısaltmalarını uygula
                String displayCountry = getDisplayCountry(countryStr);
                txtCountry.setText(displayCountry);
                txtUserCountry.setText(displayCountry);
                txtHoroscope.setText(horoscope);
                txtUserAge.setText(age);

                // Cinsiyet ikonunu ayarla
                setGenderIcon(gender);

                // Ülke bayrağını ayarla
                setCountryFlag(countryCode);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        reference.addValueEventListener(userValueEventListener);

        // Kullanıcı bilgilerini stream için yükle
        database1 = FirebaseDatabase.getInstance();
        reference1 = database1.getReference("Kullanıcılar").child(userId);

        ValueEventListener userInfoListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                namee = snapshot.child("ad").getValue(String.class);
                userName = snapshot.child("kullanıcıAdı").getValue(String.class);
                photo = snapshot.child("profileImage").getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        reference.addValueEventListener(userInfoListener);
    }

    private String getDisplayCountry(String countryStr) {
        if ("Amerika Birleşik Devletleri".equals(countryStr)) {
            return "ABD";
        } else if ("Dominik Cumhuriyeti".equals(countryStr)) {
            return "Dominik";
        } else if ("Birleşik Arap Emirlikleri".equals(countryStr)) {
            return "BAE";
        } else {
            return countryStr;
        }
    }

    private void setGenderIcon(String gender) {
        if (gender != null && !gender.isEmpty()) {
            if (gender.equals("Erkek")) {
                imgGenderFlag.setImageResource(R.drawable.maleicon);
            } else if (gender.equals("Kadın")) {
                imgGenderFlag.setImageResource(R.drawable.femaleicon);
            }
        } else {
            imgGenderFlag.setImageResource(R.drawable.ic_launcher_background);
        }
    }

    private void setCountryFlag(String countryCode) {
        if (countryCode != null && !countryCode.isEmpty() && getContext() != null) {
            int resId = getResources().getIdentifier(countryCode.toLowerCase(), "drawable", getContext().getPackageName());
            if (resId != 0) {
                imgCountryFlag = getView().findViewById(R.id.imgCountryFlagProfile);
                imgCountryFlag.setImageResource(resId);
            } else {
                imgCountryFlag = getView().findViewById(R.id.imgCountryFlagProfile);
                imgCountryFlag.setImageResource(R.drawable.tr);
            }
        }
    }

    private void setupZegoEngine() {
        ZegoEngineProfile profile = new ZegoEngineProfile();
        profile.appID = appID;
        profile.appSign = appSign;
        profile.scenario = ZegoScenario.BROADCAST;
        if (getActivity() != null) {
            profile.application = getActivity().getApplication();
            ZegoExpressEngine.createEngine(profile, null);
        }
    }

    private void pickImageFromGallery(int requestCode) {
        if (!isAdded() || getContext() == null) return;
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (!isAdded() || getContext() == null) return;

        if (resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap selectedImageBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);

                if (requestCode == REQUEST_CODE_PROFILE_IMAGE) {
                    // ✅ SecurityUtils ile güvenlik log'u
                    SecurityUtils.logSecurityEvent("Profile Image Update", "User updating profile image");
                    
                    profileImage.setImageBitmap(selectedImageBitmap);
                    saveImageToSupabase(selectedImageBitmap, "profile_" + System.currentTimeMillis() + ".jpg", true);
                } else if (requestCode == REQUEST_CODE_BACKGROUND_IMAGE) {
                    // ✅ SecurityUtils ile güvenlik log'u
                    SecurityUtils.logSecurityEvent("Background Image Update", "User updating background image");
                    
                    backgroundImage.setImageBitmap(selectedImageBitmap);
                    saveImageToSupabase(selectedImageBitmap, "background_" + System.currentTimeMillis() + ".jpg", false);
                }

            } catch (IOException e) {
                e.printStackTrace();
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Resim yüklenirken hata oluştu", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // Bitmap'i Supabase Storage'a yükleme (profil veya arka plan için)
    private void saveImageToSupabase(Bitmap bitmap, String fileName, boolean isProfileImage) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] imageData = baos.toByteArray();

        OkHttpClient client = new OkHttpClient();

        String uploadUrl;
        if (isProfileImage) {
            uploadUrl = SUPABASE_STORAGE_ENDPOINT + fileName;
        } else {
            uploadUrl = SUPABASE_BACKGROUND_ENDPOINT + fileName;
        }

        RequestBody requestBody = RequestBody.create(imageData, MediaType.parse("image/jpeg"));

        Request request = new Request.Builder()
                .url(uploadUrl)
                .header("apikey", SUPABASE_APIKEY)
                .header("Authorization", "Bearer " + SUPABASE_APIKEY)
                .header("Content-Type", "image/jpeg")
                .put(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Resim yükleme başarısız", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("Supabase", "Upload başarılı: " + fileName);

                    String bucketName = isProfileImage ? "profile-images" : "profile-images";
                    String publicUrl = SUPABASE_URL + "/storage/v1/object/public/" + bucketName + "/" + fileName;

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            saveImageUrlToFirebase(publicUrl, isProfileImage);
                        });
                    }
                } else {
                    Log.e("Supabase", "Upload hatası: " + response.code() + " " + response.message());
                }
            }
        });
    }

    // Firebase'e kaydet
    private void saveImageUrlToFirebase(String imageUrl, boolean isProfileImage) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String fieldName = isProfileImage ? "profileImage" : "backgroundImage";

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Kullanıcılar")
                .child(uid)
                .child(fieldName);

        userRef.setValue(imageUrl).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("Firebase", fieldName + " URL kaydedildi: " + imageUrl);
                if (getContext() != null) {
                    String message = isProfileImage ? "Profil fotoğrafı güncellendi" : "Arka plan fotoğrafı güncellendi";
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("Firebase", "URL kaydedilemedi", task.getException());
            }
        });
    }

    private void loadProfileImages() {
        if (!isAdded() || getContext() == null) return;

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Kullanıcılar").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || getContext() == null) return;

                if (snapshot.exists()) {
                    String profileImageUrl = snapshot.child("profileImage").getValue(String.class);
                    String backgroundImageUrl = snapshot.child("backgroundImage").getValue(String.class);

                    // Profil fotoğrafını yükle
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        GlideUtil.loadOriginalImage(requireContext(), profileImageUrl, profileImage);
                    }

                    // Arka plan fotoğrafını yükle
                    if (backgroundImageUrl != null && !backgroundImageUrl.isEmpty()) {
                        GlideUtil.loadOriginalImage(requireContext(), backgroundImageUrl, backgroundImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProfileFragment", "Error: " + error.getMessage());
            }
        });
    }

    public static String encodeToBase64(Bitmap image) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                // İzin sonucu
            }
    );

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Tüm listener'ları temizle
        if (reference != null && userValueEventListener != null) {
            reference.removeEventListener(userValueEventListener);
        }
        if (reference != null && profileImageValueEventListener != null) {
            reference.removeEventListener(profileImageValueEventListener);
        }

        // Diğer Firebase referanslarını da temizle
        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
        if (database2 != null) {
            DatabaseReference reference2 = database2.getReference("Kullanıcılar");
            if (reference2 != null) {
                if (friendsValueEventListener != null) {
                    reference2.child("Friends").removeEventListener(friendsValueEventListener);
                }
                if (postsValueEventListener != null) {
                    reference2.child("Posts").removeEventListener(postsValueEventListener);
                }
                if (followersValueEventListener != null) {
                    reference2.removeEventListener(followersValueEventListener);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ZegoExpressEngine.destroyEngine(null);
    }

    private String generateRandomID() {
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        while (builder.length() < 6) {
            int nextInt = random.nextInt(10);
            if (builder.length() == 0 && nextInt == 0) {
                continue;
            }
            builder.append(nextInt);
        }
        return builder.toString();
    }
}