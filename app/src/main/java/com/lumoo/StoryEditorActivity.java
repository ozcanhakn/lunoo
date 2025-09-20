package com.lumoo;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lumoo.FCM.FCMNotificationSender;
import com.lumoo.Model.AllUser;
import com.lumoo.Model.StoryMember;
import com.lumoo.ViewHolder.StickerGridAdapter;
import com.lumoo.ViewHolder.StoryPersonAdapter;
import com.lumoo.ViewHolder.UserAdapter;
import com.lumoo.util.ImageUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.annotations.Nullable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StoryEditorActivity extends AppCompatActivity {

    private static final String SUPABASE_URL = "https://iauuehrfhmzhnfsnsjdx.supabase.co";
    private static final String SUPABASE_APIKEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlhdXVlaHJmaG16aG5mc25zamR4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTc4NjQ2NzAsImV4cCI6MjA3MzQ0MDY3MH0.GnwTJFqC_cLAuKt7dAlSjlVIBfy4O9nTVWyn3d2wzRM";
    private static final String SUPABASE_STORAGE_ENDPOINT = SUPABASE_URL + "/storage/v1/object/stories/";

    private List<StoryTag> storyTags = new ArrayList<>();

    private View currentDraggingTag = null;
    private float dX, dY;
    private int lastAction;

    private StoryPersonAdapter userAdapter;
    private List<AllUser> allUsers = new ArrayList<>();

    private int currentPage = 0;
    private static final int PAGE_SIZE = 20;
    private boolean isLoading = false;
    private boolean hasMoreUsers = true;

    private EditText hiddenInput;
    private RelativeLayout hiddenInputContainer;

    // UI Components
    RecyclerView recAddPerson;
    EditText searchAddPersonStory;
    ImageView imageView, trashBinIcon;
    RelativeLayout buttonUpload, addTextButton, drawButton, stickerButton, btnAddPerson, panel_add_person;
    ProgressBar progressBar;
    RelativeLayout mainContainer;
    SeekBar brushSizeSeekBar;
    DrawingView drawingView;

    // Text editing components
    View textEditPanel, drawingPanel, stickerPanel;
    EditText emojiSearch;
    ImageView confirmTextButton, cancelTextButton;
    RelativeLayout confirmDrawingButton, cancelDrawingButton, clearDrawingButton, undoDrawingButton;
    RelativeLayout closeStickerButton;
    GridView emojiGridView;

    // New UI components for text editing
    private HorizontalScrollView dynamicListScroll;
    private LinearLayout dynamicListContainer;
    private LinearLayout fixedListContainer;
    private RelativeLayout btnFontSelector;
    private RelativeLayout btnColorPalette;

    // Category tabs
    TextView tabSmileys, tabHearts, tabAnimals, tabFood, tabObjects, recentEmojisLabel;
    TextView[] categoryTabs;
    int selectedCategory = 0;

    // Story data
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    String posturi, url, name, type, currentUid;
    StoryMember storyMember;
    Bitmap originalBitmap, editedBitmap;
    boolean isDrawingMode = false;

    // Text overlay data
    List<TextOverlay> textOverlays = new ArrayList<>();
    List<EmojiSticker> emojiStickers = new ArrayList<>();
    TextOverlay currentTextOverlay;
    TextOverlay selectedTextOverlay = null; // SeÃ§ili metin
    int selectedTextColor = Color.WHITE;
    boolean waitingForStickerPlacement = false;
    String selectedEmoji = "";


    //private HorizontalScrollView textStylesScroll;
    private boolean isKeyboardVisible = false;

    // Color options
    int[] colorOptions = {
            Color.WHITE, Color.BLACK, Color.RED, Color.BLUE,
            Color.GREEN, Color.YELLOW, Color.MAGENTA, Color.CYAN,
            Color.parseColor("#FF6B35"), Color.parseColor("#004225"),
            Color.parseColor("#8E44AD"), Color.parseColor("#E74C3C"),
            Color.parseColor("#FF00FF"), Color.parseColor("#00FFFF"), Color.parseColor("#FFA500")
    };

    // Yeni deÄŸiÅŸkenler
    private boolean isMovingText = false;
    private boolean isMovingSticker = false;
    private EmojiSticker selectedEmojiSticker = null;
    private float lastTouchX, lastTouchY;

    private float scaleFactor = 1.0f;
    private float rotationAngle = 0.0f;
    private ScaleGestureDetector scaleGestureDetector;


    // Yeni deÄŸiÅŸkenler ekleyelim
    private boolean isScaling = false;
    private boolean isRotating = false;
    private int selectedFont = 0; // 0: Normal, 1: KalÄ±n, 2: EÄŸik, vs.

    // Font seÃ§enekleri
    private Typeface[] fontOptions = {
            Typeface.DEFAULT,
            Typeface.DEFAULT_BOLD,
            Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC),
            Typeface.MONOSPACE,
            Typeface.create("sans-serif-light", Typeface.NORMAL),
            Typeface.create("sans-serif-condensed", Typeface.BOLD),
            Typeface.create("serif", Typeface.ITALIC),
            Typeface.create("cursive", Typeface.BOLD),
            Typeface.create("sans-serif", Typeface.NORMAL),
            Typeface.create("serif-monospace", Typeface.BOLD_ITALIC)
    };

    // Ä°ki parmakla zoom/rotate iÃ§in deÄŸiÅŸkenler
    private float startScaleFactor = 1.0f;
    private float startRotationAngle = 0.0f;

    // Variables for trash bin functionality
    private boolean isLongPressDetected = false;
    private static final long LONG_PRESS_TIMEOUT = 500; // 500ms for long press
    private Runnable longPressRunnable;
    private Handler longPressHandler = new Handler();

    // Yeni deÄŸiÅŸkenler ekleyelim
    private float lastTouchOffsetX = 0;
    private float lastTouchOffsetY = 0;

    // Mevcut gÃ¶sterilen liste tÃ¼rÃ¼ (font veya renk)
    private enum ListType {FONT, COLOR}

    private ListType currentListType = ListType.FONT;
    Vibrator vibrator;

    private int selectedStickerResId = 0; // Seçilen sticker'ın resource ID'si


    // Yeni değişkenler
    private boolean isTextMovementDetected = false;
    private static final int MOVEMENT_THRESHOLD = 10; // 10px hareket eşiği
    private static final long PANEL_OPEN_DELAY = 200; // 200ms panel açma gecikmesi
    private Handler panelOpenHandler = new Handler();
    private Runnable panelOpenRunnable;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_story_editor);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.story_editor), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        initViews();
        //loadImageFromBundle();
        loadImageFromIntent();
        setupClickListeners();
        getCurrentUser();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());


        recAddPerson.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new StoryPersonAdapter(this, allUsers);
        recAddPerson.setAdapter(userAdapter);

        // Tıklama listener'ını ekleyelim
        userAdapter.setOnUserClickListener(new StoryPersonAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(AllUser user) {
                addTag(user);
            }
        });
        // Set up search functionality

        fetchUsersFromFirebase();

        // Arama dinleyicisi
        setupSearchListener();
    }

    // ScaleListener'ı da sticker için minimum scale'i düzelt
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            isScaling = true;

            if (selectedEmojiSticker != null) {
                startScaleFactor = selectedEmojiSticker.scale;
            } else if (selectedTextOverlay != null) {
                startScaleFactor = selectedTextOverlay.scale;
            }

            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();

            // Sticker ölçekleme - minimum 0.0625f (32px) maksimum 0.5f (256px)
            if (selectedEmojiSticker != null) {
                selectedEmojiSticker.scale *= scaleFactor;
                selectedEmojiSticker.scale = Math.max(0.0625f, Math.min(selectedEmojiSticker.scale, 0.5f));
                updateImageWithOverlaysAndStickers();
            }

            // Text ölçekleme
            else if (selectedTextOverlay != null) {
                selectedTextOverlay.scale *= scaleFactor;
                selectedTextOverlay.scale = Math.max(0.5f, Math.min(selectedTextOverlay.scale, 3.0f));
                updateImageWithOverlaysAndStickers();
            }

            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            isScaling = false;
        }
    }

    private void initViews() {
        storyMember = new StoryMember();
        buttonUpload = findViewById(R.id.btn_story_up);
        progressBar = findViewById(R.id.pb_storyUp);
        imageView = findViewById(R.id.iv_storyUp);
        trashBinIcon = findViewById(R.id.trash_bin_icon); // Initialize trash bin icon
        mainContainer = findViewById(R.id.story_editor);
        addTextButton = findViewById(R.id.btn_add_text);
        drawButton = findViewById(R.id.btn_draw);
        stickerButton = findViewById(R.id.btn_sticker);
        drawingView = findViewById(R.id.drawing_view);


        recAddPerson = findViewById(R.id.recAddPerson);
        btnAddPerson = findViewById(R.id.btn_add_person);
        panel_add_person = findViewById(R.id.panel_add_person);
        searchAddPersonStory = findViewById(R.id.searchEditTextStory);


        hiddenInput = findViewById(R.id.hidden_input);
        // Hidden input ayarlarÄ±nÄ± gÃ¼ncelleyin
        hiddenInput.setCursorVisible(true);
        hiddenInput.setTextColor(Color.WHITE); // Beyaz metin rengi
        hiddenInput.setBackgroundColor(Color.TRANSPARENT);
        hiddenInput.setTextSize(18);
        hiddenInput.setHighlightColor(Color.TRANSPARENT); // SeÃ§im rengi ÅŸeffaf

// Cursor rengini beyaz yapmak iÃ§in
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            hiddenInput.setTextCursorDrawable(ContextCompat.getDrawable(this, R.drawable.cursor_white));
        }
        // InputType'Ä± dÃ¼zenleme
        hiddenInput.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES |
                InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        // IME options
        hiddenInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
        hiddenInputContainer = findViewById(R.id.hidden_input_container);

        textEditPanel = findViewById(R.id.text_edit_panel);
        //textStylesScroll = findViewById(R.id.text_styles_scroll);

        // Text editing panel
        textEditPanel = findViewById(R.id.text_edit_panel);
        //confirmTextButton = findViewById(R.id.btn_confirm_text);
        //cancelTextButton = findViewById(R.id.btn_cancel_text);

        // New UI components
        dynamicListScroll = findViewById(R.id.dynamic_list_scroll);
        dynamicListContainer = findViewById(R.id.dynamic_list_container);
        fixedListContainer = findViewById(R.id.fixed_list_container);
        btnFontSelector = findViewById(R.id.btn_font_selector);
        btnColorPalette = findViewById(R.id.btn_color_palette);

        // Drawing panel
        drawingPanel = findViewById(R.id.drawing_panel);
        confirmDrawingButton = findViewById(R.id.btn_confirm_drawing);
        cancelDrawingButton = findViewById(R.id.btn_cancel_drawing);
        clearDrawingButton = findViewById(R.id.btn_clear_drawing);
        undoDrawingButton = findViewById(R.id.btn_undo_drawing);
        brushSizeSeekBar = findViewById(R.id.seekbar_brush_size);

        // Sticker panel
        stickerPanel = findViewById(R.id.sticker_panel);
        emojiGridView = findViewById(R.id.emoji_grid);
        closeStickerButton = findViewById(R.id.btn_close_sticker);
        emojiSearch = findViewById(R.id.emoji_search); // Initialize search field
        recentEmojisLabel = findViewById(R.id.recent_emojis_label); // Initialize recent label

        // Category tabs
        tabSmileys = findViewById(R.id.tab_smileys);
        tabHearts = findViewById(R.id.tab_hearts);
        tabAnimals = findViewById(R.id.tab_animals);
        tabFood = findViewById(R.id.tab_food);
        tabObjects = findViewById(R.id.tab_objects);

        categoryTabs = new TextView[]{tabSmileys, tabHearts, tabAnimals, tabFood, tabObjects};

        setupFixedList(); // Sabit listeyi ayarla
        //setupDynamicList(); // Dinamik listeyi ayarla
        setupEmojiGrid();
        setupStickerGrid();
        setupCategoryTabs();

        setupKeyboardListener();

        textEditPanel.setVisibility(View.GONE);
        drawingPanel.setVisibility(View.GONE);
        stickerPanel.setVisibility(View.GONE);
        drawingView.setVisibility(View.GONE);

        // Hidden input iÃ§in listener'lar
        hiddenInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Sadece paneli kapat, metin zaten otomatik kaydediliyor
                hideTextEditPanel();
                return true;
            }
            return false;
        });

        hiddenInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (currentTextOverlay != null) {
                    currentTextOverlay.text = s.toString();
                    updateImageWithOverlaysAndStickers();

                    // Otomatik kaydetme - her karakter yazÄ±ldÄ±ÄŸÄ±nda gÃ¼ncelle
                    if (!s.toString().trim().isEmpty()) {
                        // EÄŸer bu yeni bir metinse ve henÃ¼z listeye eklenmediyse ekle
                        if (!textOverlays.contains(currentTextOverlay)) {
                            textOverlays.add(currentTextOverlay);
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // Klavye durumunu dinleme metodu
    private void setupKeyboardListener() {
        final View activityRootView = findViewById(R.id.story_editor);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            activityRootView.getWindowVisibleDisplayFrame(r);
            int screenHeight = activityRootView.getRootView().getHeight();

            // Klavye yÃ¼ksekliÄŸi - daha hassas hesaplama
            int keypadHeight = screenHeight - r.bottom;

            // Klavye gÃ¶rÃ¼nÃ¼r mÃ¼ kontrol et (daha hassas threshold)
            boolean isKeyboardNowVisible = keypadHeight > screenHeight * 0.15; // %15'ten bÃ¼yÃ¼kse

            if (isKeyboardNowVisible != isKeyboardVisible) {
                isKeyboardVisible = isKeyboardNowVisible;

                if (isKeyboardNowVisible && textEditPanel.getVisibility() == View.VISIBLE) {
                    // Klavye aÃ§Ä±ldÄ±ÄŸÄ±nda paneli klavyenin tam Ã¼stÃ¼ne taÅŸÄ±
                    adjustLayoutForKeyboard(keypadHeight);
                } else if (!isKeyboardVisible) {
                    // Klavye kapandÄ±ÄŸÄ±nda normal pozisyona dÃ¶n
                    resetLayout();
                }
            } else if (isKeyboardNowVisible && textEditPanel.getVisibility() == View.VISIBLE) {
                // Klavye zaten aÃ§Ä±k ama yÃ¼ksekliÄŸi deÄŸiÅŸmiÅŸ olabilir
                adjustLayoutForKeyboard(keypadHeight);
            }
        });
    }

    private void adjustLayoutForKeyboard(int keyboardHeight) {
        // EkranÄ±n alt kÄ±smÄ±ndaki navigation bar yÃ¼ksekliÄŸini hesaba katalÄ±m
        int navigationBarHeight = 0;
        @SuppressLint("InternalInsetResource") int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            navigationBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        // Klavye yÃ¼ksekliÄŸinden navigation bar'Ä± Ã§Ä±karalÄ±m
        int actualKeyboardHeight = keyboardHeight - navigationBarHeight;

        // Panelin konumunu klavyenin tam Ã¼stÃ¼ne taÅŸÄ±
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) textEditPanel.getLayoutParams();
        params.bottomMargin = actualKeyboardHeight;
        textEditPanel.setLayoutParams(params);

        // Hemen layout'u gÃ¼ncelle
        textEditPanel.requestLayout();
    }

    private void resetLayout() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) textEditPanel.getLayoutParams();
        params.bottomMargin = 0; // Margin'i sÄ±fÄ±rla
        textEditPanel.setLayoutParams(params);
        textEditPanel.requestLayout();
    }

    private void showTextEditPanel() {
        textEditPanel.setVisibility(View.VISIBLE);
        hiddenInputContainer.setVisibility(View.VISIBLE);

        // EÄŸer bir metin seÃ§ildiyse, onu dÃ¼zenle
        if (selectedTextOverlay != null) {
            currentTextOverlay = selectedTextOverlay;
        }
        // Yeni metin ekleme durumu
        else if (currentTextOverlay == null) {
            currentTextOverlay = new TextOverlay();
            currentTextOverlay.text = "";

            // Metni klavye aÃ§Ä±ldÄ±ktan sonra gÃ¶rÃ¼nÃ¼r alanÄ±n ortasÄ±na yerleÅŸtir
            if (editedBitmap != null) {
                // Ekran yÃ¼ksekliÄŸi ve klavye alanÄ±nÄ± hesapla
                int screenHeight = getResources().getDisplayMetrics().heightPixels;
                int keyboardHeight = screenHeight / 3; // YaklaÅŸÄ±k klavye yÃ¼ksekliÄŸi
                int visibleHeight = screenHeight - keyboardHeight;

                // GÃ¶rÃ¼nÃ¼r alanÄ±n ortasÄ±nÄ± hesapla
                currentTextOverlay.x = editedBitmap.getWidth() / 2f;
                currentTextOverlay.y = (visibleHeight / 2f) * (editedBitmap.getHeight() / (float) screenHeight);
            }

            currentTextOverlay.color = selectedTextColor;
            currentTextOverlay.textSize = 80;
            currentTextOverlay.fontIndex = selectedFont;
        }

        // Hidden input'u ayarla
        hiddenInput.setText(currentTextOverlay.text);

        // Cursor'Ä± gÃ¶rÃ¼nÃ¼r yap ve ayarla
        hiddenInput.setCursorVisible(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            hiddenInput.setTextCursorDrawable(null); // VarsayÄ±lan cursor kullan
        }
        hiddenInput.setSelection(hiddenInput.getText().length()); // Sonuna pozisyonla

        hiddenInput.requestFocus();

        // Ã–nce layout'u resetle
        resetLayout();

        // Sonra klavyeyi aÃ§ (biraz gecikmeyle)
        new Handler().postDelayed(() -> {
            showKeyboardWithDelay();
        }, 100);

        // VarsayÄ±lan olarak font listesini gÃ¶ster
        showFontList();

        // GÃ¶rÃ¼ntÃ¼yÃ¼ gÃ¼ncelle
        updateImageWithOverlaysAndStickers();
    }

    // Dinamik listeyi ayarla (fontlar iÃ§in)
    private void setupDynamicList() {
        dynamicListContainer.removeAllViews();

        String[] fontNames = {"Normal", "Kalın", "Eğik", "Mono", "İnce", "Yoğun", "Elegant", "Fancy", "Simple", "Creative"};

        for (int i = 0; i < fontOptions.length; i++) {
            // LayoutInflater ile font item'Ä± inflate et
            View fontView = getLayoutInflater().inflate(R.layout.item_font_option, dynamicListContainer, false);
            TextView fontTextView = fontView.findViewById(R.id.font_item);

            fontTextView.setText(fontNames[i]);
            fontTextView.setTypeface(fontOptions[i]);

            final int fontIndex = i;
            fontView.setOnClickListener(v -> {
                selectedFont = fontIndex;

                // TÃ¼m font item'larÄ±nÄ±n arka planÄ±nÄ± sÄ±fÄ±rla
                for (int j = 0; j < dynamicListContainer.getChildCount(); j++) {
                    View child = dynamicListContainer.getChildAt(j);
                    TextView childText = child.findViewById(R.id.font_item);
                    childText.setBackgroundResource(R.drawable.text_style_unselected_bg);
                    childText.setTextColor(Color.WHITE);
                }

                // SeÃ§ili item'Ä±n arka planÄ±nÄ± deÄŸiÅŸtir
                TextView selectedText = v.findViewById(R.id.font_item);
                selectedText.setBackgroundResource(R.drawable.text_style_selected_bg);
                selectedText.setTextColor(Color.BLACK);

                // YENÄ°: Sadece aktif olarak dÃ¼zenlenen metnin fontunu deÄŸiÅŸtir
                if (currentTextOverlay != null) {
                    currentTextOverlay.fontIndex = fontIndex;
                    updateImageWithOverlaysAndStickers();
                }

                // YENÄ°: SeÃ§ili metin overlay'Ä± varsa onu da gÃ¼ncelle (tÃ¼mÃ¼nÃ¼ deÄŸil)
                if (selectedTextOverlay != null) {
                    selectedTextOverlay.fontIndex = fontIndex;
                    updateImageWithOverlaysAndStickers();
                }
            });

            // Layout params ayarla
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 8, 0);
            fontView.setLayoutParams(params);

            dynamicListContainer.addView(fontView);
        }

        // Ä°lk font'u seÃ§ili yap
        if (dynamicListContainer.getChildCount() > 0) {
            View firstChild = dynamicListContainer.getChildAt(0);
            TextView firstText = firstChild.findViewById(R.id.font_item);
            firstText.setBackgroundResource(R.drawable.text_style_selected_bg);
            firstText.setTextColor(Color.BLACK);
        }
    }

    // Sabit listeyi ayarla
    private void setupFixedList() {
        // Font seÃ§ici butonuna click listener ekle
        btnFontSelector.setOnClickListener(v -> {
            showFontList();
        });

        // Renk paleti butonuna click listener ekle
        btnColorPalette.setOnClickListener(v -> {
            showColorList();
        });
    }

    // Font listesini göster
    private void showFontList() {
        currentListType = ListType.FONT;
        setupDynamicList(); // Font listesini yÃ¼kle

        // Buton arka planlarÄ±nÄ± gÃ¼ncelle
        btnFontSelector.setBackgroundResource(R.drawable.modern_tab_selected_bg);
        btnColorPalette.setBackgroundResource(R.drawable.modern_tab_unselected_bg);
    }

    // Renk listesini gÃ¶ster
    private void showColorList() {
        currentListType = ListType.COLOR;
        setupColorList(); // Renk listesini yÃ¼kle

        // Buton arka planlarÄ±nÄ± gÃ¼ncelle
        btnFontSelector.setBackgroundResource(R.drawable.modern_tab_unselected_bg);
        btnColorPalette.setBackgroundResource(R.drawable.modern_tab_selected_bg);
    }

    private void setupColorList() {
        dynamicListContainer.removeAllViews();

        for (int i = 0; i < colorOptions.length; i++) {
            // Yuvarlak renk view'i direkt oluÅŸtur
            View colorView = new View(this);
            final int color = colorOptions[i];

            // Yuvarlak background drawable oluÅŸtur
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(color);
            drawable.setStroke(1, Color.BLACK); // Beyaz border

            colorView.setBackground(drawable);

            colorView.setOnClickListener(v -> {
                selectedTextColor = color;

                // TÃ¼m renk item'larÄ±nÄ±n border'Ä±nÄ± sÄ±fÄ±rla
                for (int j = 0; j < dynamicListContainer.getChildCount(); j++) {
                    View child = dynamicListContainer.getChildAt(j);
                    GradientDrawable childDrawable = new GradientDrawable();
                    childDrawable.setShape(GradientDrawable.OVAL);
                    childDrawable.setColor(colorOptions[j]);
                    childDrawable.setStroke(6, Color.WHITE);
                    child.setBackground(childDrawable);
                }

                // SeÃ§ili item'Ä±n border'Ä±nÄ± kalÄ±nlaÅŸtÄ±r
                GradientDrawable selectedDrawable = new GradientDrawable();
                selectedDrawable.setShape(GradientDrawable.OVAL);
                selectedDrawable.setColor(color);
                selectedDrawable.setStroke(6, Color.BLACK); // SarÄ± kalÄ±n border
                v.setBackground(selectedDrawable);

                if (currentTextOverlay != null) {
                    currentTextOverlay.color = color;
                    updateImageWithOverlaysAndStickers();
                }

                if (selectedTextOverlay != null) {
                    selectedTextOverlay.color = color;
                    updateImageWithOverlaysAndStickers();
                }

                if (isDrawingMode) {
                    drawingView.setPaintColor(color);
                }
            });

            // Layout params ayarla - yuvarlak iÃ§in
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(80, 80);
            params.setMargins(0, 0, 16, 0);
            colorView.setLayoutParams(params);

            dynamicListContainer.addView(colorView);
        }

        // Ä°lk rengi seÃ§ili yap
        if (dynamicListContainer.getChildCount() > 0) {
            View firstChild = dynamicListContainer.getChildAt(0);
            GradientDrawable selectedDrawable = new GradientDrawable();
            selectedDrawable.setShape(GradientDrawable.OVAL);
            selectedDrawable.setColor(colorOptions[0]);
            selectedDrawable.setStroke(6, Color.BLACK);
            firstChild.setBackground(selectedDrawable);
        }
    }

    private void setupEmojiGrid() {
        String[] emojis = EmojiData.getEmojisByCategory(0);
        EmojiGridAdapter adapter = new EmojiGridAdapter(this, emojis, emoji -> {
            selectedEmoji = emoji;
            waitingForStickerPlacement = true;
            stickerPanel.setVisibility(View.GONE);
            Toast.makeText(this, "Emoji eklemek iÃ§in resme dokunun", Toast.LENGTH_SHORT).show();
        });
        emojiGridView.setAdapter(adapter);
    }

    private void setupCategoryTabs() {
        for (int i = 0; i < categoryTabs.length; i++) {
            final int category = i;
            categoryTabs[i].setOnClickListener(v -> selectCategory(category));
        }
        selectCategory(0);
    }

    private void selectCategory(int category) {
        selectedCategory = category;

        // Update tab backgrounds
        for (int i = 0; i < categoryTabs.length; i++) {
            if (i == category) {
                categoryTabs[i].setBackgroundResource(R.drawable.modern_tab_selected_bg);
            } else {
                categoryTabs[i].setBackgroundResource(R.drawable.modern_tab_unselected_bg);
            }
        }

        // Update sticker grid - DEĞİŞTİ
        int[] stickers = StickerData.getStickersByCategory(category);
        if (emojiGridView.getAdapter() instanceof StickerGridAdapter) {
            StickerGridAdapter adapter = (StickerGridAdapter) emojiGridView.getAdapter();
            adapter.updateStickers(stickers);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupClickListeners() {
        buttonUpload.setOnClickListener(v -> {
            // Scale animation with ripple effect
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                    .withEndAction(() -> {
                        v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100)
                                .withEndAction(() -> uploadStory());
                    });
        });

        // Add click animations to buttons
        addTextButton.setOnClickListener(v -> {
            // Add scale animation
            panel_add_person.setVisibility(View.GONE);
            stickerPanel.setVisibility(View.GONE);
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                    .withEndAction(() -> {
                        v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100);
                        showTextEditPanel();
                    });
        });

        // Buton iÃ§in touch listener ekleyelim
        buttonUpload.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                    break;
            }
            return false;
        });

        drawButton.setOnClickListener(v -> {
            // Add scale animation
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                    .withEndAction(() -> {
                        v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100);
                        showDrawingPanel();
                    });
        });

        stickerButton.setOnClickListener(v -> {
            // Add scale animation
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                    .withEndAction(() -> {
                        v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100);
                        showStickerPanel();
                    });
        });

        btnAddPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                panel_add_person.setVisibility(View.VISIBLE);
                //Toast.makeText(StoryEditorActivity.this, "Yakında Aktif...", Toast.LENGTH_SHORT).show();
                stickerPanel.setVisibility(View.GONE);
                textEditPanel.setVisibility(View.GONE);
            }
        });

        // Drawing controls
        confirmDrawingButton.setOnClickListener(v -> {
            // Add scale animation
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                    .withEndAction(() -> {
                        v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100);
                        confirmDrawing();
                    });
        });

        cancelDrawingButton.setOnClickListener(v -> {
            // Add scale animation
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                    .withEndAction(() -> {
                        v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100);
                        cancelDrawing();
                    });
        });

        clearDrawingButton.setOnClickListener(v -> {
            // Add scale animation
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                    .withEndAction(() -> {
                        v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100);
                        drawingView.clearCanvas();
                    });
        });

        undoDrawingButton.setOnClickListener(v -> {
            // Add scale animation
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                    .withEndAction(() -> {
                        v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100);
                        drawingView.undoLastPath();
                    });
        });

        brushSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float brushSize = 5 + (progress * 0.5f);
                    drawingView.setBrushSize(brushSize);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Image touch listener - GÜNCELLENMİŞ VERSİYON
// Image touch listener - GÜNCELLENMİŞ VERSİYON
// Image touch listener - SINIRLAMASIZ VERSİYON
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX();
                float y = event.getY();
                float[] bitmapCoords = getBitmapCoordinates(x, y);

                // Scale gesture detector'ı çağır
                scaleGestureDetector.onTouchEvent(event);

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        lastTouchX = x;
                        lastTouchY = y;
                        isTextMovementDetected = false;

                        // Long press detection
                        startLongPressDetection(bitmapCoords);

                        // Sticker placement
                        if (waitingForStickerPlacement && selectedStickerResId != 0) {
                            if (bitmapCoords != null) {
                                EmojiSticker newSticker = new EmojiSticker(selectedStickerResId, bitmapCoords[0], bitmapCoords[1]);
                                emojiStickers.add(newSticker);
                                updateImageWithOverlaysAndStickers();

                                selectedEmojiSticker = newSticker;
                                waitingForStickerPlacement = false;
                                selectedStickerResId = 0;
                            }
                            return true;
                        }

                        // Element seçimi
                        if (!isDrawingMode && bitmapCoords != null) {
                            // Önce sticker kontrolü
                            if (checkStickerSelection(bitmapCoords[0], bitmapCoords[1])) {
                                isMovingSticker = true;
                                selectedTextOverlay = null; // Text seçimini temizle
                                return true;
                            }
                            // Sonra text kontrolü
                            else if (checkTextSelection(bitmapCoords[0], bitmapCoords[1])) {
                                isMovingText = true;
                                selectedEmojiSticker = null; // Sticker seçimini temizle
                                startPanelOpenTimer();
                                return true;
                            }
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        float deltaX = x - lastTouchX;
                        float deltaY = y - lastTouchY;

                        // Hareket threshold kontrolü
                        if (Math.abs(deltaX) > MOVEMENT_THRESHOLD || Math.abs(deltaY) > MOVEMENT_THRESHOLD) {
                            isTextMovementDetected = true;
                            cancelPanelOpenTimer(); // Panel açmayı iptal et
                        }

                        // Text hareketi - SINIRLAMA YOK
                        if (isMovingText && selectedTextOverlay != null && bitmapCoords != null) {
                            // Offset değerlerini kullanarak doğru konumu hesapla
                            selectedTextOverlay.x = bitmapCoords[0] - lastTouchOffsetX;
                            selectedTextOverlay.y = bitmapCoords[1] - lastTouchOffsetY;

                            updateImageWithOverlaysAndStickers();
                            checkTrashBinVisibility(x, y);
                        }

                        // Sticker hareketi - SINIRLAMA YOK
                        else if (isMovingSticker && selectedEmojiSticker != null && bitmapCoords != null) {
                            // Offset değerlerini kullanarak doğru konumu hesapla
                            selectedEmojiSticker.x = bitmapCoords[0] - lastTouchOffsetX;
                            selectedEmojiSticker.y = bitmapCoords[1] - lastTouchOffsetY;

                            updateImageWithOverlaysAndStickers();
                            checkTrashBinVisibility(x, y);
                        }

                        lastTouchX = x;
                        lastTouchY = y;
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_CANCEL:
                        cancelLongPressDetection();
                        cancelPanelOpenTimer();
                        checkTrashBinDrop(x, y);

                        // Kısa tıklama ve hareket yoksa paneli aç
                        if (!isTextMovementDetected && selectedTextOverlay != null &&
                                Math.abs(x - lastTouchX) < MOVEMENT_THRESHOLD &&
                                Math.abs(y - lastTouchY) < MOVEMENT_THRESHOLD) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    currentTextOverlay = selectedTextOverlay;
                                    showTextEditPanel();
                                }
                            }, 100);
                        }

                        // Flags'leri sıfırla
                        isMovingText = false;
                        isMovingSticker = false;
                        isScaling = false;
                        isRotating = false;
                        isTextMovementDetected = false;
                        break;

                    case MotionEvent.ACTION_POINTER_DOWN:
                        // İki parmak gesture başlangıcı
                        if (event.getPointerCount() == 2) {
                            if (selectedEmojiSticker != null) {
                                isScaling = true;
                                startScaleFactor = selectedEmojiSticker.scale;
                            } else if (selectedTextOverlay != null) {
                                isScaling = true;
                                startScaleFactor = selectedTextOverlay.scale;
                            }
                        }
                        break;
                }

                return isMovingText || isMovingSticker || isScaling || isRotating ||
                        waitingForStickerPlacement || !isDrawingMode;
            }
        });
        trashBinIcon.setOnClickListener(v -> {
            // Delete the selected item with animation
            v.animate()
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(100)
                                .start();

                        if (selectedTextOverlay != null) {
                            textOverlays.remove(selectedTextOverlay);
                            selectedTextOverlay = null;
                            Toast.makeText(this, "Metin silindi", Toast.LENGTH_SHORT).show();
                        } else if (selectedEmojiSticker != null) {
                            emojiStickers.remove(selectedEmojiSticker);
                            selectedEmojiSticker = null;
                            Toast.makeText(this, "Emoji silindi", Toast.LENGTH_SHORT).show();
                        }

                        updateImageWithOverlaysAndStickers();

                        // Ã‡Ã¶p kutusunu gizle
                        v.setVisibility(View.GONE);
                    })
                    .start();
        });

        setupDrawingColorPalette();
    }

    // Panel açma zamanlayıcısını başlat
    private void startPanelOpenTimer() {
        cancelPanelOpenTimer();
        panelOpenRunnable = () -> {
            if (!isTextMovementDetected && selectedTextOverlay != null) {
                runOnUiThread(() -> {
                    currentTextOverlay = selectedTextOverlay;
                    showTextEditPanel();
                });
            }
        };
        panelOpenHandler.postDelayed(panelOpenRunnable, PANEL_OPEN_DELAY);
    }

    // Panel açma zamanlayıcısını iptal et
    private void cancelPanelOpenTimer() {
        if (panelOpenRunnable != null) {
            panelOpenHandler.removeCallbacks(panelOpenRunnable);
            panelOpenRunnable = null;
        }
    }


    // TaÅŸÄ±ma sÄ±rasÄ±nda Ã§Ã¶p kutusu gÃ¶rÃ¼nÃ¼rlÃ¼ÄŸÃ¼nÃ¼ kontrol et
    private void checkTrashBinVisibility(float x, float y) {
        // Ã‡Ã¶p kutusu ekran koordinatlarÄ±nÄ± al
        int[] trashBinLocation = new int[2];
        trashBinIcon.getLocationOnScreen(trashBinLocation);
        trashBinIcon.setVisibility(View.VISIBLE);


        // Dokunma noktasÄ±nÄ± ekran koordinatlarÄ±na Ã§evir
        int[] imageViewLocation = new int[2];
        imageView.getLocationOnScreen(imageViewLocation);
        float screenX = x + imageViewLocation[0];
        float screenY = y + imageViewLocation[1];

        // Ã‡Ã¶p kutusu boyutlarÄ±
        int trashBinWidth = trashBinIcon.getWidth();
        int trashBinHeight = trashBinIcon.getHeight();

        // Ã‡Ã¶p kutusu alanÄ±na yakÄ±n mÄ± kontrol et
        float distanceX = Math.abs(screenX - (trashBinLocation[0] + trashBinWidth / 2));
        float distanceY = Math.abs(screenY - (trashBinLocation[1] + trashBinHeight / 2));

        // EÅŸik deÄŸeri (Ã§Ã¶p kutusuna yakÄ±nlÄ±k)
        float threshold = 150f;

        if (distanceX < threshold && distanceY < threshold) {
            // Ã‡Ã¶p kutusuna yakÄ±n, bÃ¼yÃ¼tme efekti uygula ve gÃ¶ster
            trashBinIcon.setVisibility(View.VISIBLE);
            trashBinIcon.animate()
                    .scaleX(1.5f)
                    .scaleY(1.5f)
                    .setDuration(200)
                    .start();

            // TitreÅŸim efekti de ekleyebiliriz (isteÄŸe baÄŸlÄ±)
            trashBinIcon.animate()
                    .translationXBy(5f)
                    .setDuration(50)
                    .withEndAction(() -> {
                        trashBinIcon.animate()
                                .translationXBy(-10f)
                                .setDuration(50)
                                .withEndAction(() -> {
                                    trashBinIcon.animate()
                                            .translationXBy(5f)
                                            .setDuration(50)
                                            .start();
                                })
                                .start();
                    })
                    .start();
        } else {
            // Ã‡Ã¶p kutusundan uzak, normal boyuta dÃ¶ndÃ¼r
            trashBinIcon.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(200)
                    .start();
        }
    }

    @Override
    public void onBackPressed() {
        if (textEditPanel.getVisibility() == View.VISIBLE) {
            hideTextEditPanel();
        } else if (drawingPanel.getVisibility() == View.VISIBLE) {
            hideDrawingPanel();
        } else if (stickerPanel.getVisibility() == View.VISIBLE) {
            stickerPanel.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    // Yeni metin ekleme metodu
    private void addNewTextAtPosition(float x, float y) {
        // Yeni bir metin overlay oluÅŸtur
        TextOverlay newTextOverlay = new TextOverlay();
        newTextOverlay.text = ""; // BoÅŸ baÅŸlat
        newTextOverlay.x = x;
        newTextOverlay.y = y;
        newTextOverlay.color = selectedTextColor;
        newTextOverlay.textSize = 80;
        newTextOverlay.fontIndex = selectedFont;

        // Current overlay olarak ayarla
        currentTextOverlay = newTextOverlay;

        // Text edit panelini aÃ§
        showTextEditPanel();
    }


    private void setupDrawingColorPalette() {
        RelativeLayout drawingColorPalette = findViewById(R.id.drawing_color_palette);
        drawingColorPalette.removeAllViews();

        for (int i = 0; i < colorOptions.length; i++) {
            View colorView = new View(this);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(80, 80);
            params.setMargins(10, 0, 10, 0);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.leftMargin = i * 90;
            colorView.setLayoutParams(params);
            colorView.setBackgroundColor(colorOptions[i]);

            final int color = colorOptions[i];
            colorView.setOnClickListener(v -> {
                selectedTextColor = color;
                drawingView.setPaintColor(color);

                // SeÃ§ili rengi gÃ¶ster
                for (int j = 0; j < drawingColorPalette.getChildCount(); j++) {
                    View child = drawingColorPalette.getChildAt(j);
                    if (child.getBackground() instanceof ColorDrawable) {
                        ColorDrawable cd = (ColorDrawable) child.getBackground();
                        if (cd.getColor() == color) {
                            child.setBackgroundResource(R.drawable.color_selected_border);
                        } else {
                            child.setBackgroundColor(colorOptions[j]);
                        }
                    }
                }
            });

            drawingColorPalette.addView(colorView);
        }
    }

    // checkTextSelection metodunu güncelle - ölçekleme ve hareket için
    private boolean checkTextSelection(float x, float y) {
        for (TextOverlay overlay : textOverlays) {
            // Create a paint object to measure text accurately
            Paint paint = new Paint();
            paint.setTypeface(fontOptions[overlay.fontIndex]);
            paint.setTextSize(overlay.textSize * overlay.scale);

            // Measure text dimensions
            float textWidth = paint.measureText(overlay.text);
            Paint.FontMetrics fm = paint.getFontMetrics();
            float textHeight = fm.bottom - fm.top;

            // Metnin merkez noktasÄ±
            float centerX = overlay.x;
            float centerY = overlay.y;

            // SeÃ§im alanÄ±nÄ± Ã§ok daha dar yap - sadece %10 padding
            float selectionPaddingX = textWidth * 0.01f;  // %10 geniÅŸlik
            float selectionPaddingY = textHeight * 0.01f; // %10 yÃ¼kseklik

            // Ã‡ok daha hassas seÃ§im alanÄ±
            if (x >= centerX - textWidth / 2 - selectionPaddingX &&
                    x <= centerX + textWidth / 2 + selectionPaddingX &&
                    y >= centerY - textHeight / 2 - selectionPaddingY &&
                    y <= centerY + textHeight / 2 + selectionPaddingY) {

                selectedTextOverlay = overlay;
                scaleFactor = overlay.scale;
                rotationAngle = overlay.rotation;

                // Dokunulan nokta ile merkez arasÄ±ndaki farkÄ± kaydet
                lastTouchOffsetX = x - centerX;
                lastTouchOffsetY = y - centerY;

                return true;
            }
        }
        selectedTextOverlay = null;
        return false;
    }

    private float[] getBitmapCoordinates(float touchX, float touchY) {
        if (editedBitmap == null || imageView == null) return null;

        ImageView imageView = this.imageView;
        float[] values = new float[9];
        imageView.getImageMatrix().getValues(values);

        float scaleX = values[Matrix.MSCALE_X];
        float scaleY = values[Matrix.MSCALE_Y];
        float transX = values[Matrix.MTRANS_X];
        float transY = values[Matrix.MTRANS_Y];

        // CENTER_CROP için koordinat dönüşümü
        float bitmapX = (touchX - transX) / scaleX;
        float bitmapY = (touchY - transY) / scaleY;

        // CENTER_CROP'ta bitmap'in bir kısmı ekran dışında kalabilir
        // Bu yüzden sınır kontrolleri önemli
        if (bitmapX >= 0 && bitmapX <= editedBitmap.getWidth() &&
                bitmapY >= 0 && bitmapY <= editedBitmap.getHeight()) {
            return new float[]{bitmapX, bitmapY};
        }

        return null;
    }

    // checkStickerSelection metodunu güncelle - daha geniş dokunma alanı
    private boolean checkStickerSelection(float x, float y) {
        for (EmojiSticker sticker : emojiStickers) {
            Bitmap stickerBitmap = BitmapFactory.decodeResource(getResources(), sticker.stickerResId);
            if (stickerBitmap == null) continue;

            float scaledWidth = stickerBitmap.getWidth() * sticker.scale;
            float scaledHeight = stickerBitmap.getHeight() * sticker.scale;

            // Dokunma alanını %50 büyüt
            float touchPadding = Math.max(scaledWidth, scaledHeight) * 0.5f;

            float left = sticker.x - (scaledWidth / 2) - touchPadding;
            float right = sticker.x + (scaledWidth / 2) + touchPadding;
            float top = sticker.y - (scaledHeight / 2) - touchPadding;
            float bottom = sticker.y + (scaledHeight / 2) + touchPadding;

            if (x >= left && x <= right && y >= top && y <= bottom) {
                selectedEmojiSticker = sticker;
                scaleFactor = sticker.scale;

                // Offset hesaplamasını SIFIRLA - merkeze dokunma için
                lastTouchOffsetX = 0; // x - sticker.x; yerine 0
                lastTouchOffsetY = 0; // y - sticker.y; yerine 0

                return true;
            }
        }
        selectedEmojiSticker = null;
        return false;
    }


    @Override
    protected void onPause() {
        super.onPause();
        // Klavyeyi kapat
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(hiddenInput.getWindowToken(), 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // EÄŸer text paneli aÃ§Ä±ksa klavyeyi tekrar aÃ§
        if (textEditPanel.getVisibility() == View.VISIBLE) {
            showKeyboardWithDelay();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (textEditPanel.getVisibility() == View.VISIBLE &&
                event.getAction() == KeyEvent.ACTION_DOWN) {

            // Alfabetik karakterleri ve rakamlarÄ± yakala
            if ((keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) ||
                    (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) ||
                    keyCode == KeyEvent.KEYCODE_SPACE) {

                if (currentTextOverlay != null) {
                    // Karakteri metne ekle
                    char pressedChar = (char) event.getUnicodeChar();
                    currentTextOverlay.text += pressedChar;
                    updateImageWithOverlaysAndStickers();
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    private void hideTextEditPanel() {
        textEditPanel.setVisibility(View.GONE);
        hiddenInputContainer.setVisibility(View.INVISIBLE);

        // Klavyeyi kapat
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(hiddenInput.getWindowToken(), 0);
        }

        // Sadece selectedTextOverlay'Ä± sÄ±fÄ±rla, currentTextOverlay'Ä± koru
        selectedTextOverlay = null;

        // EÄŸer metin boÅŸsa ve listeye eklenmiÅŸse, listeden kaldÄ±r
        if (currentTextOverlay != null && currentTextOverlay.text.trim().isEmpty()) {
            textOverlays.remove(currentTextOverlay);
        }
    }


    private void showKeyboardWithDelay() {
        new Handler().postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(hiddenInput, InputMethodManager.SHOW_IMPLICIT);

                // Cursor'Ä± gÃ¶rÃ¼nÃ¼r yap
                hiddenInput.setSelection(hiddenInput.getText().length());
            }
        }, 200);
    }

    private void showDrawingPanel() {
        isDrawingMode = true;
        drawingPanel.setVisibility(View.VISIBLE);
        drawingView.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.VISIBLE);

        brushSizeSeekBar.setProgress(20);
        drawingView.setBrushSize(15f);
        drawingView.setPaintColor(selectedTextColor);


        drawingView.setBackgroundColor(Color.TRANSPARENT);
    }

    private void confirmDrawing() {
        if (drawingView.hasDrawing()) {
            combineImageWithDrawing();
        }
        hideDrawingPanel();

        // Ã‡izimi kalÄ±cÄ± hale getir ve canvas'Ä± temizle
        if (drawingView.hasDrawing()) {
            // Ã‡izimi bitmap'e kaydet
            Bitmap drawingBitmap = drawingView.getDrawingBitmap();
            if (drawingBitmap != null && editedBitmap != null) {
                Bitmap combined = Bitmap.createBitmap(
                        editedBitmap.getWidth(),
                        editedBitmap.getHeight(),
                        Bitmap.Config.ARGB_8888
                );

                Canvas canvas = new Canvas(combined);
                canvas.drawBitmap(editedBitmap, 0, 0, null);
                canvas.drawBitmap(drawingBitmap, 0, 0, null);

                editedBitmap = combined;
                imageView.setImageBitmap(editedBitmap);
            }
            drawingView.clearCanvas();
        }
    }

    private void cancelDrawing() {
        drawingView.clearCanvas();
        hideDrawingPanel();
    }

    private void hideDrawingPanel() {
        isDrawingMode = false;
        drawingPanel.setVisibility(View.GONE);
        drawingView.setVisibility(View.GONE);
        imageView.setVisibility(View.VISIBLE);
    }

    private void combineImageWithDrawing() {
        if (editedBitmap != null && drawingView.hasDrawing()) {
            Bitmap drawingBitmap = drawingView.getDrawingBitmap();

            Bitmap combinedBitmap = Bitmap.createBitmap(
                    editedBitmap.getWidth(),
                    editedBitmap.getHeight(),
                    Bitmap.Config.ARGB_8888
            );

            Canvas canvas = new Canvas(combinedBitmap);
            // Ã–nce orijinal fotoÄŸrafÄ± Ã§iz
            canvas.drawBitmap(editedBitmap, 0, 0, null);

            // Sonra Ã§izimleri Ã¼zerine Ã§iz (transparent background ile)
            if (drawingBitmap != null) {
                canvas.drawBitmap(drawingBitmap, 0, 0, null);
            }

            editedBitmap = combinedBitmap;
            imageView.setImageBitmap(editedBitmap);
            drawingView.clearCanvas();
        }
    }

    private void showStickerPanel() {
        stickerPanel.setVisibility(View.VISIBLE);
    }

    private void updateImageWithOverlaysAndStickers() {
        if (originalBitmap == null) return;

        editedBitmap = Bitmap.createBitmap(
                originalBitmap.getWidth(),
                originalBitmap.getHeight(),
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(editedBitmap);
        canvas.drawBitmap(originalBitmap, 0, 0, null);

        // Yüksek kaliteli çizim için paint ayarları
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);

        // Draw emoji stickers
        for (EmojiSticker sticker : emojiStickers) {
            Bitmap stickerBitmap = BitmapFactory.decodeResource(getResources(), sticker.stickerResId);
            if (stickerBitmap != null) {
                Bitmap scaledSticker = Bitmap.createScaledBitmap(
                        stickerBitmap,
                        (int)(stickerBitmap.getWidth() * sticker.scale),
                        (int)(stickerBitmap.getHeight() * sticker.scale),
                        true
                );

                canvas.save();
                canvas.rotate(sticker.rotation, sticker.x, sticker.y);

                float left = sticker.x - (scaledSticker.getWidth() / 2);
                float top = sticker.y - (scaledSticker.getHeight() / 2);

                left = Math.max(0, Math.min(left, editedBitmap.getWidth() - scaledSticker.getWidth()));
                top = Math.max(0, Math.min(top, editedBitmap.getHeight() - scaledSticker.getHeight()));

                canvas.drawBitmap(scaledSticker, left, top, paint);
                canvas.restore();

                scaledSticker.recycle();
            }
        }

        // Draw text overlays
        Paint textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setFilterBitmap(true);
        textPaint.setDither(true);
        textPaint.setStyle(Paint.Style.FILL);

        for (TextOverlay overlay : textOverlays) {
            int actualTextSize = Math.min(overlay.textSize, overlay.maxTextSize);
            textPaint.setTypeface(fontOptions[overlay.fontIndex]);
            textPaint.setColor(overlay.color);
            textPaint.setTextSize(actualTextSize * overlay.scale);
            textPaint.setSubpixelText(true);

            canvas.save();
            canvas.rotate(overlay.rotation, overlay.x, overlay.y);

            Paint.FontMetrics fm = textPaint.getFontMetrics();
            float baseline = overlay.y - (fm.ascent + fm.descent) / 2;
            canvas.drawText(overlay.text, overlay.x, baseline, textPaint);
            canvas.restore();
        }

        // DRAW STORY TAGS - YENİ EKLENDİ
        drawStoryTags(canvas);

        imageView.setImageBitmap(editedBitmap);
    }

    // Story tag'leri çiz
    private void drawStoryTags(Canvas canvas) {
        if (storyTags.isEmpty()) return;

        // Tag stili
        int backgroundColor = Color.argb(200, 41, 128, 185); // Mavi arka plan
        int textColor = Color.BLACK;
        int textSize = 36; // Bitmap boyutuna göre ayarlayın

        Paint bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint.setColor(backgroundColor);
        bgPaint.setStyle(Paint.Style.FILL);

        Paint textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setTextAlign(Paint.Align.CENTER);

        Paint borderPaint = new Paint();
        borderPaint.setAntiAlias(true);
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2);

        for (StoryTag tag : storyTags) {
            if (!tag.isVisible) continue;

            // Metin boyutlarını hesapla
            float textWidth = textPaint.measureText(tag.username);
            Rect textBounds = new Rect();
            textPaint.getTextBounds(tag.username, 0, tag.username.length(), textBounds);
            float textHeight = textBounds.height();

            // Padding ve köşe yuvarlaklığı
            float horizontalPadding = 20;
            float verticalPadding = 12;
            float cornerRadius = 25;

            // Dikdörtgen koordinatları
            float left = tag.x - textWidth / 2 - horizontalPadding;
            float top = tag.y - textHeight / 2 - verticalPadding;
            float right = tag.x + textWidth / 2 + horizontalPadding;
            float bottom = tag.y + textHeight / 2 + verticalPadding;

            // Arkaplanı çiz
            canvas.drawRoundRect(left, top, right, bottom, cornerRadius, cornerRadius, bgPaint);

            // Border çiz
            canvas.drawRoundRect(left, top, right, bottom, cornerRadius, cornerRadius, borderPaint);

            // Metni çiz (ortala)
            float textY = tag.y + (textBounds.height() / 2) - textPaint.descent();
            canvas.drawText(tag.username, tag.x, textY, textPaint);
        }
    }

    private void getCurrentUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUid = user.getUid();
        }
    }

    private void uploadStory() {
        String caption = "";
        Calendar cdate = Calendar.getInstance();
        SimpleDateFormat currentdate = new SimpleDateFormat("dd-MMM-yyyy");
        String savedate = currentdate.format(cdate.getTime());

        Calendar ctime = Calendar.getInstance();
        SimpleDateFormat currenttime = new SimpleDateFormat("HH:mm:ss");
        String savetime = currenttime.format(ctime.getTime());

        String time = savedate + ":" + savetime;

        if (editedBitmap == null) {
            Toast.makeText(this, "Görsel hazır değil", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Benzersiz dosya adı oluştur
        String fileName = "story_" + System.currentTimeMillis() + ".png";

        // Supabase'e yükle
        uploadToSupabase(editedBitmap, fileName, new SupabaseUploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                // Firebase'e URL'yi kaydet
                saveStoryToFirebase(imageUrl, caption, time);
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(StoryEditorActivity.this, "Yükleme hatası: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void saveStoryToFirebase(String imageUrl, String caption, String time) {
        FirebaseDatabase database1 = FirebaseDatabase.getInstance();
        DatabaseReference reference = database1.getReference("Kullanıcılar").child(currentUid);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                name = snapshot.child("ad").getValue(String.class);
                url = snapshot.child("profileImage").getValue(String.class);

                if (name == null || name.trim().isEmpty()) {
                    name = "Bilinmeyen Kullanıcı";
                }
                if (url == null) {
                    url = "";
                }

                long timeEnd = System.currentTimeMillis() + 86400000;
                if (!storyTags.isEmpty()) {
                    List<String> taggedUsernames = new ArrayList<>();
                    for (StoryTag tag : storyTags) {
                        taggedUsernames.add(tag.username.replace("@", ""));
                    }
                    storyMember.setTaggedUsers(taggedUsernames);
                }
                storyMember.setCaption(caption);
                storyMember.setName(name);
                storyMember.setUrl(url);
                storyMember.setPostUri(imageUrl); // Supabase URL'sini kullan
                storyMember.setUid(currentUid);
                storyMember.setTimeEnd(timeEnd);
                storyMember.setTimeUpload(time);
                storyMember.setType("image");

                saveMyStory();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(StoryEditorActivity.this, "Kullanıcı bilgileri alınamadı", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveMyStory() {
        DatabaseReference myStoryRef = FirebaseDatabase.getInstance()
                .getReference("Kullanıcılar")
                .child(currentUid)
                .child("Story")
                .push();

        String storyId = myStoryRef.getKey(); // Story ID'sini al

        myStoryRef.setValue(storyMember).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Etiketlenen kullanıcılara bildirim gönder
                if (storyTags != null && !storyTags.isEmpty()) {
                    sendTagNotifications(storyId);
                }

                saveToFriends();
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(StoryEditorActivity.this, "Story kaydedilemedi", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void sendTagNotifications(String storyId) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Kullanıcılar");

        for (StoryTag tag : storyTags) {
            String taggedUsername = tag.username.replace("@", "");

            // Kullanıcı adına göre kullanıcıyı bul
            Query userQuery = usersRef.orderByChild("kullanıcıAdı").equalTo(taggedUsername);
            userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String receiverUid = userSnapshot.getKey();
                        String receiverName = userSnapshot.child("ad").getValue(String.class);

                        if (receiverUid != null && !receiverUid.equals(currentUid)) {
                            // Bildirim mesajı oluştur
                            String message = name + " sizi bir story'sinde etiketledi!";

                            // Bildirim gönder
                            sendNotification(receiverUid, receiverName, message);

                            // Etiket bilgisini kullanıcının veritabanına da kaydet (isteğe bağlı)
                            saveTagInfoToUser(receiverUid, storyId);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Notification", "Kullanıcı bulunamadı: " + taggedUsername);
                }
            });
        }
    }
    private void saveTagInfoToUser(String taggedUserId, String storyId) {
        DatabaseReference tagRef = FirebaseDatabase.getInstance()
                .getReference("Kullanıcılar")
                .child(taggedUserId)
                .child("taggedStories")
                .child(storyId);

        Map<String, Object> tagInfo = new HashMap<>();
        tagInfo.put("taggerUid", currentUid);
        tagInfo.put("taggerName", name);
        tagInfo.put("timestamp", System.currentTimeMillis());
        tagInfo.put("storyUrl", storyMember.getPostUri());

        tagRef.setValue(tagInfo);
    }

    private void sendNotification(String receiver_uid, String receiver_name, String message1) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens")
                .child(receiver_uid).child("token");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userToken = snapshot.getValue(String.class);
                String title = "Seni Etiketledi";
                String message = message1;

                FCMNotificationSender notificationSender = new FCMNotificationSender(userToken, title, message, getApplicationContext());
                notificationSender.sendNotification();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void saveToFriends() {
        DatabaseReference friendsRef = FirebaseDatabase.getInstance()
                .getReference("Kullanıcılarr")
                .child(currentUid)
                .child("Friends");

        friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot friendSnapshot : dataSnapshot.getChildren()) {
                    String friendUid = friendSnapshot.getKey();
                    if (friendUid != null && !friendUid.equals(currentUid)) {
                        DatabaseReference friendStoryRef = FirebaseDatabase.getInstance()
                                .getReference("Kullanıcılar")
                                .child(friendUid)
                                .child("Story")
                                .push();
                        friendStoryRef.setValue(storyMember);
                    }
                }

                progressBar.setVisibility(View.GONE);
                Toast.makeText(StoryEditorActivity.this, "Story Oluşturuldu", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(StoryEditorActivity.this, "Story YÃ¼klendi (sadece sizde)", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private static class TextOverlay {
        String text = "";
        float x = 0;
        float y = 0;
        int color = Color.WHITE;
        int textSize = 80;
        int maxTextSize = 150;
        float scale = 1.0f; // Bu önemli - başlangıç değeri
        float rotation = 0.0f;
        int fontIndex = 0;
        Typeface typeface = Typeface.DEFAULT;
        Paint.Align alignment = Paint.Align.CENTER;

        public void setTextSize(int size) {
            this.textSize = Math.min(size, maxTextSize);
        }
    }


    private static class EmojiSticker {
        int stickerResId;
        float x;
        float y;
        float scale = 0.25f; // Daha büyük ölçek için (512px * 0.25 = 128px)
        float rotation = 0.0f;

        public EmojiSticker(int stickerResId, float x, float y) {
            this.stickerResId = stickerResId;
            this.x = x;
            this.y = y;
            this.scale = 0.25f; // Daha iyi kalite için ölçeği artır
        }
    }


    // Emoji data class
    public static class EmojiData {
        public static final String[] SMILEYS = {
                "ðŸ˜€", "ðŸ˜ƒ", "ðŸ˜„", "ðŸ˜", "ðŸ˜†", "ðŸ˜…", "ðŸ˜‚", "ðŸ¤£", "ðŸ˜Š", "ðŸ˜‡",
                "ðŸ™‚", "ðŸ™ƒ", "ðŸ˜‰", "ðŸ˜Œ", "ðŸ˜", "ðŸ¥°", "ðŸ˜˜", "ðŸ˜—", "ðŸ˜™", "ðŸ˜š",
                "ðŸ˜‹", "ðŸ˜›", "ðŸ˜", "ðŸ˜œ", "ðŸ¤ª", "ðŸ¤¨", "ðŸ§", "ðŸ¤“", "ðŸ˜Ž", "ðŸ¤©",
                "ðŸ¥³", "ðŸ˜", "ðŸ˜’", "ðŸ˜ž", "ðŸ˜”", "ðŸ˜Ÿ", "ðŸ˜•", "ðŸ™", "â˜¹ï¸", "ðŸ˜£"
        };

        public static final String[] HEARTS = {
                "â¤ï¸", "ðŸ§¡", "ðŸ’›", "ðŸ’š", "ðŸ’™", "ðŸ’œ", "ðŸ–¤", "ðŸ¤", "ðŸ¤Ž", "ðŸ’”",
                "â£ï¸", "ðŸ’•", "ðŸ’ž", "ðŸ’“", "ðŸ’—", "ðŸ’–", "ðŸ’˜", "ðŸ’", "ðŸ’Ÿ", "â™¥ï¸",
                "ðŸ’", "ðŸŒ¹", "ðŸŒ¸", "ðŸŒº", "ðŸŒ»", "ðŸŒ·", "ðŸŒ¼", "ðŸŒµ", "ðŸŽ€", "ðŸ’Ž"
        };

        public static final String[] ANIMALS = {
                "ðŸ¶", "ðŸ±", "ðŸ­", "ðŸ¹", "ðŸ°", "ðŸ¦Š", "ðŸ»", "ðŸ¼", "ðŸ¨", "ðŸ¯",
                "ðŸ¦", "ðŸ®", "ðŸ·", "ðŸ½", "ðŸ¸", "ðŸµ", "ðŸ™ˆ", "ðŸ™‰", "ðŸ™Š", "ðŸ’",
                "ðŸ”", "ðŸ§", "ðŸ¦", "ðŸ¤", "ðŸ£", "ðŸ¥", "ðŸ¦†", "ðŸ¦…", "ðŸ¦‰", "ðŸ¦‡",
                "ðŸº", "ðŸ—", "ðŸ´", "ðŸ¦„", "ðŸ", "ðŸ›", "ðŸ¦‹", "ðŸŒ", "ðŸž", "ðŸœ"
        };

        public static final String[] FOOD = {
                "ðŸ", "ðŸŽ", "ðŸ", "ðŸŠ", "ðŸ‹", "ðŸŒ", "ðŸ‰", "ðŸ‡", "ðŸ“", "ðŸˆ",
                "ðŸ’", "ðŸ‘", "ðŸ¥­", "ðŸ", "ðŸ¥¥", "ðŸ¥", "ðŸ…", "ðŸ†", "ðŸ¥‘", "ðŸ¥¦",
                "ðŸŒ¶ï¸", "ðŸŒ½", "ðŸ¥•", "ðŸ¥’", "ðŸ¥¬", "ðŸ¥”", "ðŸ ", "ðŸ¥œ", "ðŸŒ°", "ðŸž",
                "ðŸ¥", "ðŸ¥–", "ðŸ¥¨", "ðŸ¥¯", "ðŸ¥ž", "ðŸ§‡", "ðŸ§€", "ðŸ–", "ðŸ—", "ðŸ¥©"
        };

        public static final String[] OBJECTS = {
                "âš½", "ðŸ€", "ðŸˆ", "âš¾", "ðŸ¥Ž", "ðŸŽ¾", "ðŸ", "ðŸ‰", "ðŸ¥", "ðŸŽ±",
                "ðŸ“", "ðŸ¸", "ðŸ’", "ðŸ‘", "ðŸ¥", "ðŸ", "ðŸ¥…", "â›³", "ðŸ¹", "ðŸŽ£",
                "ðŸ¤¿", "ðŸ¥Š", "ðŸ¥‹", "ðŸŽ½", "ðŸ›¹", "ðŸ›·", "â›¸ï¸", "ðŸ¥Œ", "ðŸŽ¿", "â›·ï¸",
                "ðŸ‚", "ðŸª‚", "ðŸ‹ï¸", "ðŸ¤¸", "ðŸ¤¼", "ðŸ¤½", "ðŸ¤¾", "ðŸŒï¸", "ðŸ‡", "ðŸ§˜"
        };

        public static String[] getEmojisByCategory(int category) {
            switch (category) {
                case 0:
                    return SMILEYS;
                case 1:
                    return HEARTS;
                case 2:
                    return ANIMALS;
                case 3:
                    return FOOD;
                case 4:
                    return OBJECTS;
                default:
                    return SMILEYS;
            }
        }
    }


    // Start long press detection
    private void startLongPressDetection(float[] bitmapCoords) {
        if (bitmapCoords == null) return;

        cancelLongPressDetection();

        longPressRunnable = () -> {
            // Uzun basma tespit edildi, Ã§Ã¶p kutusunu gÃ¶ster
            runOnUiThread(() -> {
                trashBinIcon.setVisibility(View.VISIBLE);
                isLongPressDetected = true;

                // Haptic feedback
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    imageView.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);
                }

                // Ã‡Ã¶p kutusuna animasyonla gel
                trashBinIcon.setScaleX(0.5f);
                trashBinIcon.setScaleY(0.5f);
                trashBinIcon.setAlpha(0f);

                trashBinIcon.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .alpha(1.0f)
                        .setDuration(300)
                        .start();
            });
        };

        longPressHandler.postDelayed(longPressRunnable, LONG_PRESS_TIMEOUT);
    }

    // Cancel long press detection
    private void cancelLongPressDetection() {
        if (longPressRunnable != null) {
            longPressHandler.removeCallbacks(longPressRunnable);
            longPressRunnable = null;
        }

        // Ã‡Ã¶p kutusunu gizle (animasyonla)
        if (isLongPressDetected) {
            trashBinIcon.animate()
                    .scaleX(0.5f)
                    .scaleY(0.5f)
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        trashBinIcon.setVisibility(View.GONE);
                        trashBinIcon.setScaleX(1.0f);
                        trashBinIcon.setScaleY(1.0f);
                        trashBinIcon.setAlpha(1.0f);
                    })
                    .start();
        }

        isLongPressDetected = false;
    }

    // Check if item was dropped on trash bin
    // Check if item was dropped on trash bin
    private void checkTrashBinDrop(float x, float y) {
        // Get trash bin coordinates on screen
        trashBinIcon.setVisibility(View.VISIBLE);
        int[] trashBinLocation = new int[2];
        trashBinIcon.getLocationOnScreen(trashBinLocation);

        // Convert touch coordinates to screen coordinates
        int[] imageViewLocation = new int[2];
        imageView.getLocationOnScreen(imageViewLocation);
        float screenX = x + imageViewLocation[0];
        float screenY = y + imageViewLocation[1];

        // Check if drop position is within trash bin area
        int trashBinWidth = trashBinIcon.getWidth();
        int trashBinHeight = trashBinIcon.getHeight();

        if (screenX >= trashBinLocation[0] && screenX <= trashBinLocation[0] + trashBinWidth &&
                screenY >= trashBinLocation[1] && screenY <= trashBinLocation[1] + trashBinHeight) {
            trashBinIcon.setVisibility(View.VISIBLE);

            // TitreÅŸim efekti
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(50);
            }

            // Animasyonla silme iÅŸlemi
            if (selectedTextOverlay != null) {
                // Ã‡Ã¶p kovasÄ±na animasyon
                trashBinIcon.animate()
                        .scaleX(1.5f)
                        .scaleY(1.5f)
                        .setDuration(200)
                        .withEndAction(() -> {
                            trashBinIcon.animate()
                                    .scaleX(1.0f)
                                    .scaleY(1.0f)
                                    .setDuration(200)
                                    .start();

                            // TitreÅŸim efekti
                            trashBinIcon.animate()
                                    .translationXBy(5f)
                                    .setDuration(50)
                                    .withEndAction(() -> {
                                        trashBinIcon.animate()
                                                .translationXBy(-10f)
                                                .setDuration(50)
                                                .withEndAction(() -> {
                                                    trashBinIcon.animate()
                                                            .translationXBy(5f)
                                                            .setDuration(50)
                                                            .start();
                                                })
                                                .start();
                                    })
                                    .start();

                            // Metni sil
                            textOverlays.remove(selectedTextOverlay);
                            selectedTextOverlay = null;
                            currentTextOverlay = null; // Bu satÄ±rÄ± ekleyin

                            // SADECE gÃ¼ncel overlay'leri Ã§iz (orijinale dÃ¶nmeden)
                            updateImageWithOverlaysAndStickers();

                            Toast.makeText(this, "Metin silindi", Toast.LENGTH_SHORT).show();

                            // Ã‡Ã¶p kutusunu gizle
                            trashBinIcon.setVisibility(View.GONE);
                        })
                        .start();
            } else if (selectedEmojiSticker != null) {
                // Ã‡Ã¶p kovasÄ±na animasyon
                trashBinIcon.animate()
                        .scaleX(1.2f)
                        .scaleY(1.2f)
                        .setDuration(200)
                        .withEndAction(() -> {
                            trashBinIcon.animate()
                                    .scaleX(1.0f)
                                    .scaleY(1.0f)
                                    .setDuration(200)
                                    .start();

                            // Emojiyi sil
                            emojiStickers.remove(selectedEmojiSticker);
                            selectedEmojiSticker = null;

                            // SADECE gÃ¼ncel overlay'leri Ã§iz (orijinale dÃ¶nmeden)
                            updateImageWithOverlaysAndStickers();

                            Toast.makeText(this, "Emoji silindi", Toast.LENGTH_SHORT).show();

                            // Ã‡Ã¶p kutusunu gizle
                            trashBinIcon.setVisibility(View.GONE);
                        })
                        .start();
            }
        } else {
            // EÄŸer Ã§Ã¶p kutusunun Ã¼zerine bÄ±rakÄ±lmadÄ±ysa, Ã§Ã¶p kutusunu gizle
            trashBinIcon.setVisibility(View.GONE);
        }

        isLongPressDetected = false;
    }


    // Emoji data class yerine Sticker Data class
    public static class StickerData {
        // Her kategori için bir dizi Drawable Resource ID'si tanımlayın.
        // Bu ID'ler, res/drawable klasörünüzdeki dosya isimlerine karşılık gelir (R.drawable.sticker_smile_1 gibi).
        // Bu örnek listeleri, kendi drawable resource ID'lerinizle değiştirmelisiniz.

        public static final int[] SMILEYS = {
                R.drawable.stickers_cheer_up, // Örnek: "sticker_smile_1.png" dosyası
                R.drawable.stickers_cool,
                R.drawable.stickers_game_on,
                R.drawable.stickers_faq,
                R.drawable.stickers_good_night,
                R.drawable.stickers_lets_go,
                R.drawable.stickers_flowers,
                R.drawable.stickers_flower_basket,

                R.drawable.stickers_sun,
                R.drawable.stickers_plant_pot,
                R.drawable.stickers_cool,
                R.drawable.stickers_keep_rocking,

                R.drawable.stickers_mushrooms,
                R.drawable.stickers_new_post,
                R.drawable.stickers_your_are_cool,
                R.drawable.stickers_link_in_bio,
                R.drawable.stickers_focus_on_the_good,
                // ... Buraya Flaticon'dan indirdiğiniz smiley/gülen yüz sticker'larının resource ID'lerini ekleyin.
        };

        public static final int[] HEARTS = {
                R.drawable.stickers_love_you_till_the,
                R.drawable.stickers_cassette_tape,
                R.drawable.stickers_love,
                R.drawable.stickers_i_love_you,
                R.drawable.stickers_love_birds,
                R.drawable.stickers_be_mine,
                R.drawable.stickers_bee,
                // ... Buraya Flaticon'dan indirdiğiniz kalp/aşk sticker'larının resource ID'lerini ekleyin.
        };

        public static final int[] ANIMALS = {
                R.drawable.stickers_fish,
                R.drawable.stickers_cat_lover,
                R.drawable.stickers_dog_lover,
                R.drawable.stickers_bath,
                R.drawable.stickers_nice_day,
                R.drawable.stickers_paw_print
                // ... Buraya Flaticon'dan indirdiğiniz hayvan sticker'larının resource ID'lerini ekleyin.
        };

        public static final int[] FOOD = {
                R.drawable.stickers_pizza,
                R.drawable.stickers_burger,
                R.drawable.stickers_donut,
                R.drawable.stickers_kebab,
                R.drawable.stickers_french_fries,
                R.drawable.stickers_chicken,
                R.drawable.stickers_sushi
                // ... Buraya Flaticon'dan indirdiğiniz yemek/içecek sticker'larının resource ID'lerini ekleyin.
        };

        public static final int[] OBJECTS = {
                R.drawable.stickers_birthday_cake,
                R.drawable.stickers_bdc,
                R.drawable.stickers_gift,
                R.drawable.stickers_creative,
                R.drawable.stickers_creativity,
                R.drawable.stickers_creativity1,
                R.drawable.stickers_creativity2,
                R.drawable.stickers_creativity7,
                R.drawable.stickers_creativityt,
                R.drawable.stickers_creativity6,
                R.drawable.stickers_creativity4,


                // ... Buraya Flaticon'dan indirdiğiniz nesne sticker'larının resource ID'lerini ekleyin.
        };

        public static int[] getStickersByCategory(int category) {
            switch (category) {
                case 0:
                    return SMILEYS;
                case 1:
                    return HEARTS;
                case 2:
                    return ANIMALS;
                case 3:
                    return FOOD;
                case 4:
                    return OBJECTS;
                default:
                    return SMILEYS;
            }
        }
    }

    private void setupStickerGrid() {
        int[] stickers = StickerData.getStickersByCategory(0);
        StickerGridAdapter adapter = new StickerGridAdapter(this, stickers, new StickerGridAdapter.OnStickerClickListener() {
            @Override
            public void onStickerClick(int stickerResId) {
                selectedStickerResId = stickerResId;
                waitingForStickerPlacement = true;
                stickerPanel.setVisibility(View.GONE);
                Toast.makeText(StoryEditorActivity.this, "Sticker eklemek için resme dokunun", Toast.LENGTH_SHORT).show();
            }
        });
        emojiGridView.setAdapter(adapter);
    }

    private void filterEmojis(String query) {
        // This would filter emojis based on the search query
        // For now, we'll just show a toast
        if (!query.isEmpty()) {
            Toast.makeText(this, "Arama: " + query, Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchUsersFromFirebase() {
        if (isLoading || !hasMoreUsers) return;

        isLoading = true;
        userAdapter.setLoading(true); // Loading göster

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Kullanıcılar");

        // Sadece gerekli alanları çek ve sınırlı sayıda kullanıcı al
        Query query = usersRef.orderByChild("kullanıcıAdı")
                .limitToFirst(50); // Maksimum 50 kullanıcı

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isLoading = false;
                userAdapter.setLoading(false); // Loading gizle

                List<AllUser> newUsers = new ArrayList<>();
                int count = 0;

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    if (count >= 50) break; // Güvenlik sınırı

                    try {
                        AllUser user = userSnapshot.getValue(AllUser.class);
                        if (user != null ) {
                            newUsers.add(user);
                            count++;
                        }
                    } catch (Exception e) {
                        Log.e("Firebase", "User parse error: " + e.getMessage());
                        // Parse hatası olan kullanıcıyı atla
                    }
                }

                // Adapter'ı main thread'de güncelle
                runOnUiThread(() -> {
                    userAdapter.setUserList(newUsers, true);

                    if (newUsers.isEmpty()) {
                        // Kullanıcı bulunamadı mesajı göster
                        Toast.makeText(StoryEditorActivity.this, "Kullanıcı bulunamadı", Toast.LENGTH_SHORT).show();
                    }
                });

                hasMoreUsers = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                isLoading = false;
                userAdapter.setLoading(false); // Loading gizle

                Log.e("Firebase", "Error fetching users: " + error.getMessage());

                runOnUiThread(() -> {
                    Toast.makeText(StoryEditorActivity.this, "Kullanıcılar yüklenirken hata oluştu", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }    private void setupSearchListener() {
        searchAddPersonStory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                userAdapter.filter(s.toString());

                // Arama sonucu yoksa mesaj göster
                if (s.length() > 0 && userAdapter.getItemCount() == 0) {
                    // "Kullanıcı bulunamadı" mesajı gösterilebilir
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void uploadToSupabase(Bitmap bitmap, String fileName, SupabaseUploadCallback callback) {
        new Thread(() -> {
            try {
                // Yüksek kaliteli sıkıştırma
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos); // %90 kalite
                byte[] imageData = baos.toByteArray();

                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .build();

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", fileName,
                                RequestBody.create(imageData, MediaType.parse("image/jpeg")))
                        .build();

                Request request = new Request.Builder()
                        .url(SUPABASE_STORAGE_ENDPOINT + fileName)
                        .addHeader("Authorization", "Bearer " + SUPABASE_APIKEY)
                        .addHeader("apikey", SUPABASE_APIKEY)
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    String imageUrl = SUPABASE_STORAGE_ENDPOINT + fileName;
                    runOnUiThread(() -> callback.onSuccess(imageUrl));
                } else {
                    runOnUiThread(() -> callback.onError("Yükleme başarısız: " + response.code()));
                }

            } catch (Exception e) {
                runOnUiThread(() -> callback.onError("Hata: " + e.getMessage()));
            }
        }).start();
    }

    interface SupabaseUploadCallback {
        void onSuccess(String imageUrl);
        void onError(String errorMessage);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Bitmap'leri güvenli şekilde temizle - ImageUtils kullan
        ImageUtils.recycleBitmap(originalBitmap);
        ImageUtils.recycleBitmap(editedBitmap);
        originalBitmap = null;
        editedBitmap = null;
    }

    private void loadImageFromIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("image_path")) {
            try {
                String imagePath = intent.getStringExtra("image_path");
                if (imagePath != null) {
                    File imageFile = new File(imagePath);
                    if (imageFile.exists()) {
                        // Dosyadan bitmap yükle
                        originalBitmap = BitmapFactory.decodeFile(imagePath);

                        if (originalBitmap != null) {
                            // Yüksek kaliteli kopya
                            editedBitmap = Bitmap.createBitmap(
                                    originalBitmap.getWidth(),
                                    originalBitmap.getHeight(),
                                    Bitmap.Config.ARGB_8888
                            );

                            Canvas canvas = new Canvas(editedBitmap);
                            canvas.drawBitmap(originalBitmap, 0, 0, null);

                            imageView.setImageBitmap(editedBitmap);

                            // Geçici dosyayı sil (isteğe bağlı)
                            // imageFile.delete();
                        }
                    }
                }
            } catch (Exception e) {
                Toast.makeText(this, "Görsel yüklenirken hata oluştu", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Görsel verisi alınamadı", Toast.LENGTH_SHORT).show();
            finish();
        }
    }



    public void addTag(AllUser user) {
        Log.d("TAG ekleme", "addTag: çalıştı - " + user.getKullanıcıAdı());
        panel_add_person.setVisibility(View.GONE);

        FrameLayout container = findViewById(R.id.tagContainer);
        container.setVisibility(View.VISIBLE);

        View tagView = LayoutInflater.from(this).inflate(R.layout.item_tag_person, container, false);
        TextView txtTagName = tagView.findViewById(R.id.tagUserNameForStory);
        ImageView closeButton = tagView.findViewById(R.id.tagCloseButton);
        CardView tagCard = tagView.findViewById(R.id.tagCardView);

        String username = "@" + user.getKullanıcıAdı();
        txtTagName.setText(username);

        // StoryTag oluştur ve listeye ekle
        StoryTag storyTag = new StoryTag(username, container.getWidth() / 2f, container.getHeight() / 2f);
        storyTags.add(storyTag);

        // Tag view'e story tag referansını sakla
        tagView.setTag(storyTag);

        // Drag & Drop için touch listener
        setupTagDragAndDrop(tagView, container, storyTag);

        // Kapatma butonu
        closeButton.setOnClickListener(v -> {
            container.removeView(tagView);
            storyTags.remove(storyTag);
            if (container.getChildCount() == 0) {
                container.setVisibility(View.GONE);
            }
            updateImageWithOverlaysAndStickers(); // Görseli güncelle
        });

        // Tag'i containera ekle
        container.addView(tagView);

        // Başlangıç pozisyonu - ekranın ortası
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) tagView.getLayoutParams();
        params.gravity = Gravity.CENTER;
        tagView.setLayoutParams(params);

        // Görseli güncelle (tag'leri de ekle)
        updateImageWithOverlaysAndStickers();

        Toast.makeText(this, user.getKullanıcıAdı() + " etiketlendi. Sürükleyerek taşıyabilirsiniz.", Toast.LENGTH_SHORT).show();
    }

    // Drag & Drop setup metodu
    private void setupTagDragAndDrop(View tagView, FrameLayout container, StoryTag storyTag) {
        tagView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        currentDraggingTag = v;
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        lastAction = MotionEvent.ACTION_DOWN;

                        // Elevation artır
                        v.setElevation(20f);
                        v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).start();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        if (currentDraggingTag == v) {
                            float newX = event.getRawX() + dX;
                            float newY = event.getRawY() + dY;

                            // Ekran sınırlarını kontrol et
                            int containerWidth = container.getWidth();
                            int containerHeight = container.getHeight();
                            int tagWidth = v.getWidth();
                            int tagHeight = v.getHeight();

                            newX = Math.max(0, Math.min(newX, containerWidth - tagWidth));
                            newY = Math.max(0, Math.min(newY, containerHeight - tagHeight));

                            v.setX(newX);
                            v.setY(newY);

                            // StoryTag konumunu güncelle (bitmap koordinatlarına çevir)
                            updateStoryTagPosition(storyTag, newX, newY, containerWidth, containerHeight);
                        }
                        lastAction = MotionEvent.ACTION_MOVE;
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (currentDraggingTag == v) {
                            v.setElevation(6f);
                            v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                            currentDraggingTag = null;

                            // Görseli güncelle
                            updateImageWithOverlaysAndStickers();
                        }
                        return true;

                    case MotionEvent.ACTION_CANCEL:
                        if (currentDraggingTag == v) {
                            v.setElevation(6f);
                            v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                            currentDraggingTag = null;
                        }
                        return true;
                }
                return false;
            }
        });
    }

    // Tag konumunu bitmap koordinatlarına çevir
    private void updateStoryTagPosition(StoryTag storyTag, float viewX, float viewY,
                                        int containerWidth, int containerHeight) {
        if (editedBitmap == null) return;

        // View koordinatlarını bitmap koordinatlarına çevir
        float bitmapX = (viewX / containerWidth) * editedBitmap.getWidth();
        float bitmapY = (viewY / containerHeight) * editedBitmap.getHeight();

        storyTag.x = bitmapX;
        storyTag.y = bitmapY;
    }

    // Tag seçenekleri göster (isteğe bağlı)
    private void showTagOptions(View tagView, TextView textView) {
        String currentText = textView.getText().toString();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Etiket Seçenekleri");
        builder.setItems(new CharSequence[]{"Yeniden Adlandır", "Sil", "İptal"}, (dialog, which) -> {
            switch (which) {
                case 0: // Yeniden Adlandır
                    showRenameDialog(tagView, textView, currentText);
                    break;
                case 1: // Sil
                    FrameLayout container = findViewById(R.id.tagContainer);
                    container.removeView(tagView);
                    if (container.getChildCount() == 0) {
                        container.setVisibility(View.GONE);
                    }
                    break;
            }
        });
        builder.show();
    }

    // Yeniden adlandır dialogu (isteğe bağlı)
    private void showRenameDialog(View tagView, TextView textView, String currentText) {
        EditText input = new EditText(this);
        input.setText(currentText.replace("@", ""));
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        new AlertDialog.Builder(this)
                .setTitle("Etiket Adı")
                .setView(input)
                .setPositiveButton("Kaydet", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        textView.setText("@" + newName);
                    }
                })
                .setNegativeButton("İptal", null)
                .show();
    }

    private void setupAdvancedTagDrag(View tagView) {
        tagView.setOnLongClickListener(v -> {
            // Uzun basınca sürükleme moduna geç
            v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(200).start();
            return true;
        });

        tagView.setOnTouchListener(new View.OnTouchListener() {
            private float startX, startY;
            private float startTouchX, startTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                FrameLayout container = findViewById(R.id.tagContainer);

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = v.getX();
                        startY = v.getY();
                        startTouchX = event.getRawX();
                        startTouchY = event.getRawY();
                        v.setElevation(20f);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float deltaX = event.getRawX() - startTouchX;
                        float deltaY = event.getRawY() - startTouchY;

                        float newX = startX + deltaX;
                        float newY = startY + deltaY;

                        // Sınır kontrolleri
                        newX = Math.max(0, Math.min(newX, container.getWidth() - v.getWidth()));
                        newY = Math.max(0, Math.min(newY, container.getHeight() - v.getHeight()));

                        v.setX(newX);
                        v.setY(newY);
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.setElevation(6f);
                        v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();

                        // Snap to grid (isteğe bağlı)
                        snapToGrid(v);
                        return true;
                }
                return false;
            }
        });
    }

    // Snap to grid özelliği (isteğe bağlı)
    private void snapToGrid(View view) {
        float x = view.getX();
        float y = view.getY();

        // 20px grid
        float snappedX = Math.round(x / 20) * 20;
        float snappedY = Math.round(y / 20) * 20;

        view.animate()
                .x(snappedX)
                .y(snappedY)
                .setDuration(100)
                .start();
    }


    private static class StoryTag {
        String username;
        float x;
        float y;
        boolean isVisible = true;

        public StoryTag(String username, float x, float y) {
            this.username = username;
            this.x = x;
            this.y = y;
        }
    }

}
