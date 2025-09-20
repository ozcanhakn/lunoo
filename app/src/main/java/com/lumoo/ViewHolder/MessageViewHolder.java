package com.lumoo.ViewHolder;

import android.app.Application;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lumoo.R;
import com.lumoo.util.GlideUtil;

public class MessageViewHolder extends RecyclerView.ViewHolder {
    public TextView sendertv, receivertv, senderTime, receiverTime, readStatus;
    public TextView senderTimeImage, receiverTimeImage, readStatusImage;
    public ImageView iv_sender, iv_receiver;
    public LinearLayout senderImageTimeContainer;

    public MessageViewHolder(View itemView) {
        super(itemView);
    }

    public void Setmessage(Application application, String message, String time, String date, String type,
                           String senderuid, String receiveruid, boolean isRead, long timestamp, String messageId){

        // Text message elements
        sendertv = itemView.findViewById(R.id.sender_tv);
        receivertv = itemView.findViewById(R.id.receiver_tv);
        senderTime = itemView.findViewById(R.id.sender_time);
        receiverTime = itemView.findViewById(R.id.receiver_time);
        readStatus = itemView.findViewById(R.id.read_status);

        // Image message elements
        iv_receiver = itemView.findViewById(R.id.iv_receiver);
        iv_sender = itemView.findViewById(R.id.iv_sender);
        senderTimeImage = itemView.findViewById(R.id.sender_time_image);
        receiverTimeImage = itemView.findViewById(R.id.receiver_time_image);
        readStatusImage = itemView.findViewById(R.id.read_status_image);
        senderImageTimeContainer = itemView.findViewById(R.id.sender_image_time_container);

        // Hide all views initially
        hideAllViews();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentUid = user.getUid();

        // Mesajı okundu olarak işaretle
        if (!currentUid.equals(senderuid) && !isRead && messageId != null) {
            DatabaseReference messageRef = FirebaseDatabase.getInstance()
                    .getReference("Message")
                    .child(currentUid)
                    .child(senderuid)
                    .child(messageId);
            messageRef.child("read").setValue(true);
        }

        if (type.equals("text")){
            handleTextMessage(currentUid, senderuid, receiveruid, message, time, isRead, application);
        } else if (type.equals("iv")){
            handleImageMessage(currentUid, senderuid, receiveruid, message, time, isRead, application);
        }
    }

    private void hideAllViews() {
        // Hide text message views
        if (sendertv != null) sendertv.setVisibility(View.GONE);
        if (receivertv != null) receivertv.setVisibility(View.GONE);
        if (senderTime != null) senderTime.setVisibility(View.GONE);
        if (receiverTime != null) receiverTime.setVisibility(View.GONE);
        if (readStatus != null) readStatus.setVisibility(View.GONE);

        // Hide image message views
        if (iv_sender != null) iv_sender.setVisibility(View.GONE);
        if (iv_receiver != null) iv_receiver.setVisibility(View.GONE);
        if (senderTimeImage != null) senderTimeImage.setVisibility(View.GONE);
        if (receiverTimeImage != null) receiverTimeImage.setVisibility(View.GONE);
        if (readStatusImage != null) readStatusImage.setVisibility(View.GONE);
        if (senderImageTimeContainer != null) senderImageTimeContainer.setVisibility(View.GONE);

        // Hide parent containers - ViewParent'i View'a cast et
        if (sendertv != null && sendertv.getParent() != null && sendertv.getParent().getParent() instanceof View) {
            ((View) sendertv.getParent().getParent()).setVisibility(View.GONE);
        }
        if (receivertv != null && receivertv.getParent() != null && receivertv.getParent().getParent() instanceof View) {
            ((View) receivertv.getParent().getParent()).setVisibility(View.GONE);
        }
        if (iv_sender != null && iv_sender.getParent() != null && iv_sender.getParent().getParent() instanceof View) {
            ((View) iv_sender.getParent().getParent()).setVisibility(View.GONE);
        }
        if (iv_receiver != null && iv_receiver.getParent() != null && iv_receiver.getParent().getParent() instanceof View) {
            ((View) iv_receiver.getParent().getParent()).setVisibility(View.GONE);
        }
    }

    private void handleTextMessage(String currentUid, String senderuid, String receiveruid,
                                   String message, String time, boolean isRead, Application application) {
        if (currentUid.equals(senderuid)) {
            // Gönderilen mesaj
            if (sendertv != null && sendertv.getParent() != null && sendertv.getParent().getParent() instanceof View) {
                ((View) sendertv.getParent().getParent()).setVisibility(View.VISIBLE);
            }
            sendertv.setVisibility(View.VISIBLE);
            senderTime.setVisibility(View.VISIBLE);
            readStatus.setVisibility(View.VISIBLE);

            sendertv.setText(message);
            senderTime.setText(time);

            // Okunma durumunu göster
            if (isRead) {
                readStatus.setText("✓✓");
                readStatus.setTextColor(application.getResources().getColor(android.R.color.holo_blue_light));
            } else {
                readStatus.setText("✓");
                readStatus.setTextColor(application.getResources().getColor(android.R.color.darker_gray));
            }
        } else if (currentUid.equals(receiveruid)) {
            // Alınan mesaj
            if (receivertv != null && receivertv.getParent() != null && receivertv.getParent().getParent() instanceof View) {
                ((View) receivertv.getParent().getParent()).setVisibility(View.VISIBLE);
            }
            receivertv.setVisibility(View.VISIBLE);
            receiverTime.setVisibility(View.VISIBLE);

            receivertv.setText(message);
            receiverTime.setText(time);
        }
    }

    private void handleImageMessage(String currentUid, String senderuid, String receiveruid,
                                    String imageUrl, String time, boolean isRead, Application application) {
        if (currentUid.equals(senderuid)) {
            // Gönderilen resim
            if (iv_sender != null && iv_sender.getParent() != null && iv_sender.getParent().getParent() instanceof View) {
                ((View) iv_sender.getParent().getParent()).setVisibility(View.VISIBLE);
            }
            iv_sender.setVisibility(View.VISIBLE);
            senderImageTimeContainer.setVisibility(View.VISIBLE);
            senderTimeImage.setVisibility(View.VISIBLE);
            readStatusImage.setVisibility(View.VISIBLE);

            // GlideUtil ile resmi yükle
            GlideUtil.loadOriginalImage(application, imageUrl, iv_sender);

            senderTimeImage.setText(time);

            if (isRead) {
                readStatusImage.setText("✓✓");
                readStatusImage.setTextColor(application.getResources().getColor(android.R.color.white));
            } else {
                readStatusImage.setText("✓");
                readStatusImage.setTextColor(application.getResources().getColor(android.R.color.white));
            }
        } else if (currentUid.equals(receiveruid)) {
            // Alınan resim
            if (iv_receiver != null && iv_receiver.getParent() != null && iv_receiver.getParent().getParent() instanceof View) {
                ((View) iv_receiver.getParent().getParent()).setVisibility(View.VISIBLE);
            }
            iv_receiver.setVisibility(View.VISIBLE);
            receiverTimeImage.setVisibility(View.VISIBLE);

            // GlideUtil ile resmi yükle
            GlideUtil.loadOriginalImage(application, imageUrl, iv_receiver);

            receiverTimeImage.setText(time);
        }
    }
}