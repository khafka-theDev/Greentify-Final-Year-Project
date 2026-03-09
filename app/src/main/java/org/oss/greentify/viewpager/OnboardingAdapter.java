package org.oss.greentify.viewpager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class OnboardingAdapter extends FragmentStateAdapter {
    public OnboardingAdapter(FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new OnboardFragment1();
            case 1: return new OnboardFragment2();
            default: return new OnboardFragment3();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
