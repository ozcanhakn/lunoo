package com.lumoo.ViewHolder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.lumoo.R;
import com.lumoo.util.GlideUtil;

public class StoryViewHolder extends RecyclerView.ViewHolder {

    public ImageView imageView;
    public TextView textView;

    public StoryViewHolder(View itemView) {
        super(itemView);
        // ViewHolder oluşturulurken view'ları bul
        imageView = itemView.findViewById(R.id.iv_storyf4);
        textView = itemView.findViewById(R.id.unameStory);
    }

    public void setStory(FragmentActivity activity, String postUri, String name, long timeEnd,
                         String timeUpload, String type,
                         String caption, String url, String uid) {

        Log.d("StoryViewHolder", "setStory called - name: " + name +
                ", url length: " + (url != null ? url.length() : "null"));

        // 24 SAAT KONTROLÜ - Eğer süresi dolmuşsa görünmez yap
        long currentTime = System.currentTimeMillis();
        if (currentTime > timeEnd) {
            itemView.setVisibility(View.GONE);
            Log.d("StoryViewHolder", "Story expired, hiding item");
            return;
        }

        // Görünür yap
        itemView.setVisibility(View.VISIBLE);

        // SADECE KULLANICI ADINI GÖSTER - CAPTION'U GÖSTERME
        if (name != null && !name.trim().isEmpty()) {
            textView.setText(name); // Sadece kullanıcı adını göster
            textView.setVisibility(View.VISIBLE);
            Log.d("StoryViewHolder", "Name set: " + name);
        } else {
            // Kullanıcı adı yoksa uid'nin ilk 8 karakterini göster
            if (uid != null && uid.length() > 8) {
                textView.setText(uid.substring(0, 8) + "...");
            } else if (uid != null) {
                textView.setText(uid);
            } else {
                textView.setText("Kullanıcı");
            }
            textView.setVisibility(View.VISIBLE);
            Log.d("StoryViewHolder", "Default name set");
        }

        // Güvenli image ayarlama
        if (url != null && !url.trim().isEmpty()) {
            try {

                    GlideUtil.loadOriginalImage(itemView.getContext(), url, imageView);

                    imageView.setVisibility(View.VISIBLE);
                    Log.d("StoryViewHolder", "Profile image set successfully");

            } catch (Exception e) {
                Log.e("StoryViewHolder", "Error setting story profile image: " + e.getMessage());
                setDefaultProfileImage();
            }
        } else {
            Log.w("StoryViewHolder", "Profile URL is null or empty");
            setDefaultProfileImage();
        }
    }

    private void setDefaultProfileImage() {
        try {
            // Varsayılan profil resmi ayarla - android sistemindeki person iconunu kullan
            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
            imageView.setVisibility(View.VISIBLE);
            Log.d("StoryViewHolder", "Default profile image set");
        } catch (Exception e) {
            Log.e("StoryViewHolder", "Error setting default profile image: " + e.getMessage());
            // Son çare olarak görünmez yap
            imageView.setVisibility(View.GONE);
        }
    }

}