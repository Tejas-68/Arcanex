package com.procollegia;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.VH> {

    private final List<Map<String, Object>> items;

    public StaffAdapter(List<Map<String, Object>> items) { this.items = items; }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_staff, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Map<String, Object> s = items.get(pos);
        String name = str(s, "name", "Staff");
        String role = str(s, "role", "teacher");
        String dept = str(s, "department", "");
        h.tvName.setText(name);
        h.tvRole.setText(capitalize(role) + (dept.isEmpty() ? "" : " · " + dept));
        h.tvAvatar.setText(name.isEmpty() ? "?" : String.valueOf(name.charAt(0)).toUpperCase());
        h.tvEmail.setOnClickListener(v -> {
            String email = str(s, "email", "");
            if (!email.isEmpty()) {
                android.content.Intent i = new android.content.Intent(android.content.Intent.ACTION_SENDTO);
                i.setData(android.net.Uri.parse("mailto:" + email));
                v.getContext().startActivity(android.content.Intent.createChooser(i, "Send mail"));
            }
        });
    }

    @Override public int getItemCount() { return items.size(); }

    private String str(Map<String, Object> m, String key, String def) {
        Object v = m.get(key); return v != null ? v.toString() : def;
    }

    private String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvRole, tvAvatar, tvEmail;
        VH(View v) {
            super(v);
            tvName   = v.findViewById(R.id.tv_name);
            tvRole   = v.findViewById(R.id.tv_role);
            tvAvatar = v.findViewById(R.id.tv_avatar);
            tvEmail  = v.findViewById(R.id.tv_email);
        }
    }
}
