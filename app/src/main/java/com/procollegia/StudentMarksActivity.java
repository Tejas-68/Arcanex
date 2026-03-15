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
import java.util.List;
import java.util.Map;

public class StudentMarksActivity extends AppCompatActivity {

    private RecyclerView rvMarks;
    private ImageView ivBack;
    private View emptyState, loadingOverlay;
    private TextView tvAvgMarks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_marks);

        rvMarks        = findViewById(R.id.rv_marks);
        ivBack         = findViewById(R.id.iv_back);
        emptyState     = findViewById(R.id.layout_empty_state);
        loadingOverlay = findViewById(R.id.loading_overlay);
        tvAvgMarks     = findViewById(R.id.tv_avg_marks);

        rvMarks.setLayoutManager(new LinearLayoutManager(this));
        ivBack.setOnClickListener(v -> onBackPressed());
        loadMarks();
    }

    private void loadMarks() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid == null) return;

        if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance().collection("internal_marks")
                .whereEqualTo("studentId", uid)
                .get()
                .addOnSuccessListener(snap -> {
                    if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
                    List<Map<String, Object>> items = new ArrayList<>();
                    long total = 0; int count = 0;
                    for (QueryDocumentSnapshot doc : snap) {
                        items.add(doc.getData());
                        Long marks = doc.getLong("marks");
                        if (marks != null) { total += marks; count++; }
                    }
                    if (items.isEmpty() && emptyState != null) emptyState.setVisibility(View.VISIBLE);
                    if (tvAvgMarks != null && count > 0) tvAvgMarks.setText(String.valueOf(total / count));
                    rvMarks.setAdapter(new MarksAdapter(items));
                })
                .addOnFailureListener(e -> {
                    if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
                });
    }
}
