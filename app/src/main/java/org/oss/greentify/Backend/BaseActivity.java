package org.oss.greentify.Backend;

import android.content.Intent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.oss.greentify.AddFriend.FriendListActivity;
import org.oss.greentify.GlobalLeaderboard.GlobalLeaderboardActivity;
import org.oss.greentify.MainActivity;
import org.oss.greentify.ProfileActivity;
import org.oss.greentify.R;
import org.oss.greentify.Rewards.RewardsActivity;

public abstract class BaseActivity extends AppCompatActivity {

    // Abstract method to indicate which nav item is active
    protected abstract int getSelectedItemId();

    // ✅ New method: override in child class if bottom nav should be hidden
    protected boolean shouldShowBottomNav() {
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupBottomNavigation(getSelectedItemId());
    }

    protected void setupBottomNavigation(int selectedItemId) {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        if (bottomNavigationView == null) return;

        // ✅ Hide bottom nav if child activity says so
        if (!shouldShowBottomNav()) {
            bottomNavigationView.setVisibility(View.GONE);
            return;
        }

        bottomNavigationView.setVisibility(View.VISIBLE);
        bottomNavigationView.setSelectedItemId(selectedItemId);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                if (!(this instanceof MainActivity)) {
                    navigateToActivity(MainActivity.class);
                }
                return true;
            } else if (itemId == R.id.nav_friend) {
                if (!(this instanceof FriendListActivity)) {
                    navigateToActivity(FriendListActivity.class);
                }
                return true;
            } else if (itemId == R.id.nav_rewards) {
                if (!(this instanceof RewardsActivity)) {
                    navigateToActivity(RewardsActivity.class);
                }
                return true;
            } else if (itemId == R.id.nav_leaderboard) {
                if (!(this instanceof GlobalLeaderboardActivity)) {
                    navigateToActivity(GlobalLeaderboardActivity.class);
                }
                return true;
            } else if (itemId == R.id.nav_profile) {
                if (!(this instanceof ProfileActivity)) {
                    navigateToActivity(ProfileActivity.class);
                }
                return true;
            }
            return false;
        });
    }

    private void navigateToActivity(Class<? extends AppCompatActivity> activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
    }
}
