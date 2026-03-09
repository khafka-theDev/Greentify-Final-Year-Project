package org.oss.greentify.AddFriend;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.*;

import org.oss.greentify.Backend.BaseActivity;
import org.oss.greentify.R;

import java.util.HashMap;
import java.util.Map;

public class FriendProfileActivity extends BaseActivity {

    private TextView textUserName, userEmailTextView, totalPointsTextView, greenCreditTextView;
    private TextView badgeNameText, visitingNoticeText;
    private ImageView profileImageView, badgeImageView;

    private FirebaseFirestore firestore;
    private String friendUid;
    private String friendName = "Friend";

    private final String[] materials = {"paper", "plastic", "glass", "metal", "ewaste"};
    private final Map<String, View> includeViews = new HashMap<>();

    @Override
    protected int getSelectedItemId() {
        return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile); // Reuse layout

        friendUid = getIntent().getStringExtra("friendUid");
        if (friendUid == null) {
            Toast.makeText(this, "Friend UID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        firestore = FirebaseFirestore.getInstance();

        // Initialize Views
        textUserName = findViewById(R.id.textUserName);
        userEmailTextView = findViewById(R.id.textUserEmail);
        totalPointsTextView = findViewById(R.id.textTotalPoints);
        greenCreditTextView = findViewById(R.id.textGreenCredits);
        profileImageView = findViewById(R.id.profileImageView);
        badgeImageView = findViewById(R.id.badgeImageView);
        badgeNameText = findViewById(R.id.textBadgeName);
        visitingNoticeText = findViewById(R.id.friendProfileNotice);

        // Disable editing by hiding buttons
        findViewById(R.id.btnUpdateName).setVisibility(View.GONE);
        findViewById(R.id.btnChooseImage).setVisibility(View.GONE);

        visitingNoticeText.setVisibility(View.VISIBLE);

        // Include views for material stats
        includeViews.put("paper", findViewById(R.id.paperInclude));
        includeViews.put("plastic", findViewById(R.id.plasticInclude));
        includeViews.put("glass", findViewById(R.id.glassInclude));
        includeViews.put("metal", findViewById(R.id.metalInclude));
        includeViews.put("ewaste", findViewById(R.id.ewasteInclude));

        loadFriendData();
        loadRecyclingData();
    }

    private void loadFriendData() {
        firestore.collection("users").document(friendUid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("username");
                        String email = doc.getString("email");
                        String profileUrl = doc.getString("profileImageUrl");
                        Long points = doc.getLong("points");
                        Long credits = doc.getLong("greenCredits");

                        if (name != null) {
                            friendName = name;
                            textUserName.setText(name);
                        }

                        userEmailTextView.setText(email);
                        totalPointsTextView.setText("Total Points: " + (points != null ? points : 0));
                        greenCreditTextView.setText("Green Credits: " + (credits != null ? credits : 0));

                        if (profileUrl != null && !profileUrl.isEmpty()) {
                            Glide.with(this).load(profileUrl).into(profileImageView);
                        }

                        if (points != null) {
                            int badgeRes = getBadgeResource(points.intValue());
                            String badgeLabel = getBadgeName(points.intValue());
                            badgeImageView.setImageResource(badgeRes);
                            badgeNameText.setText(badgeLabel);
                        }
                    }
                });
    }

    private int getBadgeResource(int points) {
        if (points >= 200) return R.drawable.platinum_badge;
        if (points >= 150) return R.drawable.gold_badge;
        if (points >= 100) return R.drawable.silver_badge;
        if (points >= 50) return R.drawable.bronze_badge;
        return R.drawable.default_badge;
    }

    private String getBadgeName(int points) {
        if (points >= 200) return "Platinum Badge";
        if (points >= 150) return "Gold Badge";
        if (points >= 100) return "Silver Badge";
        if (points >= 50) return "Bronze Badge";
        return "Beginner Badge";
    }

    private void loadRecyclingData() {
        firestore.collection("recycle_submissions")
                .whereEqualTo("userId", friendUid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    Map<String, Float> totals = new HashMap<>();
                    for (String mat : materials) totals.put(mat, 0f);

                    for (QueryDocumentSnapshot doc : snapshot) {
                        String type = doc.getString("materialType");
                        Double weight = doc.getDouble("weight");
                        if (type != null && weight != null) {
                            String key = normalize(type);
                            if (!key.isEmpty() && totals.containsKey(key)) {
                                totals.put(key, totals.get(key) + weight.floatValue());
                            }
                        }
                    }

                    updateTreeViews(totals);
                });
    }

    private String normalize(String input) {
        if (input == null) return "";
        input = input.toLowerCase().replaceAll("[^a-z]", "");

        switch (input) {
            case "paper": return "paper";
            case "plastic": return "plastic";
            case "glass": return "glass";
            case "metal": return "metal";
            case "ewaste":
            case "electronicwaste":
            case "ewastematerial": return "ewaste";
            default: return "";
        }
    }

    private void updateTreeViews(Map<String, Float> materialTotals) {
        for (String mat : materials) {
            float weight = materialTotals.getOrDefault(mat, 0f);
            String status = String.format("%s has recycled %.1f kg", friendName, weight);
            int anim = getAnimRes(weight);

            View include = includeViews.get(mat);
            if (include != null) {
                LottieAnimationView animView = include.findViewById(R.id.treeAnimation);
                TextView statusText = include.findViewById(R.id.treeStatus);
                TextView labelText = include.findViewById(R.id.treeMaterialLabel);

                animView.setAnimation(anim);
                animView.playAnimation();
                statusText.setText(status);

                switch (mat) {
                    case "paper": labelText.setText("Paper"); break;
                    case "plastic": labelText.setText("Plastic"); break;
                    case "glass": labelText.setText("Glass"); break;
                    case "metal": labelText.setText("Metal"); break;
                    case "ewaste": labelText.setText("E-Waste"); break;
                    default: labelText.setText("Material");
                }
            }
        }
    }

    private int getAnimRes(float weight) {
        if (weight >= 45) return R.raw.fifth_phase;
        if (weight >= 30) return R.raw.forth_phase;
        if (weight >= 15) return R.raw.third_phase;
        if (weight >= 1) return R.raw.second_phase;
        return R.raw.first_phase;
    }

    @Override
    protected boolean shouldShowBottomNav() {
        return false;
    }
}
