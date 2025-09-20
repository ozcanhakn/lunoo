package com.lumoo.ViewHolder;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.lumoo.Model.Gift;
import com.lumoo.R;

import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;

public class GiftAdapter extends RecyclerView.Adapter<GiftAdapter.GiftViewHolder> {
    private List<Gift> gifts;
    private OnGiftClickListener listener;
    private int selectedPosition = -1; // Seçili item tracking

    public interface OnGiftClickListener {
        void onGiftClick(Gift gift);
    }

    public GiftAdapter(List<Gift> gifts, OnGiftClickListener listener) {
        this.gifts = gifts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GiftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gift, parent, false);
        return new GiftViewHolder(view);
    }

    @Override

    public void onBindViewHolder(@NonNull GiftViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Gift gift = gifts.get(position);

        // Gift name'i set et
        holder.textGiftName.setText(gift.getName());

        // Credit cost'u set et (sadece sayı)
        holder.textCreditCost.setText(String.valueOf(gift.getCreditCost()));

        // Glide ile image yükleme - daha smooth
        RequestOptions requestOptions = new RequestOptions()
                .transform(new RoundedCorners(16))
                .placeholder(R.drawable.ic_gift_placeholder) // Placeholder ekle
                .error(R.drawable.ic_gift_error); // Error image ekle

        if (gift.getGiftType() == Gift.GiftType.GIF) {
            // GIF için Glide kullan
            Glide.with(holder.itemView.getContext())
                    .asGif()
                    .load(gift.getRawResId())
                    .apply(requestOptions)
                    .into(holder.imageViewGift);
        } else {
            // PNG için Glide kullan
            Glide.with(holder.itemView.getContext())
                    .load(gift.getRawResId())
                    .apply(requestOptions)
                    .into(holder.imageViewGift);
        }

        // Selection state'i güncelle
        updateSelectionState(holder, position);

        // YENI - Basit click listener (animasyon olmadan)
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.util.Log.d("GiftAdapter", "=== CLICK EVENT STARTED ===");
                android.util.Log.d("GiftAdapter", "Position: " + position);
                android.util.Log.d("GiftAdapter", "Gift: " + gift.getName());
                android.util.Log.d("GiftAdapter", "Listener null check: " + (listener == null ? "NULL" : "NOT NULL"));

                // Önceki selection'ı temizle
                int previousSelected = selectedPosition;
                selectedPosition = position;

                android.util.Log.d("GiftAdapter", "Previous selected: " + previousSelected + ", New selected: " + selectedPosition);

                // UI güncellemeleri
                if (previousSelected != -1) {
                    notifyItemChanged(previousSelected);
                }
                notifyItemChanged(position);

                // Basit scale animasyonu
                v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(50)
                        .withEndAction(() -> v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(50));

                // CALLBACK'İ HEMEN ÇAĞIR - ANİMASYON BEKLEMEDEn
                android.util.Log.d("GiftAdapter", "About to call callback...");
                if (listener != null) {
                    android.util.Log.d("GiftAdapter", "Calling onGiftClick for: " + gift.getName());
                    listener.onGiftClick(gift);
                    android.util.Log.d("GiftAdapter", "Callback called successfully");
                } else {
                    android.util.Log.e("GiftAdapter", "LISTENER IS NULL - CALLBACK NOT CALLED!");
                }

                android.util.Log.d("GiftAdapter", "=== CLICK EVENT COMPLETED ===");
            }
        });
    }

    private void updateSelectionState(GiftViewHolder holder, int position) {
        if (selectedPosition == position) {
            // Selected state
            holder.selectionOverlay.setVisibility(View.VISIBLE);
            holder.itemView.setScaleX(0.98f);
            holder.itemView.setScaleY(0.98f);
        } else {
            // Normal state
            holder.selectionOverlay.setVisibility(View.GONE);
            holder.itemView.setScaleX(1.0f);
            holder.itemView.setScaleY(1.0f);
        }
    }

    @Override
    public int getItemCount() {
        return gifts.size();
    }

    // Selected position'ı reset etmek için method
    public void resetSelection() {
        int previousSelected = selectedPosition;
        selectedPosition = -1;
        if (previousSelected != -1) {
            notifyItemChanged(previousSelected);
        }
    }

    public static class GiftViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewGift;
        TextView textCreditCost;
        TextView textGiftName;
        View selectionOverlay;

        public GiftViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewGift = itemView.findViewById(R.id.imageViewGift);
            textCreditCost = itemView.findViewById(R.id.textCreditCost);
            textGiftName = itemView.findViewById(R.id.textGiftName);
            selectionOverlay = itemView.findViewById(R.id.selectionOverlay);
        }
    }
}