package com.lumoo;

import static com.lumoo.ProfileFragment.encodeToBase64;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lumoo.Model.HoroscopeItem;
import com.lumoo.ViewHolder.CountryItem;
import com.lumoo.ViewHolder.CountrySpinnerAdapter;
import com.lumoo.ViewHolder.HoroscopeSpinnerAdapter;
import com.lumoo.util.SecurityUtils;
import com.lumoo.util.ValidationUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PersonalInformationActivity extends AppCompatActivity {

    Bitmap selectedImageBitmap;

    private static final String SUPABASE_URL = "";
    private static final String SUPABASE_APIKEY = "";
    private static final String SUPABASE_STORAGE_ENDPOINT = SUPABASE_URL + "";

    private OkHttpClient httpClient;

    // Modern UI Components
    TextInputEditText edtName, edtSurname, edtUsername;
    EditText edtBirthdate;
    RelativeLayout btnContinue, btnContinueApp;
    FirebaseDatabase database;
    DatabaseReference table_user, databaseReference;
    CardView cardViewContainerImage;
    ImageView imgProfilePhotoPersonal;

    Spinner spGender, edtHoroscope;
    String base64Image;
    String[] genders = {"Cinsiyet Seçin", "Erkek", "Kadın"};

    TextView txt3;

    private static final int REQUEST_CODE_PROFILE_IMAGE = 101;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 8;
    private static final SecureRandom random = new SecureRandom();

    Spinner spinnerCountry;
    ImageView imageSelectedFlag;
    CountryItem selectedCountry;
    HoroscopeItem selectedHoroscope;

    // Animation variables
    private boolean isInitialAnimationComplete = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        setContentView(R.layout.activity_personal_information);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        httpClient = new OkHttpClient();

        initializeViews();
        setupCountryAndHoroscopeData();
        setupAnimations();
        init();
    }

    private void initializeViews() {
        // Modern TextInputEditText components
        edtName = findViewById(R.id.edtName);
        edtSurname = findViewById(R.id.edtSurname);
        edtUsername = findViewById(R.id.edtUsername);
        edtBirthdate = findViewById(R.id.edtBirthdate);
        edtHoroscope = findViewById(R.id.edtHoroscope);

        btnContinue = findViewById(R.id.btnContinue);
        btnContinueApp = findViewById(R.id.btnStarting);
        cardViewContainerImage = findViewById(R.id.personalInfoImgContainer);
        imgProfilePhotoPersonal = findViewById(R.id.personalInfoPhoto);
        spGender = findViewById(R.id.spGender);
        txt3 = findViewById(R.id.textUpper3);

        spinnerCountry = findViewById(R.id.spinnerCountry);
        imageSelectedFlag = findViewById(R.id.image_selected_flag);

        // Add modern text change animations
        setupTextChangeAnimations();
    }

    private void setupTextChangeAnimations() {
        TextWatcher modernTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                animateInputFeedback();
            }
        };

        edtName.addTextChangedListener(modernTextWatcher);
        edtSurname.addTextChangedListener(modernTextWatcher);
        edtUsername.addTextChangedListener(modernTextWatcher);
    }

    private void animateInputFeedback() {
        // Subtle scale animation for input feedback
        if (isInitialAnimationComplete) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(btnContinueApp, "scaleX", 1.0f, 1.02f, 1.0f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(btnContinueApp, "scaleY", 1.0f, 1.02f, 1.0f);
            scaleX.setDuration(200);
            scaleY.setDuration(200);
            scaleX.start();
            scaleY.start();
        }
    }

    private void setupCountryAndHoroscopeData() {
        List<CountryItem> countryList = new ArrayList<>();

        // Add countries with modern approach
        addCountriesToList(countryList);

        List<HoroscopeItem> horoscopeList = new ArrayList<>();
        addHoroscopesToList(horoscopeList);

        // Setup adapters
        CountrySpinnerAdapter countryAdapter = new CountrySpinnerAdapter(this, countryList);
        countryAdapter.setDropDownViewResource(R.layout.spinner_country_item);
        spinnerCountry.setAdapter(countryAdapter);

        HoroscopeSpinnerAdapter horoscopeAdapter = new HoroscopeSpinnerAdapter(this, horoscopeList);
        horoscopeAdapter.setDropDownViewResource(R.layout.spinner_horoscope_item);
        edtHoroscope.setAdapter(horoscopeAdapter);

        // Setup listeners with animations
        setupSpinnerListeners();

        // Setup gender spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGender.setAdapter(adapter);
    }



    private void setupSpinnerListeners() {
        spinnerCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCountry = (CountryItem) parent.getItemAtPosition(position);

                // Animate flag change
                animateFlagChange();

                int resId = getResources().getIdentifier(selectedCountry.getCountryCode(), "drawable", getPackageName());
                imageSelectedFlag.setImageResource(resId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        edtHoroscope.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedHoroscope = (HoroscopeItem) parent.getItemAtPosition(position);
                animateSelectionFeedback(parent);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void animateFlagChange() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(imageSelectedFlag, "scaleX", 0.8f, 1.1f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(imageSelectedFlag, "scaleY", 0.8f, 1.1f, 1.0f);
        scaleX.setDuration(300);
        scaleY.setDuration(300);
        scaleX.start();
        scaleY.start();
    }

    private void animateSelectionFeedback(View view) {
        ObjectAnimator pulse = ObjectAnimator.ofFloat(view, "alpha", 1.0f, 0.7f, 1.0f);
        pulse.setDuration(200);
        pulse.start();
    }

    private void setupAnimations() {
        // Initial fade-in animation for all components
        View rootView = findViewById(R.id.main);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                startInitialAnimations();
            }
        });
    }

    private void startInitialAnimations() {
        // Animate profile photo container
        animateProfilePhotoEntrance();

        // Animate form fields with staggered delays
        animateFormFieldsEntrance();

        // Animate button with final delay
        animateButtonEntrance();

        // Mark animations as complete
        new Handler().postDelayed(() -> isInitialAnimationComplete = true, 1500);
    }

    private void animateProfilePhotoEntrance() {
        cardViewContainerImage.setAlpha(0f);
        cardViewContainerImage.setScaleX(0.5f);
        cardViewContainerImage.setScaleY(0.5f);

        cardViewContainerImage.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void animateFormFieldsEntrance() {
        View[] formFields = {
                (View) findViewById(R.id.edtName).getParent().getParent(),
                (View) findViewById(R.id.edtSurname).getParent().getParent(),
                (View) findViewById(R.id.edtUsername).getParent().getParent(),
                (View) findViewById(R.id.edtBirthdate).getParent()
        };

        for (int i = 0; i < formFields.length; i++) {
            View field = (View) formFields[i];
            field.setAlpha(0f);
            field.setTranslationY(50f);

            field.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(400)
                    .setStartDelay(200 + (i * 100))
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
    }

    private void animateButtonEntrance() {
        btnContinueApp.setAlpha(0f);
        btnContinueApp.setTranslationY(30f);

        btnContinueApp.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(800)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void pickImageFromGallery(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PROFILE_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                selectedImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imgProfilePhotoPersonal.setImageBitmap(selectedImageBitmap);
                base64Image = encodeToBase64(selectedImageBitmap);

                // Animate photo selection success
                animatePhotoSelectionSuccess();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void animatePhotoSelectionSuccess() {
        // Success ripple animation
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(cardViewContainerImage, "scaleX", 1.0f, 1.1f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(cardViewContainerImage, "scaleY", 1.0f, 1.1f, 1.0f);

        scaleX.setDuration(300);
        scaleY.setDuration(300);
        scaleX.start();
        scaleY.start();

        // Hide instruction text with fade
        txt3.animate()
                .alpha(0f)
                .setDuration(200)
                .start();
    }

    private void init() {
        edtBirthdate.setOnClickListener(v -> showModernAgePicker());

        cardViewContainerImage.setOnClickListener(view -> {
            // Add click animation
            animateButtonPress(cardViewContainerImage);
            pickImageFromGallery(REQUEST_CODE_PROFILE_IMAGE);
        });

        btnContinueApp.setOnClickListener(v -> {
            // Add click animation
            animateButtonPress(btnContinueApp);

            // Validate and continue
            if (validateInputs()) {
                uploadImageToSupabase();
            }
        });
    }

    private void showModernAgePicker() {
        NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMinValue(18);
        numberPicker.setMaxValue(99);
        numberPicker.setWrapSelectorWheel(false);

        // width/height zorunlu
        numberPicker.setLayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        AlertDialog.Builder builder = new AlertDialog.Builder(this); // <- burayı sade bıraktık
        builder.setTitle("Yaşınızı Seçin");
        builder.setView(numberPicker);

        builder.setPositiveButton("Tamam", (dialog, which) -> {
            int selectedValue = numberPicker.getValue();
            edtBirthdate.setText(String.valueOf(selectedValue));
            animateInputSuccess(edtBirthdate);
        });

        builder.setNegativeButton("İptal", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void animateButtonPress(View button) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1.0f, 0.95f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1.0f, 0.95f, 1.0f);

        scaleX.setDuration(150);
        scaleY.setDuration(150);
        scaleX.start();
        scaleY.start();
    }

    private void animateInputSuccess(View input) {
        ObjectAnimator flash = ObjectAnimator.ofFloat(input, "alpha", 1.0f, 0.7f, 1.0f);
        flash.setDuration(200);
        flash.start();
    }

    private boolean validateInputs() {
        String name = edtName.getText().toString().trim();
        String surname = edtSurname.getText().toString().trim();
        String username = edtUsername.getText().toString().trim();
        String birthdate = edtBirthdate.getText().toString().trim();

        // ✅ ValidationUtils ile güvenli form kontrolü
        ValidationResult nameResult = ValidationUtils.validateName(name);
        if (!nameResult.isValid()) {
            showModernToast(nameResult.getMessage());
            animateValidationError();
            return false;
        }

        ValidationResult surnameResult = ValidationUtils.validateName(surname);
        if (!surnameResult.isValid()) {
            showModernToast(surnameResult.getMessage());
            animateValidationError();
            return false;
        }

        ValidationResult usernameResult = ValidationUtils.validateUsername(username);
        if (!usernameResult.isValid()) {
            showModernToast(usernameResult.getMessage());
            animateValidationError();
            return false;
        }

        if (TextUtils.isEmpty(birthdate) || selectedImageBitmap == null) {
            showModernToast("Lütfen tüm bilgileri doldurun");
            animateValidationError();
            return false;
        }

        // ✅ SecurityUtils ile güvenlik log'u
        SecurityUtils.logSecurityEvent("Profile Update", "User updating profile: " + username);

        return true;
    }

    private void animateValidationError() {
        // Shake animation for the button
        ObjectAnimator shakeX = ObjectAnimator.ofFloat(btnContinueApp, "translationX", 0, -10, 10, -10, 10, -5, 5, 0);
        shakeX.setDuration(500);
        shakeX.start();
    }

    private void showModernToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void uploadImageToSupabase() {
        if (selectedImageBitmap == null) {
            showModernToast("Önce bir fotoğraf seçin");
            return;
        }

        // Show loading animation
        animateLoadingState(true);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            showModernToast("Kullanıcı girişi yapılmamış");
            animateLoadingState(false);
            return;
        }

        String userId = currentUser.getUid();
        String fileName = userId + "_profile.jpg";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] imageData = baos.toByteArray();

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
                    animateLoadingState(false);
                    showModernToast("Fotoğraf yüklenemedi");
                    Log.e("Supabase", "Upload failed: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        animateLoadingState(false);
                        showModernToast("Fotoğraf yüklendi");
                        saveUserDataToFirebase(fileName);
                    });
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "No error body";
                    runOnUiThread(() -> {
                        animateLoadingState(false);
                        showModernToast("Upload hatası: " + response.code());
                        Log.e("Supabase", "Upload error: " + errorBody);
                    });
                }
            }
        });
    }

    private void animateLoadingState(boolean isLoading) {
        if (isLoading) {
            // Disable button and show loading animation
            btnContinueApp.setEnabled(false);
            btnContinueApp.setAlpha(0.7f);

            // Rotate animation
            ObjectAnimator rotation = ObjectAnimator.ofFloat(btnContinueApp, "rotation", 0f, 360f);
            rotation.setDuration(1000);
            rotation.setRepeatCount(ValueAnimator.INFINITE);
            rotation.start();
        } else {
            // Re-enable button and stop loading animation
            btnContinueApp.setEnabled(true);
            btnContinueApp.setAlpha(1.0f);
            btnContinueApp.clearAnimation();
            btnContinueApp.setRotation(0f);
        }
    }

    private void saveUserDataToFirebase(String imageFileName) {
        String name = edtName.getText().toString().trim();
        String surname = edtSurname.getText().toString().trim();
        String username = edtUsername.getText().toString().trim();
        String birthdate = edtBirthdate.getText().toString().trim();
        String gender = spGender.getSelectedItem().toString();

        if (selectedHoroscope == null) {
            showModernToast("Lütfen burç seçiniz.");
            return;
        }
        String horoscope = selectedHoroscope.getName();

        if (gender.equals("Cinsiyet Seçin")) {
            showModernToast("Lütfen cinsiyet seçiniz.");
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();
        String imageUrl = SUPABASE_URL + "/storage/v1/object/public/profile-images/" + imageFileName;
        String backgroundUrl = SUPABASE_URL + "/storage/v1/object/public/profile-images" + imageFileName ;
        database = FirebaseDatabase.getInstance();
        table_user = database.getReference("Kullanıcılar");

        HashMap<String, Object> userdata = new HashMap<>();
        userdata.put("ad", name);
        userdata.put("soyad", surname);
        userdata.put("kullanıcıAdı", username);
        userdata.put("doğumTarihi", birthdate);
        userdata.put("burç", horoscope);
        userdata.put("userId", userId);
        userdata.put("invitation", generateInviteCode());
        userdata.put("credit", "0");
        userdata.put("online", "online");
        userdata.put("profileImage", imageUrl);
        userdata.put("backgroundImage", backgroundUrl);
        userdata.put("gender", gender);
        userdata.put("country", selectedCountry.getCountryName());
        userdata.put("countryCode", selectedCountry.getCountryCode());
        userdata.put("frame", "");

        table_user.child(userId).setValue(userdata)
                .addOnSuccessListener(aVoid -> {
                    showModernToast("Veriler başarıyla kaydedildi");
                    animateSuccessAndNavigate();
                })
                .addOnFailureListener(e -> {
                    showModernToast("Veriler kaydedilirken hata oluştu: " + e.getMessage());
                });
    }

    private void animateSuccessAndNavigate() {
        // Success animation
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(btnContinueApp, "scaleX", 1.0f, 1.2f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(btnContinueApp, "scaleY", 1.0f, 1.2f, 1.0f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(findViewById(R.id.main), "alpha", 1.0f, 0.0f);

        scaleX.setDuration(300);
        scaleY.setDuration(300);
        alpha.setDuration(500);
        alpha.setStartDelay(300);

        scaleX.start();
        scaleY.start();
        alpha.start();

        alpha.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                Intent intent = new Intent(PersonalInformationActivity.this, ReadyActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    // Helper methods for data setup
    private void addCountriesToList(List<CountryItem> countryList) {
        countryList.add(new CountryItem("Afganistan", "af"));
        countryList.add(new CountryItem("Almanya", "de"));
        countryList.add(new CountryItem("Amerika Birleşik Devletleri", "us"));
        countryList.add(new CountryItem("Andorra", "ad"));
        countryList.add(new CountryItem("Angola", "ao"));
        countryList.add(new CountryItem("Arjantin", "ar"));
        countryList.add(new CountryItem("Arnavutluk", "al"));
        countryList.add(new CountryItem("Avustralya", "au"));
        countryList.add(new CountryItem("Avusturya", "at"));
        countryList.add(new CountryItem("Azerbaycan", "az"));

        countryList.add(new CountryItem("Bahreyn", "bh"));
        countryList.add(new CountryItem("Bangladeş", "bd"));
        countryList.add(new CountryItem("Belarus", "by"));
        countryList.add(new CountryItem("Belçika", "be"));
        countryList.add(new CountryItem("Benin", "bj"));
        countryList.add(new CountryItem("Birleşik Arap Emirlikleri", "ae"));
        countryList.add(new CountryItem("Bolivya", "bo"));
        countryList.add(new CountryItem("Bosna-Hersek", "ba"));
        countryList.add(new CountryItem("Brezilya", "br"));
        countryList.add(new CountryItem("Bulgaristan", "bg"));

        countryList.add(new CountryItem("Cezayir", "dz"));
        countryList.add(new CountryItem("Çekya", "cz"));
        countryList.add(new CountryItem("Çin", "cn"));
        countryList.add(new CountryItem("Danimarka", "dk"));
        countryList.add(new CountryItem("Dominik Cumhuriyeti", "do_"));
        countryList.add(new CountryItem("Ekvador", "ec"));
        countryList.add(new CountryItem("Endonezya", "id"));
        countryList.add(new CountryItem("Ermenistan", "am"));
        countryList.add(new CountryItem("Estonya", "ee"));
        countryList.add(new CountryItem("Etiyopya", "et"));

        countryList.add(new CountryItem("Fas", "ma"));
        countryList.add(new CountryItem("Fildişi Sahili", "ci"));
        countryList.add(new CountryItem("Filipinler", "ph"));
        countryList.add(new CountryItem("Finlandiya", "fi"));
        countryList.add(new CountryItem("Fransa", "fr"));
        countryList.add(new CountryItem("Gabon", "ga"));
        countryList.add(new CountryItem("Gana", "gh"));
        countryList.add(new CountryItem("Gine", "gn"));
        countryList.add(new CountryItem("Guatemala", "gt"));
        countryList.add(new CountryItem("Güney Afrika", "za"));

        countryList.add(new CountryItem("Güney Kore", "kr"));
        countryList.add(new CountryItem("Gürcistan", "ge"));
        countryList.add(new CountryItem("Haiti", "ht"));
        countryList.add(new CountryItem("Hindistan", "in"));
        countryList.add(new CountryItem("Hollanda", "nl"));
        countryList.add(new CountryItem("Honduras", "hn"));
        countryList.add(new CountryItem("Irak", "iq"));
        countryList.add(new CountryItem("İngiltere", "gb"));
        countryList.add(new CountryItem("İran", "ir"));
        countryList.add(new CountryItem("İrlanda", "ie"));

        countryList.add(new CountryItem("İspanya", "es"));
        countryList.add(new CountryItem("İsrail", "il"));
        countryList.add(new CountryItem("İsveç", "se"));
        countryList.add(new CountryItem("İsviçre", "ch"));
        countryList.add(new CountryItem("İtalya", "it"));
        countryList.add(new CountryItem("İzlanda", "is"));
        countryList.add(new CountryItem("Jamaika", "jm"));
        countryList.add(new CountryItem("Japonya", "jp"));
        countryList.add(new CountryItem("Kamboçya", "kh"));
        countryList.add(new CountryItem("Kamerun", "cm"));

        countryList.add(new CountryItem("Kanada", "ca"));
        countryList.add(new CountryItem("Katar", "qa"));
        countryList.add(new CountryItem("Kazakistan", "kz"));
        countryList.add(new CountryItem("Kenya", "ke"));
        countryList.add(new CountryItem("Kırgızistan", "kg"));
        countryList.add(new CountryItem("Kolombiya", "co"));
        countryList.add(new CountryItem("Kongo", "cg"));
        countryList.add(new CountryItem("Kosta Rika", "cr"));
        countryList.add(new CountryItem("Kuveyt", "kw"));
        countryList.add(new CountryItem("Küba", "cu"));

        countryList.add(new CountryItem("Letonya", "lv"));
        countryList.add(new CountryItem("Libya", "ly"));
        countryList.add(new CountryItem("Litvanya", "lt"));
        countryList.add(new CountryItem("Lübnan", "lb"));
        countryList.add(new CountryItem("Macaristan", "hu"));
        countryList.add(new CountryItem("Madagaskar", "mg"));
        countryList.add(new CountryItem("Malezya", "my"));
        countryList.add(new CountryItem("Meksika", "mx"));
        countryList.add(new CountryItem("Mısır", "eg"));
        countryList.add(new CountryItem("Moğolistan", "mn"));

        countryList.add(new CountryItem("Moldova", "md"));
        countryList.add(new CountryItem("Nepal", "np"));
        countryList.add(new CountryItem("Nijerya", "ng"));
        countryList.add(new CountryItem("Norveç", "no"));
        countryList.add(new CountryItem("Özbekistan", "uz"));
        countryList.add(new CountryItem("Pakistan", "pk"));
        countryList.add(new CountryItem("Panama", "pa"));
        countryList.add(new CountryItem("Paraguay", "py"));
        countryList.add(new CountryItem("Peru", "pe"));
        countryList.add(new CountryItem("Polonya", "pl"));

        countryList.add(new CountryItem("Portekiz", "pt"));
        countryList.add(new CountryItem("Romanya", "ro"));
        countryList.add(new CountryItem("Rusya", "ru"));
        countryList.add(new CountryItem("Senegal", "sn"));
        countryList.add(new CountryItem("Sırbistan", "rs"));
        countryList.add(new CountryItem("Slovakya", "sk"));
        countryList.add(new CountryItem("Slovenya", "si"));
        countryList.add(new CountryItem("Sri Lanka", "lk"));
        countryList.add(new CountryItem("Sudan", "sd"));
        countryList.add(new CountryItem("Suriye", "sy"));

        countryList.add(new CountryItem("Suudi Arabistan", "sa"));
        countryList.add(new CountryItem("Şili", "cl"));
        countryList.add(new CountryItem("Tayland", "th"));
        countryList.add(new CountryItem("Tunus", "tn"));
        countryList.add(new CountryItem("Türkiye", "tr"));
        countryList.add(new CountryItem("Türkmenistan", "tm"));
        countryList.add(new CountryItem("Ukrayna", "ua"));
        countryList.add(new CountryItem("Umman", "om"));
        countryList.add(new CountryItem("Uruguay", "uy"));
        countryList.add(new CountryItem("Ürdün", "jo"));

        countryList.add(new CountryItem("Venezuela", "ve"));
        countryList.add(new CountryItem("Vietnam", "vn"));
        countryList.add(new CountryItem("Yemen", "ye"));
        countryList.add(new CountryItem("Yeni Zelanda", "nz"));
        countryList.add(new CountryItem("Yunanistan", "gr"));
    }

    private void addHoroscopesToList(List<HoroscopeItem> horoscopeList) {

        horoscopeList.add(new HoroscopeItem("Koç","koc"));
        horoscopeList.add(new HoroscopeItem("Boğa","boga"));
        horoscopeList.add(new HoroscopeItem("İkizler","ikizler"));
        horoscopeList.add(new HoroscopeItem("Yengeç","yengec"));
        horoscopeList.add(new HoroscopeItem("Aslan","aslan"));
        horoscopeList.add(new HoroscopeItem("Başak","basak"));
        horoscopeList.add(new HoroscopeItem("Terazi","terazi"));
        horoscopeList.add(new HoroscopeItem("Akrep","akrep"));
        horoscopeList.add(new HoroscopeItem("Oğlak","oglak"));
        horoscopeList.add(new HoroscopeItem("Yay","yay"));
        horoscopeList.add(new HoroscopeItem("Balık","balik"));
        horoscopeList.add(new HoroscopeItem("Kova","kova"));
    }
    public static String generateInviteCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(index));
        }
        return code.toString();
    }
}
