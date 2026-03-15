package com.procollegia;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class LeaveRequestAdapter extends RecyclerView.Adapter<LeaveRequestAdapter.ViewHolder> {

    private final List<Map<String, Object>> requests;
    private final OnActionClickListener listener;

    public interface OnActionClickListener {
        void onAction(String docId, String status);
    }

    public LeaveRequestAdapter(List<Map<String, Object>> requests, OnActionClickListener listener) {
        this.requests = requests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_teacher_leave_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> req = requests.get(position);
        String reason = (String) req.get("reason");
        String dates = req.get("fromDate") + " to " + req.get("toDate");
        
        holder.text1.setText(reason != null ? reason : "Leave Request");
        holder.text2.setText(dates);
        
        holder.itemView.setOnClickListener(v -> {
            String docId = (String) req.get("docId");
            if (docId != null && listener != null) {
                // By default, a simple click approves it for this quick UI
                listener.onAction(docId, "approved");
            }
        });
    }

    @Override
    public int getItemCount() {
        return requests.size();
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
