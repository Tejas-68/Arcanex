package com.procollegia;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StudentAlertsActivity extends AppCompatActivity {

    private RecyclerView rvAlerts;
    private View emptyState, loadingOverlay;
    private ImageView ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_alerts);

        rvAlerts       = findViewById(R.id.rv_alerts);
        emptyState     = findViewById(R.id.layout_empty_state);
        loadingOverlay = findViewById(R.id.loading_overlay);
        ivBack         = findViewById(R.id.iv_back);

        rvAlerts.setLayoutManager(new LinearLayoutManager(this));
        ivBack.setOnClickListener(v -> onBackPressed());
        BottomNavigationHelper.setup(this, R.id.nav_alerts, "student");
        loadAlerts();
    }

    private void loadAlerts() {
        if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance().collection("alerts")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(snap -> {
                    if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
                    List<Map<String, Object>> items = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) items.add(doc.getData());
                    if (items.isEmpty() && emptyState != null) emptyState.setVisibility(View.VISIBLE);
                    rvAlerts.setAdapter(new AlertsAdapter(items));
                })
                .addOnFailureListener(e -> {
                    if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
                });
    }
}
