package com.procollegia;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private TextView tvHonorScore, tvHonorTier;
    private ProgressBar pbHonorGauge;
    private RecyclerView rvHonorEvents;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        db = FirebaseFirestore.getInstance();
        tvHonorScore = findViewById(R.id.tv_honor_score);
        tvHonorTier = findViewById(R.id.tv_honor_tier);
        pbHonorGauge = findViewById(R.id.pb_honor_gauge);
        rvHonorEvents = findViewById(R.id.rv_honor_events);

        View ivBack = findViewById(R.id.iv_back);
        if (ivBack != null) ivBack.setOnClickListener(v -> onBackPressed());

        // Setup mock recent events
        List<HonorEvent> events = new ArrayList<>();
        events.add(new HonorEvent(10, "Good Conduct", "Feb 20"));
        events.add(new HonorEvent(15, "Sports Win", "Feb 15"));
        events.add(new HonorEvent(-5, "Late Submission", "Feb 10"));
        events.add(new HonorEvent(10, "Academic Achievement", "Feb 5"));

        HonorEventAdapter adapter = new HonorEventAdapter(events);
        rvHonorEvents.setAdapter(adapter);

        loadUserData();
        
        // Determine role to set correct nav
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid != null) {
            db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
                String role = doc.getString("role");
                if ("teacher".equals(role)) BottomNavigationHelper.setup(this, -1, "teacher");
                else BottomNavigationHelper.setup(this, -1, "student");
            });
        }
    }

    private void loadUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).get().addOnSuccessListener(doc -> {
                Long honorScore = doc.getLong("honorScore");
                if (honorScore != null) {
                    tvHonorScore.setText(String.valueOf(honorScore));
                    pbHonorGauge.setProgress(Math.min(1000, honorScore.intValue()));
                    
                    String tier;
                    if (honorScore >= 900) tier = "★ PLATINUM TIER";
                    else if (honorScore >= 700) tier = "⭐ GOLD TIER";
                    else if (honorScore >= 500) tier = "🥈 SILVER TIER";
                    else tier = "🥉 BRONZE TIER";
                    
                    tvHonorTier.setText(tier);
                }
            });
        }
    }
}
