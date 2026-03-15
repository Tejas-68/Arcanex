package com.procollegia.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.procollegia.R;
import com.procollegia.TeacherMarksAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeacherMarksFragment extends Fragment {

    private RecyclerView rvStudents;
    private View btnSubmitMarks, loadingOverlay;
    private TextView tvAvgIa1, tvAvgIa2, tvAvgAssign, tvSaveStatus, tvStudentCount;
    private List<Map<String, Object>> students = new ArrayList<>();
    private TeacherMarksAdapter adapter;
    private FirebaseFirestore db;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_teacher_edit_marks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        rvStudents     = view.findViewById(R.id.rv_students);
        btnSubmitMarks = view.findViewById(R.id.btn_submit_marks);
        loadingOverlay = view.findViewById(R.id.loading_overlay);
        tvAvgIa1       = view.findViewById(R.id.tv_avg_ia1);
        tvAvgIa2       = view.findViewById(R.id.tv_avg_ia2);
        tvAvgAssign    = view.findViewById(R.id.tv_avg_assign);
        tvSaveStatus   = view.findViewById(R.id.tv_save_status);
        tvStudentCount = view.findViewById(R.id.tv_student_count);

        adapter = new TeacherMarksAdapter(students, this::updateAverages);
        rvStudents.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvStudents.setAdapter(adapter);

        // Hide back button for tab screens
        View ivBack = view.findViewById(R.id.iv_back);
        if (ivBack != null) ivBack.setVisibility(View.GONE);

        if (btnSubmitMarks != null) btnSubmitMarks.setOnClickListener(v -> submitMarks());
        loadStudents();
    }

    private void loadStudents() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid == null) return;

        if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);
        db.collection("users").document(uid).get().addOnSuccessListener(teacherDoc -> {
            String dept = teacherDoc.getString("department");
            db.collection("users").whereEqualTo("role", "student").whereEqualTo("department", dept).get()
                    .addOnSuccessListener(snap -> {
                        if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
                        students.clear();
                        for (QueryDocumentSnapshot doc : snap) {
                            Map<String, Object> s = doc.getData();
                            s.put("uid", doc.getId());
                            students.add(s);
                        }
                        adapter.notifyDataSetChanged();
                        if (tvStudentCount != null)
                            tvStudentCount.setText(students.size() + " students loaded");
                        updateAverages();
                    });
        });
    }

    private void updateAverages() {
        double ia1 = 0, ia2 = 0, assign = 0;
        int count = 0;
        for (Map<String, Object> s : students) {
            Object v1 = s.get("ia1"), v2 = s.get("ia2"), v3 = s.get("assignment");
            if (v1 instanceof Number) ia1 += ((Number) v1).doubleValue();
            if (v2 instanceof Number) ia2 += ((Number) v2).doubleValue();
            if (v3 instanceof Number) assign += ((Number) v3).doubleValue();
            count++;
        }
        if (count > 0) {
            if (tvAvgIa1 != null) tvAvgIa1.setText(String.format("%.1f", ia1/count));
            if (tvAvgIa2 != null) tvAvgIa2.setText(String.format("%.1f", ia2/count));
            if (tvAvgAssign != null) tvAvgAssign.setText(String.format("%.1f", assign/count));
        }
    }

    private void submitMarks() {
        if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);
        WriteBatch batch = db.batch();
        for (Map<String, Object> s : students) {
            String studentId = (String) s.get("uid");
            if (studentId == null) continue;
            Map<String, Object> marks = new HashMap<>();
            marks.put("ia1", s.get("ia1"));
            marks.put("ia2", s.get("ia2"));
            marks.put("assignment", s.get("assignment"));
            batch.update(db.collection("users").document(studentId), marks);
        }
        batch.commit().addOnSuccessListener(v -> {
            if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
            if (tvSaveStatus != null) tvSaveStatus.setText("✓ All changes saved");
            Toast.makeText(requireContext(), "Marks saved!", Toast.LENGTH_SHORT).show();
        });
    }
}
