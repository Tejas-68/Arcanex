package com.procollegia;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TeacherAttendanceAdapter extends RecyclerView.Adapter<TeacherAttendanceAdapter.ViewHolder> {

    private final List<Map<String, Object>> allStudents;
    private List<Map<String, Object>> filteredStudents;
    private final OnAttendanceChangedListener listener;

    public interface OnAttendanceChangedListener {
        void onCountUpdated();
    }

    public TeacherAttendanceAdapter(List<Map<String, Object>> students, OnAttendanceChangedListener listener) {
        this.allStudents = students;
        this.filteredStudents = new ArrayList<>(students);
        this.listener = listener;
    }

    public void filter(String query) {
        filteredStudents.clear();
        if (query.isEmpty()) {
            filteredStudents.addAll(allStudents);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Map<String, Object> student : allStudents) {
                String name = (String) student.get("name");
                if (name != null && name.toLowerCase().contains(lowerCaseQuery)) {
                    filteredStudents.add(student);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void selectAll(boolean present) {
        String status = present ? "present" : "absent";
        for (Map<String, Object> student : filteredStudents) {
            student.put("attendanceStatus", status);
        }
        notifyDataSetChanged();
        listener.onCountUpdated();
    }

    public List<Map<String, Object>> getFilteredStudents() {
        return filteredStudents;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_teacher_attendance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> student = filteredStudents.get(position);
        String name = (String) student.get("name");
        String roll = (String) student.get("rollNumber");
        String status = (String) student.get("attendanceStatus");
        
        holder.tvName.setText(name != null ? name : "Student");
        holder.tvRoll.setText("Roll: " + (roll != null ? roll : "---"));
        
        // Reset styles
        holder.btnPresent.setBackground(null);
        holder.btnPresent.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary));
        holder.btnAbsent.setBackground(null);
        holder.btnAbsent.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary));
        holder.btnLate.setBackground(null);
        holder.btnLate.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary));
        
        holder.ivCheckOverlay.setVisibility(View.GONE);

        if ("present".equals(status)) {
            holder.btnPresent.setBackgroundResource(R.drawable.bg_pal_present);
            holder.btnPresent.setTextColor(Color.WHITE);
            holder.ivCheckOverlay.setVisibility(View.VISIBLE);
        } else if ("absent".equals(status)) {
            holder.btnAbsent.setBackgroundResource(R.drawable.bg_pal_absent);
            holder.btnAbsent.setTextColor(Color.WHITE);
        } else if ("late".equals(status)) {
            holder.btnLate.setBackgroundResource(R.drawable.bg_pal_late);
            holder.btnLate.setTextColor(Color.WHITE);
        }
        
        holder.btnPresent.setOnClickListener(v -> {
            student.put("attendanceStatus", "present");
            notifyItemChanged(position);
            listener.onCountUpdated();
        });

        holder.btnAbsent.setOnClickListener(v -> {
            student.put("attendanceStatus", "absent");
            notifyItemChanged(position);
            listener.onCountUpdated();
        });

        holder.btnLate.setOnClickListener(v -> {
            student.put("attendanceStatus", "late");
            notifyItemChanged(position);
            listener.onCountUpdated();
        });
    }

    @Override
    public int getItemCount() {
        return filteredStudents.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView tvName, tvRoll, btnPresent, btnAbsent, btnLate;
        public final ImageView ivCheckOverlay;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_student_name);
            tvRoll = itemView.findViewById(R.id.tv_roll_no);
            btnPresent = itemView.findViewById(R.id.btn_present);
            btnAbsent = itemView.findViewById(R.id.btn_absent);
            btnLate = itemView.findViewById(R.id.btn_late);
            ivCheckOverlay = itemView.findViewById(R.id.iv_check_overlay);
        }
    }
}
