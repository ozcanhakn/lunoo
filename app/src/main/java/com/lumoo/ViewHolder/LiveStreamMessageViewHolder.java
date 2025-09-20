package com.lumoo.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.lumoo.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class LiveStreamMessageViewHolder extends RecyclerView.ViewHolder {
    public TextView liveStreamUserName, liveStreamUserMessage;
    public CircleImageView imageUserLiveStream,imgUserLiveStreamFrame;




    public LiveStreamMessageViewHolder(View itemView) {
        super(itemView);
        liveStreamUserName = itemView.findViewById(R.id.liveStreamUserName);
        liveStreamUserMessage = itemView.findViewById(R.id.liveStreamUserMessage);
        imageUserLiveStream = itemView.findViewById(R.id.imageUserLiveStream);
        imgUserLiveStreamFrame = itemView.findViewById(R.id.imgLiveStreamChatFrame);

    }
}