package com.procollegia;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import androidx.lifecycle.ViewModelProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.procollegia.viewmodel.PtDashboardViewModel;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.widget.ImageView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PtDashboardActivity extends AppCompatActivity {

    private TextView tvWelcomeName, tvPendingCount, tvActiveCount, tvOverdueCount;
    private View ivSettings, btnViewAll, btnGenerateReport;
    private LinearLayout llPendingContainer, llOverdueContainer;
    private TextView tvNoPending, tvNoOverdue;
    private PtDashboardViewModel viewModel;
    private FirebaseFirestore db;
    private SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
    private SimpleDateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pt_dashboard);

        db        = FirebaseFirestore.getInstance();
        viewModel = new ViewModelProvider(this).get(PtDashboardViewModel.class);
        initViews();
        setListeners();
        loadDashboardStats();
        loadWidgets();
        BottomNavigationHelper.setup(this, R.id.nav_dashboard, "pt");
    }

    private void initViews() {
        tvWelcomeName       = findViewById(R.id.tv_welcome_name);
        tvPendingCount      = findViewById(R.id.tv_pending_count);
        tvActiveCount       = findViewById(R.id.tv_active_count);
        tvOverdueCount      = findViewById(R.id.tv_overdue_count);
        ivSettings          = findViewById(R.id.iv_settings);
        btnViewAll          = findViewById(R.id.btn_view_all_items);
        btnGenerateReport   = findViewById(R.id.btn_generate_report);
        
        llPendingContainer  = findViewById(R.id.ll_pending_container);
        llOverdueContainer  = findViewById(R.id.ll_overdue_container);
        tvNoPending         = findViewById(R.id.tv_no_pending);
        tvNoOverdue         = findViewById(R.id.tv_no_overdue);
    }

    private void setListeners() {
        if (ivSettings != null)       ivSettings.setOnClickListener(v -> startActivity(new Intent(this, PtProfileActivity.class)));
        if (btnViewAll != null)       btnViewAll.setOnClickListener(v -> startActivity(new Intent(this, PtInventoryActivity.class)));
        if (btnGenerateReport != null) btnGenerateReport.setOnClickListener(v -> Toast.makeText(this, "Generating Report...", Toast.LENGTH_SHORT).show());
    }

    private void loadDashboardStats() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).get().addOnSuccessListener(doc -> {
                String name = doc.getString("name");
                if (name != null && tvWelcomeName != null) {
                    tvWelcomeName.setText("PT Dashboard");
                }
            });
        }

        viewModel.getPendingRequestsCount().observe(this, count -> {
            if (tvPendingCount != null) tvPendingCount.setText(String.valueOf(count));
        });
        viewModel.getActiveRequestsCount().observe(this, count -> {
            if (tvActiveCount != null) tvActiveCount.setText(String.valueOf(count));
        });
        viewModel.getOverdueRequestsCount().observe(this, count -> {
            if (tvOverdueCount != null) tvOverdueCount.setText(String.valueOf(count));
        });
    }

    private void loadWidgets() {
        // Load Pending Approvals (Limit 3)
        db.collection("equipment_requests")
                .whereEqualTo("status", "pending")
                .limit(3)
                .get()
                .addOnSuccessListener(snap -> {
                    llPendingContainer.removeAllViews();
                    if (snap.isEmpty()) {
                        llPendingContainer.addView(tvNoPending);
                        tvNoPending.setVisibility(View.VISIBLE);
                        return;
                    }
                    tvNoPending.setVisibility(View.GONE);
                    LayoutInflater inflater = LayoutInflater.from(this);
                    for (QueryDocumentSnapshot doc : snap) {
                        View row = inflater.inflate(R.layout.item_pending_approval, llPendingContainer, false);
                        
                        TextView tvAvatar    = row.findViewById(R.id.tv_avatar);
                        TextView tvName      = row.findViewById(R.id.tv_name);
                        TextView tvItem      = row.findViewById(R.id.tv_item);
                        TextView tvDate      = row.findViewById(R.id.tv_date);
                        ImageView btnAccept  = row.findViewById(R.id.btn_accept);
                        ImageView btnReject  = row.findViewById(R.id.btn_reject);

                        String studentName = doc.getString("studentName");
                        Long qty = doc.getLong("quantity");
                        String equipment = doc.getString("equipmentName");
                        String requestDateStr = doc.getString("requestDate");

                        if (studentName != null) {
                            tvName.setText(studentName);
                            tvAvatar.setText(studentName.substring(0, 1).toUpperCase());
                        }
                        tvItem.setText(equipment + (qty != null ? " x" + qty : ""));
                        
                        try {
                            if (requestDateStr != null) {
                                Date date = parseFormat.parse(requestDateStr);
                                tvDate.setText(sdf.format(date));
                            }
                        } catch (ParseException e) { tvDate.setText("Today"); }

                        btnAccept.setOnClickListener(v -> updateRequestStatus(doc.getId(), "approved", equipment, qty));
                        btnReject.setOnClickListener(v -> updateRequestStatus(doc.getId(), "rejected", equipment, 0L));

                        llPendingContainer.addView(row);
                    }
                });

        // Load Overdue Returns (Limit 3)
        String todayStr = parseFormat.format(new Date());
        db.collection("equipment_requests")
                .whereEqualTo("status", "approved")
                .whereLessThan("returnDate", todayStr)
                .limit(3)
                .get()
                .addOnSuccessListener(snap -> {
                    llOverdueContainer.removeAllViews();
                    if (snap.isEmpty()) {
                        llOverdueContainer.addView(tvNoOverdue);
                        tvNoOverdue.setVisibility(View.VISIBLE);
                        return;
                    }
                    tvNoOverdue.setVisibility(View.GONE);
                    LayoutInflater inflater = LayoutInflater.from(this);
                    Date today = new Date();
                    for (QueryDocumentSnapshot doc : snap) {
                        View row = inflater.inflate(R.layout.item_overdue_return, llOverdueContainer, false);
                        
                        TextView tvAvatar    = row.findViewById(R.id.tv_avatar);
                        TextView tvName      = row.findViewById(R.id.tv_name);
                        TextView tvItem      = row.findViewById(R.id.tv_item);
                        TextView tvDaysLate  = row.findViewById(R.id.tv_days_late);
                        TextView tvFine      = row.findViewById(R.id.tv_fine);

                        String studentName = doc.getString("studentName");
                        Long qty = doc.getLong("quantity");
                        String equipment = doc.getString("equipmentName");
                        String returnDateStr = doc.getString("returnDate");

                        if (studentName != null) {
                            tvName.setText(studentName);
                            tvAvatar.setText(studentName.substring(0, 1).toUpperCase());
                        }
                        tvItem.setText(equipment + (qty != null ? " x" + qty : ""));

                        long daysLate = 0;
                        try {
                            if (returnDateStr != null) {
                                Date returnDate = parseFormat.parse(returnDateStr);
                                long diff = today.getTime() - returnDate.getTime();
                                daysLate = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                            }
                        } catch (ParseException e) {}

                        tvDaysLate.setText(daysLate + (daysLate == 1 ? " day late" : " days late"));
                        long fine = daysLate * 10; // Rs 10 per day late
                        tvFine.setText("Fine: Rs." + fine);

                        llOverdueContainer.addView(row);
                    }
                });
    }

    private void updateRequestStatus(String docId, String status, String equipment, Long qtyAllocated) {
        db.collection("equipment_requests").document(docId).update("status", status)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Request " + status, Toast.LENGTH_SHORT).show();
                loadWidgets(); // refresh
            });
    }

    @Override
    public void onBackPressed() { moveTaskToBack(true); }
}

