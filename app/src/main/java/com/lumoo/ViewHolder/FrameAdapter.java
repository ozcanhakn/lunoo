package com.lumoo.ViewHolder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.lumoo.R;

import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;

public class FrameAdapter extends RecyclerView.Adapter<FrameAdapter.FrameViewHolder> {

    private List<String> frames;
    private OnFrameClickListener listener;

    public interface OnFrameClickListener {
        void onFrameClick(String frameName);
    }

    public FrameAdapter(List<String> frames, OnFrameClickListener listener) {
        this.frames = frames;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FrameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_frame, parent, false);
        return new FrameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FrameViewHolder holder, int position) {
        String frameName = frames.get(position);

        // Örnek: drawable/frame_sarmasik, drawable/frame_melek gibi isimlendirdiğini varsayıyorum.
        int imageResId = holder.itemView.getContext().getResources()
                .getIdentifier("frame_" + frameName.toLowerCase(), "drawable", holder.itemView.getContext().getPackageName());

        holder.imageView.setImageResource(imageResId);

        holder.itemView.setOnClickListener(v -> {
            listener.onFrameClick(frameName);
        });
    }

    @Override
    public int getItemCount() {
        return frames.size();
    }

    public static class FrameViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public FrameViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(com.lumoo.R.id.imageViewFrame);
        }
    }
}
