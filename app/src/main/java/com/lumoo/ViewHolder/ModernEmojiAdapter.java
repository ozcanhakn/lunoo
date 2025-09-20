package com.lumoo.ViewHolder;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lumoo.R;

import java.util.ArrayList;
import java.util.List;

public class ModernEmojiAdapter extends RecyclerView.Adapter<ModernEmojiAdapter.EmojiViewHolder> {

    public interface OnEmojiClickListener {
        void onEmojiClick(String emoji, int position);
        void onEmojiLongClick(String emoji, int position);
    }

    private Context context;
    private List<String> emojis;
    private OnEmojiClickListener listener;
    private List<String> filteredEmojis;
    private int selectedPosition = -1;
    private Handler animationHandler = new Handler();

    public ModernEmojiAdapter(Context context, String[] emojis, OnEmojiClickListener listener) {
        this.context = context;
        this.emojis = new ArrayList<>();
        this.filteredEmojis = new ArrayList<>();

        if (emojis != null) {
            for (String emoji : emojis) {
                this.emojis.add(emoji);
                this.filteredEmojis.add(emoji);
            }
        }

        this.listener = listener;
    }

    @NonNull
    @Override
    public EmojiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_emoji, parent, false);
        return new EmojiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmojiViewHolder holder, int position) {
        String emoji = filteredEmojis.get(position);
        holder.bind(emoji, position);

        // Staggered animation for initial load
        if (holder.itemView.getAlpha() == 0) {
            holder.itemView.setAlpha(0);
            holder.itemView.setScaleX(0.8f);
            holder.itemView.setScaleY(0.8f);

            animationHandler.postDelayed(() -> {
                ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(holder.itemView, "alpha", 0f, 1f);
                ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(holder.itemView, "scaleX", 0.8f, 1f);
                ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(holder.itemView, "scaleY", 0.8f, 1f);

                AnimatorSet animSet = new AnimatorSet();
                animSet.playTogether(alphaAnim, scaleXAnim, scaleYAnim);
                animSet.setDuration(300);
                animSet.start();
            }, position * 20); // Staggered delay
        }
    }

    @Override
    public int getItemCount() {
        return filteredEmojis != null ? filteredEmojis.size() : 0;
    }

    public void updateEmojis(String[] newEmojis) {
        this.emojis.clear();
        this.filteredEmojis.clear();

        if (newEmojis != null) {
            for (String emoji : newEmojis) {
                this.emojis.add(emoji);
                this.filteredEmojis.add(emoji);
            }
        }

        selectedPosition = -1;
        notifyDataSetChanged();
    }

    public void filterEmojis(String query) {
        filteredEmojis.clear();

        if (query == null || query.trim().isEmpty()) {
            filteredEmojis.addAll(emojis);
        } else {
            // Simple filtering - you can enhance this with emoji name matching
            String lowerQuery = query.toLowerCase().trim();
            for (String emoji : emojis) {
                // This is a simple approach - you'd want to add emoji name/keyword matching
                if (getEmojiKeywords(emoji).contains(lowerQuery)) {
                    filteredEmojis.add(emoji);
                }
            }
        }

        notifyDataSetChanged();
    }

    private String getEmojiKeywords(String emoji) {
        // Simple keyword mapping - expand this based on your needs
        switch (emoji) {
            case "😊": return "smile happy joy";
            case "😂": return "laugh funny lol";
            case "❤️": return "heart love red";
            case "😭": return "cry sad tears";
            case "🔥": return "fire hot flame";
            case "👍": return "thumbs up like good";
            case "🎉": return "party celebrate confetti";
            case "😍": return "love eyes heart";
            case "🤔": return "thinking wonder hmm";
            case "😎": return "cool sunglasses awesome";
            // Add more mappings as needed
            default: return emoji.toLowerCase();
        }
    }

    public void selectEmoji(int position) {
        int oldPosition = selectedPosition;
        selectedPosition = position;

        if (oldPosition != -1) {
            notifyItemChanged(oldPosition);
        }
        if (selectedPosition != -1) {
            notifyItemChanged(selectedPosition);
        }
    }

    class EmojiViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout emojiContainer;
        private TextView emojiText;
        private View selectionOverlay;
        private View pressOverlay;

        public EmojiViewHolder(@NonNull View itemView) {
            super(itemView);
            emojiContainer = itemView.findViewById(com.lumoo.R.id.emoji_container);
            emojiText = itemView.findViewById(R.id.emoji_text);
            selectionOverlay = itemView.findViewById(R.id.selection_overlay);
            pressOverlay = itemView.findViewById(R.id.press_overlay);
        }

        public void bind(String emoji, int position) {
            emojiText.setText(emoji);

            // Selection state
            boolean isSelected = position == selectedPosition;
            selectionOverlay.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            selectionOverlay.setAlpha(isSelected ? 1f : 0f);

            // Click listeners with animations
            emojiContainer.setOnClickListener(v -> {
                // Press animation
                animatePress(v, () -> {
                    if (listener != null) {
                        selectEmoji(position);
                        listener.onEmojiClick(emoji, position);
                    }
                });
            });

            emojiContainer.setOnLongClickListener(v -> {
                // Long press animation
                animateLongPress(v, () -> {
                    if (listener != null) {
                        listener.onEmojiLongClick(emoji, position);
                    }
                });
                return true;
            });

            // Hover effects for better UX
            emojiContainer.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        animateHoverStart();
                        break;
                    case android.view.MotionEvent.ACTION_UP:
                    case android.view.MotionEvent.ACTION_CANCEL:
                        animateHoverEnd();
                        break;
                }
                return false; // Allow other listeners to process
            });
        }

        private void animatePress(View view, Runnable onComplete) {
            ObjectAnimator scaleXDown = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.9f);
            ObjectAnimator scaleYDown = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.9f);

            AnimatorSet downSet = new AnimatorSet();
            downSet.playTogether(scaleXDown, scaleYDown);
            downSet.setDuration(100);

            downSet.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    ObjectAnimator scaleXUp = ObjectAnimator.ofFloat(view, "scaleX", 0.9f, 1f);
                    ObjectAnimator scaleYUp = ObjectAnimator.ofFloat(view, "scaleY", 0.9f, 1f);

                    AnimatorSet upSet = new AnimatorSet();
                    upSet.playTogether(scaleXUp, scaleYUp);
                    upSet.setDuration(100);
                    upSet.start();

                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            });

            downSet.start();
        }

        private void animateLongPress(View view, Runnable onComplete) {
            // Vibration effect simulation
            ValueAnimator vibrate = ValueAnimator.ofFloat(0f, 5f, -5f, 3f, -3f, 0f);
            vibrate.setDuration(200);
            vibrate.addUpdateListener(anim -> {
                float value = (Float) anim.getAnimatedValue();
                view.setTranslationX(value);
            });

            vibrate.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            });

            vibrate.start();
        }

        private void animateHoverStart() {
            pressOverlay.setVisibility(View.VISIBLE);
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(pressOverlay, "alpha", 0f, 0.3f);
            fadeIn.setDuration(150);
            fadeIn.start();

            ObjectAnimator scaleUp = ObjectAnimator.ofFloat(emojiText, "scaleX", 1f, 1.1f);
            ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(emojiText, "scaleY", 1f, 1.1f);

            AnimatorSet hoverSet = new AnimatorSet();
            hoverSet.playTogether(scaleUp, scaleUpY);
            hoverSet.setDuration(150);
            hoverSet.start();
        }

        private void animateHoverEnd() {
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(pressOverlay, "alpha", 0.3f, 0f);
            fadeOut.setDuration(150);
            fadeOut.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    pressOverlay.setVisibility(View.GONE);
                }
            });
            fadeOut.start();

            ObjectAnimator scaleDown = ObjectAnimator.ofFloat(emojiText, "scaleX", 1.1f, 1f);
            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(emojiText, "scaleY", 1.1f, 1f);

            AnimatorSet hoverSet = new AnimatorSet();
            hoverSet.playTogether(scaleDown, scaleDownY);
            hoverSet.setDuration(150);
            hoverSet.start();
        }
    }
}