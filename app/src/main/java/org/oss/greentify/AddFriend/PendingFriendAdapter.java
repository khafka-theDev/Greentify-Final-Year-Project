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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import org.oss.greentify.R;
import org.oss.greentify.model.User;
import java.util.List;

public class PendingFriendAdapter extends RecyclerView.Adapter<PendingFriendAdapter.PendingViewHolder> {

    private List<DocumentSnapshot> requestList;
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final String currentUid = FirebaseAuth.getInstance().getUid();
    private final boolean isSentRequestList;

    public PendingFriendAdapter(List<DocumentSnapshot> requestList) {
        this.requestList = requestList;
        this.isSentRequestList = false;
    }

    public PendingFriendAdapter(List<DocumentSnapshot> requestList, boolean isSentRequestList) {
        this.requestList = requestList;
        this.isSentRequestList = isSentRequestList;
    }

    // ✅ Added for dynamic updates
    public void updateData(List<DocumentSnapshot> newList) {
        this.requestList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PendingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pending_friend, parent, false);
        return new PendingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PendingViewHolder holder, int position) {
        DocumentSnapshot requestDoc = requestList.get(position);
        String otherUid = isSentRequestList ? requestDoc.getString("to") : requestDoc.getString("from");

        if (otherUid == null) return;

        firestore.collection("users").document(otherUid)
                .get()
                .addOnSuccessListener(userDoc -> {
                    User user = userDoc.toObject(User.class);
                    if (user != null) {
                        holder.usernameText.setText(user.getUsername());
                        holder.emailText.setText(user.getEmail());

                        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                            Glide.with(holder.itemView.getContext())
                                    .load(user.getProfileImageUrl())
                                    .placeholder(R.drawable.ic_profile)
                                    .into(holder.profileImage);
                        } else {
                            holder.profileImage.setImageResource(R.drawable.ic_profile);
                        }
                    }
                });

        if (isSentRequestList) {
            holder.acceptBtn.setVisibility(View.GONE);
            holder.rejectBtn.setVisibility(View.GONE);
            holder.statusRequested.setVisibility(View.VISIBLE);
        } else {
            holder.acceptBtn.setVisibility(View.VISIBLE);
            holder.rejectBtn.setVisibility(View.VISIBLE);
            holder.statusRequested.setVisibility(View.GONE);

            holder.acceptBtn.setOnClickListener(v -> {
                String fromUid = requestDoc.getString("from");

                if (currentUid != null && fromUid != null) {
                    requestDoc.getReference().update("status", "accepted", "users", List.of(currentUid, fromUid))
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(holder.itemView.getContext(), "Friend request accepted", Toast.LENGTH_SHORT).show();
                                disableButtons(holder);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(holder.itemView.getContext(), "Failed to accept", Toast.LENGTH_SHORT).show();
                            });
                }
            });

            holder.rejectBtn.setOnClickListener(v -> {
                requestDoc.getReference().update("status", "rejected")
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(holder.itemView.getContext(), "Request rejected", Toast.LENGTH_SHORT).show();
                            disableButtons(holder);
                        });
            });
        }
    }

    private void disableButtons(PendingViewHolder holder) {
        holder.acceptBtn.setEnabled(false);
        holder.rejectBtn.setEnabled(false);
    }

    @Override
    public int getItemCount() {
        return requestList != null ? requestList.size() : 0;
    }

    static class PendingViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText, emailText, statusRequested;
        Button acceptBtn, rejectBtn;
        ImageView profileImage;

        public PendingViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.pendingFriendUsername);
            emailText = itemView.findViewById(R.id.pendingFriendEmail);
            profileImage = itemView.findViewById(R.id.pendingFriendProfilePic);
            acceptBtn = itemView.findViewById(R.id.btnAccept);
            rejectBtn = itemView.findViewById(R.id.btnReject);
            statusRequested = itemView.findViewById(R.id.statusRequested);
        }
    }
}
