package org.oss.greentify.Rewards;

import android.animation.Animator;
import android.app.Dialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import org.oss.greentify.R;


public class RewardDetailActivity extends AppCompatActivity {

    private static final String TAG = "RewardDetailActivity";

    private ImageView rewardDetailImage;
    private TextView rewardDetailName;
    private TextView rewardDetailCost;
    private TextView rewardDetailDescription;
    private TextView rewardDetailExpiry;
    private TextView rewardDetailStock;
    private Button claimRewardButton;

    private String rewardId;
    private Reward currentReward;

    private FirebaseFirestore db;
    private DocumentReference userRef;
    private DocumentReference rewardRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_detail);

        rewardDetailImage = findViewById(R.id.rewardDetailImage);
        rewardDetailName = findViewById(R.id.rewardDetailName);
        rewardDetailCost = findViewById(R.id.rewardDetailCost);
        rewardDetailDescription = findViewById(R.id.rewardDetailDescription);
        rewardDetailExpiry = findViewById(R.id.rewardDetailExpiry);
        rewardDetailStock = findViewById(R.id.rewardDetailStock);
        claimRewardButton = findViewById(R.id.claimRewardButton);

        rewardId = getIntent().getStringExtra("rewardId");
        if (rewardId == null || rewardId.isEmpty()) {
            Toast.makeText(this, "No reward ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userRef = db.collection("users").document(currentUser.getUid());
        rewardRef = db.collection("rewards").document(rewardId);

        fetchRewardDetails();

        claimRewardButton.setOnClickListener(v -> {
            if (currentReward != null) {
                claimReward(currentReward);
            }
        });
    }

    private void fetchRewardDetails() {
        rewardRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentReward = documentSnapshot.toObject(Reward.class);
                        if (currentReward != null) {
                            currentReward.setId(documentSnapshot.getId());
                            updateUIWithReward(currentReward);
                        }
                    } else {
                        Toast.makeText(this, "Reward not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading reward: ", e);
                    Toast.makeText(this, "Error loading reward", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUIWithReward(Reward reward) {
        rewardDetailName.setText("🎁 " + reward.getName());
        rewardDetailCost.setText("🪙 " + reward.getPointsRequired() + " Points");
        rewardDetailDescription.setText("✨ " + reward.getDescription());
        rewardDetailExpiry.setText("⏳ Valid until: " + reward.getExpiryDate());
        rewardDetailStock.setText("📦 Stock: " + reward.getStock());

        Glide.with(this)
                .load(reward.getImageUrl())
                .placeholder(R.drawable.bg_round_white)
                .into(rewardDetailImage);
    }

    private void claimReward(Reward reward) {
        userRef.get().addOnSuccessListener(userSnap -> {
            Long greenCreditsLong = userSnap.getLong("greenCredits");
            if (greenCreditsLong == null) {
                Toast.makeText(this, "Unable to retrieve your GreenCredits.", Toast.LENGTH_SHORT).show();
                return;
            }

            int userCredits = greenCreditsLong.intValue();
            if (userCredits < reward.getPointsRequired()) {
                Toast.makeText(this, "Not enough GreenCredits.", Toast.LENGTH_SHORT).show();
                return;
            }

            db.runTransaction((Transaction.Function<Void>) transaction -> {
                DocumentSnapshot userSnapshot = transaction.get(userRef);
                DocumentSnapshot rewardSnapshot = transaction.get(rewardRef);

                Long currentCredits = userSnapshot.getLong("greenCredits");
                Long stock = rewardSnapshot.getLong("stock");

                if (stock == null || stock <= 0) {
                    throw new FirebaseFirestoreException("OUT_OF_STOCK", FirebaseFirestoreException.Code.ABORTED);
                }

                if (currentCredits == null || currentCredits < reward.getPointsRequired()) {
                    throw new FirebaseFirestoreException("INSUFFICIENT_CREDIT", FirebaseFirestoreException.Code.ABORTED);
                }

                transaction.update(userRef, "greenCredits", currentCredits - reward.getPointsRequired());
                transaction.update(userRef, "claimedRewards", FieldValue.arrayUnion(reward.getId()));
                transaction.update(rewardRef, "stock", stock - 1);

                return null;
            }).addOnSuccessListener(unused -> {
                showSuccessDialog();
            }).addOnFailureListener(e -> {
                if (e.getMessage() != null && e.getMessage().contains("OUT_OF_STOCK")) {
                    Toast.makeText(this, "Not enough stock to claim.", Toast.LENGTH_SHORT).show();
                } else if (e.getMessage() != null && e.getMessage().contains("INSUFFICIENT_CREDIT")) {
                    Toast.makeText(this, "Not enough GreenCredits.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to claim reward. Please try again.", Toast.LENGTH_SHORT).show();
                }
                Log.e(TAG, "Claim failed", e);
            });

        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to check GreenCredits.", Toast.LENGTH_SHORT).show();
        });
    }


    private void showSuccessDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_reward_success);
        dialog.setCancelable(false);

        // Initialize Lottie animation
        LottieAnimationView anim = dialog.findViewById(R.id.lottieRewardSuccess);

        // Initialize and start sound effect
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.claim_reward);
        mediaPlayer.start();

        anim.playAnimation();

        anim.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mediaPlayer != null) {
                    mediaPlayer.release(); // ✅ Clean up sound
                }
                dialog.dismiss();
                finish(); // Go back to reward list
            }

            @Override public void onAnimationStart(Animator animation) {}
            @Override public void onAnimationCancel(Animator animation) {}
            @Override public void onAnimationRepeat(Animator animation) {}
        });

        dialog.show();
    }
}
