package com.procollegia;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TeacherLeaveRequestsActivity extends AppCompatActivity {

    private RecyclerView rvRequests;
    private View emptyState, loadingOverlay;
    private ImageView ivBack;
    private FirebaseFirestore db;
    private List<Map<String, Object>> requests = new ArrayList<>();
    private LeaveRequestAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_leave_requests);

        db = FirebaseFirestore.getInstance();
        rvRequests     = findViewById(R.id.rv_requests);
        emptyState     = findViewById(R.id.layout_empty_state);
        loadingOverlay = findViewById(R.id.loading_overlay);
        ivBack         = findViewById(R.id.iv_back);

        adapter = new LeaveRequestAdapter(requests, (docId, status) -> updateStatus(docId, status));
        rvRequests.setLayoutManager(new LinearLayoutManager(this));
        rvRequests.setAdapter(adapter);
        ivBack.setOnClickListener(v -> onBackPressed());
        BottomNavigationHelper.setup(this, R.id.nav_leaves, "teacher");
        loadRequests();
    }

    private void loadRequests() {
        if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);
        db.collection("leave_requests").whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(snap -> {
                    if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
                    requests.clear();
                    for (QueryDocumentSnapshot doc : snap) {
                        Map<String, Object> item = doc.getData();
                        item.put("docId", doc.getId());
                        requests.add(item);
                    }
                    if (requests.isEmpty() && emptyState != null) emptyState.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> { if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE); });
    }

    private void updateStatus(String docId, String status) {
        db.collection("leave_requests").document(docId)
                .update("status", status)
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Request " + status + ".", Toast.LENGTH_SHORT).show();
                    loadRequests();
                })
                .addOnFailureListener(e -> Toast.makeText(this, getString(R.string.error_network), Toast.LENGTH_SHORT).show());
    }
}
