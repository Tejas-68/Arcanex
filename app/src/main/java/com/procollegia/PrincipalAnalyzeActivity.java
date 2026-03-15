package com.procollegia;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.procollegia.viewmodel.PrincipalDashboardViewModel;

public class PrincipalAnalyzeActivity extends AppCompatActivity {

    private TextView tvTotalStudents, tvTotalStaff, tvAvgAttendance, tvPassPct;
    private PrincipalDashboardViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal_analyze);

        viewModel = new ViewModelProvider(this).get(PrincipalDashboardViewModel.class);

        tvTotalStudents = findViewById(R.id.tv_total_students);
        tvTotalStaff    = findViewById(R.id.tv_total_staff);
        tvAvgAttendance = findViewById(R.id.tv_avg_attendance);
        tvPassPct       = findViewById(R.id.tv_pass_pct);

        ImageView ivBack = findViewById(R.id.iv_back);
        if (ivBack != null) ivBack.setOnClickListener(v -> onBackPressed());

        BottomNavigationHelper.setup(this, R.id.nav_analyze, "principal");
        loadAnalytics();
    }

    private void loadAnalytics() {
        viewModel.getTotalStudents().observe(this, count -> {
            if (tvTotalStudents != null) tvTotalStudents.setText(String.valueOf(count));
        });
        viewModel.getTotalStaff().observe(this, count -> {
            if (tvTotalStaff != null) tvTotalStaff.setText(String.valueOf(count));
        });
        viewModel.getAvgAttendance().observe(this, pct -> {
            if (tvAvgAttendance != null) tvAvgAttendance.setText(pct + "%");
        });
        viewModel.getPassPercentage().observe(this, pct -> {
            if (tvPassPct != null) tvPassPct.setText(pct + "%");
        });
    }
}
