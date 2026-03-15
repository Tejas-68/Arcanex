package com.procollegia;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TimetableAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<TimetableEvent> events;

    public TimetableAdapter(List<TimetableEvent> events) {
        this.events = events;
    }
    
    public void setEvents(List<TimetableEvent> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return events.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TimetableEvent.TYPE_CLASS) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timetable_class, parent, false);
            return new ClassViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timetable_break, parent, false);
            return new BreakViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TimetableEvent ev = events.get(position);
        if (holder instanceof ClassViewHolder) {
            ((ClassViewHolder) holder).bind(ev);
        } else if (holder instanceof BreakViewHolder) {
            ((BreakViewHolder) holder).bind(ev);
        }
    }

    @Override
    public int getItemCount() {
        return events != null ? events.size() : 0;
    }

    static class ClassViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvSubject, tvRoom, tvProf;
        View viewTimelineColor;

        public ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvSubject = itemView.findViewById(R.id.tv_subject);
            tvRoom = itemView.findViewById(R.id.tv_room);
            tvProf = itemView.findViewById(R.id.tv_prof);
            viewTimelineColor = itemView.findViewById(R.id.view_timeline_color);
        }

        public void bind(TimetableEvent ev) {
            tvTime.setText(ev.time);
            tvSubject.setText(ev.title);
            tvRoom.setText(ev.room);
            tvProf.setText(ev.professor);

            // Set the colored pill dynamically
            try {
                GradientDrawable bg = (GradientDrawable) viewTimelineColor.getBackground();
                bg.setColor(Color.parseColor(ev.colorHex));
            } catch (Exception ignored) {}
        }
    }

    static class BreakViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvBreakName;

        public BreakViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvBreakName = itemView.findViewById(R.id.tv_break_name);
        }

        public void bind(TimetableEvent ev) {
            tvTime.setText(ev.time + ":");
            tvBreakName.setText(ev.title);
        }
    }
}
