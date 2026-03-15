package com.procollegia;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.firebase.auth.FirebaseAuth;
import android.content.Intent;

public class StudentSportsTournamentsActivity extends AppCompatActivity {

    private RecyclerView rvTournaments;
    private View progressBar, layoutEmpty;
    private TextView tvCount;
    private final List<Map<String, Object>> tournaments = new ArrayList<>();
    private TournamentAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_sports_tournaments);

        db = FirebaseFirestore.getInstance();

        rvTournaments = findViewById(R.id.rv_tournaments);
        progressBar   = findViewById(R.id.progress_bar);
        layoutEmpty   = findViewById(R.id.layout_empty);
        tvCount       = findViewById(R.id.tv_count);

        ImageView ivBack = findViewById(R.id.iv_back);
        if (ivBack != null) ivBack.setOnClickListener(v -> onBackPressed());

        // Role-based Add button
        View btnAdd = findViewById(R.id.iv_add_tournament);
        if (btnAdd != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
            if (uid != null) {
                db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
                    if ("pt".equals(doc.getString("role"))) {
                        btnAdd.setVisibility(View.VISIBLE);
                        btnAdd.setOnClickListener(v -> startActivity(new Intent(this, PtCreateCompetitionActivity.class)));
                    } else {
                        btnAdd.setVisibility(View.GONE);
                    }
                });
            } else {
                btnAdd.setVisibility(View.GONE);
            }
        }

        adapter = new TournamentAdapter(tournaments);
        rvTournaments.setLayoutManager(new LinearLayoutManager(this));
        rvTournaments.setAdapter(adapter);

        // Filter chips
        View chipAll          = findViewById(R.id.chip_all);
        View chipUpcoming     = findViewById(R.id.chip_upcoming);
        View chipRegistration = findViewById(R.id.chip_registration);
        if (chipAll != null)          chipAll.setOnClickListener(v -> loadTournaments(null));
        if (chipUpcoming != null)     chipUpcoming.setOnClickListener(v -> loadTournaments("upcoming"));
        if (chipRegistration != null) chipRegistration.setOnClickListener(v -> loadTournaments("open"));

        BottomNavigationHelper.setup(this, R.id.nav_sports, "student");
        loadTournaments(null);
    }

    private void loadTournaments(String statusFilter) {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        tournaments.clear();
        adapter.notifyDataSetChanged();

        com.google.firebase.firestore.Query q = db.collection("tournaments");
        if (statusFilter != null) q = q.whereEqualTo("status", statusFilter);

        q.get().addOnSuccessListener(snap -> {
            progressBar.setVisibility(View.GONE);
            for (QueryDocumentSnapshot doc : snap) {
                Map<String, Object> t = new HashMap<>(doc.getData());
                t.put("id", doc.getId());
                tournaments.add(t);
            }
            adapter.notifyDataSetChanged();
            tvCount.setText(tournaments.size() + " Active");
            if (tournaments.isEmpty()) layoutEmpty.setVisibility(View.VISIBLE);
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            // Show empty state on error too
            layoutEmpty.setVisibility(View.VISIBLE);
        });
    }
}
