package com.procollegia;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class EquipmentSelectAdapter extends RecyclerView.Adapter<EquipmentSelectAdapter.ViewHolder> {

    private final List<Map<String, Object>> items;
    private final List<Map<String, Object>> selectedItems;

    public EquipmentSelectAdapter(List<Map<String, Object>> items, List<Map<String, Object>> selectedItems) {
        this.items = items;
        this.selectedItems = selectedItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_equipment_select, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> item = items.get(position);
        String name = (String) item.get("name");
        Long available = (Long) item.get("quantity");
        
        holder.tvName.setText((name != null ? name : "Equipment") + " (" + (available != null ? available : 0) + " available)");
        
        boolean isSelected = selectedItems.contains(item);
        if (isSelected) {
            holder.ivCheckbox.setBackgroundResource(R.drawable.bg_neumorph_circle_raised);
            holder.ivCheckbox.setImageResource(android.R.drawable.checkbox_on_background); // Indicator
        } else {
            holder.ivCheckbox.setBackgroundResource(R.drawable.bg_neumorph_circle_inset);
            holder.ivCheckbox.setImageDrawable(null);
        }
        
        holder.itemView.setOnClickListener(v -> {
            boolean wasSelected = selectedItems.contains(item);
            if (!wasSelected) {
                item.put("selectedQty", 1L);
                selectedItems.add(item);
            } else {
                selectedItems.remove(item);
            }
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView tvName;
        public final ImageView ivCheckbox;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(android.R.id.text1);
            ivCheckbox = itemView.findViewById(R.id.iv_checkbox);
        }
    }
}
