package com.lumoo.ViewHolder;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.lumoo.MatchFragment;
import com.lumoo.ProfileFragment;
import com.lumoo.SharesFragment;

import io.reactivex.rxjava3.annotations.NonNull;

public class TabPagerAdapter extends FragmentStateAdapter {
    private final String uid;
    private final String url;

    public TabPagerAdapter(@NonNull FragmentActivity fragmentActivity, String uid, String url) {
        super(fragmentActivity);
        this.uid = uid;
        this.url = url;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) { // Paylaşımlar Fragment'i
            SharesFragment fragment = new SharesFragment();
            Bundle bundle = new Bundle();
            bundle.putString("uid", uid);
            bundle.putString("url", url);
            fragment.setArguments(bundle);
            return fragment;
        } else {
            return new ProfileFragment(); // Profil Fragment'i (gerekirse ona da veri gönderilebilir)
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Toplam tab sayısı
    }
}
