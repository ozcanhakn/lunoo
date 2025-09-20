package com.lumoo.ViewHolder;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.lumoo.Model.AllUser;
import com.lumoo.R;
import com.lumoo.StoryEditorActivity;
import com.lumoo.util.GlideUtil;

import java.util.ArrayList;
import java.util.List;

public class StoryPersonAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<AllUser> userList;
    private List<AllUser> filteredList;
    private Context context;

    private static final int TYPE_USER = 0;
    private static final int TYPE_LOADING = 1;

    private boolean showLoading = false;

    public StoryPersonAdapter(Context context, List<AllUser> userList) {
        this.context = context;
        this.userList = new ArrayList<>(userList); // Orijinal listeyi koru
        this.filteredList = new ArrayList<>();
        filter(""); // İlk başta ilk 5 kullanıcıyı göster
    }

    @Override
    public int getItemViewType(int position) {
        return (position == filteredList.size() && showLoading) ? TYPE_LOADING : TYPE_USER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_LOADING) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.story_person_item, parent, false);
            return new UserViewHolder(view);
        }
    }
    public interface OnUserClickListener {
        void onUserClick(AllUser user);
    }

    private OnUserClickListener userClickListener;

    public void setOnUserClickListener(OnUserClickListener listener) {
        this.userClickListener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof UserViewHolder) {
            AllUser user = filteredList.get(position);
            UserViewHolder userHolder = (UserViewHolder) holder;

            userHolder.txtUsername.setText(user.getKullanıcıAdı());
            GlideUtil.loadOriginalImage(holder.itemView.getContext(), user.getProfileImage(), userHolder.userImage);

            // Tıklama olayı
            // Tıklama olayı - DÜZELTİLMİŞ
            userHolder.storyItem.setOnClickListener(v -> {
                if (userClickListener != null) {
                    userClickListener.onUserClick(user);
                }
                // Kullanıcıya tıklama işlemleri
            });
        } else if (holder instanceof LoadingViewHolder) {
            // Loading görünümü için herhangi bir işlem yapma
        }
    }

    @Override
    public int getItemCount() {
        return filteredList.size() + (showLoading ? 1 : 0);
    }

    // Arama filtresi - DÜZELTİLMİŞ
    public void filter(String query) {
        filteredList.clear();

        if (query.isEmpty()) {
            // İlk 5 kullanıcıyı göster
            int limit = Math.min(userList.size(), 5);
            for (int i = 0; i < limit; i++) {
                filteredList.add(userList.get(i));
            }
        } else {
            // Arama sorgusuna göre filtrele
            String lowerCaseQuery = query.toLowerCase();
            for (AllUser user : userList) {
                if (user.getKullanıcıAdı().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(user);
                    if (filteredList.size() >= 20) break; // Maksimum 20 sonuç
                }
            }
        }
        notifyDataSetChanged();
    }

    // Tüm kullanıcıları güncelle - DÜZELTİLMİŞ
    public void setUserList(List<AllUser> userList, boolean clearList) {
        if (clearList) {
            this.userList.clear();
        }

        // Yeni kullanıcıları ekle
        this.userList.addAll(userList);

        // Maksimum 50 kullanıcı ile sınırla
        if (this.userList.size() > 50) {
            this.userList = this.userList.subList(0, 50);
        }

        // Filtreyi yeniden uygula
        filter("");
    }

    // Yükleme durumunu ayarla
    public void setLoading(boolean loading) {
        if (showLoading != loading) {
            showLoading = loading;
            if (loading) {
                notifyItemInserted(filteredList.size());
            } else {
                notifyItemRemoved(filteredList.size());
            }
        }
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView userImage;
        TextView txtUsername;
        CardView cardUserImage;
        ConstraintLayout storyItem;
        View statusIndicator;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.userImageRecStory);
            txtUsername = itemView.findViewById(R.id.txtFriendRequestNameStory);
            cardUserImage = itemView.findViewById(R.id.cardUserImageStory);
            storyItem = itemView.findViewById(R.id.recItemConstStory);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);



            setupAnimations();
            setupClickEffects();
            showGlowEffect();
            startPulseAnimation();
        }
        private void setupAnimations() {
            // Dönen ring veya pulse efekti için örnek animasyon
            Animation rotateAnimation = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.story_ring_rotation);
            Animation pulseAnimation = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.pulse_animation);

            // Başlangıçta kullanıcı online ise pulse animasyonu uygula
            // Burada örnek olarak her userImage için pulse yapıyoruz
            userImage.startAnimation(pulseAnimation);

            // İsteğe bağlı: cardUserImage üzerinde sürekli dönen animasyon
            cardUserImage.startAnimation(rotateAnimation);
        }

        private void setupClickEffects() {
            cardUserImage.setOnClickListener(v -> {
                // Scale animasyonu ile tıklama efekti
                cardUserImage.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(100)
                        .withEndAction(() -> {
                            cardUserImage.animate()
                                    .scaleX(1.05f)
                                    .scaleY(1.05f)
                                    .setDuration(100)
                                    .withEndAction(() -> {
                                        cardUserImage.animate()
                                                .scaleX(1.0f)
                                                .scaleY(1.0f)
                                                .setDuration(100)
                                                .start();
                                    }).start();
                        }).start();
            });

            // Long press glow efekti
            cardUserImage.setOnLongClickListener(v -> {
                cardUserImage.animate()
                        .translationZ(12f)
                        .setDuration(300)
                        .withEndAction(() -> {
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                cardUserImage.animate()
                                        .translationZ(4f)
                                        .setDuration(300)
                                        .start();
                            }, 2000);
                        }).start();
                return true;
            });
        }

        private void showGlowEffect() {
            // Glow efekti için elevation artır
            storyItem.animate()
                    .translationZ(12f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        // 2 saniye sonra normale dön
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            storyItem.animate()
                                    .translationZ(4f)
                                    .setDuration(300)
                                    .start();
                        }, 2000);
                    })
                    .start();
        }

        private void startPulseAnimation() {
            Animation pulseAnimation = AnimationUtils.loadAnimation(
                    itemView.getContext(),
                    R.anim.pulse_animation
            );
            statusIndicator.startAnimation(pulseAnimation);
        }
    }


    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}