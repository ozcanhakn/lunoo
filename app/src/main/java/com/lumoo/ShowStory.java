package com.lumoo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lumoo.Model.StoryMember;
import com.lumoo.util.GlideUtil; // GlideUtil import edildi

import java.util.ArrayList;
import java.util.List;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import jp.shts.android.storiesprogressview.StoriesProgressView;

public class ShowStory extends AppCompatActivity implements StoriesProgressView.StoriesListener {

    int counter = 0;
    ImageView imageViewShowStory, imageViewUrl;
    TextView textView;

    String userid;

    List<String> posturi;
    List<String> url10;
    List<String> username;

    StoriesProgressView storiesProgressView;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference reference;

    EditText editText;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_show_story);

        imageViewShowStory = findViewById(R.id.iv_storyview);
        imageViewUrl = findViewById(R.id.iv_ss);
        textView = findViewById(R.id.tv_uname_ss);
        storiesProgressView = findViewById(R.id.stories);

        editText = findViewById(R.id.et_story);
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShowStory.this, MessageActivity.class);
                intent.putExtra("uid", userid);
                startActivity(intent);
            }
        });

        View reverse = findViewById(R.id.view_prev);
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storiesProgressView.reverse();
            }
        });

        reverse.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                storiesProgressView.pause();
                return false;
            }
        });
        reverse.setOnTouchListener(onTouchListener);

        View next = findViewById(R.id.view_next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storiesProgressView.skip();
            }
        });

        next.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                storiesProgressView.pause();
                return false;
            }
        });
        next.setOnTouchListener(onTouchListener);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            userid = bundle.getString("u");
            Log.d("ShowStory", "User ID received: " + userid);
        } else {
            Toast.makeText(this, "Hata: Kullanıcı ID'si alınamadı", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Gelen userid'nin story'lerini göster
        // BURADA DEĞİŞİKLİK: userid'nin story'lerini değil, mevcut kullanıcının story'lerini göster
        String currentUuid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        reference = database.getReference("Kullanıcılar").child(userid).child("Story");
    }

    @Override
    protected void onStart() {
        super.onStart();
        getStories();
    }

    // ShowStory.java içinde getStories() metodunu güncelleyin
    private void getStories() {
        posturi = new ArrayList<>();
        username = new ArrayList<>();
        url10 = new ArrayList<>();

        Log.d("ShowStory", "Getting stories from reference: " + reference.toString());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.d("ShowStory", "onDataChange called, children count: " + snapshot.getChildrenCount());

                posturi.clear();
                url10.clear();
                username.clear();

                long currentTime = System.currentTimeMillis();

                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    StoryMember storyMember = snapshot1.getValue(StoryMember.class);

                    if (storyMember != null) {
                        // 24 saat kontrolü - sadece süresi dolmamış story'leri göster
                        if (currentTime > storyMember.getTimeEnd()) {
                            Log.d("ShowStory", "Story expired, removing: " + snapshot1.getKey());
                            snapshot1.getRef().removeValue(); // Süresi dolan story'yi sil
                            continue;
                        }

                        String postUri = storyMember.getPostUri();
                        String url = storyMember.getUrl();
                        String name = storyMember.getName();

                        Log.d("ShowStory", "Story found - Name: " + name +
                                ", PostUri: " + (postUri != null ? "exists(" + postUri.length() + ")" : "null") +
                                ", URL: " + (url != null ? "exists(" + url.length() + ")" : "null"));

                        if (postUri != null && url != null && name != null) {
                            posturi.add(postUri);
                            url10.add(url);
                            username.add(name);
                        } else {
                            Log.w("ShowStory", "Eksik story verisi: " + snapshot1.getKey());
                        }
                    } else {
                        Log.w("ShowStory", "StoryMember is null for key: " + snapshot1.getKey());
                    }
                }

                Log.d("ShowStory", "Total valid stories: " + posturi.size());

                if (posturi.size() > 0) {
                    storiesProgressView.setStoriesCount(posturi.size());
                    storiesProgressView.setStoryDuration(5000L);
                    storiesProgressView.setStoriesListener(ShowStory.this);
                    storiesProgressView.startStories(counter);

                    // İlk story'yi göster
                    displayStory(counter);
                } else {
                    Log.d("ShowStory", "No stories available");
                    //Toast.makeText(ShowStory.this, "Henüz story yok", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("ShowStory", "Database error: " + error.getMessage());
                //Toast.makeText(ShowStory.this, "Hata: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayStory(int index) {
        if (index < 0 || index >= posturi.size()) {
            Log.w("ShowStory", "Invalid story index: " + index);
            return;
        }

        try {
            // Story görselini göster - GlideUtil ile
            if (posturi.get(index) != null) {
                GlideUtil.loadImage(ShowStory.this, posturi.get(index), imageViewShowStory);
                Log.d("ShowStory", "Story image displayed for index: " + index);
            }

            // Profil resmini göster - GlideUtil ile (daire şeklinde)
            if (url10.get(index) != null) {
                GlideUtil.loadImage(ShowStory.this, url10.get(index), imageViewUrl, true);
                Log.d("ShowStory", "Profile image displayed for index: " + index);
            }

            // Kullanıcı adını göster
            if (username.get(index) != null) {
                textView.setText(username.get(index));
                Log.d("ShowStory", "Username displayed: " + username.get(index));
            }

        } catch (Exception e) {
            Log.e("ShowStory", "Error displaying story at index " + index + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onNext() {
        if (counter + 1 < posturi.size()) {
            counter++;
            displayStory(counter);
            Log.d("ShowStory", "Next story: " + counter);
        }
    }

    @Override
    public void onPrev() {
        if (counter - 1 >= 0) {
            counter--;
            displayStory(counter);
            Log.d("ShowStory", "Previous story: " + counter);
        }
    }

    @Override
    public void onComplete() {
        Log.d("ShowStory", "Stories completed");
        finish();
    }

    @Override
    protected void onDestroy() {
        if (storiesProgressView != null) {
            storiesProgressView.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (storiesProgressView != null) {
            storiesProgressView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (storiesProgressView != null) {
            storiesProgressView.resume();
        }
    }
}