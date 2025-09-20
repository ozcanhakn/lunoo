package com.lumoo.ViewHolder;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.lumoo.Model.BackgroundTheme;
import com.lumoo.R;

import java.util.List;

public class BackgroundThemeAdapter extends RecyclerView.Adapter<BackgroundThemeAdapter.ThemeViewHolder> {

    private List<BackgroundTheme> themeList;
    private OnThemeClickListener listener;

    public interface OnThemeClickListener {
        void onThemeClick(BackgroundTheme theme);
    }

    public BackgroundThemeAdapter(List<BackgroundTheme> themeList, OnThemeClickListener listener) {
        this.themeList = themeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ThemeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_background_theme, parent, false);
        return new ThemeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThemeViewHolder holder, int position) {
        BackgroundTheme theme = themeList.get(position);

        holder.themeName.setText(theme.getName());
        holder.themeColor.setCardBackgroundColor(Color.parseColor(theme.getColorCode()));

        // Seçili tema gösterimi - MaterialCardView stroke özellikleri
        if (theme.isSelected()) {
            holder.themeColor.setStrokeColor(Color.parseColor("#25D366"));
            holder.themeColor.setStrokeWidth(4);
        } else {
            holder.themeColor.setStrokeWidth(0);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onThemeClick(theme);
            }
        });
    }

    @Override
    public int getItemCount() {
        return themeList.size();
    }

    static class ThemeViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView themeColor;
        TextView themeName;

        public ThemeViewHolder(@NonNull View itemView) {
            super(itemView);
            themeColor = itemView.findViewById(R.id.themeColor);
            themeName = itemView.findViewById(R.id.themeName);
        }
    }
}