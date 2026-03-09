package org.oss.greentify.viewpager;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import org.oss.greentify.R;

public class OnboardFragment1 extends Fragment {

    public OnboardFragment1() {
        super(R.layout.fragment_onboard1); // links to your XML layout
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnGetStarted = view.findViewById(R.id.btnGetStarted);
        btnGetStarted.setOnClickListener(v -> {
            ViewPager2 viewPager = requireActivity().findViewById(R.id.viewPagerOnboarding);
            if (viewPager != null) {
                viewPager.setCurrentItem(1, true); // Slide to OnboardFragment2
            }
        });
    }
}
