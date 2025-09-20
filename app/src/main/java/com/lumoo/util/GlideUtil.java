package com.lumoo.util;

import android.content.Context;
import android.widget.ImageView;
import androidx.annotation.DrawableRes;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.lumoo.R;
import com.lumoo.util.SecurityUtils;

// GlideUtil.java
public class GlideUtil {

    // Daire şeklinde kesme OPSİYONEL olsun
    private static final RequestOptions DEFAULT_OPTIONS = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .dontTransform(); // Dönüşüm yapma, orijinal haliyle göster

    private static final RequestOptions CIRCULAR_OPTIONS = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .circleCrop(); // Sadece daire şekli istendiğinde

    public static void loadImage(Context context, String imageUrl, ImageView imageView) {
        loadImage(context, imageUrl, imageView, false); // Varsayılan: düz
    }

    public static void loadImage(Context context, String imageUrl, ImageView imageView, boolean circular) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_launcher_background);
            return;
        }

        RequestOptions options = circular ? CIRCULAR_OPTIONS : DEFAULT_OPTIONS;

        Glide.with(context)
                .load(imageUrl)
                .apply(options)
                .thumbnail(0.3f)
                .into(imageView);
    }

    // Sadece orijinal boyutta yükleme için özel metod - Güvenlik kontrolleri ile
    public static void loadOriginalImage(Context context, String imageUrl, ImageView imageView) {
        if (context == null || imageView == null) return;
        
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_launcher_background);
            return;
        }

        // Supabase URL güvenlik kontrolü
        if (imageUrl.startsWith("http") && !SecurityUtils.isValidSupabaseUrl(imageUrl)) {
            SecurityUtils.logSecurityEvent("Invalid Image URL", "Suspicious image URL: " + imageUrl);
            imageView.setImageResource(R.drawable.ic_launcher_background);
            return;
        }

        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .dontTransform(); // Hiçbir dönüşüm yapma

        Glide.with(context)
                .load(imageUrl)
                .apply(options)
                .into(imageView);
    }
    
    // Memory temizleme - Activity/Fragment destroy'da çağır
    public static void clearImageCache(Context context) {
        if (context != null) {
            Glide.get(context).clearMemory();
        }
    }

    // Background thread'de cache temizleme
    public static void clearImageCacheAsync(Context context) {
        if (context != null) {
            new Thread(() -> Glide.get(context).clearDiskCache()).start();
        }
    }
}