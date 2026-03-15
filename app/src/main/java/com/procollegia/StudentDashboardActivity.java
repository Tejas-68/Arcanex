package com.procollegia;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.procollegia.viewmodel.StudentDashboardViewModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StudentDashboardActivity extends AppCompatActivity {

    private TextView tvWelcomeName;
    private TextView tvNextClassName, tvNextClassTime;
    private TextView tvAttendancePct, tvHonorScore, tvHonorTier;
    private ProgressBar pbAttendance;
    private TextView tvTournamentInfo, tvAnnouncementText;
    private View cardHighPriority;
    private TextView tvAlertText;
    // Nav cards
    private View cardAttendance, cardHonor, cardTournament, cardAlerts, cardTodayClass;
    private View ivNotifications, ivProfile;

    private StudentDashboardViewModel viewModel;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        db        = FirebaseFirestore.getInstance();
        viewModel = new ViewModelProvider(this).get(StudentDashboardViewModel.class);

        bindViews();
        setClickListeners();
        BottomNavigationHelper.setup(this, R.id.nav_home, "student");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) loadAllData(user.getUid());
    }

    private void bindViews() {
        tvWelcomeName     = findViewById(R.id.tv_welcome_name);
        tvNextClassName   = findViewById(R.id.tv_next_class_name);
        tvNextClassTime   = findViewById(R.id.tv_next_class_time);
        tvAttendancePct   = findViewById(R.id.tv_attendance_pct);
        pbAttendance      = findViewById(R.id.pb_attendance);
        tvHonorScore      = findViewById(R.id.tv_honor_score);
        tvHonorTier       = findViewById(R.id.tv_honor_tier);
        tvTournamentInfo  = findViewById(R.id.tv_tournament_info);
        tvAnnouncementText= findViewById(R.id.tv_announcement_text);
        cardHighPriority  = findViewById(R.id.card_high_priority);
        tvAlertText       = findViewById(R.id.tv_alert_text);
        cardAttendance    = findViewById(R.id.card_attendance);
        cardHonor         = findViewById(R.id.card_honor);
        cardTournament    = findViewById(R.id.card_tournament);
        cardAlerts        = findViewById(R.id.card_alerts);
        cardTodayClass    = findViewById(R.id.card_today_class);
        ivNotifications   = findViewById(R.id.iv_notifications);
        ivProfile         = findViewById(R.id.iv_profile);
    }

    private void setClickListeners() {
        ivNotifications.setOnClickListener(v -> startActivity(new Intent(this, StudentAlertsActivity.class)));
        ivProfile.setOnClickListener(v -> startActivity(new Intent(this, StudentProfileActivity.class)));
        cardAttendance.setOnClickListener(v -> startActivity(new Intent(this, StudentAttendanceActivity.class)));
        cardHonor.setOnClickListener(v -> startActivity(new Intent(this, LeaderboardActivity.class)));
        cardTournament.setOnClickListener(v -> startActivity(new Intent(this, StudentSportsTournamentsActivity.class)));
        cardAlerts.setOnClickListener(v -> startActivity(new Intent(this, StudentAlertsActivity.class)));
        if (cardHighPriority != null)
            cardHighPriority.setOnClickListener(v -> startActivity(new Intent(this, StudentAlertsActivity.class)));
        if (cardTodayClass != null)
            cardTodayClass.setOnClickListener(v -> startActivity(new Intent(this, TimetableActivity.class)));
    }

    private void loadAllData(String uid) {
        // Greeting + name
        viewModel.getUserName(uid).observe(this, name -> {
            if (name != null && !name.equals("Error")) {
                String hour = new SimpleDateFormat("HH", Locale.getDefault()).format(new Date());
                int h = Integer.parseInt(hour);
                String greeting = h < 12 ? "Good Morning" : h < 17 ? "Good Afternoon" : "Good Evening";
                // Only first name
                String firstName = name.split(" ")[0];
                tvWelcomeName.setText(greeting + ", " + firstName);
            }
        });

        // Attendance %
        viewModel.getAttendancePercentage(uid).observe(this, pct -> {
            tvAttendancePct.setText(pct + "%");
            if (pbAttendance != null) pbAttendance.setProgress(pct);
        });

        // Honor score + tier
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            Long honor = doc.getLong("honorScore");
            if (honor != null) {
                tvHonorScore.setText(honor + " pts");
                if (tvHonorTier != null) {
                    String tier = honor >= 900 ? "Platinum Tier"
                            : honor >= 700 ? "Gold Tier"
                            : honor >= 500 ? "Silver Tier"
                            : "Bronze Tier";
                    tvHonorTier.setText(tier);
                }
            }
        });

        // Next timetable class (today's day)
        String today = new SimpleDateFormat("EEE", Locale.getDefault()).format(new Date());
        db.collection("timetable")
                .whereEqualTo("studentId", uid)
                .whereEqualTo("day", today)
                .orderBy("time")
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    for (QueryDocumentSnapshot doc : snap) {
                        String subject = doc.getString("subject");
                        String time    = doc.getString("time");
                        String room    = doc.getString("room");
                        if (tvNextClassName != null) tvNextClassName.setText(subject != null ? subject : "No class");
                        if (tvNextClassTime != null) tvNextClassTime.setText((time != null ? time : "") + (room != null ? " | " + room : ""));
                    }
                });

        // Upcoming tournament
        db.collection("tournaments")
                .whereEqualTo("status", "open")
                .orderBy("date")
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    for (QueryDocumentSnapshot doc : snap) {
                        String sport = doc.getString("sport");
                        String date  = doc.getString("date");
                        Long   slots = doc.getLong("slotsAvailable");
                        if (tvTournamentInfo != null) {
                            tvTournamentInfo.setText(
                                    (sport != null ? sport : "General") + " — " + (date != null ? date : "TBD")
                                    + "       " + (slots != null ? slots : "--") + " slots open"
                            );
                        }
                    }
                });

        // Latest announcement
        db.collection("announcements")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    for (QueryDocumentSnapshot doc : snap) {
                        String msg = doc.getString("message");
                        if (tvAnnouncementText != null && msg != null) tvAnnouncementText.setText(msg);
                    }
                });

        // High priority alerts
        db.collection("announcements")
                .whereEqualTo("priority", "high")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    for (QueryDocumentSnapshot doc : snap) {
                        String msg = doc.getString("message");
                        if (cardHighPriority != null && msg != null) {
                            cardHighPriority.setVisibility(View.VISIBLE);
                            if (tvAlertText != null) tvAlertText.setText(msg);
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
