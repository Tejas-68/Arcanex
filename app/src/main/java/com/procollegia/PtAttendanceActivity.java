package com.procollegia;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import com.google.firebase.firestore.WriteBatch;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PtAttendanceActivity extends AppCompatActivity {

    private RecyclerView rvStudents;
    private TextInputEditText etSearch;
    private View btnSubmit, loadingOverlay;
    private ImageView ivBack;
    private FirebaseFirestore db;
    private List<Map<String, Object>> students = new ArrayList<>();
    private PtAttendanceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pt_attendance);

        db = FirebaseFirestore.getInstance();
        rvStudents     = findViewById(R.id.rv_students);
        etSearch       = findViewById(R.id.et_search);
        btnSubmit      = findViewById(R.id.btn_submit_attendance);
        loadingOverlay = findViewById(R.id.loading_overlay);
        ivBack         = findViewById(R.id.iv_back);

        adapter = new PtAttendanceAdapter(students);
        rvStudents.setLayoutManager(new LinearLayoutManager(this));
        rvStudents.setAdapter(adapter);

        ivBack.setOnClickListener(v -> onBackPressed());
        btnSubmit.setOnClickListener(v -> submitAttendance());
        etSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void onTextChanged(CharSequence s, int st, int b, int c) { adapter.filter(s.toString()); }
            public void afterTextChanged(Editable s) {}
        });

        loadStudents();
        BottomNavigationHelper.setup(this, R.id.nav_attendance, "pt");
    }

    private void loadStudents() {
        if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);
        db.collection("users").whereEqualTo("role", "student").get()
                .addOnSuccessListener(snap -> {
                    if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
                    students.clear();
                    for (QueryDocumentSnapshot doc : snap) {
                        Map<String, Object> s = new HashMap<>(doc.getData());
                        s.put("id", doc.getId());
                        s.put("attendanceStatus", "present");
                        students.add(s);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> { if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE); });
    }

    private void submitAttendance() {
        if (students.isEmpty()) return;
        if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        WriteBatch batch = db.batch();
        for (Map<String, Object> s : students) {
            String sid = (String) s.get("id");
            if (sid == null) continue;
            Map<String, Object> log = new HashMap<>();
            log.put("studentId", sid);
            log.put("date", today);
            log.put("status", s.get("attendanceStatus"));
            log.put("type", "sports");
            batch.set(db.collection("pt_attendance").document(sid + "_" + today), log);
        }
        batch.commit()
                .addOnSuccessListener(v2 -> {
                    if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, "Sports attendance submitted!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                });
    }
}
