package com.procollegia.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.procollegia.AttendanceRecordPagingAdapter;
import com.procollegia.R;
import com.procollegia.data.AttendancePagingSource;
import java.util.Map;

public class StudentAcademicsFragment extends Fragment {

    private RecyclerView rvAttendance;
    private TextView tvOverallPct, tvPresentCount, tvAbsentCount;
    private View emptyState, loadingOverlay;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_student_attendance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvAttendance   = view.findViewById(R.id.rv_attendance);
        tvOverallPct   = view.findViewById(R.id.tv_overall_pct);
        tvPresentCount = view.findViewById(R.id.tv_present_count);
        tvAbsentCount  = view.findViewById(R.id.tv_absent_count);
        emptyState     = view.findViewById(R.id.layout_empty_state);
        loadingOverlay = view.findViewById(R.id.loading_overlay);

        // Hide back button — this is a top-level tab
        View ivBack = view.findViewById(R.id.iv_back);
        if (ivBack != null) ivBack.setVisibility(View.GONE);

        rvAttendance.setLayoutManager(new LinearLayoutManager(requireContext()));

        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid == null) return;

        if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);

        Pager<?, Map<String, Object>> pager = new Pager<>(
                new PagingConfig(15, 5, false),
                () -> new AttendancePagingSource(FirebaseFirestore.getInstance(), uid)
        );

        LiveData<PagingData<Map<String, Object>>> pagingLiveData = PagingLiveData.getLiveData(pager);
        AttendanceRecordPagingAdapter adapter = new AttendanceRecordPagingAdapter();
        rvAttendance.setAdapter(adapter);

        pagingLiveData.observe(getViewLifecycleOwner(), pagingData -> {
            if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
            adapter.submitData(getViewLifecycleOwner().getLifecycle(), pagingData);
        });
    }
}
