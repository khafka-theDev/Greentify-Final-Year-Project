package org.oss.greentify.GlobalLeaderboard;

import android.app.AlertDialog;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.oss.greentify.Backend.BaseActivity;
import org.oss.greentify.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;

public class GlobalLeaderboardActivity extends BaseActivity {

    RecyclerView recyclerView;
    ArrayList<GlobalLeaderboardScore> leaderboardList = new ArrayList<>();
    GlobalLeaderboardScoreAdapter adapter;
    CollectionReference globalRef;
    TextView userRankText;
    ImageView userAvatarImage;
    TextView userPointsText;
    TextView bannerMessage;

    String currentUserEmail;
    boolean hasEnteredPage = false;
    boolean popupShownAndClosed = false;
    int lastRankWhenPopupClosed = -1;

    private static final String PREFS_NAME = "GreentifyPrefs";
    private static final String KEY_HAS_SEEN_LEADERBOARD_WALKTHROUGH = "hasSeenLeaderboardWalkthrough";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_leaderboard);

        recyclerView = findViewById(R.id.globalRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userRankText = findViewById(R.id.userRankText);
        userPointsText = findViewById(R.id.userPoints);
        userAvatarImage = findViewById(R.id.userAvatar);
        bannerMessage = findViewById(R.id.bannerMessage);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserEmail = currentUser.getEmail();
            String uid = currentUser.getUid();

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            DocumentReference userRef = firestore.collection("users").document(uid);
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(this).load(profileImageUrl).circleCrop().into(userAvatarImage);
                    }

                    Long points = documentSnapshot.getLong("points");
                    if (points != null) {
                        NumberFormat formatter = NumberFormat.getInstance();
                        userPointsText.setText(formatter.format(points));
                        bannerMessage.setText(points > 0
                                ? "Ranking season resets every 6 month. Climb your way up now!"
                                : "Start recycling to join the leaderboard now!");
                    }
                }
            });
        } else {
            currentUserEmail = "Guest";
        }

        adapter = new GlobalLeaderboardScoreAdapter(leaderboardList, currentUserEmail);
        recyclerView.setAdapter(adapter);

        globalRef = FirebaseFirestore.getInstance().collection("global_leaderboard");

        globalRef.orderBy("totalPoints", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshot == null || snapshot.isEmpty()) return;

                    ArrayList<GlobalLeaderboardScore> tempList = new ArrayList<>();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String email = doc.getString("email");
                        String username = doc.getString("username");
                        Long points = doc.getLong("totalPoints");

                        if (email != null && username != null && points != null) {
                            tempList.add(new GlobalLeaderboardScore(email, username, points.intValue()));
                        }
                    }

                    Collections.sort(tempList, (a, b) -> Integer.compare(b.getPoints(), a.getPoints()));
                    for (int i = 0; i < tempList.size(); i++) {
                        tempList.get(i).setRank(i + 1);
                    }

                    for (GlobalLeaderboardScore score : tempList) {
                        if (score.getEmail().equals(currentUserEmail)) {
                            int currentRank = score.getRank();
                            userRankText.setText("You are in Rank: " + currentRank);

                            int previousRank = getPreviousRank();
                            if (hasEnteredPage && previousRank != -1 && currentRank < previousRank) {
                                if (!popupShownAndClosed || lastRankWhenPopupClosed != currentRank) {
                                    showRankUpPopup(previousRank, currentRank);
                                }
                            }

                            saveCurrentRank(currentRank); // update last known rank
                        }
                    }

                    runOnUiThread(() -> {
                        leaderboardList.clear();
                        leaderboardList.addAll(tempList);
                        adapter.notifyDataSetChanged();
                    });
                });

        hasEnteredPage = true;
    }

    private void showRankUpPopup(int oldRank, int newRank) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.rank_up_popup, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(popupView)
                .setCancelable(false)
                .create();

        dialog.setOnShowListener(d -> {
            LottieAnimationView lottie = popupView.findViewById(R.id.rankUpAnimation);
            lottie.setAnimation(R.raw.rank_up); // your Lottie JSON
            lottie.playAnimation();

            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.rank_up_1); // your sound
            mediaPlayer.setOnCompletionListener(mp -> {
                mp.stop();
                mp.release();
            });
            mediaPlayer.start();
        });

        TextView rankChangeText = popupView.findViewById(R.id.rankChangeText);
        rankChangeText.setText("Old Rank: " + oldRank + " → New Rank: " + newRank);

        popupView.findViewById(R.id.closePopup).setOnClickListener(v -> {
            popupShownAndClosed = true;
            lastRankWhenPopupClosed = newRank;
            dialog.dismiss();
        });

        dialog.show();
    }

    private void saveCurrentRank(int rank) {
        getSharedPreferences("RankPrefs", MODE_PRIVATE)
                .edit()
                .putInt("lastKnownRank", rank)
                .apply();
    }

    private int getPreviousRank() {
        return getSharedPreferences("RankPrefs", MODE_PRIVATE)
                .getInt("lastKnownRank", -1);
    }

    @Override
    protected int getSelectedItemId() {
        return R.id.nav_leaderboard;
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean hasSeen = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getBoolean(KEY_HAS_SEEN_LEADERBOARD_WALKTHROUGH, false);

        if (!hasSeen) {
            new android.os.Handler().postDelayed(() -> {
                showLeaderboardStep1();
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                        .edit()
                        .putBoolean(KEY_HAS_SEEN_LEADERBOARD_WALKTHROUGH, true)
                        .apply();
            }, 800); // ensure views are fully ready
        }
    }

    private void showLeaderboardStep1() {
        TapTargetView.showFor(this,
                TapTarget.forView(findViewById(R.id.globalTitle), "Leaderboard", "See top recyclers and compare your points with others.")
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
                        showLeaderboardStep2();
                    }
                });
    }

    private void showLeaderboardStep2() {
        TapTargetView.showFor(this,
                TapTarget.forView(userRankText, "Your Rank", "This shows your current position among all users on the leaderboard.")
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
