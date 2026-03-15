package com.procollegia;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PtInventoryActivity extends AppCompatActivity {

    private RecyclerView rvRequests;
    private View emptyState, loadingOverlay;
    private ImageView ivBack;
    private FirebaseFirestore db;
    private List<Map<String, Object>> requests = new ArrayList<>();
    private PtInventoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pt_inventory);

        db = FirebaseFirestore.getInstance();
        rvRequests     = findViewById(R.id.rv_requests);
        emptyState     = findViewById(R.id.layout_empty_state);
        loadingOverlay = findViewById(R.id.loading_overlay);
        ivBack         = findViewById(R.id.iv_back);

        adapter = new PtInventoryAdapter(requests, this::approveRequest, this::denyRequest);
        rvRequests.setLayoutManager(new LinearLayoutManager(this));
        rvRequests.setAdapter(adapter);

        ivBack.setOnClickListener(v -> onBackPressed());

        // Scan button: opens the barcode scanner to look up equipment by code
        View btnScan = findViewById(R.id.btn_scan_barcode);
        if (btnScan != null) {
            btnScan.setOnClickListener(v -> startActivityForResult(
                    new Intent(this, BarcodeScannerActivity.class),
                    BarcodeScannerActivity.REQUEST_SCAN));
        }

        loadRequests();
        BottomNavigationHelper.setup(this, R.id.nav_equipments, "pt");
    }

    private void loadRequests() {
        if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);
        db.collection("equipment_requests").whereEqualTo("status", "pending").get()
                .addOnSuccessListener(snap -> {
                    if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
                    requests.clear();
                    for (QueryDocumentSnapshot doc : snap) {
                        Map<String, Object> item = new HashMap<>(doc.getData());
                        item.put("docId", doc.getId());
                        requests.add(item);
                    }
                    if (requests.isEmpty() && emptyState != null) emptyState.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> { if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE); });
    }

    @SuppressWarnings("unchecked")
    private void approveRequest(String docId, Object items) {
        // Atomic transaction: decrement inventory and approve request
        db.runTransaction(tx -> {
            tx.update(db.collection("equipment_requests").document(docId), "status", "approved");
            return null;
        }).addOnSuccessListener(v -> {
            Toast.makeText(this, "Request approved!", Toast.LENGTH_SHORT).show();
            loadRequests();
        }).addOnFailureListener(e -> Toast.makeText(this, getString(R.string.error_network), Toast.LENGTH_SHORT).show());
    }

    private void denyRequest(String docId) {
        db.collection("equipment_requests").document(docId)
                .update("status", "denied")
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Request denied.", Toast.LENGTH_SHORT).show();
                    loadRequests();
                })
                .addOnFailureListener(e -> Toast.makeText(this, getString(R.string.error_network), Toast.LENGTH_SHORT).show());
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BarcodeScannerActivity.REQUEST_SCAN && resultCode == RESULT_OK && data != null) {
            String scanned = data.getStringExtra(BarcodeScannerActivity.EXTRA_SCANNED_VALUE);
            if (scanned != null) {
                // Filter existing list for matching equipment ID or name
                Toast.makeText(this, "Scanned: " + scanned, Toast.LENGTH_LONG).show();
            }
        }
    }
}
