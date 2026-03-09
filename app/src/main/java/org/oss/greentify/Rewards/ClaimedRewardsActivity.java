package org.oss.greentify.Rewards;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import org.oss.greentify.R;

import java.util.ArrayList;
import java.util.List;

public class ClaimedRewardsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ClaimedRewardsAdapter adapter;
    private ArrayList<Reward> claimedList = new ArrayList<>();
    private FirebaseFirestore db;
    private String userId;
    private TextView textNoClaimedRewards;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claimed_rewards);

        recyclerView = findViewById(R.id.recyclerClaimedRewards);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClaimedRewardsAdapter(claimedList);
        recyclerView.setAdapter(adapter);

        textNoClaimedRewards = findViewById(R.id.textNoClaimedRewards); // ✅ Important!

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadClaimedRewards();
    }

    private void loadClaimedRewards() {
        db.collection("users").document(userId).get().addOnSuccessListener(snapshot -> {
            List<String> claimedIds = (List<String>) snapshot.get("claimedRewards");

            if (claimedIds == null || claimedIds.isEmpty()) {
                textNoClaimedRewards.setVisibility(View.VISIBLE);
                claimedList.clear();
                adapter.notifyDataSetChanged();
                return;
            }

            db.collection("rewards")
                    .whereIn(FieldPath.documentId(), claimedIds)
                    .get()
                    .addOnSuccessListener(query -> {
                        claimedList.clear();
                        for (DocumentSnapshot doc : query.getDocuments()) {
                            Reward reward = doc.toObject(Reward.class);
                            if (reward != null) claimedList.add(reward);
                        }

                        adapter.notifyDataSetChanged();

                        // ✅ Show or hide message
                        if (claimedList.isEmpty()) {
                            textNoClaimedRewards.setVisibility(View.VISIBLE);
                        } else {
                            textNoClaimedRewards.setVisibility(View.GONE);
                        }
                    });
        });
    }

}

