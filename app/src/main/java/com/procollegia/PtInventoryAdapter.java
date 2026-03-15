package com.procollegia;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.List;
import java.util.Map;

public class PtInventoryAdapter extends RecyclerView.Adapter<PtInventoryAdapter.ViewHolder> {

    private final List<Map<String, Object>> requests;
    private final OnRequestAction listenerApprove;
    private final OnRequestActionDeny listenerDeny;

    public interface OnRequestAction {
        void onAction(String docId, Object items);
    }
    
    public interface OnRequestActionDeny {
        void onAction(String docId);
    }

    public PtInventoryAdapter(List<Map<String, Object>> requests, OnRequestAction listenerApprove, OnRequestActionDeny listenerDeny) {
        this.requests = requests;
        this.listenerApprove = listenerApprove;
        this.listenerDeny = listenerDeny;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> req = requests.get(position);
        String docId = (String) req.get("docId");
        String studentId = (String) req.get("studentId");
        Object items = req.get("items");
        
        holder.tvStudent.setText("Student ID: " + (studentId != null ? studentId : "Unknown"));
        holder.tvItems.setText("Items Requested: " + (items != null ? items.toString() : "0"));
        
        holder.btnApprove.setOnClickListener(v -> {
            if (listenerApprove != null) listenerApprove.onAction(docId, items);
        });
        
        holder.btnDeny.setOnClickListener(v -> {
            if (listenerDeny != null) listenerDeny.onAction(docId);
        });
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView tvStudent;
        public final TextView tvItems;
        public final MaterialButton btnApprove;
        public final MaterialButton btnDeny;

        public ViewHolder(View itemView) {
            super(itemView);
            tvStudent = itemView.findViewById(R.id.tv_student_name);
            tvItems = itemView.findViewById(R.id.tv_items);
            btnApprove = itemView.findViewById(R.id.btn_approve);
            btnDeny = itemView.findViewById(R.id.btn_deny);
        }
    }
}
