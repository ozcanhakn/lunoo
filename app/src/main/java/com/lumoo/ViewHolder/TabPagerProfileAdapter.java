package com.lumoo.ViewHolder;


import android.provider.Settings;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.lumoo.MatchFragment;
import com.lumoo.PersonalInfoFragment;
import com.lumoo.PostFragment;
import com.lumoo.SettingsFragment;
import com.lumoo.SharesFragment;

import io.reactivex.rxjava3.annotations.NonNull;

public class TabPagerProfileAdapter extends FragmentStateAdapter {

    public TabPagerProfileAdapter(FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new PostFragment();
            case 1:
                return new PersonalInfoFragment();
            case 2:
                return new SettingsFragment();
            default:
                throw new IllegalStateException("Mevcut Olmayan Pozisyon " + position);
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}