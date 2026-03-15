package com.procollegia;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.VH> {

    private final List<Map<String, Object>> items;

    public LeaderboardAdapter(List<Map<String, Object>> items) { this.items = items; }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Map<String, Object> s = items.get(pos);
        String name = str(s, "name", "Student");
        long score  = toLong(s.get("honorScore"));
        h.tvRank.setText("#" + (pos + 4)); // 4th place onwards (top 3 on podium)
        h.tvName.setText(name);
        h.tvScore.setText(score + " pts");
        h.tvAvatar.setText(name.isEmpty() ? "?" : String.valueOf(name.charAt(0)).toUpperCase());
    }

    @Override public int getItemCount() { return items.size(); }

    private String str(Map<String, Object> m, String k, String d) {
        Object v = m.get(k); return v != null ? v.toString() : d;
    }

    private long toLong(Object o) {
        if (o instanceof Long) return (Long) o;
        if (o instanceof Number) return ((Number) o).longValue();
        return 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvRank, tvName, tvScore, tvAvatar;
        VH(View v) {
            super(v);
            tvRank   = v.findViewById(R.id.tv_rank);
            tvName   = v.findViewById(R.id.tv_name);
            tvScore  = v.findViewById(R.id.tv_score);
            tvAvatar = v.findViewById(R.id.tv_avatar);
        }
    }
}
