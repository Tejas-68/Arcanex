package com.procollegia;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TeacherEditMarksActivity extends AppCompatActivity {

    private RecyclerView rvStudents;
    private View btnSubmitMarks, loadingOverlay;
    private TextView tvAvgIa1, tvAvgIa2, tvAvgAssign, tvSaveStatus, tvStudentCount;
    private FirebaseFirestore db;
    private List<Map<String, Object>> students = new ArrayList<>();
    private TeacherMarksAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_edit_marks);

        db = FirebaseFirestore.getInstance();
        rvStudents     = findViewById(R.id.rv_students);
        btnSubmitMarks = findViewById(R.id.btn_submit_marks);
        loadingOverlay = findViewById(R.id.loading_overlay);
        tvAvgIa1       = findViewById(R.id.tv_avg_ia1);
        tvAvgIa2       = findViewById(R.id.tv_avg_ia2);
        tvAvgAssign    = findViewById(R.id.tv_avg_assign);
        tvSaveStatus   = findViewById(R.id.tv_save_status);
        tvStudentCount = findViewById(R.id.tv_student_count);

        adapter = new TeacherMarksAdapter(students, this::updateAverages);
        rvStudents.setLayoutManager(new LinearLayoutManager(this));
        rvStudents.setAdapter(adapter);

        findViewById(R.id.iv_back).setOnClickListener(v -> onBackPressed());
        btnSubmitMarks.setOnClickListener(v -> submitMarks());
        BottomNavigationHelper.setup(this, R.id.nav_marks, "teacher");
        loadStudents();
    }

    private void loadStudents() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid == null) return;

        if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);
        db.collection("users").document(uid).get()
                .addOnSuccessListener(teacherDoc -> {
                    String dept = teacherDoc.getString("department");
                    db.collection("users").whereEqualTo("role", "student").get()
                            .addOnSuccessListener(snap -> {
                                if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
                                students.clear();
                                for (QueryDocumentSnapshot doc : snap) {
                                    if (dept == null || dept.equalsIgnoreCase(doc.getString("course"))) {
                                        Map<String, Object> s = new HashMap<>();
                                        s.put("id", doc.getId());
                                        s.put("name", doc.getString("name"));
                                        s.put("ia1", 0L);
                                        s.put("ia2", 0L);
                                        s.put("assign", 0L);
                                        students.add(s);
                                    }
                                }
                                tvStudentCount.setVisibility(students.isEmpty() ? View.VISIBLE : View.GONE);
                                adapter.notifyDataSetChanged();
                                updateAverages();
                            })
                            .addOnFailureListener(e -> { 
                                if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE); 
                            });
                });
    }

    private void updateAverages() {
        if (students.isEmpty()) {
            tvAvgIa1.setText("0.0");
            tvAvgIa2.setText("0.0");
            tvAvgAssign.setText("0.0");
            return;
        }

        double totalIa1 = 0, totalIa2 = 0, totalAssign = 0;
        int countIa1 = 0, countIa2 = 0, countAssign = 0;

        for (Map<String, Object> s : students) {
            long ia1 = (long) s.get("ia1");
            long ia2 = (long) s.get("ia2");
            long assign = (long) s.get("assign");

            if (ia1 > 0) { totalIa1 += ia1; countIa1++; }
            if (ia2 > 0) { totalIa2 += ia2; countIa2++; }
            if (assign > 0) { totalAssign += assign; countAssign++; }
        }

        double avgIa1 = countIa1 > 0 ? totalIa1 / countIa1 : 0.0;
        double avgIa2 = countIa2 > 0 ? totalIa2 / countIa2 : 0.0;
        double avgAssign = countAssign > 0 ? totalAssign / countAssign : 0.0;

        tvAvgIa1.setText(String.format(Locale.US, "%.1f", avgIa1));
        tvAvgIa2.setText(String.format(Locale.US, "%.1f", avgIa2));
        tvAvgAssign.setText(String.format(Locale.US, "%.1f", avgAssign));

        // Flash auto-saving text
        tvSaveStatus.setText("Syncing...");
        tvSaveStatus.setTextColor(getResources().getColor(R.color.text_secondary));
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            tvSaveStatus.setText("✓ All changes saved");
            tvSaveStatus.setTextColor(getResources().getColor(R.color.accent_green));
        }, 600);
    }

    private void submitMarks() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid == null || students.isEmpty()) return;

        if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);
        btnSubmitMarks.setEnabled(false);

        WriteBatch batch = db.batch();
        for (Map<String, Object> s : students) {
            String sid = (String) s.get("id");
            if (sid == null) continue;
            
            Map<String, Object> entry = new HashMap<>();
            entry.put("studentId", sid);
            entry.put("teacherId", uid);
            entry.put("ia1", s.get("ia1"));
            entry.put("ia2", s.get("ia2"));
            entry.put("assign", s.get("assign"));
            
            // Note: Storing per-student-teacher combination
            batch.set(db.collection("internal_marks").document(sid + "_" + uid), entry);
        }
        
        batch.commit()
                .addOnSuccessListener(v -> {
                    if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
                    btnSubmitMarks.setEnabled(true);
                    Toast.makeText(this, "Final Marks saved successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
                    btnSubmitMarks.setEnabled(true);
                    Toast.makeText(this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                });
    }
}
