package com.procollegia;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class TeacherMarksAdapter extends RecyclerView.Adapter<TeacherMarksAdapter.ViewHolder> {

    private final List<Map<String, Object>> students;
    private final OnMarksChangeListener listener;
    
    // Constant thresholds for alerting (red box)
    private static final int PASS_MARK_IA = 15;
    private static final int PASS_MARK_ASSIGN = 6;

    public interface OnMarksChangeListener {
        void onMarksChanged();
    }

    public TeacherMarksAdapter(List<Map<String, Object>> students, OnMarksChangeListener listener) {
        this.students = students;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_mark, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> student = students.get(position);
        Context ctx = holder.itemView.getContext();
        
        String name = (String) student.get("name");
        holder.tvName.setText(name != null ? name : "Student");

        Object param1 = student.get("ia1");
        Object param2 = student.get("ia2");
        Object param3 = student.get("assign");

        long ia1 = param1 != null ? (long) param1 : 0L;
        long ia2 = param2 != null ? (long) param2 : 0L;
        long assign = param3 != null ? (long) param3 : 0L;

        // Temporarily remove watchers to avoid recursion during set
        holder.clearWatchers();

        holder.etIa1.setText(ia1 > 0 ? String.valueOf(ia1) : "");
        holder.etIa2.setText(ia2 > 0 ? String.valueOf(ia2) : "");
        holder.etAssign.setText(assign > 0 ? String.valueOf(assign) : "");
        
        // Highlight low marks immediately
        applyErrorBackgrounds(holder, ia1, ia2, assign, ctx);

        // IA-1 Watcher
        holder.watcherIa1 = createWatcher(holder.etIa1, student, "ia1", PASS_MARK_IA, holder, ctx);
        holder.etIa1.addTextChangedListener(holder.watcherIa1);
        
        // IA-2 Watcher
        holder.watcherIa2 = createWatcher(holder.etIa2, student, "ia2", PASS_MARK_IA, holder, ctx);
        holder.etIa2.addTextChangedListener(holder.watcherIa2);
        
        // Assign Watcher
        holder.watcherAssign = createWatcher(holder.etAssign, student, "assign", PASS_MARK_ASSIGN, holder, ctx);
        holder.etAssign.addTextChangedListener(holder.watcherAssign);
    }

    private void applyErrorBackgrounds(ViewHolder holder, long ia1, long ia2, long assign, Context ctx) {
        // Evaluate individually highlighting invalid fields
        if (ia1 > 0 && ia1 < PASS_MARK_IA) {
            holder.etIa1.setBackgroundResource(R.drawable.bg_marks_input_alert);
            holder.etIa1.setTextColor(ContextCompat.getColor(ctx, R.color.accent_red));
        } else {
            holder.etIa1.setBackgroundResource(R.drawable.bg_marks_input_normal);
            holder.etIa1.setTextColor(ContextCompat.getColor(ctx, R.color.text_primary));
        }

        if (ia2 > 0 && ia2 < PASS_MARK_IA) {
            holder.etIa2.setBackgroundResource(R.drawable.bg_marks_input_alert);
            holder.etIa2.setTextColor(ContextCompat.getColor(ctx, R.color.accent_red));
        } else {
            holder.etIa2.setBackgroundResource(R.drawable.bg_marks_input_normal);
            holder.etIa2.setTextColor(ContextCompat.getColor(ctx, R.color.text_primary));
        }

        if (assign > 0 && assign < PASS_MARK_ASSIGN) {
            holder.etAssign.setBackgroundResource(R.drawable.bg_marks_input_alert);
            holder.etAssign.setTextColor(ContextCompat.getColor(ctx, R.color.accent_red));
        } else {
            holder.etAssign.setBackgroundResource(R.drawable.bg_marks_input_normal);
            holder.etAssign.setTextColor(ContextCompat.getColor(ctx, R.color.text_primary));
        }
    }

    private TextWatcher createWatcher(EditText editText, Map<String, Object> student, String key, int passThreshold, ViewHolder holder, Context ctx) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    long val = Long.parseLong(s.toString());
                    student.put(key, val);
                    
                    if (val < passThreshold) {
                        editText.setBackgroundResource(R.drawable.bg_marks_input_alert);
                        editText.setTextColor(ContextCompat.getColor(ctx, R.color.accent_red));
                    } else {
                        editText.setBackgroundResource(R.drawable.bg_marks_input_normal);
                        editText.setTextColor(ContextCompat.getColor(ctx, R.color.text_primary));
                    }
                } catch (NumberFormatException e) {
                    student.put(key, 0L);
                    editText.setBackgroundResource(R.drawable.bg_marks_input_normal);
                    editText.setTextColor(ContextCompat.getColor(ctx, R.color.text_primary));
                }
                
                if (listener != null) {
                    listener.onMarksChanged();
                }
            }
        };
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView tvName;
        public final EditText etIa1, etIa2, etAssign;
        
        public TextWatcher watcherIa1, watcherIa2, watcherAssign;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_student_name);
            etIa1 = itemView.findViewById(R.id.et_ia1);
            etIa2 = itemView.findViewById(R.id.et_ia2);
            etAssign = itemView.findViewById(R.id.et_assign);
        }
        
        public void clearWatchers() {
            if (watcherIa1 != null) etIa1.removeTextChangedListener(watcherIa1);
            if (watcherIa2 != null) etIa2.removeTextChangedListener(watcherIa2);
            if (watcherAssign != null) etAssign.removeTextChangedListener(watcherAssign);
        }
    }
}
