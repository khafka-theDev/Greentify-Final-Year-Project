package org.oss.greentify.AddFriend;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.oss.greentify.R;
import org.oss.greentify.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

    private List<User> userList;
    private FirebaseUser currentUser;
    private FirebaseFirestore firestore;

    public FriendAdapter(List<User> userList) {
        this.userList = userList;
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_user, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        User user = userList.get(position);
        holder.usernameText.setText(user.getUsername());
        holder.emailText.setText(user.getEmail());

        // ✅ Load profile image if available
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(user.getProfileImageUrl())
                    .placeholder(R.drawable.ic_profile)
                    .into(holder.profileImage);
        } else {
            holder.profileImage.setImageResource(R.drawable.ic_profile);
        }

        holder.addFriendButton.setOnClickListener(v -> {
            if (currentUser != null && !currentUser.getUid().equals(user.getUid())) {
                sendFriendRequest(currentUser.getUid(), user.getUid(), holder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateList(List<User> filtered) {
        userList = filtered;
        notifyDataSetChanged();
    }

    private void sendFriendRequest(String senderUid, String receiverUid, FriendViewHolder holder) {
        Map<String, Object> request = new HashMap<>();
        request.put("from", senderUid);
        request.put("to", receiverUid);
        request.put("status", "pending");
        request.put("timestamp", FieldValue.serverTimestamp());

        firestore.collection("friend_requests")
                .add(request)
                .addOnSuccessListener(docRef -> {
                    holder.addFriendButton.setText("Requested");
                    holder.addFriendButton.setEnabled(false);
                    Toast.makeText(holder.itemView.getContext(), "Friend request sent!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(holder.itemView.getContext(), "Failed to send request.", Toast.LENGTH_SHORT).show());
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText, emailText;
        Button addFriendButton;
        ImageView profileImage;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.friendUsername);
            emailText = itemView.findViewById(R.id.friendEmail);
            addFriendButton = itemView.findViewById(R.id.btnAddFriend);
            profileImage = itemView.findViewById(R.id.friendProfilePic);
        }
    }
}
