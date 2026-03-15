package com.procollegia;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.procollegia.viewmodel.PrincipalDashboardViewModel;

public class PrincipalDashboardActivity extends AppCompatActivity {

    private TextView tvTotalStudents, tvTotalStaff;
    private View ivSettings, ivProfile;
    private View cardProfile, cardAnalytics, cardAlerts;
    private PrincipalDashboardViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal_dashboard);

        viewModel = new ViewModelProvider(this).get(PrincipalDashboardViewModel.class);
        initViews();
        setListeners();
        loadAnalytics();
        BottomNavigationHelper.setup(this, R.id.nav_home, "principal");
    }

    private void initViews() {
        tvTotalStudents = findViewById(R.id.tv_total_students);
        tvTotalStaff    = findViewById(R.id.tv_total_staff);
        ivSettings      = findViewById(R.id.iv_settings);
        ivProfile       = findViewById(R.id.iv_profile);
        cardProfile     = findViewById(R.id.card_profile);    // Staff Directory
        cardAnalytics   = findViewById(R.id.card_analytics);  // Analytics
        cardAlerts      = findViewById(R.id.card_alerts);     // Announcements
    }

    private void setListeners() {
        if (cardProfile != null)   cardProfile.setOnClickListener(v -> startActivity(new Intent(this, PrincipalStaffsActivity.class)));
        if (cardAnalytics != null) cardAnalytics.setOnClickListener(v -> startActivity(new Intent(this, PrincipalAnalyzeActivity.class)));
        if (cardAlerts != null)    cardAlerts.setOnClickListener(v -> startActivity(new Intent(this, StudentAlertsActivity.class)));
        if (ivProfile != null)     ivProfile.setOnClickListener(v -> startActivity(new Intent(this, PrincipalProfileActivity.class)));
        if (ivSettings != null)    ivSettings.setOnClickListener(v -> startActivity(new Intent(this, PrincipalProfileActivity.class)));
    }

    private void loadAnalytics() {
        viewModel.getTotalStudents().observe(this, count -> {
            if (tvTotalStudents != null) tvTotalStudents.setText(String.valueOf(count));
        });
        viewModel.getTotalStaff().observe(this, count -> {
            if (tvTotalStaff != null) tvTotalStaff.setText(String.valueOf(count));
        });
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
