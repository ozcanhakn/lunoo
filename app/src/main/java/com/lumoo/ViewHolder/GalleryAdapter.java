package com.lumoo.ViewHolder;


import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lumoo.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {

    private List<Uri> imageList;
    private OnImageClickListener onImageClickListener;

    // Tıklama olayını alacak bir arayüz (Interface)
    public interface OnImageClickListener {
        void onImageClick(Uri imageUri);
    }

    // Yapıcı metot (Constructor) ile tıklama listener'ını alacağız
    public GalleryAdapter(List<Uri> imageList, OnImageClickListener onImageClickListener) {
        this.imageList = imageList;
        this.onImageClickListener = onImageClickListener;
    }

    @Override
    public GalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_image, parent, false);
        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GalleryViewHolder holder, int position) {
        Uri imageUri = imageList.get(position);
        Picasso.get()
                .load(imageUri)
                .resize(300, 300)
                .centerCrop()
                .into(holder.imageView);

        // Tıklama olayını her öğe için ekliyoruz
        holder.itemView.setOnClickListener(v -> onImageClickListener.onImageClick(imageUri));
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public static class GalleryViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public GalleryViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageItem);
        }
    }
}
