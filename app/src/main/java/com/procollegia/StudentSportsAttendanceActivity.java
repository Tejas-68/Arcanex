package com.procollegia;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentSportsAttendanceActivity extends AppCompatActivity {

    private RecyclerView rvLog;
    private View progressBar, layoutEmpty;
    private TextView tvPct;
    private FirebaseFirestore db;
    private final List<Map<String, Object>> logs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_sports_attendance);

        db          = FirebaseFirestore.getInstance();
        rvLog       = findViewById(R.id.rv_attendance_log);
        progressBar = findViewById(R.id.progress_bar);
        layoutEmpty = findViewById(R.id.layout_empty);
        tvPct       = findViewById(R.id.tv_pct);

        ImageView ivBack = findViewById(R.id.iv_back);
        if (ivBack != null) ivBack.setOnClickListener(v -> onBackPressed());

        // Simple text adapter
        AttendanceRecordAdapter adapter = new AttendanceRecordAdapter(logs);
        rvLog.setLayoutManager(new LinearLayoutManager(this));
        rvLog.setAdapter(adapter);

        BottomNavigationHelper.setup(this, R.id.nav_sports, "student");
        loadSportsAttendance(adapter);
    }

    private void loadSportsAttendance(AttendanceRecordAdapter adapter) {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid == null) { layoutEmpty.setVisibility(View.VISIBLE); return; }

        progressBar.setVisibility(View.VISIBLE);

        db.collection("sports_attendance")
                .whereEqualTo("studentId", uid)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    progressBar.setVisibility(View.GONE);
                    logs.clear();
                    int present = 0;
                    for (QueryDocumentSnapshot doc : snap) {
                        Map<String, Object> rec = new HashMap<>(doc.getData());
                        logs.add(rec);
                        if ("present".equals(rec.get("status"))) present++;
                    }
                    adapter.notifyDataSetChanged();
                    if (logs.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                        tvPct.setText("N/A");
                    } else {
                        int pct = (int) Math.round((present * 100.0) / logs.size());
                        tvPct.setText(pct + "%");
                        tvPct.setTextColor(getColor(pct >= 75 ? R.color.accent_green : R.color.accent_red));
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                });
    }
}
