package org.oss.greentify.Home;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.oss.greentify.R;
import org.oss.greentify.Rewards.Reward;
import org.oss.greentify.Rewards.RewardDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class CarouselAdapter extends RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder> {

    private Context context;
    private List<Reward> rewardList;

    public CarouselAdapter(Context context, List<Reward> rewardList) {
        this.context = context;
        this.rewardList = rewardList != null ? rewardList : new ArrayList<>();
    }

    @NonNull
    @Override
    public CarouselViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_carousel, parent, false);
        return new CarouselViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarouselViewHolder holder, int position) {
        Reward reward = rewardList.get(position);

        // Load image
        Glide.with(context)
                .load(reward.getImageUrl())
                .transform(new com.bumptech.glide.load.resource.bitmap.CenterCrop(), new com.bumptech.glide.load.resource.bitmap.RoundedCorners(32))
                .into(holder.imageView);

        // Set name and points
        holder.rewardName.setText(reward.getName());
        holder.rewardPoints.setText(reward.getPointsRequired() + " Points");

        // ✅ Add click listener
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RewardDetailActivity.class);
            intent.putExtra("rewardId", reward.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return rewardList != null ? rewardList.size() : 0;
    }

    static class CarouselViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView rewardName;
        TextView rewardPoints;

        public CarouselViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.carouselImageView);
            rewardName = itemView.findViewById(R.id.rewardName);
            rewardPoints = itemView.findViewById(R.id.rewardPoints);
        }
    }
}
