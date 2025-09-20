package com.lumoo.ViewHolder;



import android.app.Application;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lumoo.R;
import com.lumoo.util.GlideUtil;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MessageFragmentViewHolder extends RecyclerView.ViewHolder{
    public TextView sendMessageBtn;
    public ConstraintLayout linearLayout;
    public ImageView imageView, frameImageView;
    public TextView unreadCountBadge;

    public MessageFragmentViewHolder(View itemView) {
        super(itemView);
    }

    public void setProfileInChat(Application fragmentActivity, String name, String uid,
                                 String url, String lastMessage, long lastMessageTimestamp,
                                 int unreadCount, String frame, boolean hasFrame){

        imageView = itemView.findViewById(R.id.userImageRecChat);
        frameImageView = itemView.findViewById(R.id.userFrameRecChat);
        TextView nametv = itemView.findViewById(R.id.txtFriendRequestName);
        TextView lastMessageTv = itemView.findViewById(R.id.txtRecChatUserMessage);
        TextView lastMessageTimeTv = itemView.findViewById(R.id.txtLastMessageTime);
        unreadCountBadge = itemView.findViewById(R.id.unreadCountBadge);
        linearLayout = itemView.findViewById(R.id.recItemConst);

        Log.d("ViewHolder", "Setting profile: " + name + " - " + lastMessage);

        // Kullanıcı bilgilerini set et
        nametv.setText(name);

        // Profil resmini decode et ve set et
        try {
            if (url != null && !url.isEmpty()) {
                   GlideUtil.loadOriginalImage(itemView.getContext(), url, imageView);
            }
        } catch (Exception e) {
            Log.e("ViewHolder", "Error decoding profile image: " + e.getMessage());
        }

        // Çerçeve kontrolü
        if (hasFrame && frame != null && !frame.isEmpty()) {
            frameImageView.setVisibility(View.VISIBLE);
            Picasso.get().load(frame).into(frameImageView);
        } else {
            frameImageView.setVisibility(View.GONE);
        }

        // Son mesajı göster
        if (lastMessage != null && !lastMessage.isEmpty()) {
            lastMessageTv.setText(lastMessage);
        } else {
            lastMessageTv.setText("Henüz mesaj yok");
        }

        // Zamanı formatla ve göster
        if (lastMessageTimestamp > 0) {
            String timeText = formatTime(lastMessageTimestamp);
            lastMessageTimeTv.setText(timeText);
        } else {
            lastMessageTimeTv.setText("");
        }

        // Okunmamış mesaj sayısını göster
        if (unreadCount > 0) {
            unreadCountBadge.setVisibility(View.VISIBLE);
            unreadCountBadge.setText(String.valueOf(unreadCount));
        } else {
            unreadCountBadge.setVisibility(View.GONE);
        }
    }

    // Eski method - geriye dönük uyumluluk için
    public void setProfileInChat(Application fragmentActivity, String name, String uid, String url){
        setProfileInChat(fragmentActivity, name, uid, url, "Henüz mesaj yok", 0, 0, null, false);
    }

    private String formatTime(long timestamp) {
        Date messageDate = new Date(timestamp);
        Date currentDate = new Date();
        Calendar messageCal = Calendar.getInstance();
        Calendar currentCal = Calendar.getInstance();

        messageCal.setTime(messageDate);
        currentCal.setTime(currentDate);

        // Aynı gün ise sadece saat göster
        if (messageCal.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR) &&
                messageCal.get(Calendar.DAY_OF_YEAR) == currentCal.get(Calendar.DAY_OF_YEAR)) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return timeFormat.format(messageDate);
        }
        // Dün ise "Dün" göster
        else if (currentCal.get(Calendar.DAY_OF_YEAR) - messageCal.get(Calendar.DAY_OF_YEAR) == 1 &&
                messageCal.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR)) {
            return "Dün";
        }
        // Daha eski ise tarih göster
        else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
            return dateFormat.format(messageDate);
        }
    }
}