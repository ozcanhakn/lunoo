package com.lumoo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lumoo.ViewHolder.GalleryAdapter;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;

public class ShareActivity extends AppCompatActivity {

    private ImageView imgShare, btnCancelShare;
    private RecyclerView galleryRecyclerView;
    private List<Uri> imageUris = new ArrayList<>();
    private GalleryAdapter galleryAdapter;
    private static final int STORAGE_PERMISSION_CODE = 100;

    TextView btnContinueShare;
    private int selectedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_share);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imgShare = findViewById(R.id.imgShare);
        galleryRecyclerView = findViewById(R.id.galleryRecyclerView);
        btnContinueShare = findViewById(R.id.btnContinueShare);
        btnCancelShare = findViewById(R.id.btnCancelShare);

        imageUris = getGalleryImages(this);

        btnCancelShare.setOnClickListener(v -> {
            Intent intent = new Intent(ShareActivity.this, HomeActivity.class);
            intent.putExtra("goToFragment", "profile");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnContinueShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedPosition == -1) {
                    Toast.makeText(ShareActivity.this, "Lütfen bir fotoğraf seçin", Toast.LENGTH_SHORT).show();
                    return;
                }

                Uri selectedImageUri = imageUris.get(selectedPosition);
                Intent intent = new Intent(ShareActivity.this, PostShareActivity.class);
                intent.putExtra("imageUri", selectedImageUri.toString());
                startActivity(intent);
            }
        });

        galleryRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        galleryAdapter = new GalleryAdapter(imageUris, imageUri -> {
            imgShare.setImageURI(imageUri);
            selectedPosition = imageUris.indexOf(imageUri);
        });

        checkGalleryPermission();
        galleryRecyclerView.setAdapter(galleryAdapter);
        galleryRecyclerView.setItemViewCacheSize(20);
        galleryRecyclerView.setDrawingCacheEnabled(true);
        galleryRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        galleryAdapter.notifyDataSetChanged();
    }

    private void checkGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_CODE);
            } else {
                getGalleryImages(this);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            } else {
                getGalleryImages(this);
            }
        } else {
            getGalleryImages(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getGalleryImages(this);
            } else {
                Toast.makeText(this, "Galeri erişimi reddedildi!", Toast.LENGTH_SHORT).show();
                if (!shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showPermissionDeniedDialog();
                }
            }
        }
    }

    private void showPermissionDeniedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Galeri Erişimi Gerekli");
        builder.setMessage("Fotoğraflarınızı görmek için izin vermelisiniz. Ayarlardan izin verebilirsiniz.");
        builder.setPositiveButton("Ayarlar", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        });
        builder.setNegativeButton("İptal", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private List<Uri> getGalleryImages(Context context) {
        List<Uri> uris = new ArrayList<>();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media._ID};

        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                Uri imageUri = Uri.withAppendedPath(uri, String.valueOf(cursor.getLong(columnIndex)));
                uris.add(imageUri);
            }
            cursor.close();
        }

        return uris;
    }
}