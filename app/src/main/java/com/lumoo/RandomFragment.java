package com.lumoo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lumoo.ViewHolder.CountryItem;
import com.lumoo.ViewHolder.CountrySpinnerAdapter;
import com.lumoo.util.GlideUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import io.reactivex.rxjava3.annotations.NonNull;

public class RandomFragment extends Fragment {

    // UI Elements
    private TextView textView2, textView10, loadingSubtext, appTitle;
    private TextView txtWelcome;
    private ImageView imgMatchPersonPhoto, txtMatchFlag;
    private ImageButton btnFilter;
    private CardView cardContainer, matchButtonAgain, buttonSendMessage;
    private RelativeLayout btnMatch;
    private TextView txtMatchPersonName, txtMatchAge, txtMatchGender, txtDistance;
    private LinearLayout linearll, welcomeContainer,loadingContainer;
    private View filterActiveBadge;

    // Lottie Animations
    private LottieAnimationView progressBar;

    // Firebase
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference;

    // Data variables
    private String url, name, randomUid;
    private int minAge = 18;
    private int maxAge = 65;
    private String selectedCountry = "Tümü";
    private SharedPreferences sharedPreferences;
    private boolean isFirstTime = true;
    private List<CountryItem> countryList;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private String[] loadingMessages = {
            "Uyumluluk analiz ediliyor...",
            "Mükemmel eşleşmeler bulunuyor...",
            "Kimya hesaplanıyor...",
            "Neredeyse hazır...",
            "Sihir gerçekleşiyor..."
    };

    private String selectedGender;
    private int currentLoadingMessage = 0;
    String userGender;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1, mParam2;

    public RandomFragment() {
        // Required empty public constructor
    }

    public static RandomFragment newInstance(String param1, String param2) {
        RandomFragment fragment = new RandomFragment();
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

        sharedPreferences = getActivity().getSharedPreferences("UserFilters", getActivity().MODE_PRIVATE);
        minAge = sharedPreferences.getInt("minAge", 18);
        maxAge = sharedPreferences.getInt("maxAge", 65);
        selectedCountry = sharedPreferences.getString("selectedCountry", "Tümü");
        // Kullanıcının kendi cinsiyeti (uygulamada girişte kaydedilmiş olmalı)
        userGender = sharedPreferences.getString("userGender", "Tümü");

        // Karşı cinsiyeti filtreye ayarla
        if (userGender.equals("Erkek")) {
            selectedGender = "Kadın";
        } else if (userGender.equals("Kadın")) {
            selectedGender = "Erkek";
        } else {
            selectedGender = "Tümü"; // kullanıcı cinsiyet belirtmemişse
        }

        initializeCountryList();
    }

    private void initializeCountryList() {
        countryList = new ArrayList<>();
        countryList.add(new CountryItem("Tümü", ""));
        countryList.add(new CountryItem("Türkiye", "tr"));
        countryList.add(new CountryItem("Almanya", "de"));
        countryList.add(new CountryItem("Fransa", "fr"));
        countryList.add(new CountryItem("İtalya", "it"));
        countryList.add(new CountryItem("İspanya", "es"));
        countryList.add(new CountryItem("İngiltere", "gb"));
        countryList.add(new CountryItem("Amerika Birleşik Devletleri", "us"));
        countryList.add(new CountryItem("Kanada", "ca"));
        // Daha fazla ülke eklenebilir
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_random, container, false);

        initViews(view);
        setupAnimations();
        setupClickListeners();
        updateFilterBadge();

        return view;
    }

    private void initViews(View view) {
        // Header elements
        appTitle = view.findViewById(R.id.appTitle);
        btnFilter = view.findViewById(R.id.btnFilter);
        filterActiveBadge = view.findViewById(R.id.filterActiveBadge);

        // Welcome section
        welcomeContainer = view.findViewById(R.id.welcomeContainer);
        txtWelcome = view.findViewById(R.id.txtWelcome);

        // Loading section
        loadingContainer = view.findViewById(R.id.loadingContainer);
        progressBar = view.findViewById(R.id.progressBar);
        textView10 = view.findViewById(R.id.textView10);
        loadingSubtext = view.findViewById(R.id.loadingSubtext);

        // Match card
        cardContainer = view.findViewById(R.id.cardViewContainer);
        imgMatchPersonPhoto = view.findViewById(R.id.imgMatchPhoto);
        txtMatchPersonName = view.findViewById(R.id.txtMatchName);
        txtMatchAge = view.findViewById(R.id.txtMatchAge);
        txtMatchGender = view.findViewById(R.id.txtMatchGender);
        txtMatchFlag = view.findViewById(R.id.txtMatchFlag);
        txtDistance = view.findViewById(R.id.txtDistance);

        // Action buttons
        textView2 = view.findViewById(R.id.textView2);
        btnMatch = view.findViewById(R.id.btnMatch);
        linearll = view.findViewById(R.id.linearll);
        matchButtonAgain = view.findViewById(R.id.btnMatchAgain);
        buttonSendMessage = view.findViewById(R.id.btnSendMessage);
    }

    private void setupAnimations() {
        // İlk giriş animasyonları
        animateWelcomeEntrance();
        setupGradientAnimation();
    }

    private void animateWelcomeEntrance() {
        welcomeContainer.setTranslationY(100f);
        welcomeContainer.setAlpha(0f);
        welcomeContainer.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(1000)
                .setStartDelay(300)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void setupGradientAnimation() {
        // App title için gradient animasyon efekti
        ValueAnimator gradientAnimator = ValueAnimator.ofFloat(0f, 1f);
        gradientAnimator.setDuration(3000);
        gradientAnimator.setRepeatCount(ValueAnimator.INFINITE);
        gradientAnimator.setRepeatMode(ValueAnimator.REVERSE);
        gradientAnimator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            if (appTitle != null) {
                appTitle.setAlpha(0.8f + 0.2f * progress);
            }
        });
        gradientAnimator.start();
    }

    private void setupClickListeners() {
        // Filter button
        btnFilter.setOnClickListener(v -> {
            animateButtonPress(v, this::showEnhancedFilterDialog);
        });

        // Ana eşleşme butonu
        btnMatch.setOnClickListener(v -> {
            animateMainButtonPress(v, () -> {
                if (isFirstTime) {
                    animateWelcomeToLoading();
                    btnMatch.setVisibility(View.GONE);
                    isFirstTime = false;
                } else {
                    showLoadingWithTransition();
                }
                yeniUidSec();
            });
        });

        // Tekrar eşleş butonu
        matchButtonAgain.setOnClickListener(v -> {
            animateButtonPress(v, () -> {
                animateSkipAction();
                yeniUidSec();
            });
        });

        // Mesaj butonu
        buttonSendMessage.setOnClickListener(v -> {
            animateButtonPress(v, this::mesajAt);
        });
    }

    private void animateButtonPress(View button, Runnable onComplete) {
        button.animate()
                .scaleX(0.92f)
                .scaleY(0.92f)
                .setDuration(150)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    button.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(150)
                            .setInterpolator(new OvershootInterpolator())
                            .withEndAction(onComplete)
                            .start();
                })
                .start();
    }

    private void animateMainButtonPress(View button, Runnable onComplete) {
        // Ölçek animasyonu
        AnimatorSet scaleSet = new AnimatorSet();
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.95f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.95f);
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(button, "scaleX", 0.95f, 1.05f, 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(button, "scaleY", 0.95f, 1.05f, 1f);

        scaleDownX.setDuration(100);
        scaleDownY.setDuration(100);
        scaleUpX.setDuration(300);
        scaleUpY.setDuration(300);

        scaleSet.play(scaleDownX).with(scaleDownY);
        scaleSet.play(scaleUpX).with(scaleUpY).after(scaleDownX);

        scaleSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                onComplete.run();
            }
        });
        scaleSet.start();
    }

    private void animateWelcomeToLoading() {
        // Hoşgeldin mesajını kayboltur
        AnimatorSet welcomeOut = new AnimatorSet();
        ObjectAnimator welcomeFadeOut = ObjectAnimator.ofFloat(welcomeContainer, "alpha", 1f, 0f);
        ObjectAnimator welcomeScaleOut = ObjectAnimator.ofFloat(welcomeContainer, "scaleX", 1f, 0.8f);
        ObjectAnimator welcomeScaleOutY = ObjectAnimator.ofFloat(welcomeContainer, "scaleY", 1f, 0.8f);

        welcomeOut.playTogether(welcomeFadeOut, welcomeScaleOut, welcomeScaleOutY);
        welcomeOut.setDuration(400);
        welcomeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                welcomeContainer.setVisibility(View.GONE);
                showLoadingWithEntrance();
            }
        });
        welcomeOut.start();
    }

    private void showLoadingWithEntrance() {
        loadingContainer.setVisibility(View.VISIBLE);
        loadingContainer.setScaleX(0.8f);
        loadingContainer.setScaleY(0.8f);
        loadingContainer.setAlpha(0f);

        loadingContainer.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(500)
                .setInterpolator(new OvershootInterpolator())
                .start();

        startLoadingMessageRotation();
    }

    private void showLoadingWithTransition() {
        // Mevcut eşleşme kartını gizle
        if (cardContainer.getVisibility() == View.VISIBLE) {
            cardContainer.animate()
                    .alpha(0f)
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        cardContainer.setVisibility(View.GONE);
                        showLoadingWithEntrance();
                    })
                    .start();
        } else {
            showLoadingWithEntrance();
        }

        // Aksiyon butonlarını gizle
        if (linearll.getVisibility() == View.VISIBLE) {
            animateButtonsOut();
        }
    }

    private void startLoadingMessageRotation() {
        currentLoadingMessage = 0;
        rotateLoadingMessage();
    }

    private void rotateLoadingMessage() {
        if (loadingContainer.getVisibility() == View.VISIBLE) {
            loadingSubtext.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        loadingSubtext.setText(loadingMessages[currentLoadingMessage]);
                        currentLoadingMessage = (currentLoadingMessage + 1) % loadingMessages.length;
                        loadingSubtext.animate()
                                .alpha(1f)
                                .setDuration(300)
                                .start();
                    })
                    .start();

            mainHandler.postDelayed(this::rotateLoadingMessage, 2000);
        }
    }

    private void showMatchWithEntrance(String matchUrl, String matchName, String matchGender, int matchAge) {
        // Yükleme ekranını gizle
        loadingContainer.animate()
                .alpha(0f)
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(300)
                .withEndAction(() -> {
                    loadingContainer.setVisibility(View.GONE);
                    showMatchCard(matchUrl, matchName, matchGender, matchAge);
                })
                .start();
    }

    private void showMatchCard(String matchUrl, String matchName, String matchGender, int matchAge) {
        // Veri setleme
        if (matchUrl != null) {
            GlideUtil.loadOriginalImage(requireContext(), matchUrl, imgMatchPersonPhoto);
        }
        txtMatchPersonName.setText(matchName != null ? matchName : "Bilinmiyor");
        txtMatchGender.setText(matchGender != null ? matchGender : "Belirtilmemiş");
        txtMatchAge.setText(String.valueOf(matchAge));

        // Rastgele mesafe ve uyumluluk skoru
        Random random = new Random();
        int distance = random.nextInt(20) + 1;
        txtDistance.setText(distance + " km uzakta");

        // Kartı giriş animasyonu ile göster
        cardContainer.setVisibility(View.VISIBLE);
        cardContainer.setScaleX(0.8f);
        cardContainer.setScaleY(0.8f);
        cardContainer.setAlpha(0f);
        cardContainer.setRotationY(15f);

        AnimatorSet cardEntrance = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(cardContainer, "scaleX", 0.8f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(cardContainer, "scaleY", 0.8f, 1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(cardContainer, "alpha", 0f, 1f);
        ObjectAnimator rotationY = ObjectAnimator.ofFloat(cardContainer, "rotationY", 15f, 0f);

        cardEntrance.playTogether(scaleX, scaleY, alpha, rotationY);
        cardEntrance.setDuration(600);
        cardEntrance.setInterpolator(new DecelerateInterpolator());
        cardEntrance.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                showActionButtons();
            }
        });
        cardEntrance.start();
    }

    private void showActionButtons() {
        linearll.setVisibility(View.VISIBLE);

        // Buton giriş animasyonları
        matchButtonAgain.setTranslationY(100f);
        matchButtonAgain.setAlpha(0f);
        buttonSendMessage.setTranslationY(100f);
        buttonSendMessage.setAlpha(0f);

        matchButtonAgain.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(400)
                .setStartDelay(200)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        buttonSendMessage.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(400)
                .setStartDelay(300)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void animateButtonsOut() {
        linearll.animate()
                .translationY(100f)
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> linearll.setVisibility(View.GONE))
                .start();
    }

    private void animateSkipAction() {
        // Kart kayma animasyonu
        cardContainer.animate()
                .translationX(-getView().getWidth())
                .alpha(0.7f)
                .setDuration(400)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    cardContainer.setTranslationX(0);
                    cardContainer.setAlpha(1f);
                })
                .start();
    }

    private void updateFilterBadge() {
        boolean hasActiveFilters = !selectedCountry.equals("Tümü") || minAge != 18 || maxAge != 65;
        filterActiveBadge.setVisibility(hasActiveFilters ? View.VISIBLE : View.GONE);

        if (hasActiveFilters) {
            // Badge giriş animasyonu
            filterActiveBadge.setScaleX(0f);
            filterActiveBadge.setScaleY(0f);
            filterActiveBadge.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(300)
                    .setInterpolator(new OvershootInterpolator())
                    .start();
        }
    }

    private void showEnhancedFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomDialogTheme);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_age_filter, null);

        EditText etMinAge = dialogView.findViewById(R.id.etMinAge);
        EditText etMaxAge = dialogView.findViewById(R.id.etMaxAge);
        Spinner spinnerCountry = dialogView.findViewById(R.id.spinnerCountry);
        Spinner spinnerGender = dialogView.findViewById(R.id.spinnerGender); // ✅ yeni eklendi
        TextView tvCurrentFilter = dialogView.findViewById(R.id.tvCurrentFilter);
        CardView btnCancel = dialogView.findViewById(R.id.btnCancel);
        CardView btnApply = dialogView.findViewById(R.id.btnApply);

        // Ülke spinner
        CountrySpinnerAdapter countryAdapter = new CountrySpinnerAdapter(getActivity(), countryList);
        spinnerCountry.setAdapter(countryAdapter);

        // Yaş alanlarını doldur
        etMinAge.setText(String.valueOf(minAge));
        etMaxAge.setText(String.valueOf(maxAge));

        // Seçili ülkeyi ayarla
        for (int i = 0; i < countryList.size(); i++) {
            if (countryList.get(i).getCountryName().equals(selectedCountry)) {
                spinnerCountry.setSelection(i);
                break;
            }
        }

                // ✅ Cinsiyet spinner
                List<String> genderList = Arrays.asList("Tümü", "Erkek", "Kadın");
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                genderList
        );
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        // Seçili cinsiyeti ayarla
        int genderIndex = genderList.indexOf(selectedGender);
        if (genderIndex >= 0) {
            spinnerGender.setSelection(genderIndex);
        }

        // Mevcut filtre metnini güncelle
        String filterText = "Mevcut filtre: " + minAge + "-" + maxAge + " yaş";
        if (!selectedCountry.equals("Tümü")) {
            filterText += ", " + selectedCountry;
        }
        if (!selectedGender.equals("Tümü")) {
            filterText += ", " + selectedGender;
        }
        tvCurrentFilter.setText(filterText);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Cancel button
        btnCancel.setOnClickListener(v -> {
            animateButtonPress(v, dialog::dismiss);
        });

        // Apply button
        btnApply.setOnClickListener(v -> {
            animateButtonPress(v, () -> {
                String minAgeStr = etMinAge.getText().toString().trim();
                String maxAgeStr = etMaxAge.getText().toString().trim();

                if (TextUtils.isEmpty(minAgeStr) || TextUtils.isEmpty(maxAgeStr)) {
                    showCustomToast("Lütfen tüm yaş alanlarını doldurun", false);
                    return;
                }

                try {
                    int newMinAge = Integer.parseInt(minAgeStr);
                    int newMaxAge = Integer.parseInt(maxAgeStr);

                    if (newMinAge < 18 || newMaxAge > 99 || newMinAge >= newMaxAge) {
                        showCustomToast("Geçersiz yaş aralığı! (18-99 arası, min < max)", false);
                        return;
                    }

                    // Yeni değerleri ata
                    minAge = newMinAge;
                    maxAge = newMaxAge;
                    CountryItem selectedCountryItem = (CountryItem) spinnerCountry.getSelectedItem();
                    selectedCountry = selectedCountryItem.getCountryName();
                    selectedGender = spinnerGender.getSelectedItem().toString(); // ✅ yeni

                    // Kaydet
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("minAge", minAge);
                    editor.putInt("maxAge", maxAge);
                    editor.putString("selectedCountry", selectedCountry);
                    editor.putString("selectedGender", selectedGender); // ✅ yeni
                    editor.apply();

                    updateFilterBadge();

                    // Başarı mesajı
                    String successMessage = "Filtreler güncellendi: " + minAge + "-" + maxAge + " yaş";
                    if (!selectedCountry.equals("Tümü")) {
                        successMessage += ", " + selectedCountry;
                    }
                    if (!selectedGender.equals("Tümü")) {
                        successMessage += ", " + selectedGender;
                    }

                    showCustomToast(successMessage, true);
                    dialog.dismiss();

                } catch (NumberFormatException e) {
                    showCustomToast("Geçerli bir yaş girin", false);
                }
            });
        });

        dialog.show();

        // Dialog giriş animasyonu
        View dialogContainer = dialog.getWindow().getDecorView();
        dialogContainer.setScaleX(0.8f);
        dialogContainer.setScaleY(0.8f);
        dialogContainer.setAlpha(0f);
        dialogContainer.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(300)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void showCustomToast(String message, boolean isSuccess) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void yeniUidSec() {
        DatabaseReference currentUserRef = FirebaseDatabase.getInstance()
                .getReference("Kullanıcılar")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String currentUserGender = snapshot.child("gender").getValue(String.class);
                    if (currentUserGender == null) {
                        hideLoadingShowError("Kullanıcının cinsiyet bilgisi eksik");
                        return;
                    }

                    String targetGender = currentUserGender.equals("Erkek") ? "Kadın" : "Erkek";
                    if (targetGender.equals("Erkek")){
                         userGender = "Kadın";
                    } else if (targetGender.equals("Kadın")) {
                        userGender = "Erkek";

                    }
                    findMatchingUsers(targetGender);
                } else {
                    hideLoadingShowError("Mevcut kullanıcı bilgisi bulunamadı");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideLoadingShowError("İşlem iptal edildi");
            }
        });
    }

    private void findMatchingUsers(String targetGender) {
        DatabaseReference allUsersRef = FirebaseDatabase.getInstance().getReference("Kullanıcılar");
        allUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> uidList = new ArrayList<>();

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userGender = userSnapshot.child("gender").getValue(String.class);
                    Object ageObj = userSnapshot.child("doğumTarihi").getValue();
                    String userCountry = userSnapshot.child("country").getValue(String.class);

                    if (userGender != null && ageObj != null) {
                        try {
                            int userAge = parseAge(ageObj);

                            boolean ageMatch = userAge >= minAge && userAge <= maxAge;
                            boolean countryMatch = selectedCountry.equals("Tümü") ||
                                    (userCountry != null && userCountry.equals(selectedCountry));

                            // ✅ Cinsiyet kontrolü: filtre + hedef cinsiyet
                            boolean genderMatch;
                            if (selectedGender.equals("Tümü")) {
                                genderMatch = userGender.equals(targetGender); // default karşı cins
                            } else {
                                genderMatch = userGender.equals(selectedGender); // filtrede seçilen cinsiyet
                            }

                            if (ageMatch && countryMatch && genderMatch) {
                                uidList.add(userSnapshot.getKey());
                            }
                        } catch (NumberFormatException e) {
                            continue;
                        }
                    }
                }

                if (!uidList.isEmpty()) {
                    Random random = new Random();
                    randomUid = uidList.get(random.nextInt(uidList.size()));
                    loadUserProfile(randomUid);
                } else {
                    hideLoadingShowNoResults();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideLoadingShowError("Veri yüklenirken hata oluştu");
            }
        });
    }

    private int parseAge(Object ageObj) throws NumberFormatException {
        if (ageObj instanceof Long) {
            return ((Long) ageObj).intValue();
        } else if (ageObj instanceof String) {
            return Integer.parseInt((String) ageObj);
        }
        throw new NumberFormatException("Geçersiz yaş formatı");
    }

    private void loadUserProfile(String uid) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Kullanıcılar").child(uid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                url = dataSnapshot.child("profileImage").getValue(String.class);
                name = dataSnapshot.child("kullanıcıAdı").getValue(String.class);
                String gender = dataSnapshot.child("gender").getValue(String.class);
                Object ageObj = dataSnapshot.child("doğumTarihi").getValue();

                int age = 0;
                if (ageObj != null) {
                    try {
                        age = parseAge(ageObj);
                    } catch (NumberFormatException e) {
                        age = 0;
                    }
                }

                // Daha iyi UX için minimum yükleme süresi
                int finalAge = age;
                mainHandler.postDelayed(() -> {
                    showMatchWithEntrance(url, name, gender, finalAge);
                }, 1500);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideLoadingShowError("Kullanıcı profili yüklenirken hata oluştu");
            }
        });
    }

    private void hideLoadingShowError(String message) {
        if (loadingContainer.getVisibility() == View.VISIBLE) {
            loadingContainer.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        loadingContainer.setVisibility(View.GONE);
                        showCustomToast(message, false);
                        resetToInitialState();
                    })
                    .start();
        }
    }

    private void hideLoadingShowNoResults() {
        if (loadingContainer.getVisibility() == View.VISIBLE) {
            loadingContainer.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        loadingContainer.setVisibility(View.GONE);
                        String message = "Belirlenen filtrelere uygun kullanıcı bulunamadı";
                        if (!selectedCountry.equals("Tümü")) {
                            message += "\n(Yaş: " + minAge + "-" + maxAge + ", Ülke: " + selectedCountry + ")";
                        } else {
                            message += "\n(Yaş: " + minAge + "-" + maxAge + ")";
                        }
                        showCustomToast(message, false);
                        resetToInitialState();
                    })
                    .start();
        }
    }

    private void resetToInitialState() {
        if (isFirstTime) {
            welcomeContainer.setVisibility(View.VISIBLE);
            welcomeContainer.setAlpha(0f);
            welcomeContainer.animate()
                    .alpha(1f)
                    .setDuration(400)
                    .start();
        }

        textView2.setVisibility(View.VISIBLE);
        btnMatch.setVisibility(View.VISIBLE);

        textView2.setAlpha(0f);
        btnMatch.setAlpha(0f);
        textView2.animate().alpha(1f).setDuration(400).start();
        btnMatch.animate().alpha(1f).setDuration(400).setStartDelay(100).start();
    }

    private void mesajAt() {
        Intent intent = new Intent(getActivity(), MessageActivity.class);
        intent.putExtra("uid", randomUid);
        intent.putExtra("u", url);
        intent.putExtra("n", name);
        startActivity(intent);

        // Geçiş animasyonu
        if (getActivity() != null) {
            getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Handler'ları ve animasyonları temizle
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }
    }
}