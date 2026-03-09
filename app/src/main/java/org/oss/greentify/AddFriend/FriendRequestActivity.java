package org.oss.greentify.AddFriend;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import org.oss.greentify.Backend.BaseActivity;
import org.oss.greentify.R;

import java.util.ArrayList;
import java.util.List;

public class FriendRequestActivity extends BaseActivity {

    private RecyclerView recyclerPending, recyclerSent;
    private TextView titlePending, titleSent;
    private PendingFriendAdapter pendingFriendAdapter;
    private OutgoingRequestAdapter outgoingRequestAdapter;

    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;

    private ListenerRegistration pendingListener, sentListener;

    @Override
    protected int getSelectedItemId() {
        return 0;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);

        titlePending = findViewById(R.id.titlePending);
        titleSent = findViewById(R.id.titleSent);
        recyclerPending = findViewById(R.id.recyclerPendingRequests);
        recyclerSent = findViewById(R.id.recyclerSentRequests);

        recyclerPending.setLayoutManager(new LinearLayoutManager(this));
        recyclerSent.setLayoutManager(new LinearLayoutManager(this));

        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        pendingFriendAdapter = new PendingFriendAdapter(new ArrayList<>());
        outgoingRequestAdapter = new OutgoingRequestAdapter(new ArrayList<>());

        recyclerPending.setAdapter(pendingFriendAdapter);
        recyclerSent.setAdapter(outgoingRequestAdapter);

        listenForPendingRequests(); // 👂 listen for incoming requests
        listenForSentRequests();    // 👂 listen for outgoing requests
    }

    private void listenForPendingRequests() {
        if (currentUser == null) return;

        pendingListener = firestore.collection("friend_requests")
                .whereEqualTo("to", currentUser.getUid())
                .whereEqualTo("status", "pending")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading pending requests", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (querySnapshot != null) {
                        List<DocumentSnapshot> docs = querySnapshot.getDocuments();
                        pendingFriendAdapter.updateData(docs);

                        if (docs.isEmpty()) {
                            titlePending.setText("No friend request available");
                        } else {
                            titlePending.setText("Pending Friend Request");
                        }
                    }
                });
    }

    private void listenForSentRequests() {
        if (currentUser == null) return;

        sentListener = firestore.collection("friend_requests")
                .whereEqualTo("from", currentUser.getUid())
                .whereEqualTo("status", "pending")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading sent requests", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (querySnapshot != null) {
                        List<DocumentSnapshot> docs = querySnapshot.getDocuments();
                        outgoingRequestAdapter.updateData(docs);

                        if (docs.isEmpty()) {
                            titleSent.setText("You haven't sent any requests");
                        } else {
                            titleSent.setText("Your Sent Friend Requests");
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pendingListener != null) pendingListener.remove();
        if (sentListener != null) sentListener.remove();
    }
}
