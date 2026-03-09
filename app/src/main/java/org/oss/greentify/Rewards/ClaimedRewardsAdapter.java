package org.oss.greentify.Rewards;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.oss.greentify.R;

import java.util.List;

public class ClaimedRewardsAdapter extends RecyclerView.Adapter<ClaimedRewardsAdapter.ViewHolder> {
    private List<Reward> rewardList;

    public ClaimedRewardsAdapter(List<Reward> rewards) {
        this.rewardList = rewards;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, points;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.rewardImage);
            name = itemView.findViewById(R.id.rewardName);
            points = itemView.findViewById(R.id.rewardPoints);
        }
    }

    @NonNull
    @Override
    public ClaimedRewardsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_claimed_reward, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reward reward = rewardList.get(position);
        holder.name.setText(reward.getName());
        holder.points.setText(reward.getPointsRequired() + " Points");
        Glide.with(holder.image.getContext()).load(reward.getImageUrl()).into(holder.image);
    }

    @Override
    public int getItemCount() {
        return rewardList.size();
    }
}
