package org.oss.greentify.GlobalLeaderboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.oss.greentify.R;
import org.oss.greentify.RecycleStart.RecycleSubmission;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final List<RecycleSubmission> submissions;

    public HistoryAdapter(List<RecycleSubmission> submissions) {
        this.submissions = submissions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recycle_submission, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecycleSubmission item = submissions.get(position);
        holder.materialType.setText(item.getMaterialType());
        holder.date.setText(formatTimestamp(item.getTimestamp()));
        holder.weight.setText(item.getWeightKg() + " kg");
    }

    @Override
    public int getItemCount() {
        return submissions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView materialType, date, weight;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            materialType = itemView.findViewById(R.id.textMaterialType);
            date = itemView.findViewById(R.id.textDate);
            weight = itemView.findViewById(R.id.textWeight);
        }
    }

    // Helper method to format the timestamp to a readable date
    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        Date date = new Date(timestamp);
        return sdf.format(date);
    }
}
