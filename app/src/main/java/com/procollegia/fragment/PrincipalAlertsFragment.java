package com.procollegia.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.procollegia.AlertsAdapter;
import com.procollegia.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PrincipalAlertsFragment extends Fragment {

    private RecyclerView rvAlerts;
    private View emptyState, loadingOverlay;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_student_alerts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvAlerts       = view.findViewById(R.id.rv_alerts);
        emptyState     = view.findViewById(R.id.layout_empty_state);
        loadingOverlay = view.findViewById(R.id.loading_overlay);
        View ivBack = view.findViewById(R.id.iv_back);
        if (ivBack != null) ivBack.setVisibility(View.GONE);
        rvAlerts.setLayoutManager(new LinearLayoutManager(requireContext()));
        if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance().collection("alerts")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50).get()
                .addOnSuccessListener(snap -> {
                    if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
                    List<Map<String, Object>> items = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) items.add(doc.getData());
                    if (items.isEmpty() && emptyState != null) emptyState.setVisibility(View.VISIBLE);
                    rvAlerts.setAdapter(new AlertsAdapter(items));
                })
                .addOnFailureListener(e -> { if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE); });
    }
}
