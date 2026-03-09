package org.oss.greentify.Rewards;

import android.animation.Animator;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import org.oss.greentify.R;

import java.util.HashMap;
import java.util.Map;

public class RewardClaimActivity extends AppCompatActivity {

    private static final String TAG = "RewardClaimActivity";

    private TextView rewardName, rewardDesc, rewardPoints, rewardStock, rewardExpiry;
    private Button claimBtn;

    private DocumentReference userRef, rewardRef;
    private String rewardId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_claim);

        rewardName = findViewById(R.id.rewardNameText);
        rewardDesc = findViewById(R.id.rewardDescText);
        rewardPoints = findViewById(R.id.rewardPointsText);
        rewardStock = findViewById(R.id.rewardStockText);
        rewardExpiry = findViewById(R.id.rewardExpiryText);
        claimBtn = findViewById(R.id.claimRewardBtn);

        rewardId = getIntent().getStringExtra("rewardId");
        if (rewardId == null || rewardId.isEmpty()) {
            Toast.makeText(this, "Reward ID not found.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Reward ID is null or empty, finishing activity.");
            finish();
            return;
        }
        Log.d(TAG, "Reward ID received: " + rewardId);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "No user logged in, finishing activity.");
            finish();
            return;
        }
        Log.d(TAG, "Current user ID: " + currentUser.getUid());

        String userId = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        userRef = db.collection("users").document(userId);
        rewardRef = db.collection("rewards").document(rewardId);

        // Fetch reward data
        rewardRef.get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        Toast.makeText(this, "Reward not found in database.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Reward document does not exist for rewardId: " + rewardId);
                        finish();
                        return;
                    }
                    Log.d(TAG, "Reward document found: " + snapshot.getId());

                    Reward reward = snapshot.toObject(Reward.class);
                    if (reward != null) {
                        reward.setId(snapshot.getId()); // set reward ID
                        Log.d(TAG, "Reward data loaded: " + reward.getName());
                        rewardName.setText(reward.getName());
                        rewardDesc.setText(reward.getDescription());
                        rewardPoints.setText("Requires: " + reward.getPointsRequired() + " GreenCredits");
                        rewardStock.setText("Stock: " + reward.getStock());
                        rewardExpiry.setText("Expiry: " + reward.getExpiryDate());

                        claimBtn.setOnClickListener(v -> {
                            Log.d(TAG, "Claim button clicked");
                            claimReward(reward);
                        });
                    } else {
                        Toast.makeText(this, "Reward data invalid.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Reward data is null for rewardId: " + rewardId);
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load reward data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to load reward data", e);
                    finish();
                });
    }

    private void claimReward(Reward reward) {
        Log.d(TAG, "Starting claimReward transaction for reward: " + reward.getId());
        userRef.get()
                .addOnSuccessListener(snapshot -> {
                    Long greenCreditsLong = snapshot.getLong("greenCredits");
                    if (greenCreditsLong == null) {
                        Toast.makeText(this, "You do not have enough wallet credit.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "User greenCredits field is missing.");
                        return;
                    }

                    int greenCredits = greenCreditsLong.intValue();
                    Log.d(TAG, "User greenCredits: " + greenCredits);

                    if (greenCredits >= reward.getPointsRequired()) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        db.runTransaction((Transaction.Function<Void>) transaction -> {
                            Log.d(TAG, "Running transaction to claim reward...");
                            DocumentSnapshot userSnap = transaction.get(userRef);
                            DocumentSnapshot rewardSnap = transaction.get(rewardRef);

                            Long userCredits = userSnap.getLong("greenCredits");
                            Long rewardStock = rewardSnap.getLong("stock");

                            Log.d(TAG, "Inside transaction - userCredits: " + userCredits + ", rewardStock: " + rewardStock);

                            if (userCredits == null || rewardStock == null) {
                                throw new FirebaseFirestoreException("Missing required data", FirebaseFirestoreException.Code.ABORTED);
                            }
                            if (rewardStock <= 0) {
                                throw new FirebaseFirestoreException("Reward out of stock", FirebaseFirestoreException.Code.ABORTED);
                            }
                            if (userCredits < reward.getPointsRequired()) {
                                throw new FirebaseFirestoreException("Insufficient credits", FirebaseFirestoreException.Code.ABORTED);
                            }

                            transaction.update(userRef, "greenCredits", userCredits - reward.getPointsRequired());
                            Log.d(TAG, "Deducted greenCredits: " + (userCredits - reward.getPointsRequired()));

                            transaction.update(userRef, "claimedRewards", FieldValue.arrayUnion(reward.getId()));
                            Log.d(TAG, "Added reward to claimedRewards array");

                            Map<String, Object> rewardStockUpdate = new HashMap<>();
                            rewardStockUpdate.put("stock", rewardStock - 1);
                            transaction.update(rewardRef, rewardStockUpdate);
                            Log.d(TAG, "Decremented reward stock to: " + (rewardStock - 1));

                            return null;
                        }).addOnSuccessListener(unused -> {
                                Log.d(TAG, "Reward claim transaction succeeded.");

                                Dialog successDialog = new Dialog(RewardClaimActivity.this);
                                successDialog.setContentView(R.layout.dialog_reward_success);
                                successDialog.setCancelable(false);

                                LottieAnimationView animationView = successDialog.findViewById(R.id.lottieRewardSuccess);
                                animationView.playAnimation();

                                animationView.addAnimatorListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        successDialog.dismiss();
                                        finish(); // or redirect user if needed
                                    }

                                    @Override public void onAnimationStart(Animator animation) {}
                                    @Override public void onAnimationCancel(Animator animation) {}
                                    @Override public void onAnimationRepeat(Animator animation) {}
                                });

                                successDialog.show();

                        }).addOnFailureListener(e -> {
                            String message = e instanceof FirebaseFirestoreException
                                    ? ((FirebaseFirestoreException) e).getMessage()
                                    : e.getMessage();
                            Toast.makeText(this, "Failed to claim reward: " + message, Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Reward claim transaction failed.", e);
                        });

                    } else {
                        Toast.makeText(this, "Not enough GreenCredits", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "User does not have enough GreenCredits.");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to check credits: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to retrieve user credits", e);
                });
    }
}
