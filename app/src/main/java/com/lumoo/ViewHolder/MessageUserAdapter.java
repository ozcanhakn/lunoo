package com.lumoo.ViewHolder;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lumoo.MessageActivity;
import com.lumoo.Model.MessageUser;
import com.lumoo.R;
import com.lumoo.ViewHolder.MessageFragmentViewHolder;

import java.util.List;

public class MessageUserAdapter extends RecyclerView.Adapter<MessageFragmentViewHolder> {
    private Context context;
    private List<MessageUser> messageUserList;

    public MessageUserAdapter(Context context, List<MessageUser> messageUserList) {
        this.context = context;
        this.messageUserList = messageUserList;
    }

    @NonNull
    @Override
    public MessageFragmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_layout_fragment_rec_item, parent, false);
        return new MessageFragmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageFragmentViewHolder holder, int position) {
        MessageUser user = messageUserList.get(position);

        // Güncellenmiş setProfileInChat metodunu kullan
        holder.setProfileInChat(
                ((android.app.Activity) context).getApplication(),
                user.getUsername(),
                user.getUid(),
                user.getProfileImage(),
                user.getLastMessage(),
                user.getLastMessageTimestamp(),
                user.getUnreadCount(),
                user.getFrame(),
                user.isHasFrame()
        );

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MessageActivity.class);
                intent.putExtra("uid", user.getUid());
                intent.putExtra("n", user.getUsername());
                intent.putExtra("u", user.getProfileImage());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return messageUserList.size();
    }
}