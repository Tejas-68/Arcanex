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
import java.util.List;
import java.util.Map;

public class PrincipalStaffsActivity extends AppCompatActivity {

    private RecyclerView rvStaff;
    private View progressBar;
    private TextView tvStaffCount;
    private FirebaseFirestore db;
    private final List<Map<String, Object>> staff = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal_staffs);

        db           = FirebaseFirestore.getInstance();
        rvStaff      = findViewById(R.id.rv_staff);
        progressBar  = findViewById(R.id.progress_bar);
        tvStaffCount = findViewById(R.id.tv_staff_count);

        ImageView ivBack = findViewById(R.id.iv_back);
        if (ivBack != null) ivBack.setOnClickListener(v -> onBackPressed());

        StaffAdapter adapter = new StaffAdapter(staff);
        rvStaff.setLayoutManager(new LinearLayoutManager(this));
        rvStaff.setAdapter(adapter);

        BottomNavigationHelper.setup(this, R.id.nav_staffs, "principal");
        loadStaff(adapter);
    }

    private void loadStaff(StaffAdapter adapter) {
        progressBar.setVisibility(View.VISIBLE);
        // Query all users whose role is teacher or pt
        db.collection("users")
                .whereIn("role", java.util.Arrays.asList("teacher", "pt"))
                .get()
                .addOnSuccessListener(snap -> {
                    progressBar.setVisibility(View.GONE);
                    staff.clear();
                    for (QueryDocumentSnapshot doc : snap) {
                        Map<String, Object> m = new HashMap<>(doc.getData());
                        m.put("uid", doc.getId());
                        staff.add(m);
                    }
                    adapter.notifyDataSetChanged();
                    tvStaffCount.setText(staff.size() + " staff members");
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    tvStaffCount.setText("Couldn't load staff");
                });
    }
}
