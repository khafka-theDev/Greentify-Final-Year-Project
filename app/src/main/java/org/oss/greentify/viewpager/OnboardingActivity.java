package org.oss.greentify.viewpager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.oss.greentify.MainActivity;
import org.oss.greentify.R;
import org.oss.greentify.RegisterActivity;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private OnboardingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("onboarding", MODE_PRIVATE);
        boolean skipOnboarding = prefs.getBoolean("skipOnboarding", false);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // 🔁 Redirect logic:
        if (skipOnboarding && currentUser != null) {
            // User has seen onboarding AND is logged in → Go to MainActivity
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        } else if (skipOnboarding) {
            // User has seen onboarding but not logged in → Go to Login/Register
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
            return;
        }

        // Else → show onboarding
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPagerOnboarding);
        adapter = new OnboardingAdapter(this);
        viewPager.setAdapter(adapter);
    }
}
