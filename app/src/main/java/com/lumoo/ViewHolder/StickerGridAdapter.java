package com.lumoo.ViewHolder;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class StickerGridAdapter extends BaseAdapter {

    public interface OnStickerClickListener {
        void onStickerClick(int stickerResId);
    }

    private Context context;
    private int[] stickerResIds;
    private OnStickerClickListener listener;

    public StickerGridAdapter(Context context, int[] stickerResIds, OnStickerClickListener listener) {
        this.context = context;
        this.stickerResIds = stickerResIds;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return stickerResIds != null ? stickerResIds.length : 0;
    }

    @Override
    public Object getItem(int position) {
        return stickerResIds != null ? stickerResIds[position] : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;

        if (convertView == null) {
            imageView = new ImageView(context);
            // Arka planı kaldır - şeffaf
            imageView.setBackgroundResource(android.R.color.transparent);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            // 64x64 dp boyutları
            int sizeInPx = (int) (128 * context.getResources().getDisplayMetrics().density);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    sizeInPx, // 64dp genişlik
                    sizeInPx  // 64dp yükseklik
            );
            imageView.setLayoutParams(params);

        } else {
            imageView = (ImageView) convertView;
        }

        if (stickerResIds != null && position < stickerResIds.length) {
            final int resId = stickerResIds[position];
            imageView.setImageResource(resId);

            imageView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStickerClick(resId);
                }
            });
        }

        return imageView;
    }

    public void updateStickers(int[] newStickerResIds) {
        this.stickerResIds = newStickerResIds;
        notifyDataSetChanged();
    }
}