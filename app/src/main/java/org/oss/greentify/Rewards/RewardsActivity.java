package org.oss.greentify.Rewards;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.oss.greentify.Backend.BaseActivity;
import org.oss.greentify.R;

import java.util.ArrayList;
import java.util.List;

public class RewardsActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private RewardsAdapter rewardsAdapter;
    private List<Reward> rewardList;
    private ProgressBar progressBar;

    private static final String TAG = "RewardsActivity";

    private FirebaseFirestore db;

    private static final String PREFS_NAME = "GreentifyPrefs";
    private static final String KEY_HAS_SEEN_REWARDS_WALKTHROUGH = "hasSeenRewardsWalkthrough";


    @Override
    protected int getSelectedItemId() {
        return R.id.nav_rewards;  // Highlight the Rewards menu item
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);
        loadGreenWallet();
        recyclerView = findViewById(R.id.recyclerViewRewards);
        progressBar = findViewById(R.id.progressBar);

        findViewById(R.id.buttonViewClaimedRewards).setOnClickListener(v -> {
            startActivity(new Intent(RewardsActivity.this, ClaimedRewardsActivity.class));
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        rewardList = new ArrayList<>();
        rewardsAdapter = new RewardsAdapter(this, rewardList, reward -> {
            Intent intent = new Intent(RewardsActivity.this, RewardDetailActivity.class);
            intent.putExtra("rewardId", reward.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(rewardsAdapter);

        db = FirebaseFirestore.getInstance();

        fetchRewardsFromFirestore();
    }

    private void loadGreenWallet() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long credits = documentSnapshot.getLong("greenCredits");
                        TextView walletText = findViewById(R.id.greenWalletText); // fixed ID
                        if (walletText != null && credits != null) {
                            walletText.setText("Green Wallet: " + credits);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("RewardsActivity", "Failed to fetch greenCredits", e);
                    Toast.makeText(this, "Unable to load wallet balance.", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchRewardsFromFirestore() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("rewards")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        rewardList.clear();
                        QuerySnapshot snapshots = task.getResult();
                        if (snapshots != null && !snapshots.isEmpty()) {
                            for (QueryDocumentSnapshot doc : snapshots) {
                                Reward reward = doc.toObject(Reward.class);
                                reward.setId(doc.getId());
                                rewardList.add(reward);
                            }
                            rewardsAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(RewardsActivity.this, "No rewards found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Firestore fetch error: ", task.getException());
                        Toast.makeText(RewardsActivity.this, "Failed to load rewards", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean hasSeen = prefs.getBoolean(KEY_HAS_SEEN_REWARDS_WALKTHROUGH, false);

        if (!hasSeen) {
            new android.os.Handler().postDelayed(() -> {
                showRewardsStep1();
                prefs.edit().putBoolean(KEY_HAS_SEEN_REWARDS_WALKTHROUGH, true).apply();
            }, 500); // wait for views to load
        }
    }

    private void showRewardsStep1() {
        TapTargetView.showFor(this,
                TapTarget.forView(findViewById(R.id.greenWalletText), "Green Wallet", "This shows your current green credit balance.")
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
                        showRewardsStep2();
                    }
                });
    }

    private void showRewardsStep2() {
        TapTargetView.showFor(this,
                TapTarget.forView(findViewById(R.id.buttonViewClaimedRewards), "Claimed Rewards", "Tap here to see rewards you've already claimed.")
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
                        showRewardsStep3();
                    }
                });
    }

    private void showRewardsStep3() {
        RecyclerView.ViewHolder firstItem = recyclerView.findViewHolderForAdapterPosition(0);
        if (firstItem != null) {
            View card = firstItem.itemView;
            TapTargetView.showFor(this,
                    TapTarget.forView(card, "Available Rewards", "Tap any reward to view details and redeem.")
                            .outerCircleColorInt(Color.TRANSPARENT)
                            .targetCircleColor(android.R.color.white)
                            .titleTextColor(android.R.color.white)
                            .descriptionTextColor(android.R.color.white)
                            .dimColor(android.R.color.black)
                            .tintTarget(true)
                            .transparentTarget(true)
                            .drawShadow(true)
                            .cancelable(true));
        }
    }

}
