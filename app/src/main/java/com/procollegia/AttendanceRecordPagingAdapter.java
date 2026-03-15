package com.procollegia;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Map;

public class AttendanceRecordPagingAdapter extends PagingDataAdapter<Map<String, Object>, AttendanceRecordPagingAdapter.ViewHolder> {

    public AttendanceRecordPagingAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Map<String, Object>> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Map<String, Object>>() {
                @Override
                public boolean areItemsTheSame(@NonNull Map<String, Object> oldItem, @NonNull Map<String, Object> newItem) {
                    Object oldDate = oldItem.get("date");
                    Object newDate = newItem.get("date");
                    return oldDate != null && oldDate.equals(newDate);
                }

                @Override
                public boolean areContentsTheSame(@NonNull Map<String, Object> oldItem, @NonNull Map<String, Object> newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Reuse the same simple_list_item_2 layout as AttendanceRecordAdapter
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> record = getItem(position);
        if (record == null) return;
        String date = record.get("date") != null ? (String) record.get("date") : "Unknown Date";
        String status = record.get("status") != null ? (String) record.get("status") : "UNKNOWN";
        holder.text1.setText(date);
        holder.text2.setText(status.toUpperCase());
        if ("present".equalsIgnoreCase(status)) {
            holder.text2.setTextColor(holder.itemView.getContext().getColor(R.color.accent_green));
        } else {
            holder.text2.setTextColor(holder.itemView.getContext().getColor(R.color.accent_red));
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}
