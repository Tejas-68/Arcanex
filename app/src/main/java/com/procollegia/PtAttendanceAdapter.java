package com.procollegia;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PtAttendanceAdapter extends RecyclerView.Adapter<PtAttendanceAdapter.ViewHolder> {

    private final List<Map<String, Object>> students;
    private final List<Map<String, Object>> filteredStudents;

    public PtAttendanceAdapter(List<Map<String, Object>> students) {
        this.students = students;
        this.filteredStudents = new ArrayList<>(students);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_multiple_choice, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> student = filteredStudents.get(position);
        String name = (String) student.get("name");
        String status = (String) student.get("attendanceStatus");
        
        holder.text1.setText(name != null ? name : "Student");
        holder.text1.setChecked("present".equals(status));
        
        holder.itemView.setOnClickListener(v -> {
            boolean isPresent = holder.text1.isChecked();
            holder.text1.setChecked(!isPresent);
            student.put("attendanceStatus", !isPresent ? "present" : "absent");
        });
    }

    @Override
    public int getItemCount() {
        return filteredStudents.size();
    }

    public void filter(String query) {
        filteredStudents.clear();
        if (query.isEmpty()) {
            filteredStudents.addAll(students);
        } else {
            for (Map<String, Object> s : students) {
                String name = (String) s.get("name");
                if (name != null && name.toLowerCase().contains(query.toLowerCase())) {
                    filteredStudents.add(s);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final CheckedTextView text1;

        public ViewHolder(View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
        }
    }
}
