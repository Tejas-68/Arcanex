package com.procollegia;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.text.method.HideReturnsTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    // Step 1 views
    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private ImageView ivPwdToggle;
    private AppCompatButton btnNext;
    private TextView tvHaveAccount;

    // Step 2 views
    private LinearLayout cardStudent, cardTeacher, cardPt;
    private EditText etCollegeId, etDepartment;
    private AppCompatButton btnCreateAccount;
    private TextView tvBackToStep1;

    // Step indicator views
    private View viewStep1Bar, viewStep2Bar;
    private TextView tvStep1Label, tvStep2Label;

    // ViewFlipper
    private ViewFlipper viewFlipper;

    // Loading overlay
    private View loadingOverlay;

    // State
    private int currentStep = 0; // 0 = personal, 1 = academic
    private String selectedRole = "student";
    private boolean pwdVisible = false;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setListeners();
        updateStepIndicator(0);
        updateRoleCards();
    }

    private void initViews() {
        viewFlipper = findViewById(R.id.view_flipper);
        loadingOverlay = findViewById(R.id.loading_overlay);

        // Step indicator
        viewStep1Bar = findViewById(R.id.view_step1_bar);
        viewStep2Bar = findViewById(R.id.view_step2_bar);
        tvStep1Label = findViewById(R.id.tv_step1_label);
        tvStep2Label = findViewById(R.id.tv_step2_label);

        // Step 1
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        ivPwdToggle = findViewById(R.id.iv_pwd_toggle);
        btnNext = findViewById(R.id.btn_next);
        tvHaveAccount = findViewById(R.id.tv_have_account);

        // Step 2
        cardStudent = findViewById(R.id.card_role_student);
        cardTeacher = findViewById(R.id.card_role_teacher);
        cardPt = findViewById(R.id.card_role_pt);
        etCollegeId = findViewById(R.id.et_college_id);
        etDepartment = findViewById(R.id.et_department);
        btnCreateAccount = findViewById(R.id.btn_create_account);
        tvBackToStep1 = findViewById(R.id.tv_back_to_step1);
    }

    private void setListeners() {
        btnNext.setOnClickListener(v -> goToStep2());
        tvHaveAccount.setOnClickListener(v -> finish());

        cardStudent.setOnClickListener(v -> { selectedRole = "student"; updateRoleCards(); });
        cardTeacher.setOnClickListener(v -> { selectedRole = "teacher"; updateRoleCards(); });
        cardPt.setOnClickListener(v -> { selectedRole = "pt"; updateRoleCards(); });

        btnCreateAccount.setOnClickListener(v -> attemptRegister());
        tvBackToStep1.setOnClickListener(v -> goToStep1());

        ivPwdToggle.setOnClickListener(v -> {
            pwdVisible = !pwdVisible;
            if (pwdVisible) {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        // Step 1 tab click — already on step 1, navigate back if on step 2
        findViewById(R.id.ll_step1_tab).setOnClickListener(v -> {
            if (currentStep == 1) goToStep1();
        });
    }

    private void goToStep2() {
        if (!validateStep1()) return;

        // Set forward animations
        viewFlipper.setInAnimation(this, R.anim.slide_in_right);
        viewFlipper.setOutAnimation(this, R.anim.slide_out_left);
        viewFlipper.setDisplayedChild(1);
        currentStep = 1;
        updateStepIndicator(1);
    }

    private void goToStep1() {
        // Set backward animations
        viewFlipper.setInAnimation(this, R.anim.slide_in_left);
        viewFlipper.setOutAnimation(this, R.anim.slide_out_right);
        viewFlipper.setDisplayedChild(0);
        currentStep = 0;
        updateStepIndicator(0);
    }

    private boolean validateStep1() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String pwd = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String confirm = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";

        etName.setError(null);
        etEmail.setError(null);
        etPassword.setError(null);
        etConfirmPassword.setError(null);

        boolean valid = true;
        if (TextUtils.isEmpty(name)) {
            etName.setError(getString(R.string.error_empty_fields));
            valid = false;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.error_invalid_email));
            valid = false;
        }
        if (pwd.length() < 6) {
            etPassword.setError(getString(R.string.error_password_short));
            valid = false;
        }
        if (!pwd.equals(confirm)) {
            etConfirmPassword.setError(getString(R.string.error_password_mismatch));
            valid = false;
        }
        return valid;
    }

    private void attemptRegister() {
        String collegeId = etCollegeId.getText() != null ? etCollegeId.getText().toString().trim() : "";
        if (TextUtils.isEmpty(collegeId)) {
            etCollegeId.setError("Please enter your College ID");
            return;
        }

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String department = etDepartment.getText() != null ? etDepartment.getText().toString().trim() : "";

        setLoading(true);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().getUser() != null) {
                        String uid = task.getResult().getUser().getUid();
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("name", name);
                        userMap.put("email", email);
                        userMap.put("role", selectedRole);
                        userMap.put("collegeId", collegeId);
                        if (!TextUtils.isEmpty(department)) {
                            userMap.put("department", department);
                        }
                        db.collection("users").document(uid).set(userMap)
                                .addOnCompleteListener(dbTask -> {
                                    setLoading(false);
                                    startActivity(new Intent(this, RoleDetectionActivity.class));
                                    finishAffinity();
                                });
                    } else {
                        setLoading(false);
                        android.widget.Toast.makeText(this,
                                getString(R.string.error_login_failed), android.widget.Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void updateStepIndicator(int step) {
        if (step == 0) {
            viewStep1Bar.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_step_indicator_active));
            viewStep2Bar.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_step_indicator_inactive));
            tvStep1Label.setTextColor(ContextCompat.getColor(this, R.color.accent_blue));
            tvStep1Label.setTypeface(null, android.graphics.Typeface.BOLD);
            tvStep2Label.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
            tvStep2Label.setTypeface(null, android.graphics.Typeface.NORMAL);
        } else {
            viewStep1Bar.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_step_indicator_inactive));
            viewStep2Bar.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_step_indicator_active));
            tvStep2Label.setTextColor(ContextCompat.getColor(this, R.color.accent_blue));
            tvStep2Label.setTypeface(null, android.graphics.Typeface.BOLD);
            tvStep1Label.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
            tvStep1Label.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
    }

    private void updateRoleCards() {
        int activeColor = ContextCompat.getColor(this, R.color.accent_blue);
        int defaultColor = ContextCompat.getColor(this, R.color.text_primary);

        // Reset all cards
        resetCard(cardStudent, R.id.tv_role_student, defaultColor);
        resetCard(cardTeacher, R.id.tv_role_teacher, defaultColor);
        resetCard(cardPt, R.id.tv_role_pt, defaultColor);

        // Highlight selected
        LinearLayout selected;
        int selectedTextId;
        switch (selectedRole) {
            case "teacher":
                selected = cardTeacher; selectedTextId = R.id.tv_role_teacher; break;
            case "pt":
                selected = cardPt; selectedTextId = R.id.tv_role_pt; break;
            default:
                selected = cardStudent; selectedTextId = R.id.tv_role_student; break;
        }
        selected.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_neumorph_card_pressed));
        ((TextView) selected.findViewById(selectedTextId)).setTextColor(activeColor);
    }

    private void resetCard(LinearLayout card, int textViewId, int color) {
        card.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_neumorph_raised));
        ((TextView) card.findViewById(textViewId)).setTextColor(color);
    }

    private void setLoading(boolean isLoading) {
        if (loadingOverlay != null)
            loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnCreateAccount.setEnabled(!isLoading);
    }

    @Override
    public void onBackPressed() {
        if (currentStep == 1) {
            goToStep1();
        } else {
            super.onBackPressed();
        }
    }
}
