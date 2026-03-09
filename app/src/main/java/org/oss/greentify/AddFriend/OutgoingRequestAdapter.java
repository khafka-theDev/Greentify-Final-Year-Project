package org.oss.greentify.AddFriend;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.oss.greentify.R;

import java.util.List;

public class OutgoingRequestAdapter extends RecyclerView.Adapter<OutgoingRequestAdapter.ViewHolder> {

    private List<DocumentSnapshot> requestList;

    public OutgoingRequestAdapter(List<DocumentSnapshot> requestList) {
        this.requestList = requestList;
    }

    // ✅ NEW: Add this method to allow live updating
    public void updateData(List<DocumentSnapshot> newList) {
        this.requestList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_outgoing_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentSnapshot doc = requestList.get(position);
        String toUserId = doc.getString("to");

        holder.usernameText.setText("Loading...");
        holder.emailText.setText("");
        holder.profileImage.setImageResource(R.drawable.ic_profile);

        if (toUserId != null) {
            FirebaseFirestore.getInstance().collection("users")
                    .document(toUserId)
                    .get()
                    .addOnSuccessListener(userDoc -> {
                        if (userDoc.exists()) {
                            String username = userDoc.getString("username");
                            String email = userDoc.getString("email");
                            String profileUrl = userDoc.getString("profileImageUrl");

                            holder.usernameText.setText(username != null ? username : "Unknown");
                            holder.emailText.setText(email != null ? email : "N/A");

                            if (profileUrl != null && !profileUrl.isEmpty()) {
                                Glide.with(holder.itemView.getContext()).load(profileUrl).into(holder.profileImage);
                            } else {
                                holder.profileImage.setImageResource(R.drawable.ic_profile);
                            }
                        }
                    });
        }
    }

    @Override
    public int getItemCount() {
        return requestList != null ? requestList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView usernameText, emailText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.outgoingFriendProfilePic);
            usernameText = itemView.findViewById(R.id.outgoingFriendUsername);
            emailText = itemView.findViewById(R.id.outgoingFriendEmail);
        }
    }
}
