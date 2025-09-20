package com.lumoo.ViewHolder;

import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lumoo.Model.AllUser;
import com.lumoo.PublicProfileActivity;
import com.lumoo.R;
import com.lumoo.util.GlideUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.rxjava3.annotations.NonNull;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private ArrayList<AllUser> userList = new ArrayList<>();
    private Map<String, String> userFramesCache = new HashMap<>();
    private Map<String, String> userCountryCache = new HashMap<>();

    public void setUserList(ArrayList<AllUser> users) {
        this.userList = users;
        // Sadece görünecek kullanıcılar için ön yükleme yap
        preloadUserDataForVisibleUsers();
        notifyDataSetChanged();
    }

    private void preloadUserDataForVisibleUsers() {
        if (userList.isEmpty()) return;

        ArrayList<String> uidsToLoad = new ArrayList<>();
        int loadLimit = Math.min(userList.size(), 15); // Sadece ilk 15 kullanıcı

        for (int i = 0; i < loadLimit; i++) {
            AllUser user = userList.get(i);
            if (user.getUserId() != null && !user.getUserId().isEmpty()) {
                uidsToLoad.add(user.getUserId());
            }
        }

        if (uidsToLoad.isEmpty()) return;

        // Tek seferde tüm kullanıcı verilerini al
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Kullanıcılar");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userFramesCache.clear();
                userCountryCache.clear();

                for (String uid : uidsToLoad) {
                    DataSnapshot userSnapshot = snapshot.child(uid);
                    if (userSnapshot.exists()) {
                        // Frame bilgisi
                        String frame = userSnapshot.child("frame").getValue(String.class);
                        if (frame != null && !frame.isEmpty()) {
                            userFramesCache.put(uid, frame);
                        }

                        // Ülke kodu
                        String countryCode = userSnapshot.child("countryCode").getValue(String.class);
                        if (countryCode != null && !countryCode.isEmpty()) {
                            userCountryCache.put(uid, countryCode);
                        }
                    }
                }
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("UserAdapter", "Firebase query cancelled: " + error.getMessage());
            }
        });
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.online_user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        AllUser user = userList.get(position);
        String uid = user.getUserId();

        // Temel kullanıcı bilgileri
        holder.userName.setText(user.getKullanıcıAdı());
        GlideUtil.loadOriginalImage(holder.itemView.getContext(), user.getProfileImage(), holder.userImage);
        holder.userAge.setText(user.getDoğumTarihi());

        // Cinsiyet ikonu
        if (user.getGender() != null && !user.getGender().isEmpty()) {
            if (user.getGender().equals("Erkek")) {
                holder.imgGenderFlagOnline.setImageResource(R.drawable.maleicon);
            } else if (user.getGender().equals("Kadın")) {
                holder.imgGenderFlagOnline.setImageResource(R.drawable.femaleicon);
            }
        } else {
            holder.imgGenderFlagOnline.setImageResource(R.drawable.maleicon);
        }

        // UID kontrolü
        if (uid == null || uid.isEmpty()) {
            holder.imgOnlineFrame.setVisibility(View.GONE);
            holder.cardOnlineStatus.setCardBackgroundColor(Color.GRAY);
            holder.cardOnlineStatus.setVisibility(View.VISIBLE);
            holder.onlineUserCard.setOnClickListener(null);
            return;
        }

        // Online durumu
        if ("online".equals(user.getOnline())) {
            holder.cardOnlineStatus.setCardBackgroundColor(Color.GREEN);
            holder.cardOnlineStatus.setVisibility(View.VISIBLE);
        } else {
            holder.cardOnlineStatus.setCardBackgroundColor(Color.RED);
            holder.cardOnlineStatus.setVisibility(View.VISIBLE);
        }

        // Ülke bayrağı - Önbellekten al
        String countryCode = userCountryCache.get(uid);
        if (countryCode != null && !countryCode.isEmpty()) {
            int resId = holder.itemView.getResources().getIdentifier(
                    countryCode.toLowerCase(), "drawable",
                    holder.itemView.getContext().getPackageName()
            );
            if (resId != 0) {
                holder.imgCountryFlag.setVisibility(View.VISIBLE);
                holder.imgCountryFlag.setImageResource(resId);
            } else {
                holder.imgCountryFlag.setVisibility(View.VISIBLE);
                holder.imgCountryFlag.setImageResource(R.drawable.tr);
            }
        } else {
            holder.imgCountryFlag.setVisibility(View.VISIBLE);
            holder.imgCountryFlag.setImageResource(R.drawable.tr);
        }

        // Frame - Önbellekten al
        String frame = userFramesCache.get(uid);
        if (frame != null && !frame.isEmpty()) {
            holder.imgOnlineFrame.setVisibility(View.VISIBLE);
            setFrameImage(holder, frame);
        } else {
            holder.imgOnlineFrame.setVisibility(View.GONE);
        }

        // Tıklama listener'ı
        holder.onlineUserCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), PublicProfileActivity.class);
                intent.putExtra("uid", uid);
                intent.putExtra("url", user.getProfileImage());
                view.getContext().startActivity(intent);
            }
        });
    }

    private void setFrameImage(UserViewHolder holder, String frame) {
        switch (frame) {
            case "Melek":
                holder.imgOnlineFrame.setImageResource(R.drawable.melek_cercevesi);
                break;
            case "Goril":
                holder.imgOnlineFrame.setImageResource(R.drawable.goril_cercevesi);
                break;
            case "Aslan":
                holder.imgOnlineFrame.setImageResource(R.drawable.aslan_cercevesi);
                break;
            case "AskoKusko":
                holder.imgOnlineFrame.setImageResource(R.drawable.askokusko_cercevesi);
                break;
            case "Elmas":
                holder.imgOnlineFrame.setImageResource(R.drawable.elmas_cercevesi);
                break;
            case "Sarmasık":
                holder.imgOnlineFrame.setImageResource(R.drawable.sarmasik_cercevesi);
                break;
            case "Hilal":
                holder.imgOnlineFrame.setImageResource(R.drawable.hilalvekilic);
                break;
            case "Kelebek":
                holder.imgOnlineFrame.setImageResource(R.drawable.kelecekcercevesi);
                break;
            case "Miğfer":
                holder.imgOnlineFrame.setImageResource(R.drawable.migfer_cercevesi);
                break;
            case "Ateş":
                holder.imgOnlineFrame.setImageResource(R.drawable.ates_cercevesi);
                break;
            default:
                holder.imgOnlineFrame.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userAge;
        CircleImageView userImage;
        CircleImageView imgOnlineFrame;
        ImageView imgCountryFlag, imgGenderFlagOnline;
        RelativeLayout onlineUserCard;
        CardView cardOnlineStatus;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.txtNameOnline);
            userAge = itemView.findViewById(R.id.txtAgeOnline);
            userImage = itemView.findViewById(R.id.onlineProfileImage);
            onlineUserCard = itemView.findViewById(R.id.cardOnlineUser);
            cardOnlineStatus = itemView.findViewById(R.id.cardOnlineStatus);
            imgCountryFlag = itemView.findViewById(R.id.imgCountryFlag);
            imgOnlineFrame = itemView.findViewById(R.id.imgOnlineFrame);
            imgGenderFlagOnline = itemView.findViewById(R.id.imgGenderFlagOnline);
        }
    }
}