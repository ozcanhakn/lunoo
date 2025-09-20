package com.lumoo.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.lumoo.ItemClickListener;
import com.lumoo.R;

public class AllUserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView txtNameOnline,txtAgeOnline;
    public ImageView onlineProfileImage,onlineStatusImg;
    public CardView cardOnlineUser;

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }




    public AllUserViewHolder(View itemView) {
        super(itemView);
        txtNameOnline = itemView.findViewById(R.id.txtNameOnline);
        txtAgeOnline = itemView.findViewById(R.id.txtAgeOnline);
        onlineProfileImage = itemView.findViewById(R.id.onlineProfileImage);
        cardOnlineUser = itemView.findViewById(R.id.cardOnlineUser);

    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view,getAdapterPosition(),false);
    }
}
