package org.oss.greentify.AddFriend;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FriendRequestHandler {

    public static void acceptFriendRequest(Context context, String currentUid, String requesterUid, DocumentSnapshot requestDoc) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(requesterUid).get()
                .addOnSuccessListener(requesterSnap -> {
                    String friendName = requesterSnap.getString("username");
                    String friendUrl = requesterSnap.getString("profileUrl");

                    Map<String, Object> friendEntry = new HashMap<>();
                    friendEntry.put("uid", requesterUid);
                    friendEntry.put("username", friendName);
                    friendEntry.put("profileUrl", friendUrl);
                    friendEntry.put("addedAt", FieldValue.serverTimestamp());

                    db.collection("users").document(currentUid)
                            .collection("friends").document(requesterUid)
                            .set(friendEntry);

                    db.collection("users").document(currentUid).get()
                            .addOnSuccessListener(currentSnap -> {
                                String currentName = currentSnap.getString("username");
                                String currentUrl = currentSnap.getString("profileUrl");

                                Map<String, Object> backEntry = new HashMap<>();
                                backEntry.put("uid", currentUid);
                                backEntry.put("username", currentName);
                                backEntry.put("profileUrl", currentUrl);
                                backEntry.put("addedAt", FieldValue.serverTimestamp());

                                db.collection("users").document(requesterUid)
                                        .collection("friends").document(currentUid)
                                        .set(backEntry);

                                requestDoc.getReference().update("status", "accepted");
                                Toast.makeText(context, "Friend request accepted!", Toast.LENGTH_SHORT).show();
                            });
                });
    }
}
