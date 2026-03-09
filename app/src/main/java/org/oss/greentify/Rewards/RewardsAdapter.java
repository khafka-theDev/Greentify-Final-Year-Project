package org.oss.greentify.Rewards;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import org.oss.greentify.R;

import java.util.List;

public class RewardsAdapter extends RecyclerView.Adapter<RewardsAdapter.ViewHolder> {

    private Context context;
    private List<Reward> rewards;
    private OnRewardClickListener listener;

    public interface OnRewardClickListener {
        void onRewardClick(Reward reward);
    }

    public RewardsAdapter(Context context, List<Reward> rewards, OnRewardClickListener listener) {
        this.context = context;
        this.rewards = rewards;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reward, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Reward reward = rewards.get(position);

        holder.nameTextView.setText(reward.getName());
        holder.descriptionTextView.setText(reward.getDescription());
        holder.pointsTextView.setText(String.format("%d points", reward.getPointsRequired()));
        holder.expiryTextView.setText("Expires on: " + reward.getExpiryDate());
        holder.stockTextView.setText("Stock: " + reward.getStock());

        Glide.with(context)
                .load(reward.getImageUrl())
                .placeholder(R.drawable.bg_round_white)
                .into(holder.imageView);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRewardClick(reward);
            }
        });
    }

    @Override
    public int getItemCount() {
        return rewards.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, descriptionTextView, pointsTextView, expiryTextView, stockTextView;
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.rewardName);
            descriptionTextView = itemView.findViewById(R.id.rewardDescription);
            pointsTextView = itemView.findViewById(R.id.rewardPoints);
            expiryTextView = itemView.findViewById(R.id.rewardExpiry);
            stockTextView = itemView.findViewById(R.id.rewardStock);
            imageView = itemView.findViewById(R.id.rewardImage);
        }
    }
}
