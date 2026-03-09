package org.oss.greentify;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import org.oss.greentify.Backend.BaseActivity;
import org.oss.greentify.Backend.CloudinaryManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends BaseActivity {
    private ConstraintLayout popupLayout;
    private LottieAnimationView levelUpAnimation;
    private TextView badgeLevelText;
    private Button closeBadgePopup;
    private int badgeClickCount = 0;
    private static final String PREFS_NAME = "BadgePrefs";
    private static final String PREF_LAST_BADGE = "lastBadgeLevel";
    private String currentBadge = "";
    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText editUserName;
    private TextView badgeNameTextView, userEmailTextView, totalPointsTextView, greenCreditTextView;
    private ImageView profileImageView, badgeImageView;
    private Button btnUpdateName, btnChooseImage;
    private FirebaseUser user;
    private FirebaseFirestore firestore;
    private DocumentReference userRef;
    private TextView textUserName;

    private MediaPlayer mediaPlayer; // declare at top of the class

    private Uri imageUri;

    private final String[] treeLottieFiles = {
            "first_phase.json",   // Level 0
            "second_phase.json",  // Level 1
            "third_phase.json",   // Level 2
            "fourth_phase.json"   // Level 3
    };

    private final String[] treeLevelLabels = {
            "Sprout", "Small Tree", "Big Tree", "Old Tree"
    };
    private int paperLevel = 0, plasticLevel = 0, glassLevel = 0, metalLevel = 0, ewasteLevel = 0;
    private final double[] levelThresholds = {1, 15, 30, 45}; // ✅ Add this here

    private static final String PREF_PROFILE_WALKTHROUGH_SHOWN = "hasSeenProfileWalkthrough";
    private boolean shouldTriggerWalkthrough = false;


    @Override
    protected int getSelectedItemId() {
        return R.id.nav_profile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_profile);

        CloudinaryManager.init(this);
        textUserName = findViewById(R.id.textUserName);
        userEmailTextView = findViewById(R.id.textUserEmail);
        totalPointsTextView = findViewById(R.id.textTotalPoints);
        greenCreditTextView = findViewById(R.id.textGreenCredits);
        profileImageView = findViewById(R.id.profileImageView);
        badgeImageView = findViewById(R.id.badgeImageView);
        badgeNameTextView = findViewById(R.id.textBadgeName);

        popupLayout = findViewById(R.id.popupLayout);
        levelUpAnimation = findViewById(R.id.levelUpAnimation);
        badgeLevelText = findViewById(R.id.badgeLevelText);
        closeBadgePopup = findViewById(R.id.closeBadgePopup);
        TextView friendNotice = findViewById(R.id.friendProfileNotice);
        friendNotice.setVisibility(View.GONE);

        closeBadgePopup.setOnClickListener(v -> {
            // Stop Lottie animation
            if (levelUpAnimation.isAnimating()) {
                levelUpAnimation.cancelAnimation();
                levelUpAnimation.setVisibility(View.GONE);
            }

            // Stop sound
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }

            // Hide popup
            popupLayout.setVisibility(View.GONE);

            // ✅ Only now trigger walkthrough if it's user's first profile visit
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            if (!prefs.getBoolean(PREF_PROFILE_WALKTHROUGH_SHOWN, false)) {
                new android.os.Handler().postDelayed(() -> {
                    showStepProfileImage();
                    prefs.edit().putBoolean(PREF_PROFILE_WALKTHROUGH_SHOWN, true).apply();
                }, 500);
            }
        });

        badgeImageView.setAlpha(1f);
        badgeImageView.setOnClickListener(view -> {
            badgeClickCount++;

            if (badgeClickCount == 1) {
                Toast.makeText(ProfileActivity.this,
                        "🏅 This is your badge.\nTap again to learn how to upgrade!",
                        Toast.LENGTH_SHORT).show();

                new android.os.Handler().postDelayed(() -> badgeClickCount = 0, 2000);
            } else if (badgeClickCount == 2) {
                badgeClickCount = 0;
                showBadgeDetailsPopup();
            }
        });

        btnUpdateName = findViewById(R.id.btnUpdateName);
        btnChooseImage = findViewById(R.id.btnChooseImage);

        user = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        if (user != null) {
            String uid = user.getUid();
            userEmailTextView.setText(user.getEmail());
            userRef = firestore.collection("users").document(uid);

            loadUserData();

            btnUpdateName.setOnClickListener(v -> {
                EditText input = new EditText(ProfileActivity.this);
                input.setHint("Enter new username");

                new AlertDialog.Builder(ProfileActivity.this)
                        .setTitle("Update Username")
                        .setView(input)
                        .setPositiveButton("Update", (dialog, which) -> {
                            String newName = input.getText().toString().trim();
                            if (!newName.isEmpty()) {
                                textUserName.setText(newName); // UI update
                                userRef.update("username", newName) // Firestore update
                                        .addOnSuccessListener(aVoid ->
                                                Toast.makeText(ProfileActivity.this, "Username updated", Toast.LENGTH_SHORT).show())
                                        .addOnFailureListener(e ->
                                                Toast.makeText(ProfileActivity.this, "Failed to update username", Toast.LENGTH_SHORT).show());
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });

            btnChooseImage.setOnClickListener(v -> openFileChooser());
        }

        // Replace BarChart with tree animation system
        loadRecyclingChartData(); // <-- This loads Lottie-based tree progress
    }


    private void loadUserData() {
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String username = document.getString("username");
                    String profileImageUrl = document.getString("profileImageUrl");
                    Long points = document.getLong("points");
                    Long greenCredits = document.getLong("greenCredits");

                    textUserName.setText(username != null ? username : "No Username");
                    totalPointsTextView.setText(points != null ? "Total Points: " + points : "Total Points: 0");
                    greenCreditTextView.setText(greenCredits != null ? "Credits: " + greenCredits : "Credits: 0");

                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(this).load(profileImageUrl).into(profileImageView);
                    }

                    updateBadge(points != null ? points.intValue() : 0);

                    // <-- Load the chart data here
                    loadRecyclingChartData();

                } else {
                    Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void playBadgeUpgradeAnimation(String badgeName) {
        // Show popup and set text
        popupLayout.setVisibility(View.VISIBLE);
        badgeLevelText.setText("You’ve unlocked: " + badgeName + " Badge!");

        // Show and loop the Lottie animation
        levelUpAnimation.setVisibility(View.VISIBLE);
        levelUpAnimation.setRepeatCount(LottieDrawable.INFINITE);
        levelUpAnimation.playAnimation();

        // Play the sound fully
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        mediaPlayer = MediaPlayer.create(this, R.raw.level_up_sound);
        mediaPlayer.start();
    }


    private void updateBadge(int points) {
        String newBadge;
        int badgeRes;

        if (points >= 9999) {
            newBadge = "Platinum Badge";
            badgeRes = R.drawable.platinum_badge;
        } else if (points >= 1500) {
            newBadge = "Gold Badge";
            badgeRes = R.drawable.gold_badge;
        } else if (points >= 1000) {
            newBadge = "Silver Badge";
            badgeRes = R.drawable.silver_badge;
        } else if (points >= 500) {
            newBadge = "Bronze Badge";
            badgeRes = R.drawable.bronze_badge;
        } else {
            newBadge = "Beginner Badge";
            badgeRes = R.drawable.default_badge;
        }

        // Set badge image and label
        badgeImageView.setImageResource(badgeRes);
        badgeNameTextView.setText(newBadge);
        currentBadge = newBadge;

        // Load saved badge from SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String lastBadge = prefs.getString(PREF_LAST_BADGE, ""); // default empty

        if (!newBadge.equals(lastBadge)) {
            // Show popup once only after upgrade
            playBadgeUpgradeAnimation(newBadge);
            prefs.edit().putString(PREF_LAST_BADGE, newBadge).apply();
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImageView.setImageBitmap(bitmap);
                uploadImageToCloudinary(imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImageToCloudinary(Uri imageUri) {
        MediaManager.get().upload(imageUri)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Toast.makeText(ProfileActivity.this, "Uploading...", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String url = resultData.get("secure_url").toString();

                        userRef.update("profileImageUrl", url)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(ProfileActivity.this, "Profile picture updated", Toast.LENGTH_SHORT).show();

                                    // ✅ THIS LINE refreshes the profile image and data
                                    loadUserData();
                                });
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(ProfileActivity.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                    }
                }).dispatch();
    }

    private int getCurrentUserPoints() {
        String pointsText = totalPointsTextView.getText().toString().replaceAll("[^0-9]", "");
        try {
            return Integer.parseInt(pointsText);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void showBadgeDetailsPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_badge_details, null);

        ImageView badgeImage = view.findViewById(R.id.dialogCurrentBadgeImage);
        TextView badgeTitle = view.findViewById(R.id.dialogCurrentBadgeTitle);
        TextView badgePoints = view.findViewById(R.id.dialogCurrentBadgePoints);

        int points = getCurrentUserPoints();
        badgePoints.setText(points + " Points");

        if (points >= 9999) {
            badgeImage.setImageResource(R.drawable.platinum_badge);
            badgeTitle.setText("Platinum Badge");
        } else if (points >= 1500) {
            badgeImage.setImageResource(R.drawable.gold_badge);
            badgeTitle.setText("Gold Badge");
        } else if (points >= 1000) {
            badgeImage.setImageResource(R.drawable.silver_badge);
            badgeTitle.setText("Silver Badge");
        } else if (points >= 500) {
            badgeImage.setImageResource(R.drawable.bronze_badge);
            badgeTitle.setText("Bronze Badge");
        } else {
            badgeImage.setImageResource(R.drawable.default_badge);
            badgeTitle.setText("Beginner Badge");
        }


        builder.setView(view);
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    private String normalizeMaterialKey(String material) {
        if (material == null) return null;
        material = material.toLowerCase().replace("-", "");

        switch (material) {
            case "paper":
                return "Paper";
            case "plastic":
                return "Plastic";
            case "glass":
                return "Glass";
            case "metal":
                return "Metal";
            case "ewaste":
                return "E-Waste";
            default:
                return material; // fallback
        }
    }

    private final int[] materialColors = {
            Color.parseColor("#4CAF50"),  // Paper - green
            Color.parseColor("#FFC107"),  // Plastic - amber
            Color.parseColor("#2196F3"),  // Glass - blue
            Color.parseColor("#9C27B0"),  // Metal - purple
            Color.parseColor("#FF5722")   // E-Waste - deep orange
    };


    private final String[] materials = {"Paper", "Plastic", "Glass", "Metal", "E-Waste"};


    private void setLottieAnimFromInclude(int includeId, int animRes) {
        View includeBlock = findViewById(includeId);
        LottieAnimationView treeAnim = includeBlock.findViewById(R.id.treeAnimation);
        treeAnim.setAnimation(animRes);
        treeAnim.playAnimation();
    }


    private void updateTreeViews(Map<String, Float> materialTotals) {
        for (String material : materials) {
            try {
                float weight = materialTotals.getOrDefault(material, 0f);
                String statusText = String.format("You’ve recycled %.1f kg", weight);
                int animationRes;
                int levelIndex = calculateTreeLevel(weight);

                if (weight >= 45) {
                    animationRes = R.raw.fifth_phase;
                } else if (weight >= 30) {
                    animationRes = R.raw.forth_phase;
                } else if (weight >= 15) {
                    animationRes = R.raw.third_phase;
                } else if (weight >= 1) {
                    animationRes = R.raw.second_phase;
                } else {
                    animationRes = R.raw.first_phase;
                }

                View block = null;
                switch (material.toLowerCase()) {
                    case "paper":
                        block = findViewById(R.id.paperInclude);
                        break;
                    case "plastic":
                        block = findViewById(R.id.plasticInclude);
                        break;
                    case "glass":
                        block = findViewById(R.id.glassInclude);
                        break;
                    case "metal":
                        block = findViewById(R.id.metalInclude);
                        break;
                    case "ewaste":
                    case "e-waste":
                        block = findViewById(R.id.ewasteInclude);
                        break;
                }

                if (block != null) {
                    setLottieAnimFromInclude(block.getId(), animationRes);
                    ((TextView) block.findViewById(R.id.treeStatus)).setText(statusText);
                    ((TextView) block.findViewById(R.id.treeMaterialLabel)).setText(normalizeMaterialKey(material));

                    // ✅ Pass weight into setupMaterialClick
                    setupMaterialClick(block, normalizeMaterialKey(material), levelIndex, weight);
                } else {
                    Log.e("TreeView", "Block view not found for: " + material);
                }

            } catch (Exception e) {
                Log.e("TreeView", "Error setting tree view for material: " + material, e);
            }
        }
    }


    private void loadRecyclingChartData() {
        firestore.collection("recycle_submissions")
                .whereEqualTo("userId", user.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Float> materialTotals = new HashMap<>();

                    for (String material : materials) {
                        materialTotals.put(material, 0f);
                    }

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String material = doc.getString("materialType");
                        Double weight = doc.getDouble("weight");
                        if (material == null || weight == null) continue;

                        material = normalizeMaterialKey(material);
                        float currentTotal = materialTotals.getOrDefault(material, 0f);
                        materialTotals.put(material, currentTotal + weight.floatValue());
                    }

                    // update trees instead of chart
                    updateTreeViews(materialTotals);
                });
    }

    private int calculateTreeLevel(double weight) {
        if (weight >= 45) return 4; // fifth_phase
        if (weight >= 30) return 3; // forth_phase
        if (weight >= 15) return 2; // third_phase
        if (weight >= 1) return 1; // second_phase
        return 0; // first_phase
    }

    private void setupMaterialClick(View materialView, String materialName, int levelIndex, float materialWeight) {
        try {
            if (materialView == null) {
                Log.e("MaterialClick", "View is null for: " + materialName);
                return;
            }

            materialView.setOnClickListener(v -> {
                Toast.makeText(this, "Tapped " + materialName + " (Level " + levelIndex + ")", Toast.LENGTH_SHORT).show();

                runOnUiThread(() -> {
                    showProgressionPopup(materialName, levelIndex, materialWeight);
                });
            });
        } catch (Exception e) {
            Log.e("MaterialClick", "Error setting click listener for " + materialName, e);
        }
    }

    private void showProgressionPopup(String material, int levelIndex, float materialWeight) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_progression_popup, null);
        builder.setView(view);

        LottieAnimationView mainAnim = view.findViewById(R.id.treePopupMainAnim);
        TextView levelText = view.findViewById(R.id.treePopupLevelLabel);
        TextView totalText = view.findViewById(R.id.treePopupTotalRecycled);
        ProgressBar progressBar = view.findViewById(R.id.treePopupProgressBar);
        TextView remainingText = view.findViewById(R.id.treePopupRemainingText);
        LinearLayout previewStrip = view.findViewById(R.id.treePopupPreviewStrip);

        String[] levelNames = {"Seed", "Sprout", "Small Plant", "Flowers", "Blooming Flowers"};
        String[] unlockWeights = {"0.0kg", "1.0kg", "15.0kg", "30.0kg", "45.0kg"};
        int[] anims = {
                R.raw.first_phase,
                R.raw.second_phase,
                R.raw.third_phase,
                R.raw.forth_phase,
                R.raw.fifth_phase
        };

        double[] thresholds = {0.0, 1.0, 15.0, 30.0, 45.0, 100.0}; // Include base 0 and max cap

        // 🌱 Determine actual current level by weight
        int actualLevel = 0;
        for (int i = 0; i < thresholds.length - 1; i++) {
            if (materialWeight >= thresholds[i] && materialWeight < thresholds[i + 1]) {
                actualLevel = i;
                break;
            } else if (materialWeight >= thresholds[thresholds.length - 2]) {
                actualLevel = thresholds.length - 2;
                break;
            }
        }

        double currentThreshold = thresholds[actualLevel];
        double nextThreshold = thresholds[actualLevel + 1];
        int progress = (int) (((materialWeight - currentThreshold) / (nextThreshold - currentThreshold)) * 100);
        progress = Math.max(0, Math.min(progress, 100));
        progressBar.setProgress(progress);

        if (materialWeight >= nextThreshold) {
            remainingText.setText("✅ Max level reached!");
        } else {
            double remaining = nextThreshold - materialWeight;
            remainingText.setText(String.format("%.1f kg to next stage", remaining));
        }

        // 🧾 UI Setup
        mainAnim.setAnimation(anims[actualLevel]);
        mainAnim.playAnimation();
        levelText.setText(material + ": " + levelNames[actualLevel]);
        totalText.setText("You’ve recycled " + String.format("%.1f", materialWeight) + " kg");

        // 🌱 Populate preview stages
        previewStrip.removeAllViews();
        for (int i = 0; i < anims.length; i++) {
            LinearLayout stageContainer = new LinearLayout(this);
            stageContainer.setOrientation(LinearLayout.VERTICAL);
            stageContainer.setGravity(Gravity.CENTER);
            stageContainer.setPadding(12, 0, 12, 0);
            stageContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            TextView kgText = new TextView(this);
            kgText.setText(unlockWeights[i]);
            kgText.setTextSize(10f);
            kgText.setTextColor(Color.BLACK);
            kgText.setGravity(Gravity.CENTER);

            FrameLayout circleHolder = new FrameLayout(this);
            FrameLayout.LayoutParams circleParams = new FrameLayout.LayoutParams(80, 80);
            circleHolder.setLayoutParams(circleParams);
            circleHolder.setBackgroundResource(R.drawable.bg_preview_circle);

            LottieAnimationView preview = new LottieAnimationView(this);
            FrameLayout.LayoutParams animParams = new FrameLayout.LayoutParams(70, 70, Gravity.CENTER);
            preview.setLayoutParams(animParams);
            preview.setAnimation(anims[i]);
            preview.setRepeatCount(0);
            preview.setAlpha(i <= actualLevel ? 1f : 0.3f);

            if (i > actualLevel) {
                ImageView lockIcon = new ImageView(this);
                lockIcon.setImageResource(R.drawable.ic_lock);
                FrameLayout.LayoutParams lockParams = new FrameLayout.LayoutParams(24, 24, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
                lockParams.bottomMargin = 4;
                lockIcon.setLayoutParams(lockParams);
                circleHolder.addView(lockIcon);

                preview.setOnClickListener(v -> {
                    new AlertDialog.Builder(this)
                            .setTitle("🚫 Locked Progress")
                            .setMessage("🌱 Level up to unlock the next progress stage!\nKeep recycling to grow your tree! 🌳")
                            .setPositiveButton("Got it!", null)
                            .show();
                });
            } else {
                int finalI = i;
                preview.setOnClickListener(v -> {
                    mainAnim.setAnimation(anims[finalI]);
                    mainAnim.playAnimation();
                    levelText.setText(material + ": " + levelNames[finalI]);
                });
            }

            circleHolder.addView(preview);
            stageContainer.addView(kgText);
            stageContainer.addView(circleHolder);
            previewStrip.addView(stageContainer);
        }

        builder.setPositiveButton("Close", null);
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean hasSeen = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getBoolean(PREF_PROFILE_WALKTHROUGH_SHOWN, false);

        // ✅ Don't trigger walkthrough here — just prepare for later
        // Let the badge popup finish first before launching walkthrough
        if (!hasSeen && popupLayout.getVisibility() != View.VISIBLE) {
            // We don’t call showStepProfileImage() here anymore
            shouldTriggerWalkthrough = true;
        }
    }

    private void showStepProfileImage() {
        TapTargetView.showFor(this,
                TapTarget.forView(profileImageView, "Profile Picture", "Tap here to change your profile photo.")
                        .outerCircleColorInt(Color.TRANSPARENT)
                        .targetCircleColor(android.R.color.white)
                        .titleTextColor(android.R.color.white)
                        .descriptionTextColor(android.R.color.white)
                        .dimColor(android.R.color.black)
                        .tintTarget(true)
                        .transparentTarget(true)
                        .drawShadow(true)
                        .cancelable(false),
                new TapTargetView.Listener() {
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);
                        showStepBadge();
                    }
                });
    }

    private void showStepBadge() {
        TapTargetView.showFor(this,
                TapTarget.forView(badgeImageView, "Your Badge", "This badge represents your recycling level. Tap to view details.")
                        .outerCircleColorInt(Color.TRANSPARENT)
                        .targetCircleColor(android.R.color.white)
                        .titleTextColor(android.R.color.white)
                        .descriptionTextColor(android.R.color.white)
                        .dimColor(android.R.color.black)
                        .tintTarget(true)
                        .transparentTarget(true)
                        .drawShadow(true),
                new TapTargetView.Listener() {
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);
                        showStepPoints();
                    }
                });
    }

    private void showStepPoints() {
        TapTargetView.showFor(this,
                TapTarget.forView(totalPointsTextView, "Total Points", "These points are earned from recycling activities.")
                        .outerCircleColorInt(Color.TRANSPARENT)
                        .targetCircleColor(android.R.color.white)
                        .titleTextColor(android.R.color.white)
                        .descriptionTextColor(android.R.color.white)
                        .dimColor(android.R.color.black)
                        .tintTarget(true)
                        .transparentTarget(true)
                        .drawShadow(true),
                new TapTargetView.Listener() {
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);
                        showStepGreenWallet();
                    }
                });
    }

    private void showStepGreenWallet() {
        TapTargetView.showFor(this,
                TapTarget.forView(greenCreditTextView, "Green Wallet", "You can redeem rewards using these credits.")
                        .outerCircleColorInt(Color.TRANSPARENT)
                        .targetCircleColor(android.R.color.white)
                        .titleTextColor(android.R.color.white)
                        .descriptionTextColor(android.R.color.white)
                        .dimColor(android.R.color.black)
                        .tintTarget(true)
                        .transparentTarget(true)
                        .drawShadow(true),
                new TapTargetView.Listener() {
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);
                        showStepUpdateName();
                    }
                });
    }

    private void showStepUpdateName() {
        TapTargetView.showFor(this,
                TapTarget.forView(btnUpdateName, "Update Name", "Click here to change your username.")
                        .outerCircleColorInt(Color.TRANSPARENT)
                        .targetCircleColor(android.R.color.white)
                        .titleTextColor(android.R.color.white)
                        .descriptionTextColor(android.R.color.white)
                        .dimColor(android.R.color.black)
                        .tintTarget(true)
                        .transparentTarget(true)
                        .drawShadow(true),
                new TapTargetView.Listener() {
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);
                        showStepChangeImage();
                    }
                });
    }

    private void showStepChangeImage() {
        TapTargetView.showFor(this,
                TapTarget.forView(btnChooseImage, "Change Picture", "Select a new image for your profile.")
                        .outerCircleColorInt(Color.TRANSPARENT)
                        .targetCircleColor(android.R.color.white)
                        .titleTextColor(android.R.color.white)
                        .descriptionTextColor(android.R.color.white)
                        .dimColor(android.R.color.black)
                        .tintTarget(true)
                        .transparentTarget(true)
                        .drawShadow(true),
                new TapTargetView.Listener() {
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);
                        showStepRecyclingProgress();
                    }
                });
    }

    private void showStepRecyclingProgress() {
        View progressBlock = findViewById(R.id.paperInclude);
        if (progressBlock != null) {
            TapTargetView.showFor(this,
                    TapTarget.forView(progressBlock, "Recycling Progress", "View your progress for each material.\nTap any tree to see growth stages.")
                            .outerCircleColorInt(Color.TRANSPARENT)
                            .targetCircleColor(android.R.color.white)
                            .titleTextColor(android.R.color.white)
                            .descriptionTextColor(android.R.color.white)
                            .dimColor(android.R.color.black)
                            .tintTarget(true)
                            .transparentTarget(true)
                            .drawShadow(true),
                    new TapTargetView.Listener() {
                        @Override
                        public void onTargetClick(TapTargetView view) {
                            super.onTargetClick(view);
                            Toast.makeText(ProfileActivity.this, "🎉 Walkthrough completed!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
