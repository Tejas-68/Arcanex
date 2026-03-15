package com.procollegia;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.EditText;
import androidx.appcompat.widget.AppCompatButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class StudentLeaveRequestActivity extends AppCompatActivity {

    private EditText etFromDate, etToDate, etReason;
    private AppCompatButton btnSubmit;
    private ImageView ivBack;
    private View loadingOverlay;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_leave_request);

        db = FirebaseFirestore.getInstance();
        etFromDate     = findViewById(R.id.et_from_date);
        etToDate       = findViewById(R.id.et_to_date);
        etReason       = findViewById(R.id.et_reason);
        btnSubmit      = findViewById(R.id.btn_submit);
        ivBack         = findViewById(R.id.iv_back);
        loadingOverlay = findViewById(R.id.loading_overlay);

        ivBack.setOnClickListener(v -> onBackPressed());
        btnSubmit.setOnClickListener(v -> submitLeaveRequest());
    }

    private void submitLeaveRequest() {
        String from   = etFromDate.getText() != null ? etFromDate.getText().toString().trim() : "";
        String to     = etToDate.getText() != null ? etToDate.getText().toString().trim() : "";
        String reason = etReason.getText() != null ? etReason.getText().toString().trim() : "";

        if (TextUtils.isEmpty(from) || TextUtils.isEmpty(to) || TextUtils.isEmpty(reason)) {
            Toast.makeText(this, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid == null) return;

        if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        Map<String, Object> leave = new HashMap<>();
        leave.put("studentId", uid);
        leave.put("fromDate", from);
        leave.put("toDate", to);
        leave.put("reason", reason);
        leave.put("status", "pending");
        leave.put("submittedAt", new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date()));

        db.collection("leave_requests").add(leave)
                .addOnSuccessListener(ref -> {
                    if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, "Leave request submitted!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                });
    }
}
