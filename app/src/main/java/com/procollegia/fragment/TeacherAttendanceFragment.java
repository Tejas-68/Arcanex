package com.procollegia.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.procollegia.BarcodeScannerActivity;
import com.procollegia.R;
import com.procollegia.TeacherAttendanceAdapter;
import com.procollegia.viewmodel.TeacherAttendanceViewModel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TeacherAttendanceFragment extends Fragment {

    private RecyclerView rvStudents;
    private View loadingOverlay;
    private TeacherAttendanceViewModel viewModel;
    private List<Map<String, Object>> allStudents = new ArrayList<>();
    private TeacherAttendanceAdapter adapter;
    private TextView tvCountPresent, tvCountAbsent, tvCountLate, btnSelectAll, tvStudentCount;
    private EditText etSearch;
    private boolean isAllSelected = false;
    private FirebaseFirestore db;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_teacher_attendance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        viewModel = new ViewModelProvider(this).get(TeacherAttendanceViewModel.class);

        rvStudents     = view.findViewById(R.id.rv_students);
        loadingOverlay = view.findViewById(R.id.loading_overlay);
        tvCountPresent = view.findViewById(R.id.tv_count_present);
        tvCountAbsent  = view.findViewById(R.id.tv_count_absent);
        tvCountLate    = view.findViewById(R.id.tv_count_late);
        btnSelectAll   = view.findViewById(R.id.btn_select_all);
        etSearch       = view.findViewById(R.id.et_search_student);
        tvStudentCount = view.findViewById(R.id.tv_student_count);

        adapter = new TeacherAttendanceAdapter(allStudents, this::updateCounts);
        rvStudents.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvStudents.setAdapter(adapter);

        // No back button for tab fragments
        View ivBack = view.findViewById(R.id.iv_back);
        if (ivBack != null) ivBack.setVisibility(View.GONE);

        view.findViewById(R.id.btn_submit_attendance).setOnClickListener(v -> submitAttendance());

        View btnScan = view.findViewById(R.id.btn_scan_barcode);
        if (btnScan != null)
            btnScan.setOnClickListener(v -> startActivityForResult(
                    new Intent(requireActivity(), BarcodeScannerActivity.class),
                    BarcodeScannerActivity.REQUEST_SCAN));

        if (etSearch != null)
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
                @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) { adapter.filter(s.toString()); }
                @Override public void afterTextChanged(Editable s) {}
            });

        if (btnSelectAll != null)
            btnSelectAll.setOnClickListener(v -> {
                isAllSelected = !isAllSelected;
                for (Map<String, Object> s : allStudents) s.put("status", isAllSelected ? "present" : "absent");
                adapter.notifyDataSetChanged();
                updateCounts();
                btnSelectAll.setText(isAllSelected ? "Deselect All" : "Select All");
            });

        loadStudents();
    }

    private void loadStudents() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid == null) return;

        if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);
        db.collection("users").document(uid).get().addOnSuccessListener(teacherDoc -> {
            String section = teacherDoc.getString("section");
            db.collection("users").whereEqualTo("role", "student")
                    .whereEqualTo("section", section).get()
                    .addOnSuccessListener(snap -> {
                        if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
                        allStudents.clear();
                        for (com.google.firebase.firestore.QueryDocumentSnapshot doc : snap) {
                            Map<String, Object> s = doc.getData();
                            s.put("uid", doc.getId());
                            s.put("status", "absent");
                            allStudents.add(s);
                        }
                        adapter.notifyDataSetChanged();
                        if (tvStudentCount != null) tvStudentCount.setText(allStudents.size() + " students");
                        updateCounts();
                    });
        });
    }

    private void updateCounts() {
        long present = 0, absent = 0, late = 0;
        for (Map<String, Object> s : allStudents) {
            String st = (String) s.get("status");
            if ("present".equals(st)) present++;
            else if ("late".equals(st)) late++;
            else absent++;
        }
        if (tvCountPresent != null) tvCountPresent.setText(String.valueOf(present));
        if (tvCountAbsent  != null) tvCountAbsent.setText(String.valueOf(absent));
        if (tvCountLate    != null) tvCountLate.setText(String.valueOf(late));
    }

    private void submitAttendance() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid == null) return;
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        WriteBatch batch = db.batch();
        for (Map<String, Object> s : allStudents) {
            String studentId = (String) s.get("uid");
            if (studentId == null) continue;
            Map<String, Object> record = new HashMap<>();
            record.put("studentId", studentId);
            record.put("teacherId", uid);
            record.put("date", date);
            record.put("status", s.get("status"));
            batch.set(db.collection("attendance").document(date + "_" + studentId), record);
        }
        batch.commit().addOnSuccessListener(v ->
                Toast.makeText(requireContext(), "Attendance submitted!", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BarcodeScannerActivity.REQUEST_SCAN
                && resultCode == android.app.Activity.RESULT_OK && data != null) {
            // Use the correct constant EXTRA_SCANNED_VALUE
            String scannedId = data.getStringExtra(BarcodeScannerActivity.EXTRA_SCANNED_VALUE);
            if (scannedId != null) {
                for (Map<String, Object> s : allStudents) {
                    if (scannedId.equals(s.get("uid")) || scannedId.equals(s.get("rollNo"))) {
                        s.put("status", "present");
                        adapter.notifyDataSetChanged();
                        updateCounts();
                        break;
                    }
                }
            }
        }
    }
}
