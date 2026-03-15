package com.procollegia.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.procollegia.LeaveRequestAdapter;
import com.procollegia.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TeacherLeavesFragment extends Fragment {

    private RecyclerView rvRequests;
    private View emptyState, loadingOverlay;
    private FirebaseFirestore db;
    private List<Map<String, Object>> requests = new ArrayList<>();
    private LeaveRequestAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_teacher_leave_requests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        rvRequests     = view.findViewById(R.id.rv_requests);
        emptyState     = view.findViewById(R.id.layout_empty_state);
        loadingOverlay = view.findViewById(R.id.loading_overlay);

        // Hide back button for tab screens
        View ivBack = view.findViewById(R.id.iv_back);
        if (ivBack != null) ivBack.setVisibility(View.GONE);

        adapter = new LeaveRequestAdapter(requests, this::updateStatus);
        rvRequests.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRequests.setAdapter(adapter);
        loadRequests();
    }

    private void loadRequests() {
        if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);
        db.collection("leave_requests").whereEqualTo("status", "pending").get()
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
        db.collection("leave_requests").document(docId).update("status", status)
                .addOnSuccessListener(v -> {
                    Toast.makeText(requireContext(), "Request " + status + ".", Toast.LENGTH_SHORT).show();
                    loadRequests();
                });
    }
}
