package org.oss.greentify;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.oss.greentify.AddFriend.FriendListActivity;
import org.oss.greentify.Backend.BaseActivity;
import org.oss.greentify.Backend.CloudinaryManager;
import org.oss.greentify.GlobalLeaderboard.GlobalLeaderboardActivity;
import org.oss.greentify.RecycleStart.LocationSelectionActivity;
import org.oss.greentify.Rewards.Reward;
import org.oss.greentify.Home.CarouselAdapter;
import org.oss.greentify.Rewards.RewardsActivity;

import java.util.ArrayList;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final String PREFS_NAME = "prefs";
    private static final String KEY_HAS_NEW_NOTIFICATION = "hasNewNotification";

    private ArrayList<Reward> rewardList;
    private CarouselAdapter carouselAdapter;

    private TextView logout, welcomeUserText, userCreditsText;
    private ImageButton buttonPaper, buttonPlastic, buttonEWaste, buttonGlass, buttonMetal;
    private ImageView profileImageView, hamburgerMenu, notificationButton;
    private DrawerLayout drawerLayout;
    private ViewPager2 rewardCarousel;
    private ImageView drawerProfileImage;
    private TextView drawerUsername, drawerPoints, drawerWallet;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;

    private boolean hasNewNotification = false;

    private final Handler carouselHandler = new Handler(Looper.getMainLooper());
    private Runnable carouselRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rewardList = new ArrayList<>();

        setupBottomNavigation(R.id.nav_home);
        CloudinaryManager.init(this);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        requestLocationPermission();
        hasNewNotification = loadNotificationState();

        initializeViews();
        updateNotificationIcon();
        setupDrawerNavigation();
        setupRecycleButtons();
        loadRewards();
        loadUserData();
        checkForNewNotifications();

        Button buttonProfile = findViewById(R.id.buttonProfile);
        Button buttonLeaderboard = findViewById(R.id.buttonLeaderboard);
        Button buttonReward = findViewById(R.id.buttonReward);
        Button buttonFriendList = findViewById(R.id.buttonFriendList);

        buttonProfile.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        });

        buttonLeaderboard.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(MainActivity.this, GlobalLeaderboardActivity.class));
        });

        buttonReward.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(MainActivity.this, RewardsActivity.class));
        });

        buttonFriendList.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(MainActivity.this, FriendListActivity.class));
        });

    }

    private void initializeViews() {
        logout = findViewById(R.id.logout);
        welcomeUserText = findViewById(R.id.welcomeUserText);
        userCreditsText = findViewById(R.id.userCreditsText);
        profileImageView = findViewById(R.id.profileImageView);
        buttonPaper = findViewById(R.id.buttonPaper);
        buttonPlastic = findViewById(R.id.buttonPlastic);
        buttonEWaste = findViewById(R.id.buttonEWaste);
        buttonGlass = findViewById(R.id.buttonGlass);
        buttonMetal = findViewById(R.id.buttonMetal);
        hamburgerMenu = findViewById(R.id.hamburgerMenu);
        notificationButton = findViewById(R.id.notificationButton);
        drawerLayout = findViewById(R.id.drawerLayout);
        rewardCarousel = findViewById(R.id.rewardCarousel);
        drawerProfileImage = findViewById(R.id.drawerProfileImage);
        drawerUsername = findViewById(R.id.drawerUsername);
        drawerPoints = findViewById(R.id.drawerPoints);
        drawerWallet = findViewById(R.id.drawerWallet);

        if (logout != null) {
            logout.setOnClickListener(view -> performLogout());
        }

        if (hamburgerMenu != null) {
            hamburgerMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        }

        if (notificationButton != null) {
            notificationButton.setOnClickListener(v -> {
                hasNewNotification = false;
                saveNotificationState(false);
                updateNotificationIcon();
                startActivity(new Intent(this, org.oss.greentify.Home.HistoryActivity.class));
            });
        }
    }

    private void performLogout() {
        mAuth.signOut();
        Toast.makeText(this, "Successfully logged out", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupDrawerNavigation() {
        if (drawerLayout != null) {
            findViewById(R.id.buttonAbout).setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(this, org.oss.greentify.Home.AboutActivity.class));
            });

            findViewById(R.id.buttonLogout).setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                performLogout();
            });
        }
    }

    private void setupRecycleButtons() {
        if (buttonPaper != null) buttonPaper.setOnClickListener(v -> navigateToLocationSelection("paper"));
        if (buttonPlastic != null) buttonPlastic.setOnClickListener(v -> navigateToLocationSelection("plastic"));
        if (buttonEWaste != null) buttonEWaste.setOnClickListener(v -> navigateToLocationSelection("e-waste"));
        if (buttonGlass != null) buttonGlass.setOnClickListener(v -> navigateToLocationSelection("glass"));
        if (buttonMetal != null) buttonMetal.setOnClickListener(v -> navigateToLocationSelection("metal"));
    }

    private void navigateToLocationSelection(String materialType) {
        Intent intent = new Intent(this, LocationSelectionActivity.class);
        intent.putExtra("type", materialType);
        startActivity(intent);
    }

    private void loadRewards() {
        if (carouselAdapter == null) {
            carouselAdapter = new CarouselAdapter(this, rewardList);
            rewardCarousel.setAdapter(carouselAdapter);
        }

        db.collection("rewards")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(3)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        new Thread(() -> {
                            ArrayList<Reward> tempList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Reward reward = document.toObject(Reward.class);
                                if (reward.getImageUrl() != null && !reward.getImageUrl().isEmpty()) {
                                    reward.setId(document.getId());
                                    tempList.add(reward);
                                }
                            }
                            runOnUiThread(() -> {
                                rewardList.clear();
                                rewardList.addAll(tempList);
                                carouselAdapter.notifyDataSetChanged();
                                startAutoSlide(); // ✅ Start auto-slide
                            });
                        }).start();
                    } else {
                        Log.e(TAG, "Error getting rewards", task.getException());
                    }
                });
    }

    private void startAutoSlide() {
        if (carouselRunnable != null) {
            carouselHandler.removeCallbacks(carouselRunnable);
        }

        carouselRunnable = new Runnable() {
            @Override
            public void run() {
                int itemCount = carouselAdapter.getItemCount();
                if (itemCount > 1) {
                    int nextItem = (rewardCarousel.getCurrentItem() + 1) % itemCount;
                    rewardCarousel.setCurrentItem(nextItem, true);
                }
                carouselHandler.postDelayed(this, 3000);
            }
        };

        carouselHandler.postDelayed(carouselRunnable, 3000);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!rewardList.isEmpty()) {
            startAutoSlide();
        }

        // ✅ Tutorial logic here instead of onCreate()
        SharedPreferences prefs = getSharedPreferences("GreentifyPrefs", MODE_PRIVATE);
        boolean isFirstTimeUser = prefs.getBoolean("isFirstTimeUser", false);
        boolean hasSeenMainTutorial = prefs.getBoolean("hasSeenMainTutorial", false);

        if (isFirstTimeUser && !hasSeenMainTutorial) {
            new Handler().postDelayed(() -> {
                startMainWalkthrough();
                prefs.edit().putBoolean("isFirstTimeUser", false).apply(); // ✅ clear flag
            }, 1000); // safer delay after layout/transition
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        carouselHandler.removeCallbacks(carouselRunnable);
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            Long credits = documentSnapshot.getLong("greenCredits");
                            Long points = documentSnapshot.getLong("points");
                            String profileImageUrl = documentSnapshot.getString("profileImageUrl");

                            if (username != null) welcomeUserText.setText("Welcome, " + username + "!");
                            if (credits != null) userCreditsText.setText("Green Wallet: " + credits);
                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                Glide.with(this)
                                        .load(profileImageUrl)
                                        .circleCrop()
                                        .placeholder(R.drawable.ic_profile)
                                        .into(profileImageView);
                            }

                            if (drawerUsername != null) drawerUsername.setText(username != null ? username : "\uD83D\uDC64 User");
                            if (drawerPoints != null) drawerPoints.setText("⭐ Points: " + (points != null ? points : 0));
                            if (drawerWallet != null) drawerWallet.setText("\uD83D\uDCB0 Wallet: " + (credits != null ? credits : 0));
                            if (profileImageUrl != null && drawerProfileImage != null) {
                                Glide.with(this)
                                        .load(profileImageUrl)
                                        .circleCrop()
                                        .placeholder(R.drawable.ic_profile)
                                        .into(drawerProfileImage);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching user data", e);
                        Toast.makeText(this, "Failed to load user info", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void checkForNewNotifications() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("notifications")
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    hasNewNotification = !querySnapshot.isEmpty();
                    saveNotificationState(hasNewNotification);
                    updateNotificationIcon();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to check notifications", e));
    }

    private void updateNotificationIcon() {
        if (notificationButton == null) return;
        int iconRes = hasNewNotification ? R.drawable.noti_satu : R.drawable.noti_kosong;
        notificationButton.setImageResource(iconRes);
    }

    private void saveNotificationState(boolean state) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_HAS_NEW_NOTIFICATION, state)
                .apply();
    }

    private boolean loadNotificationState() {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getBoolean(KEY_HAS_NEW_NOTIFICATION, false);
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastLocation();
        }
    }

    private void getLastLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        Log.d(TAG, "Location: " + location.getLatitude() + ", " + location.getLongitude());
                    } else {
                        Log.d(TAG, "Location is null.");
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
        } else {
            Toast.makeText(this, "Location permission is needed to find nearby recycling centers", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Log.d(TAG, "User is logged in: " + user.getEmail());
        }
    }

    @Override
    protected int getSelectedItemId() {
        return R.id.nav_home;
    }

    private void checkSubmissionUpdates() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        db.collection("recycle_submission_history")
                .whereEqualTo("userId", user.getUid())
                .whereEqualTo("notified", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String status = doc.getString("status");
                        if ("Approved".equals(status) || "Rejected".equals(status)) {
                            showLocalNotification(
                                    "Submission " + status,
                                    "Your recycling submission has been " + status.toLowerCase() + "."
                            );

                            // Mark as notified
                            doc.getReference().update("notified", true);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("SubmissionCheck", "Failed to fetch submission updates", e));
    }

    private void showLocalNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "submission_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Submission Updates",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
    private void startMainWalkthrough() {
        TapTargetView.showFor(this,
                TapTarget.forView(findViewById(R.id.buttonPaper), "Recycle Material", "Tap to recycle Paper, Plastic, Metal, and more")
                        .outerCircleColorInt(Color.TRANSPARENT)
                        .targetCircleColor(android.R.color.white)
                        .titleTextColor(android.R.color.white)
                        .descriptionTextColor(android.R.color.white)
                        .textColor(android.R.color.white)
                        .dimColor(android.R.color.black)
                        .drawShadow(true)
                        .cancelable(false)
                        .tintTarget(true)
                        .transparentTarget(true),
                new TapTargetView.Listener() {
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);
                        showWalletGuide();
                    }
                });
    }

    private void showWalletGuide() {
        TapTargetView.showFor(this,
                TapTarget.forView(findViewById(R.id.userCreditsText), "Green Wallet", "This shows your Green Credits balance.")
                        .outerCircleColorInt(Color.TRANSPARENT)
                        .targetCircleColor(android.R.color.white)
                        .titleTextColor(android.R.color.white)
                        .descriptionTextColor(android.R.color.white)
                        .dimColor(android.R.color.black)
                        .drawShadow(true)
                        .tintTarget(true)
                        .transparentTarget(true)
                        .cancelable(false),
                new TapTargetView.Listener() {
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);
                        showRewardsGuide();
                    }
                });
    }

    private void showRewardsGuide() {
        TapTargetView.showFor(this,
                TapTarget.forView(findViewById(R.id.rewardCarousel), "Rewards", "Swipe to explore redeemable rewards.")
                        .outerCircleColorInt(Color.TRANSPARENT)
                        .targetCircleColor(android.R.color.white)
                        .titleTextColor(android.R.color.white)
                        .descriptionTextColor(android.R.color.white)
                        .dimColor(android.R.color.black)
                        .drawShadow(true)
                        .tintTarget(true)
                        .transparentTarget(true),
                new TapTargetView.Listener() {
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);
                        showMenuGuide();
                    }
                });
    }

    private void showMenuGuide() {
        TapTargetView.showFor(this,
                TapTarget.forView(findViewById(R.id.hamburgerMenu), "Menu", "Access Profile, Leaderboard, and more.")
                        .outerCircleColorInt(Color.TRANSPARENT)
                        .targetCircleColor(android.R.color.white)
                        .titleTextColor(android.R.color.white)
                        .descriptionTextColor(android.R.color.white)
                        .dimColor(android.R.color.black)
                        .drawShadow(true)
                        .tintTarget(true)
                        .transparentTarget(true)
                        .cancelable(false),
                new TapTargetView.Listener() {
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);
                        showBottomNavGuide();
                    }
                });
    }

    private void showBottomNavGuide() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        if (bottomNav != null && bottomNav.getChildCount() > 0) {
            View navView = ((View) bottomNav.getChildAt(0)).findViewById(R.id.nav_rewards);
            if (navView != null) {
                TapTargetView.showFor(this,
                        TapTarget.forView(navView, "Bottom Navigation", "Use these tabs to access Rewards, Leaderboard, Profile, and more.")
                                .outerCircleColorInt(Color.TRANSPARENT)
                                .targetCircleColor(android.R.color.white)
                                .titleTextColor(android.R.color.white)
                                .descriptionTextColor(android.R.color.white)
                                .dimColor(android.R.color.black)
                                .drawShadow(true)
                                .tintTarget(true)
                                .transparentTarget(true)
                                .cancelable(false),
                        new TapTargetView.Listener() {
                            @Override
                            public void onTargetClick(TapTargetView view) {
                                super.onTargetClick(view);
                                markWalkthroughComplete();
                            }
                        });
            }
        }
    }


    private void markWalkthroughComplete() {
        SharedPreferences prefs = getSharedPreferences("GreentifyPrefs", MODE_PRIVATE);
        prefs.edit().putBoolean("hasSeenMainTutorial", true).apply();
    }
}
