package com.procollegia;

import android.os.Bundle;
import android.view.View;

import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.procollegia.data.AttendancePagingSource;
import java.util.Map;

public class StudentAttendanceActivity extends AppCompatActivity {

    private RecyclerView rvAttendance;
    private TextView tvOverallPct, tvPresentCount, tvAbsentCount;
    private View ivBack;
    private View emptyState, loadingOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_attendance);

        rvAttendance   = findViewById(R.id.rv_attendance);
        tvOverallPct   = findViewById(R.id.tv_overall_pct);
        tvPresentCount  = findViewById(R.id.tv_present_count);
        tvAbsentCount   = findViewById(R.id.tv_absent_count);
        emptyState      = findViewById(R.id.layout_empty_state);
        loadingOverlay  = findViewById(R.id.loading_overlay);
        ivBack          = findViewById(R.id.iv_back);

        rvAttendance.setLayoutManager(new LinearLayoutManager(this));
        ivBack.setOnClickListener(v -> onBackPressed());
        BottomNavigationHelper.setup(this, R.id.nav_academics, "student");

        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid == null) return;

        if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);

        // Build a Pager using our cursor-based AttendancePagingSource.
        // Only 15 records are loaded at a time, scrolling loads more automatically.
        Pager<?, Map<String, Object>> pager = new Pager<>(
                new PagingConfig(/* pageSize= */ 15, /* prefetchDistance= */5, /* enablePlaceholders= */ false),
                () -> new AttendancePagingSource(FirebaseFirestore.getInstance(), uid)
        );

        LiveData<PagingData<Map<String, Object>>> pagingLiveData = PagingLiveData.getLiveData(pager);
        AttendanceRecordPagingAdapter adapter = new AttendanceRecordPagingAdapter();
        rvAttendance.setAdapter(adapter);

        pagingLiveData.observe(this, pagingData -> {
            if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
            adapter.submitData(getLifecycle(), pagingData);
        });
    }
}
