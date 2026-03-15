package com.procollegia;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HonorEventAdapter extends RecyclerView.Adapter<HonorEventAdapter.ViewHolder> {

    private final List<HonorEvent> events;

    public HonorEventAdapter(List<HonorEvent> events) {
        this.events = events;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_honor_event, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HonorEvent event = events.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventArrow, tvEventPts, tvEventDesc, tvEventDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventArrow = itemView.findViewById(R.id.tv_event_arrow);
            tvEventPts   = itemView.findViewById(R.id.tv_event_pts);
            tvEventDesc  = itemView.findViewById(R.id.tv_event_desc);
            tvEventDate  = itemView.findViewById(R.id.tv_event_date);
        }

        public void bind(HonorEvent event) {
            tvEventDesc.setText(event.description);
            tvEventDate.setText(event.date);

            int green = ContextCompat.getColor(itemView.getContext(), R.color.accent_green);
            int red   = ContextCompat.getColor(itemView.getContext(), R.color.accent_red);

            if (event.points >= 0) {
                tvEventArrow.setText("↑");
                tvEventArrow.setTextColor(green);
                tvEventPts.setText("+ " + event.points + " pts");
                tvEventPts.setTextColor(green);
            } else {
                tvEventArrow.setText("↓");
                tvEventArrow.setTextColor(red);
                tvEventPts.setText("- " + Math.abs(event.points) + " pts");
                tvEventPts.setTextColor(red);
            }
        }
    }
}
