package com.lumoo.ViewHolder;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.lumoo.FriendsSharesFragment;
import com.lumoo.LiveStreamsFragment;
import com.lumoo.MatchFragment;
import com.lumoo.SharesFragment;

import io.reactivex.rxjava3.annotations.NonNull;

public class TabPagerMainAdapter extends FragmentStateAdapter {

    public TabPagerMainAdapter(FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new FriendsSharesFragment();
            case 1:
                return new LiveStreamsFragment();
            default:
                throw new IllegalStateException("Mevcut Olmayan Pozisyon " + position);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}