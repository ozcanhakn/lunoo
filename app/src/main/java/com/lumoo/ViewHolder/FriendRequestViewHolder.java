package com.lumoo.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.lumoo.ItemClickListener;
import com.lumoo.R;

public class FriendRequestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView txtFriendRequestName;
    public ImageView userImageRequest;
    public CardView cardViewApprove,cardViewReject;

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }




    public FriendRequestViewHolder(View itemView) {
        super(itemView);
        txtFriendRequestName = itemView.findViewById(R.id.txtFriendRequestName);
        cardViewApprove = itemView.findViewById(R.id.cardViewApprove);
        userImageRequest = itemView.findViewById(R.id.userImageRequest);
        cardViewReject = itemView.findViewById(R.id.cardViewReject);

    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view,getAdapterPosition(),false);
    }
}
