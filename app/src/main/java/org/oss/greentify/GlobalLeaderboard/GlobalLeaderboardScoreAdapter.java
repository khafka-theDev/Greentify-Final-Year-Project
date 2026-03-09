package org.oss.greentify.GlobalLeaderboard;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import org.oss.greentify.R;

import java.text.NumberFormat;
import java.util.ArrayList;

public class GlobalLeaderboardScoreAdapter extends RecyclerView.Adapter<GlobalLeaderboardScoreAdapter.ViewHolder> {

    private final ArrayList<GlobalLeaderboardScore> leaderboardList;
    private final String currentUserEmail;

    public GlobalLeaderboardScoreAdapter(ArrayList<GlobalLeaderboardScore> leaderboardList, String currentUserEmail) {
        this.leaderboardList = leaderboardList;
        this.currentUserEmail = currentUserEmail;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard_score, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GlobalLeaderboardScore score = leaderboardList.get(position);

        holder.rankText.setText(String.valueOf(score.getRank()));

        // Fetch username from 'users' collection using email
        FirebaseFirestore.getInstance()
                .collection("users")
                .whereEqualTo("email", score.getEmail())
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String username = querySnapshot.getDocuments().get(0).getString("username");
                        holder.usernameText.setText(username != null ? username : "Unknown");
                    } else {
                        holder.usernameText.setText("Unknown");
                    }
                })
                .addOnFailureListener(e -> {
                    holder.usernameText.setText("Error");
                    Log.e("LeaderboardUsername", "Failed to fetch username", e);
                });

        // Format points with commas
        NumberFormat formatter = NumberFormat.getInstance();
        holder.pointsText.setText(formatter.format(score.getPoints()));

        // Background styling
        switch (score.getRank()) {
            case 1:
                holder.itemView.setBackgroundResource(R.drawable.bg_rank_1);
                break;
            case 2:
                holder.itemView.setBackgroundResource(R.drawable.bg_rank_2);
                break;
            case 3:
                holder.itemView.setBackgroundResource(R.drawable.bg_rank_3);
                break;
            default:
                if (score.getEmail().equals(currentUserEmail)) {
                    holder.itemView.setBackgroundResource(R.drawable.bg_rank_other);
                } else {
                    holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
                }
        }

        // Show badge based on points
        int points = score.getPoints();
        if (points >= 2000) {
            holder.avatarImage.setImageResource(R.drawable.platinum_badge);
        } else if (points >= 1500) {
            holder.avatarImage.setImageResource(R.drawable.gold_badge);
        } else if (points >= 1000) {
            holder.avatarImage.setImageResource(R.drawable.silver_badge);
        } else if (points >= 500) {
            holder.avatarImage.setImageResource(R.drawable.bronze_badge);
        } else {
            holder.avatarImage.setImageResource(R.drawable.default_badge);
        }
    }

    @Override
    public int getItemCount() {
        return leaderboardList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView rankText;
        ImageView avatarImage;
        TextView usernameText;
        TextView pointsText;
        LinearLayout pointsContainer;

        public ViewHolder(View itemView) {
            super(itemView);
            rankText = itemView.findViewById(R.id.rankText);
            avatarImage = itemView.findViewById(R.id.avatarImage);
            usernameText = itemView.findViewById(R.id.usernameText);
            pointsText = itemView.findViewById(R.id.pointsText);
            pointsContainer = itemView.findViewById(R.id.pointsContainer);
        }
    }
}
