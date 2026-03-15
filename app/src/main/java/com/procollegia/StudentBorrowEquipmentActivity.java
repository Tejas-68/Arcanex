package com.procollegia;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StudentBorrowEquipmentActivity extends AppCompatActivity {

    private Spinner spinnerEquipment;
    private TextView tvQty;
    private EditText etPurpose;
    private Button btnPickDate, btnSubmit;
    private int quantity = 1;
    private String returnDate = null;
    private FirebaseFirestore db;

    private static final List<String> EQUIPMENT_LIST = Arrays.asList(
            "Cricket Bat", "Cricket Ball", "Football", "Volleyball",
            "Basketball", "Badminton Racket", "Table Tennis Bat",
            "Hockey Stick", "Athletics Shoes", "Stopwatch"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_borrow_equipment);

        db = FirebaseFirestore.getInstance();

        ImageView ivBack = findViewById(R.id.iv_back);
        spinnerEquipment = findViewById(R.id.spinner_equipment);
        tvQty            = findViewById(R.id.tv_qty);
        etPurpose        = findViewById(R.id.et_purpose);
        btnPickDate      = findViewById(R.id.btn_pick_date);
        btnSubmit        = findViewById(R.id.btn_submit_request);
        Button btnMinus  = findViewById(R.id.btn_minus);
        Button btnPlus   = findViewById(R.id.btn_plus);

        if (ivBack != null) ivBack.setOnClickListener(v -> onBackPressed());

        // Populate equipment spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, EQUIPMENT_LIST);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEquipment.setAdapter(adapter);

        // Quantity stepper
        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) { quantity--; tvQty.setText(String.valueOf(quantity)); }
        });
        btnPlus.setOnClickListener(v -> {
            if (quantity < 10) { quantity++; tvQty.setText(String.valueOf(quantity)); }
        });

        // Date picker
        btnPickDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_MONTH, 1);
            new DatePickerDialog(this, (view, year, month, day) -> {
                returnDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
                String display = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        .format(new Date(year - 1900, month, day));
                btnPickDate.setText("📅 " + display);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        BottomNavigationHelper.setup(this, R.id.nav_sports, "student");
        btnSubmit.setOnClickListener(v -> submitRequest());
    }

    private void submitRequest() {
        String equipment = spinnerEquipment.getSelectedItem() != null
                ? spinnerEquipment.getSelectedItem().toString() : "";
        String purpose = etPurpose.getText().toString().trim();

        if (purpose.isEmpty()) {
            etPurpose.setError("Please describe the purpose");
            return;
        }
        if (returnDate == null) {
            Toast.makeText(this, "Please select a return date", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid == null) return;

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Submitting…");

        Map<String, Object> request = new HashMap<>();
        request.put("studentId", uid);
        request.put("equipment", equipment);
        request.put("quantity", quantity);
        request.put("purpose", purpose);
        request.put("returnDate", returnDate);
        request.put("status", "pending");
        request.put("requestedAt", new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date()));

        db.collection("equipment_requests").add(request)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this, "Request submitted! Awaiting PT Admin approval.", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Submit Request");
                    Toast.makeText(this, "Failed to submit: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
