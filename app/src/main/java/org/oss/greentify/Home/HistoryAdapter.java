package org.oss.greentify.Home;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.oss.greentify.R;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final List<HistoryItem> historyList;

    public HistoryAdapter(List<HistoryItem> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryItem item = historyList.get(position);

        holder.materialText.setText("Material Type: " + item.getMaterialType());
        holder.weightText.setText("Weight: " + item.getWeight());
        holder.locationText.setText("Location: " + item.getLocation());
        holder.dateText.setText("Date Submitted: " + item.getDateSubmitted());
        holder.pointsText.setText("Points Gained: " + item.getPointGained());
        holder.creditText.setText("Green Credit Gained: " + (item.getGreenCreditGained() != null ? item.getGreenCreditGained() : "0"));
        holder.statusText.setText("Status: " + item.getStatus());

        // Status Dot Color
        String status = item.getStatus().toLowerCase();
        if (status.equals("pending")) {
            holder.statusDot.setBackgroundResource(R.drawable.dot_pending);
        } else if (status.equals("approved")) {
            holder.statusDot.setBackgroundResource(R.drawable.dot_accepted);
        } else if (status.equals("rejected")) {
            holder.statusDot.setBackgroundResource(R.drawable.dot_rejected);
        } else {
            holder.statusDot.setBackgroundResource(R.drawable.dot_pending);
        }

        // Highlight unread items
        if (!item.isRead()) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.unread_background)); // you can define this color
            holder.statusText.setTypeface(null, Typeface.BOLD);
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.transparent));
            holder.statusText.setTypeface(null, Typeface.NORMAL);
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView materialText, weightText, locationText, dateText, pointsText, creditText, statusText;
        View statusDot;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            materialText = itemView.findViewById(R.id.historyMaterial);
            weightText = itemView.findViewById(R.id.historyWeight);
            locationText = itemView.findViewById(R.id.historyLocation);
            dateText = itemView.findViewById(R.id.historyDate);
            pointsText = itemView.findViewById(R.id.historyPoints);
            creditText = itemView.findViewById(R.id.historyCredit);
            statusText = itemView.findViewById(R.id.historyStatus);
            statusDot = itemView.findViewById(R.id.statusDot);
        }
    }
}
