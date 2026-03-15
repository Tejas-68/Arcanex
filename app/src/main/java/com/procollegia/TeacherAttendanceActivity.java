package com.procollegia;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.procollegia.viewmodel.TeacherAttendanceViewModel;
import com.procollegia.data.StudentEntity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TeacherAttendanceActivity extends AppCompatActivity {

    private RecyclerView rvStudents;
    private View loadingOverlay;
    private FirebaseFirestore db;
    private TeacherAttendanceViewModel viewModel;
    private List<Map<String, Object>> allStudents = new ArrayList<>();
    private TeacherAttendanceAdapter adapter;
    
    // Bottom Bar Counts
    private TextView tvCountPresent, tvCountAbsent, tvCountLate, btnSelectAll, tvStudentCount;
    private EditText etSearch;
    private boolean isAllSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_attendance);

        db             = FirebaseFirestore.getInstance();
        viewModel      = new ViewModelProvider(this).get(TeacherAttendanceViewModel.class);
        
        rvStudents     = findViewById(R.id.rv_students);
        loadingOverlay = findViewById(R.id.loading_overlay);
        tvCountPresent = findViewById(R.id.tv_count_present);
        tvCountAbsent  = findViewById(R.id.tv_count_absent);
        tvCountLate    = findViewById(R.id.tv_count_late);
        btnSelectAll   = findViewById(R.id.btn_select_all);
        etSearch       = findViewById(R.id.et_search_student);
        tvStudentCount = findViewById(R.id.tv_student_count);

        adapter = new TeacherAttendanceAdapter(allStudents, this::updateCounts);
        rvStudents.setLayoutManager(new LinearLayoutManager(this));
        rvStudents.setAdapter(adapter);

        findViewById(R.id.iv_back).setOnClickListener(v -> onBackPressed());
        findViewById(R.id.btn_submit_attendance).setOnClickListener(v -> submitAttendance());
        BottomNavigationHelper.setup(this, R.id.nav_attendance, "teacher");

        // Scan button: open barcode scanner
        View btnScan = findViewById(R.id.btn_scan_barcode);
        if (btnScan != null) {
            btnScan.setOnClickListener(v -> startActivityForResult(
                    new Intent(this, BarcodeScannerActivity.class),
                    BarcodeScannerActivity.REQUEST_SCAN));
        }

        // Search Filter
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Select All behavior
        btnSelectAll.setOnClickListener(v -> {
            isAllSelected = !isAllSelected;
            btnSelectAll.setText(isAllSelected ? "Deselect All" : "Select All");
            adapter.selectAll(isAllSelected);
        });

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
                    if (dept != null) {
                        viewModel.getStudentsByDepartment(dept).observe(this, studentEntities -> {
                            if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
                            allStudents.clear();
                            for (StudentEntity entity : studentEntities) {
                                Map<String, Object> s = new HashMap<>();
                                s.put("id", entity.id);
                                s.put("name", entity.name);
                                s.put("rollNumber", entity.rollNumber);
                                s.put("course", entity.course);
                                s.put("batch", entity.batch);
                                s.put("attendanceStatus", "present"); // Default present
                                allStudents.add(s);
                            }
                            isAllSelected = true;
                            btnSelectAll.setText("Deselect All");
                            tvStudentCount.setVisibility(View.GONE);
                            adapter.filter(""); // Resets filter & updates list
                            updateCounts();
                        });
                    } else {
                        if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
                });
    }

    private void updateCounts() {
        int present = 0, absent = 0, late = 0;
        for (Map<String, Object> s : allStudents) {
            String status = (String) s.get("attendanceStatus");
            if ("present".equals(status)) present++;
            else if ("absent".equals(status)) absent++;
            else if ("late".equals(status)) late++;
        }
        tvCountPresent.setText(String.valueOf(present));
        tvCountAbsent.setText(String.valueOf(absent));
        tvCountLate.setText(String.valueOf(late));
    }

    private void submitAttendance() {
        if (allStudents.isEmpty()) return;
        if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);
        
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        WriteBatch batch = db.batch();
        
        for (Map<String, Object> s : allStudents) {
            String sid = (String) s.get("id");
            if (sid == null) continue;
            Map<String, Object> log = new HashMap<>();
            log.put("studentId", sid);
            log.put("date", today);
            log.put("status", s.get("attendanceStatus"));
            batch.set(db.collection("attendance_logs").document(), log);
        }
        
        batch.commit()
                .addOnSuccessListener(v2 -> {
                    if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
                    Toast.makeText(this, "Attendance submitted successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
                    Toast.makeText(this, "Network error: Unable to submit", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BarcodeScannerActivity.REQUEST_SCAN && resultCode == RESULT_OK && data != null) {
            String scannedId = data.getStringExtra(BarcodeScannerActivity.EXTRA_SCANNED_VALUE);
            if (scannedId != null) {
                for (Map<String, Object> s : allStudents) {
                    if (scannedId.equals(s.get("id")) || scannedId.equals(s.get("rollNumber"))) {
                        s.put("attendanceStatus", "present");
                        adapter.notifyDataSetChanged();
                        updateCounts();
                        Toast.makeText(this, "Marked present: " + s.get("name"), Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        }
    }
}
