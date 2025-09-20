package com.lumoo.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.lumoo.R;

import java.io.ByteArrayOutputStream;

/**
 * Güvenli resim yönetimi için gelişmiş ImageUtils sınıfı
 * Memory leak'leri önler ve performansı optimize eder
 */
public class ImageUtils {
    
    private static final String TAG = "ImageUtils";
    
    // Maksimum bitmap boyutları (Android sınırları içinde)
    private static final int MAX_BITMAP_WIDTH = 2048;
    private static final int MAX_BITMAP_HEIGHT = 2048;
    private static final int MAX_BITMAP_SIZE = MAX_BITMAP_WIDTH * MAX_BITMAP_HEIGHT * 4; // 4 bytes per pixel (ARGB)
    
    // Base64 boyut limitleri
    private static final int MAX_BASE64_LENGTH = 2000000; // ~1.5MB
    private static final int MAX_DECODED_SIZE = 1500000; // 1.5MB
    
    // JPEG kalite ayarları
    private static final int HIGH_QUALITY = 90;
    private static final int MEDIUM_QUALITY = 70;
    private static final int LOW_QUALITY = 50;

    /**
     * Güvenli bitmap boyut kontrolü
     */
    public static boolean isBitmapSafe(Bitmap bitmap) {
        if (bitmap == null) return false;
        
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        long size = (long) width * height * 4; // 4 bytes per pixel
        
        Log.d(TAG, "Bitmap dimensions: " + width + "x" + height + ", size: " + size + " bytes");
        
        return width <= MAX_BITMAP_WIDTH && 
               height <= MAX_BITMAP_HEIGHT && 
               size <= MAX_BITMAP_SIZE;
    }

    /**
     * Güvenli bitmap boyut küçültme
     */
    public static Bitmap resizeBitmapSafely(Bitmap originalBitmap, int maxWidth, int maxHeight) {
        if (originalBitmap == null) return null;
        
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();
        
        // Eğer zaten uygun boyuttaysa, aynı bitmap'i döndür
        if (width <= maxWidth && height <= maxHeight) {
            return originalBitmap;
        }
        
        float scaleWidth = ((float) maxWidth) / width;
        float scaleHeight = ((float) maxHeight) / height;
        float scale = Math.min(scaleWidth, scaleHeight);
        
        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);
        
        // Minimum boyut kontrolü
        if (newWidth <= 0) newWidth = 1;
        if (newHeight <= 0) newHeight = 1;
        
        try {
            return Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "OutOfMemoryError while resizing bitmap", e);
            // Daha da küçük boyutta tekrar dene
            return Bitmap.createScaledBitmap(originalBitmap, newWidth/2, newHeight/2, true);
        }
    }

    /**
     * Güvenli Base64 kodlama - Memory leak önleyici
     */
    public static String encodeBase64Compressed(Bitmap bitmap, int quality) {
        if (bitmap == null) return "";
        
        try {
            // Önce boyutu küçült
            Bitmap resizedBitmap = resizeBitmapSafely(bitmap, 800, 800);
            if (resizedBitmap == null) return "";
            
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            
            // JPEG kullan ve kaliteyi ayarla
            boolean compressed = resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);
            if (!compressed) {
                Log.e(TAG, "Bitmap compression failed");
                return "";
            }
            
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            
            // Base64 boyutu kontrolü
            if (byteArray.length > MAX_DECODED_SIZE) {
                Log.w(TAG, "Encoded image too large: " + byteArray.length + " bytes");
                // Kaliteyi düşürerek tekrar dene
                byteArrayOutputStream.reset();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality/2, byteArrayOutputStream);
                byteArray = byteArrayOutputStream.toByteArray();
            }
            
            Log.d(TAG, "Final encoded size: " + byteArray.length + " bytes");
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
            
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "OutOfMemoryError during encoding", e);
            return "";
        } catch (Exception e) {
            Log.e(TAG, "Error during encoding", e);
            return "";
        }
    }

    /**
     * Güvenli Base64 decode - Memory leak önleyici
     */
    public static Bitmap decodeBase64Safe(String encodedImage) {
        // Null veya boş string kontrolü
        if (encodedImage == null || encodedImage.trim().isEmpty()) {
            Log.w(TAG, "Encoded image is null or empty");
            return null;
        }
        
        try {
            // Base64 string boyutu kontrolü
            if (encodedImage.length() > MAX_BASE64_LENGTH) {
                Log.w(TAG, "Base64 string too large: " + encodedImage.length());
                return null;
            }
            
            byte[] decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT);
            
            // Decoded bytes boyut kontrolü
            if (decodedBytes.length > MAX_DECODED_SIZE) {
                Log.w(TAG, "Decoded bytes too large: " + decodedBytes.length);
                return null;
            }
            
            // BitmapFactory options ile güvenli decode
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true; // Sadece boyutları al
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);
            
            // Boyut kontrolü
            if (options.outWidth <= 0 || options.outHeight <= 0) {
                Log.w(TAG, "Invalid bitmap dimensions");
                return null;
            }
            
            // Çok büyükse sample size ayarla
            options.inSampleSize = calculateInSampleSize(options, MAX_BITMAP_WIDTH, MAX_BITMAP_HEIGHT);
            options.inJustDecodeBounds = false;
            
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);
            
            if (bitmap == null) {
                Log.w(TAG, "Failed to create bitmap from decoded bytes");
            } else {
                Log.d(TAG, "Successfully decoded bitmap: " + bitmap.getWidth() + "x" + bitmap.getHeight());
            }
            
            return bitmap;
            
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid Base64 string: " + e.getMessage());
            return null;
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "OutOfMemoryError while decoding: " + e.getMessage());
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error decoding Base64: " + e.getMessage());
            return null;
        }
    }

    /**
     * InSampleSize hesaplama
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        
        return inSampleSize;
    }

    /**
     * Bitmap'i güvenli şekilde temizle
     */
    public static void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    /**
     * Glide ile güvenli resim yükleme - Supabase URL'leri için optimize edilmiş
     */
    public static void loadImageSafely(Context context, String imageUrl, ImageView imageView) {
        if (context == null || imageView == null) return;
        
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_launcher_background);
            return;
        }
        
        // Supabase URL kontrolü
        if (imageUrl.startsWith("http")) {
            loadImageFromUrl(context, imageUrl, imageView);
        } else {
            // Base64 veya local path
            loadImageFromBase64(context, imageUrl, imageView);
        }
    }

    /**
     * URL'den resim yükleme - Supabase için optimize edilmiş
     */
    private static void loadImageFromUrl(Context context, String imageUrl, ImageView imageView) {
        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .dontTransform();
        
        Glide.with(context)
                .load(imageUrl)
                .apply(options)
                .thumbnail(0.3f)
                .into(imageView);
    }

    /**
     * Base64'ten resim yükleme
     */
    private static void loadImageFromBase64(Context context, String base64Image, ImageView imageView) {
        Bitmap bitmap = decodeBase64Safe(base64Image);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(R.drawable.ic_launcher_background);
        }
    }

    /**
     * Memory temizleme - Activity/Fragment destroy'da çağır
     */
    public static void clearImageCache(Context context) {
        if (context != null) {
            Glide.get(context).clearMemory();
        }
    }

    /**
     * Background thread'de cache temizleme
     */
    public static void clearImageCacheAsync(Context context) {
        if (context != null) {
            new Thread(() -> Glide.get(context).clearDiskCache()).start();
        }
    }
}
