package org.oss.greentify.Home;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import org.oss.greentify.R;

import java.text.SimpleDateFormat;
import java.util.*;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView historyRecyclerView;
    private HistoryAdapter historyAdapter;
    private final List<HistoryItem> fullHistoryList = new ArrayList<>();
    private final List<HistoryItem> filteredHistoryList = new ArrayList<>();

    private Button btnFilterAll, btnFilterPending, btnFilterAccepted, btnFilterRejected;
    private TextView textNoHistory;

    private FirebaseUser user;
    private FirebaseFirestore firestore;

    private ListenerRegistration listenerRegistration;

    private String currentFilter = "All";
    private static final String TAG = "HistoryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyRecyclerView = findViewById(R.id.recyclerViewHistory);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new HistoryAdapter(filteredHistoryList);
        historyRecyclerView.setAdapter(historyAdapter);

        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterPending = findViewById(R.id.btnFilterPending);
        btnFilterAccepted = findViewById(R.id.btnFilterAccepted);
        btnFilterRejected = findViewById(R.id.btnFilterRejected);
        textNoHistory = findViewById(R.id.textNoHistory);

        user = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (getIntent().getBooleanExtra("fromUpload", false)) {
            new android.os.Handler().postDelayed(() -> {
                Toast.makeText(
                        this,
                        "✅ Submission sent! Track your status here.\nYou'll earn points and appear on the leaderboard once it's approved.",
                        Toast.LENGTH_LONG
                ).show();
            }, 500);
        }

        setupFilterButtons();
        startRealtimeListener();
    }

    private void setupFilterButtons() {
        btnFilterAll.setOnClickListener(v -> applyFilter("All"));
        btnFilterPending.setOnClickListener(v -> applyFilter("Pending"));
        btnFilterAccepted.setOnClickListener(v -> applyFilter("Approved"));
        btnFilterRejected.setOnClickListener(v -> applyFilter("Rejected"));
    }

    private void applyFilter(String filter) {
        currentFilter = filter;
        filteredHistoryList.clear();

        for (HistoryItem item : fullHistoryList) {
            if (filter.equals("All") || item.getStatus().equalsIgnoreCase(filter)) {
                filteredHistoryList.add(item);
            }
        }

        Collections.sort(filteredHistoryList, (a, b) -> b.getActualTimestamp().compareTo(a.getActualTimestamp()));
        historyAdapter.notifyDataSetChanged();

        if (filteredHistoryList.isEmpty()) {
            textNoHistory.setVisibility(View.VISIBLE);
            switch (filter) {
                case "All": textNoHistory.setText("No submission history yet. Start recycling now!"); break;
                case "Pending": textNoHistory.setText("No pending submissions."); break;
                case "Approved": textNoHistory.setText("No approved submissions yet."); break;
                case "Rejected": textNoHistory.setText("All good, no rejected submissions."); break;
            }
        } else {
            textNoHistory.setVisibility(View.GONE);
        }
    }

    private void startRealtimeListener() {
        if (listenerRegistration != null) listenerRegistration.remove();

        listenerRegistration = firestore.collection("recycle_submissions")
                .whereEqualTo("userId", user.getUid())
                .addSnapshotListener((submissionSnapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed: ", error);
                        return;
                    }

                    if (submissionSnapshots == null || submissionSnapshots.isEmpty()) {
                        fullHistoryList.clear();
                        applyFilter(currentFilter);
                        return;
                    }

                    fullHistoryList.clear();

                    for (DocumentSnapshot submissionDoc : submissionSnapshots.getDocuments()) {
                        String submissionId = submissionDoc.getId();
                        String materialType = submissionDoc.getString("materialType");
                        Double weight = submissionDoc.getDouble("weight");
                        String weightString = weight != null ? String.format(Locale.getDefault(), "%.2f", weight) : "0";
                        String location = submissionDoc.getString("location");
                        Long points = submissionDoc.getLong("points");
                        String pointGained = points != null ? points.toString() : "0";
                        Double greenCredits = submissionDoc.getDouble("greenCredits");
                        String greenCreditsGained = greenCredits != null ? String.format(Locale.getDefault(), "%.2f", greenCredits) : "0.00";

                        firestore.collection("recycle_submission_history")
                                .whereEqualTo("submissionId", submissionId)
                                .get()
                                .addOnSuccessListener(historyQuerySnapshot -> {
                                    if (!historyQuerySnapshot.isEmpty()) {
                                        DocumentSnapshot historyDoc = historyQuerySnapshot.getDocuments().get(0);

                                        String status = historyDoc.getString("status");
                                        String note = historyDoc.getString("note");
                                        Timestamp timestamp = historyDoc.getTimestamp("timestamp");
                                        Date actualTimestamp = timestamp != null ? timestamp.toDate() : new Date(0);
                                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
                                        String formattedTimestamp = sdf.format(actualTimestamp);

                                        List<String> readBy = (List<String>) historyDoc.get("readByUserIds");
                                        boolean isRead = readBy != null && readBy.contains(user.getUid());

                                        HistoryItem item = new HistoryItem(
                                                materialType,
                                                weightString,
                                                location,
                                                formattedTimestamp,
                                                pointGained,
                                                greenCreditsGained,
                                                status,
                                                note,
                                                isRead,
                                                actualTimestamp
                                        );

                                        fullHistoryList.add(item);
                                        applyFilter(currentFilter);
                                    }
                                })
                                .addOnFailureListener(e -> Log.e(TAG, "Failed fetching history for submissionId: " + submissionId, e));
                    }
                });
    }

    @Override
    protected void onDestroy() {
        if (listenerRegistration != null) listenerRegistration.remove();
        super.onDestroy();
    }
}
