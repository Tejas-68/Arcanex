package com.procollegia;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class AttendanceRecordAdapter extends RecyclerView.Adapter<AttendanceRecordAdapter.ViewHolder> {

    private final List<Map<String, Object>> records;

    public AttendanceRecordAdapter(List<Map<String, Object>> records) {
        this.records = records;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> record = records.get(position);
        String date = (String) record.get("date");
        String status = (String) record.get("status");
        
        holder.text1.setText(date != null ? date : "Unknown Date");
        holder.text2.setText(status != null ? status.toUpperCase() : "UNKNOWN");
        
        if ("present".equalsIgnoreCase(status)) {
            holder.text2.setTextColor(holder.itemView.getContext().getColor(R.color.accent_green));
        } else {
            holder.text2.setTextColor(holder.itemView.getContext().getColor(R.color.accent_red));
        }
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView text1;
        public final TextView text2;

        public ViewHolder(View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}
