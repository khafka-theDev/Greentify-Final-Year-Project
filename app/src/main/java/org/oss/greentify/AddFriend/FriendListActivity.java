package org.oss.greentify.AddFriend;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import org.oss.greentify.Backend.BaseActivity;
import org.oss.greentify.R;
import org.oss.greentify.model.User;

import java.util.*;

public class FriendListActivity extends BaseActivity {

    private EditText searchInput;
    private RecyclerView recyclerFutureFriends, recyclerAcceptedFriends;

    private FriendAdapter futureFriendAdapter;
    private AcceptedFriendAdapter acceptedFriendAdapter;

    private List<User> futureFriends = new ArrayList<>();
    private List<User> acceptedFriends = new ArrayList<>();

    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    private String currentUid;

    private static final String PREFS_NAME = "GreentifyPrefs";
    private static final String KEY_HAS_SEEN_FRIEND_LIST_WALKTHROUGH = "hasSeenFriendListWalkthrough";


    private final Set<String> alreadyRequestedOrFriends = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        searchInput = findViewById(R.id.searchFriendInput);
        recyclerFutureFriends = findViewById(R.id.recyclerFriends);
        recyclerAcceptedFriends = findViewById(R.id.recyclerAcceptedFriends);

        recyclerFutureFriends.setLayoutManager(new LinearLayoutManager(this));
        recyclerAcceptedFriends.setLayoutManager(new LinearLayoutManager(this));

        futureFriendAdapter = new FriendAdapter(futureFriends);
        acceptedFriendAdapter = new AcceptedFriendAdapter(acceptedFriends, this);

        recyclerFutureFriends.setAdapter(futureFriendAdapter);
        recyclerAcceptedFriends.setAdapter(acceptedFriendAdapter);

        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUid = currentUser.getUid();

        // First: Get friend request info, then load users
        loadFriendRequestStatus(this::loadAllUsers);
        loadAcceptedFriends();

        Button btnFriendRequests = findViewById(R.id.btnFriendRequests);
        btnFriendRequests.setOnClickListener(v -> {
            Intent intent = new Intent(FriendListActivity.this, FriendRequestActivity.class);
            startActivity(intent);
        });

        // Slide panel controls
        View yourFriendsPanel = findViewById(R.id.yourFriendsPanel);
        Button btnShowYourFriends = findViewById(R.id.btnShowYourFriends);
        Button btnCloseYourFriends = findViewById(R.id.btnCloseYourFriends);

        btnShowYourFriends.setOnClickListener(v -> {
            yourFriendsPanel.setVisibility(View.VISIBLE);
            yourFriendsPanel.animate().translationX(0).setDuration(300).start();
        });

        btnCloseYourFriends.setOnClickListener(v -> {
            yourFriendsPanel.animate()
                    .translationX(-yourFriendsPanel.getWidth())
                    .setDuration(300)
                    .withEndAction(() -> yourFriendsPanel.setVisibility(View.GONE))
                    .start();
        });


        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected int getSelectedItemId() {
        return R.id.nav_friend;
    }

    private void loadFriendRequestStatus(Runnable callback) {
        firestore.collection("friend_requests")
                .whereIn("status", Arrays.asList("pending", "accepted"))
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query) {
                        String from = doc.getString("from");
                        String to = doc.getString("to");

                        if (currentUid.equals(from) && to != null) {
                            alreadyRequestedOrFriends.add(to);
                        }
                        if (currentUid.equals(to) && from != null) {
                            alreadyRequestedOrFriends.add(from);
                        }
                    }
                    callback.run();
                });
    }

    private void loadAllUsers() {
        firestore.collection("users").get().addOnSuccessListener(query -> {
            futureFriends.clear();
            for (DocumentSnapshot doc : query) {
                User user = doc.toObject(User.class);
                if (user != null) {
                    user.setUid(doc.getId());

                    boolean isNotAdminOrSelf = !user.getEmail().equalsIgnoreCase("admin@gmail.com")
                            && !user.getUid().equals(currentUid);

                    if (isNotAdminOrSelf && !alreadyRequestedOrFriends.contains(user.getUid())) {
                        futureFriends.add(user);
                    }
                }
            }
            futureFriendAdapter.updateList(futureFriends);
        });
    }

    // ✅ Updated to match FriendRequestActivity logic (correct one)
    private void loadAcceptedFriends() {
        firestore.collection("friend_requests")
                .whereEqualTo("status", "accepted")
                .whereArrayContains("users", currentUid)
                .get()
                .addOnSuccessListener(query -> {
                    acceptedFriends.clear(); // Reset first

                    for (DocumentSnapshot doc : query) {
                        String fromUid = doc.getString("from");
                        String toUid = doc.getString("to");

                        // Figure out who the *other* user is
                        String otherUid = currentUid.equals(fromUid) ? toUid : fromUid;

                        if (otherUid != null && !otherUid.isEmpty()) {
                            firestore.collection("users").document(otherUid)
                                    .get()
                                    .addOnSuccessListener(userDoc -> {
                                        User friend = userDoc.toObject(User.class);
                                        if (friend != null) {
                                            friend.setUid(userDoc.getId());
                                            acceptedFriends.add(friend);
                                            acceptedFriendAdapter.notifyDataSetChanged();
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load accepted friends", Toast.LENGTH_SHORT).show();
                });
    }


    private void filterUsers(String keyword) {
        List<User> filtered = new ArrayList<>();
        for (User user : futureFriends) {
            if (user.getUsername() != null && user.getUsername().toLowerCase().contains(keyword.toLowerCase())) {
                filtered.add(user);
            }
        }
        futureFriendAdapter.updateList(filtered);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean hasSeen = prefs.getBoolean(KEY_HAS_SEEN_FRIEND_LIST_WALKTHROUGH, false);

        if (!hasSeen) {
            new android.os.Handler().postDelayed(() -> {
                showFriendListStep1();
                prefs.edit().putBoolean(KEY_HAS_SEEN_FRIEND_LIST_WALKTHROUGH, true).apply();
            }, 600); // Delay for layout inflation
        }
    }

    private void showFriendListStep1() {
        TapTargetView.showFor(this,
                TapTarget.forView(findViewById(R.id.searchFriendInput), "Search Friends", "Type a name to find someone new to add.")
                        .outerCircleColorInt(Color.TRANSPARENT)
                        .targetCircleColor(android.R.color.white)
                        .titleTextColor(android.R.color.white)
                        .descriptionTextColor(android.R.color.white)
                        .dimColor(android.R.color.black)
                        .tintTarget(true)
                        .transparentTarget(true)
                        .drawShadow(true)
                        .cancelable(false),
                new TapTargetView.Listener() {
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);
                        showFriendListStep2();
                    }
                });
    }

    private void showFriendListStep2() {
        TapTargetView.showFor(this,
                TapTarget.forView(findViewById(R.id.btnFriendRequests), "Friend Requests", "Tap here to manage pending or received friend requests.")
                        .outerCircleColorInt(Color.TRANSPARENT)
                        .targetCircleColor(android.R.color.white)
                        .titleTextColor(android.R.color.white)
                        .descriptionTextColor(android.R.color.white)
                        .dimColor(android.R.color.black)
                        .tintTarget(true)
                        .transparentTarget(true)
                        .drawShadow(true)
                        .cancelable(false),
                new TapTargetView.Listener() {
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);
                        showFriendListStep3();
                    }
                });
    }

    private void showFriendListStep3() {
        TapTargetView.showFor(this,
                TapTarget.forView(findViewById(R.id.btnShowYourFriends), "Your Friends", "Tap here to view all your accepted friends.")
                        .outerCircleColorInt(Color.TRANSPARENT)
                        .targetCircleColor(android.R.color.white)
                        .titleTextColor(android.R.color.white)
                        .descriptionTextColor(android.R.color.white)
                        .dimColor(android.R.color.black)
                        .tintTarget(true)
                        .transparentTarget(true)
                        .drawShadow(true)
                        .cancelable(false),
                new TapTargetView.Listener() {
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);
                        showFriendListStep4();
                    }
                });
    }

    private void showFriendListStep4() {
        RecyclerView.ViewHolder firstItemViewHolder = recyclerFutureFriends.findViewHolderForAdapterPosition(0);
        if (firstItemViewHolder != null) {
            View addButton = firstItemViewHolder.itemView.findViewById(R.id.btnAddFriend);
            if (addButton != null) {
                TapTargetView.showFor(this,
                        TapTarget.forView(addButton, "Add Friend", "Tap this to send a friend request to this user.")
                                .outerCircleColorInt(Color.TRANSPARENT)
                                .targetCircleColor(android.R.color.white)
                                .titleTextColor(android.R.color.white)
                                .descriptionTextColor(android.R.color.white)
                                .dimColor(android.R.color.black)
                                .tintTarget(true)
                                .transparentTarget(true)
                                .drawShadow(true)
                                .cancelable(true));
            }
        }
    }

}

