package com.procollegia;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class TournamentAdapter extends RecyclerView.Adapter<TournamentAdapter.VH> {

    private final List<Map<String, Object>> items;

    public TournamentAdapter(List<Map<String, Object>> items) {
        this.items = items;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tournament, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Map<String, Object> t = items.get(pos);
        h.tvName.setText(getString(t, "name", "Tournament"));
        h.tvSport.setText(getString(t, "sport", "General"));
        h.tvDate.setText("📅 " + getString(t, "date", "TBD"));
        h.tvVenue.setText("📍 " + getString(t, "venue", "TBD"));
        String status = getString(t, "status", "upcoming");
        h.tvStatus.setText(status);
        if ("open".equalsIgnoreCase(status)) {
            h.tvStatus.setTextColor(h.itemView.getContext().getResources().getColor(R.color.accent_green, null));
            h.btnRegister.setVisibility(View.VISIBLE);
        } else {
            h.tvStatus.setTextColor(h.itemView.getContext().getResources().getColor(R.color.text_secondary, null));
            h.btnRegister.setVisibility(View.GONE);
        }
        h.btnRegister.setOnClickListener(v ->
            Toast.makeText(v.getContext(), "Registration submitted for " + getString(t, "name", ""), Toast.LENGTH_SHORT).show());
    }

    @Override public int getItemCount() { return items.size(); }

    private String getString(Map<String, Object> m, String key, String def) {
        Object v = m.get(key);
        return v != null ? v.toString() : def;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvSport, tvDate, tvVenue, tvStatus;
        Button btnRegister;
        VH(View v) {
            super(v);
            tvName      = v.findViewById(R.id.tv_name);
            tvSport     = v.findViewById(R.id.tv_sport_tag);
            tvDate      = v.findViewById(R.id.tv_date);
            tvVenue     = v.findViewById(R.id.tv_venue);
            tvStatus    = v.findViewById(R.id.tv_status);
            btnRegister = v.findViewById(R.id.btn_register);
        }
    }
}
