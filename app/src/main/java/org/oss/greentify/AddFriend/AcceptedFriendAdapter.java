package org.oss.greentify.AddFriend;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.oss.greentify.R;
import org.oss.greentify.model.User;

import java.util.List;

public class AcceptedFriendAdapter extends RecyclerView.Adapter<AcceptedFriendAdapter.FriendViewHolder> {

    private List<User> friendList;
    private Context context;

    public AcceptedFriendAdapter(List<User> friendList, Context context) {
        this.friendList = friendList;
        this.context = context;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_accepted_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        User user = friendList.get(position);

        holder.usernameText.setText(user.getUsername());

        // Load profile image
        Glide.with(context)
                .load(user.getProfileImageUrl())
                .placeholder(R.drawable.ic_profile)
                .into(holder.profileImage);

        // View Profile
        holder.viewProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, FriendProfileActivity.class);
            intent.putExtra("friendUid", user.getUid());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText;
        ImageView profileImage;
        Button viewProfileButton;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.acceptedFriendUsername);
            profileImage = itemView.findViewById(R.id.acceptedFriendProfilePic);
            viewProfileButton = itemView.findViewById(R.id.btnViewProfile);
        }
    }
}
