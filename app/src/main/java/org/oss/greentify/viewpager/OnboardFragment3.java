package org.oss.greentify.viewpager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.oss.greentify.R;
import org.oss.greentify.RegisterActivity;

public class OnboardFragment3 extends Fragment {
    public OnboardFragment3() {
        super(R.layout.fragment_onboard3);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CheckBox dontShowAgain = view.findViewById(R.id.checkboxDontShowAgain);
        Button registerNow = view.findViewById(R.id.btnRegisterNow);

        if (registerNow != null) {
            registerNow.setOnClickListener(v -> {
                if (dontShowAgain != null && dontShowAgain.isChecked()) {
                    SharedPreferences prefs = requireContext().getSharedPreferences("onboarding", Context.MODE_PRIVATE);
                    prefs.edit().putBoolean("skipOnboarding", true).apply();

                    // Optional: confirm with toast
                    Toast.makeText(requireContext(), "We'll skip this next time!", Toast.LENGTH_SHORT).show();
                }

                startActivity(new Intent(requireContext(), RegisterActivity.class));
                requireActivity().finish();
            });
        }
    }
}
