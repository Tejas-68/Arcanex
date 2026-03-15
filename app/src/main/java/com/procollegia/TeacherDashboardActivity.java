package com.procollegia;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import androidx.lifecycle.ViewModelProvider;
import com.procollegia.viewmodel.TeacherDashboardViewModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TeacherDashboardActivity extends AppCompatActivity {

    private TextView tvWelcomeName, tvPendingLeaves, tvTodayClasses;
    private View ivSettings, ivProfile;
    private View cardAttendance, cardLeaves, cardMarks, cardAlerts, cardHonorScore;
    private TeacherDashboardViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        viewModel = new ViewModelProvider(this).get(TeacherDashboardViewModel.class);
        initViews();
        setListeners();
        loadUserData();
        BottomNavigationHelper.setup(this, R.id.nav_home, "teacher");
    }

    private void initViews() {
        tvWelcomeName   = findViewById(R.id.tv_welcome_name);
        tvPendingLeaves = findViewById(R.id.tv_pending_leaves);
        tvTodayClasses  = findViewById(R.id.tv_today_classes);
        ivSettings      = findViewById(R.id.iv_settings);
        ivProfile       = findViewById(R.id.iv_profile);
        cardAttendance  = findViewById(R.id.card_attendance);
        cardLeaves      = findViewById(R.id.card_leaves);
        cardMarks       = findViewById(R.id.card_marks);
        cardAlerts      = findViewById(R.id.card_alerts);
        cardHonorScore  = findViewById(R.id.card_honor_score);
    }

    private void setListeners() {
        if (ivProfile != null)    ivProfile.setOnClickListener(v -> startActivity(new Intent(this, TeacherProfileActivity.class)));
        if (ivSettings != null)   ivSettings.setOnClickListener(v -> startActivity(new Intent(this, TeacherProfileActivity.class)));
        if (cardAttendance != null) cardAttendance.setOnClickListener(v -> startActivity(new Intent(this, TeacherAttendanceActivity.class)));
        if (cardLeaves != null)   cardLeaves.setOnClickListener(v -> startActivity(new Intent(this, TeacherLeaveRequestsActivity.class)));
        if (cardMarks != null)    cardMarks.setOnClickListener(v -> startActivity(new Intent(this, TeacherEditMarksActivity.class)));
        if (cardAlerts != null)   cardAlerts.setOnClickListener(v -> startActivity(new Intent(this, StudentAlertsActivity.class)));
        if (cardHonorScore != null) cardHonorScore.setOnClickListener(v -> startActivity(new Intent(this, TeacherEditHonorScoreActivity.class)));
    }

    private void loadUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        viewModel.getUserName(user.getUid()).observe(this, name -> {
            if (name != null && !name.equals("Error") && tvWelcomeName != null) {
                String hour = new SimpleDateFormat("HH", Locale.getDefault()).format(new Date());
                int h = Integer.parseInt(hour);
                String greeting = h < 12 ? "Good Morning" : h < 17 ? "Good Afternoon" : "Good Evening";
                tvWelcomeName.setText(greeting + ", " + name.split(" ")[0]);
            }
        });

        viewModel.getPendingLeavesCount().observe(this, count -> {
            if (tvPendingLeaves != null) {
                tvPendingLeaves.setText(String.valueOf(count));
            }
        });
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
