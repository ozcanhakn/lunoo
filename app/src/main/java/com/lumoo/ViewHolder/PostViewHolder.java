package com.lumoo.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.lumoo.ItemClickListener;
import com.lumoo.R;

public class PostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView txtPostDesc, txtUsernamePost, txtPostDate, txtLikeCount, txtViewComments;
    public ImageView userImage, imgPost, btnLike, btnComment, btnPostMenu;

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public PostViewHolder(View itemView) {
        super(itemView);
        txtPostDesc = itemView.findViewById(R.id.txtPostDesc);
        txtUsernamePost = itemView.findViewById(R.id.txtUsernamePost);
        txtPostDate = itemView.findViewById(R.id.txtPostDate);
        txtLikeCount = itemView.findViewById(R.id.txtLikeCount);
        txtViewComments = itemView.findViewById(R.id.txtViewComments);
        userImage = itemView.findViewById(R.id.imgPostUserPhoto);
        imgPost = itemView.findViewById(R.id.imgPostRecItem);
        btnLike = itemView.findViewById(R.id.btnLike);
        btnComment = itemView.findViewById(R.id.btnComment);
        btnPostMenu = itemView.findViewById(R.id.btnPostMenu);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (itemClickListener != null) {
            itemClickListener.onClick(view, getAdapterPosition(), false);
        }
    }
}