package com.lumoo;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class EmojiGridAdapter extends BaseAdapter {

    public interface OnEmojiClickListener {
        void onEmojiClick(String emoji);
    }

    private Context context;
    private String[] emojis;
    private OnEmojiClickListener listener;

    public EmojiGridAdapter(Context context, String[] emojis, OnEmojiClickListener listener) {
        this.context = context;
        this.emojis = emojis;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return emojis != null ? emojis.length : 0;
    }

    @Override
    public Object getItem(int position) {
        return emojis != null ? emojis[position] : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView;

        if (convertView == null) {
            textView = new TextView(context);
            textView.setTextSize(24);
            textView.setPadding(16, 16, 16, 16);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setGravity(android.view.Gravity.CENTER);

            // Simple background
            textView.setBackgroundResource(android.R.drawable.btn_default);

            // Set layout params
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    120
            );
            textView.setLayoutParams(params);
        } else {
            textView = (TextView) convertView;
        }

        if (emojis != null && position < emojis.length) {
            String emoji = emojis[position];
            textView.setText(emoji);

            textView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEmojiClick(emoji);
                }
            });
        }

        return textView;
    }

    public void updateEmojis(String[] newEmojis) {
        this.emojis = newEmojis;
        notifyDataSetChanged();
    }
}