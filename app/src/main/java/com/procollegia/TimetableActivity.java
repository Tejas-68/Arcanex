package com.procollegia;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class TimetableActivity extends AppCompatActivity {

    private RecyclerView rvTimetable;
    private TimetableAdapter adapter;
    private TextView[] dayPills;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);

        rvTimetable = findViewById(R.id.rv_timetable);
        View ivBack = findViewById(R.id.iv_back);
        if (ivBack != null) ivBack.setOnClickListener(v -> onBackPressed());

        dayPills = new TextView[]{
                findViewById(R.id.tv_day_mon),
                findViewById(R.id.tv_day_tue),
                findViewById(R.id.tv_day_wed),
                findViewById(R.id.tv_day_thu),
                findViewById(R.id.tv_day_fri)
        };

        for (int i = 0; i < dayPills.length; i++) {
            final int index = i;
            if (dayPills[i] != null) {
                dayPills[i].setOnClickListener(v -> selectDay(index));
            }
        }

        adapter = new TimetableAdapter(new ArrayList<>());
        rvTimetable.setAdapter(adapter);

        // Default to Wednesday (index 2) as in the mockup
        selectDay(2);

        // Set up bottom navigation based on role
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid != null) {
            db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
                String role = doc.getString("role");
                if ("teacher".equals(role)) BottomNavigationHelper.setup(this, -1, "teacher");
                else BottomNavigationHelper.setup(this, -1, "student");
            });
        }
    }

    private void selectDay(int selectedIndex) {
        for (int i = 0; i < dayPills.length; i++) {
            if (dayPills[i] != null) {
                if (i == selectedIndex) {
                    dayPills[i].setBackgroundResource(R.drawable.bg_day_selected);
                    dayPills[i].setTextColor(Color.WHITE);
                    dayPills[i].setTypeface(null, Typeface.BOLD);
                } else {
                    dayPills[i].setBackgroundResource(R.drawable.bg_day_unselected);
                    dayPills[i].setTextColor(getResources().getColor(R.color.text_secondary));
                    dayPills[i].setTypeface(null, Typeface.NORMAL);
                }
            }
        }

        // Load mock timetable matching the beautiful UI
        List<TimetableEvent> events = new ArrayList<>();
        if (selectedIndex == 2) { // Wednesday Mockup Data
            events.add(new TimetableEvent("08:00 — 09:00", "Data Structures", "Room 204", "Prof. Sharma", "#4A90E2")); // Blue
            events.add(new TimetableEvent("09:00 — 10:00", "Mathematics", "Room 101", "Prof. Rao", "#4CAF50")); // Green
            events.add(new TimetableEvent("10:00 — 10:15", "Short Break"));
            events.add(new TimetableEvent("10:15 — 11:15", "DBMS", "Room 204", "Prof. Meera", "#9C27B0")); // Purple
            events.add(new TimetableEvent("11:15 — 12:15", "Python Programming", "Lab 3", "Prof. Kumar", "#009688")); // Teal
            events.add(new TimetableEvent("12:15 — 01:00", "Lunch Break"));
            events.add(new TimetableEvent("01:00 — 02:00", "Computer Networks", "Room 202", "Prof. Joshi", "#FF9800")); // Orange
        } else {
            // Random alternative data for other days
            events.add(new TimetableEvent("08:00 — 09:00", "Operating Systems", "Room 301", "Prof. Verma", "#F44336")); 
            events.add(new TimetableEvent("09:00 — 10:00", "Computer Architecture", "Room 302", "Prof. Singh", "#4A90E2"));
            events.add(new TimetableEvent("10:00 — 10:15", "Short Break"));
            events.add(new TimetableEvent("10:15 — 12:15", "Software Engineering Lab", "Lab 1", "Prof. Gupta", "#E91E63"));
            events.add(new TimetableEvent("12:15 — 01:00", "Lunch Break"));
        }
        
        adapter.setEvents(events);
    }
}
