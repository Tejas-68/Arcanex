package com.procollegia.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.procollegia.LeaderboardActivity;
import com.procollegia.R;
import com.procollegia.StudentAttendanceActivity;
import com.procollegia.StudentAlertsActivity;
import com.procollegia.StudentSportsTournamentsActivity;
import com.procollegia.TimetableActivity;
import com.procollegia.viewmodel.StudentDashboardViewModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StudentHomeFragment extends Fragment {

    private TextView tvWelcomeName, tvNextClassName, tvNextClassTime;
    private TextView tvAttendancePct, tvHonorScore, tvHonorTier;
    private ProgressBar pbAttendance;
    private TextView tvTournamentInfo, tvAnnouncementText, tvAlertText;
    private View cardHighPriority, cardAttendance, cardHonor, cardTournament, cardAlerts, cardTodayClass;
    private View ivNotifications, ivProfile;
    private StudentDashboardViewModel viewModel;
    private FirebaseFirestore db;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_student_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        viewModel = new ViewModelProvider(requireActivity()).get(StudentDashboardViewModel.class);

        tvWelcomeName     = view.findViewById(R.id.tv_welcome_name);
        tvNextClassName   = view.findViewById(R.id.tv_next_class_name);
        tvNextClassTime   = view.findViewById(R.id.tv_next_class_time);
        tvAttendancePct   = view.findViewById(R.id.tv_attendance_pct);
        pbAttendance      = view.findViewById(R.id.pb_attendance);
        tvHonorScore      = view.findViewById(R.id.tv_honor_score);
        tvHonorTier       = view.findViewById(R.id.tv_honor_tier);
        tvTournamentInfo  = view.findViewById(R.id.tv_tournament_info);
        tvAnnouncementText= view.findViewById(R.id.tv_announcement_text);
        cardHighPriority  = view.findViewById(R.id.card_high_priority);
        tvAlertText       = view.findViewById(R.id.tv_alert_text);
        cardAttendance    = view.findViewById(R.id.card_attendance);
        cardHonor         = view.findViewById(R.id.card_honor);
        cardTournament    = view.findViewById(R.id.card_tournament);
        cardAlerts        = view.findViewById(R.id.card_alerts);
        cardTodayClass    = view.findViewById(R.id.card_today_class);
        ivNotifications   = view.findViewById(R.id.iv_notifications);
        ivProfile         = view.findViewById(R.id.iv_profile);

        if (ivNotifications != null)
            ivNotifications.setOnClickListener(v -> startActivity(new Intent(requireActivity(), StudentAlertsActivity.class)));
        if (ivProfile != null)
            ivProfile.setOnClickListener(v -> requireActivity().getSupportFragmentManager()
                    .beginTransaction().replace(R.id.fragment_container, new StudentProfileFragment()).commitAllowingStateLoss());
        if (cardAttendance != null)
            cardAttendance.setOnClickListener(v -> requireActivity().getSupportFragmentManager()
                    .beginTransaction().replace(R.id.fragment_container, new StudentAcademicsFragment()).commitAllowingStateLoss());
        if (cardHonor != null)
            cardHonor.setOnClickListener(v -> startActivity(new Intent(requireActivity(), LeaderboardActivity.class)));
        if (cardTournament != null)
            cardTournament.setOnClickListener(v -> startActivity(new Intent(requireActivity(), StudentSportsTournamentsActivity.class)));
        if (cardAlerts != null)
            cardAlerts.setOnClickListener(v -> requireActivity().getSupportFragmentManager()
                    .beginTransaction().replace(R.id.fragment_container, new StudentAlertsFragment()).commitAllowingStateLoss());
        if (cardHighPriority != null)
            cardHighPriority.setOnClickListener(v -> requireActivity().getSupportFragmentManager()
                    .beginTransaction().replace(R.id.fragment_container, new StudentAlertsFragment()).commitAllowingStateLoss());
        if (cardTodayClass != null)
            cardTodayClass.setOnClickListener(v -> startActivity(new Intent(requireActivity(), TimetableActivity.class)));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) loadAllData(user.getUid());
    }

    private void loadAllData(String uid) {
        viewModel.getUserName(uid).observe(getViewLifecycleOwner(), name -> {
            if (name != null && !name.equals("Error")) {
                String hour = new SimpleDateFormat("HH", Locale.getDefault()).format(new Date());
                int h = Integer.parseInt(hour);
                String greeting = h < 12 ? "Good Morning" : h < 17 ? "Good Afternoon" : "Good Evening";
                String firstName = name.split(" ")[0];
                if (tvWelcomeName != null) tvWelcomeName.setText(greeting + ", " + firstName);
            }
        });
        viewModel.getAttendancePercentage(uid).observe(getViewLifecycleOwner(), pct -> {
            if (tvAttendancePct != null) tvAttendancePct.setText(pct + "%");
            if (pbAttendance != null) pbAttendance.setProgress(pct);
        });
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            Long honor = doc.getLong("honorScore");
            if (honor != null) {
                if (tvHonorScore != null) tvHonorScore.setText(honor + " pts");
                if (tvHonorTier != null) {
                    String tier = honor >= 900 ? "Platinum Tier" : honor >= 700 ? "Gold Tier"
                            : honor >= 500 ? "Silver Tier" : "Bronze Tier";
                    tvHonorTier.setText(tier);
                }
            }
        });
        String today = new SimpleDateFormat("EEE", Locale.getDefault()).format(new Date());
        db.collection("timetable").whereEqualTo("studentId", uid).whereEqualTo("day", today)
                .orderBy("time").limit(1).get().addOnSuccessListener(snap -> {
                    for (QueryDocumentSnapshot doc : snap) {
                        String subject = doc.getString("subject"), time = doc.getString("time"), room = doc.getString("room");
                        if (tvNextClassName != null) tvNextClassName.setText(subject != null ? subject : "No class");
                        if (tvNextClassTime != null) tvNextClassTime.setText((time != null ? time : "") + (room != null ? " | " + room : ""));
                    }
                });
        db.collection("tournaments").whereEqualTo("status", "open").orderBy("date").limit(1).get()
                .addOnSuccessListener(snap -> {
                    for (QueryDocumentSnapshot doc : snap) {
                        String sport = doc.getString("sport"), date = doc.getString("date");
                        Long slots = doc.getLong("slotsAvailable");
                        if (tvTournamentInfo != null)
                            tvTournamentInfo.setText((sport != null ? sport : "General") + " — " + (date != null ? date : "TBD")
                                    + "       " + (slots != null ? slots : "--") + " slots open");
                    }
                });
        db.collection("announcements").orderBy("createdAt", Query.Direction.DESCENDING).limit(1).get()
                .addOnSuccessListener(snap -> {
                    for (QueryDocumentSnapshot doc : snap) {
                        String msg = doc.getString("message");
                        if (tvAnnouncementText != null && msg != null) tvAnnouncementText.setText(msg);
                    }
                });
        db.collection("announcements").whereEqualTo("priority", "high")
                .orderBy("createdAt", Query.Direction.DESCENDING).limit(1).get()
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
}
