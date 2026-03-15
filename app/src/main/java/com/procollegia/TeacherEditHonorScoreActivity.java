package com.procollegia;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TeacherEditHonorScoreActivity extends AppCompatActivity {

    private EditText etSearch, etOptionalNote;
    private View cardStudentProfile, layoutConfigurator, loadingOverlay, btnApplyChanges;
    private TextView tvStudentInfo, tvCurrentScore, tvTierBadge;
    private TextView btnToggleAdd, btnToggleDeduct, tvStepValue;
    private ImageView btnStepMinus, btnStepPlus;
    private Spinner spinnerReason;

    private FirebaseFirestore db;
    private DocumentSnapshot currentStudent = null;

    private boolean isAdding = true;
    private int currentStepValue = 10;

    private static final String[] REASONS = {
            "Select Reason...",
            "Good Conduct",
            "Sports Win",
            "Academic Achievement",
            "Late Submission",
            "Attendance Default"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_edit_honor_score);

        db = FirebaseFirestore.getInstance();

        // 1. Bind Views
        etSearch = findViewById(R.id.et_search_student);
        etOptionalNote = findViewById(R.id.et_optional_note);
        cardStudentProfile = findViewById(R.id.card_student_profile);
        layoutConfigurator = findViewById(R.id.layout_configurator);
        loadingOverlay = findViewById(R.id.loading_overlay);
        btnApplyChanges = findViewById(R.id.btn_apply_changes);

        tvStudentInfo = findViewById(R.id.tv_student_info);
        tvCurrentScore = findViewById(R.id.tv_current_score);
        tvTierBadge = findViewById(R.id.tv_tier_badge);

        btnToggleAdd = findViewById(R.id.btn_toggle_add);
        btnToggleDeduct = findViewById(R.id.btn_toggle_deduct);
        tvStepValue = findViewById(R.id.tv_step_value);
        btnStepMinus = findViewById(R.id.btn_step_minus);
        btnStepPlus = findViewById(R.id.btn_step_plus);
        spinnerReason = findViewById(R.id.spinner_reason);

        findViewById(R.id.iv_back).setOnClickListener(v -> onBackPressed());

        // 2. Setup Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, REASONS);
        spinnerReason.setAdapter(adapter);

        // 3. Search Listener
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                // We assume Roll Number or UID search in real-time. Wait for at least 3 chars
                if (query.length() >= 3) {
                    searchStudent(query);
                } else {
                    hideStudentCard();
                }
            }
        });

        // 4. Configurator Listeners
        btnToggleAdd.setOnClickListener(v -> setAddingMode(true));
        btnToggleDeduct.setOnClickListener(v -> setAddingMode(false));

        btnStepMinus.setOnClickListener(v -> {
            if (currentStepValue > 5) {
                currentStepValue -= 5;
                updateStepUI();
            }
        });

        btnStepPlus.setOnClickListener(v -> {
            if (currentStepValue < 100) {
                currentStepValue += 5;
                updateStepUI();
            }
        });

        btnApplyChanges.setOnClickListener(v -> submitChanges());
    }

    private void searchStudent(String query) {
        db.collection("users")
                .whereEqualTo("role", "student")
                .whereGreaterThanOrEqualTo("name", query)
                .whereLessThan("name", query + "\uf8ff")
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        currentStudent = snap.getDocuments().get(0);
                        showStudentCard();
                    } else {
                        // Fallback: Check Roll Number exact match
                        db.collection("users")
                                .whereEqualTo("role", "student")
                                .whereEqualTo("rollNumber", query)
                                .limit(1)
                                .get()
                                .addOnSuccessListener(snapRoll -> {
                                    if (!snapRoll.isEmpty()) {
                                        currentStudent = snapRoll.getDocuments().get(0);
                                        showStudentCard();
                                    } else {
                                        hideStudentCard();
                                    }
                                });
                    }
                });
    }

    private void showStudentCard() {
        if (currentStudent == null) return;
        
        cardStudentProfile.setVisibility(View.VISIBLE);
        layoutConfigurator.setVisibility(View.VISIBLE);
        btnApplyChanges.setVisibility(View.VISIBLE);

        String name = currentStudent.getString("name");
        String course = currentStudent.getString("course");
        String roll = currentStudent.getString("rollNumber");
        tvStudentInfo.setText(String.format("%s | %s | Roll: %s", name, course, roll));

        Long scoreObj = currentStudent.getLong("honorScore");
        long score = scoreObj != null ? scoreObj : 0L;
        tvCurrentScore.setText(score + " pts");

        // Set Tier Badge
        if (score >= 1000) {
             tvTierBadge.setText(" Platinum Tier ");
             tvTierBadge.setBackgroundResource(R.drawable.bg_gold_tier_badge); // Using gold drawable for both for now
        } else if (score >= 500) {
             tvTierBadge.setText(" Gold Tier ");
             tvTierBadge.setBackgroundResource(R.drawable.bg_gold_tier_badge);
        } else if (score >= 200) {
             tvTierBadge.setText(" Silver Tier ");
             tvTierBadge.setBackgroundResource(R.drawable.bg_neumorph_pill); // default
        } else {
             tvTierBadge.setText(" Bronze Tier ");
             tvTierBadge.setBackgroundResource(R.drawable.bg_neumorph_pill);
        }
    }

    private void hideStudentCard() {
        currentStudent = null;
        cardStudentProfile.setVisibility(View.GONE);
        layoutConfigurator.setVisibility(View.GONE);
        // hide the button until student is selected
        btnApplyChanges.setVisibility(View.GONE); 
    }

    private void setAddingMode(boolean isAdd) {
        isAdding = isAdd;
        if (isAdding) {
            btnToggleAdd.setBackgroundResource(R.drawable.bg_neumorph_pill);
            btnToggleAdd.setTextColor(Color.WHITE);
            btnToggleAdd.setBackgroundTintList(getResources().getColorStateList(R.color.accent_blue));

            btnToggleDeduct.setBackground(null);
            btnToggleDeduct.setTextColor(getResources().getColor(R.color.text_primary));
            btnToggleDeduct.setBackgroundTintList(null);
        } else {
            btnToggleDeduct.setBackgroundResource(R.drawable.bg_neumorph_pill);
            btnToggleDeduct.setTextColor(Color.WHITE);
            btnToggleDeduct.setBackgroundTintList(getResources().getColorStateList(R.color.accent_blue));

            btnToggleAdd.setBackground(null);
            btnToggleAdd.setTextColor(getResources().getColor(R.color.text_primary));
            btnToggleAdd.setBackgroundTintList(null);
        }
    }

    private void updateStepUI() {
        tvStepValue.setText(String.valueOf(currentStepValue));
    }

    private void submitChanges() {
        if (currentStudent == null) return;
        
        int reasonIndex = spinnerReason.getSelectedItemPosition();
        if (reasonIndex == 0) {
            Toast.makeText(this, "Please select a mandatory reason", Toast.LENGTH_SHORT).show();
            return;
        }

        String reason = REASONS[reasonIndex];
        String note = etOptionalNote.getText().toString();
        
        int delta = isAdding ? currentStepValue : -currentStepValue;

        if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);
        btnApplyChanges.setEnabled(false);

        Long currentScoreObj = currentStudent.getLong("honorScore");
        long oldScore = currentScoreObj != null ? currentScoreObj : 0L;
        long newScore = oldScore + delta;
        if (newScore < 0) newScore = 0; // Floor at 0

        WriteBatch batch = db.batch();
        
        // 1. Update the student's total score
        batch.update(currentStudent.getReference(), "honorScore", newScore);

        // 2. Add an Honor Event Log
        String fullDescription = (isAdding ? "Gained Points: " : "Lost Points: ") + reason + (note.isEmpty() ? "" : " - " + note);
        HonorEvent ev = new HonorEvent(
            delta,
            fullDescription,
            new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date())
        );

        batch.set(db.collection("users").document(currentStudent.getId())
                .collection("honor_events").document(), ev);

        batch.commit()
                .addOnSuccessListener(v -> {
                    if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
                    Toast.makeText(this, "Changes Applied!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
                    btnApplyChanges.setEnabled(true);
                    Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show();
                });
    }
}
