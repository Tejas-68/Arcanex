package com.procollegia;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private android.widget.TextView tvRegister;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> loginUser());
        
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- DEVELOPMENT BYPASS FOR QUICK TESTING ---
        if (email.startsWith("student")) {
            startActivity(new Intent(this, StudentDashboardActivity.class));
            finish();
            return;
        } else if (email.startsWith("teacher")) {
            startActivity(new Intent(this, TeacherDashboardActivity.class));
            finish();
            return;
        } else if (email.startsWith("pt")) {
            startActivity(new Intent(this, PtAdminDashboardActivity.class));
            finish();
            return;
        } else if (email.startsWith("principal")) {
            startActivity(new Intent(this, PrincipalDashboardActivity.class));
            finish();
            return;
        }

        // --- PRODUCTION FIREBASE LOGIN ---
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String uid = task.getResult().getUser().getUid();
                    FirebaseFirestore.getInstance().collection("users").document(uid).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            progressBar.setVisibility(View.GONE);
                            btnLogin.setEnabled(true);

                            if (documentSnapshot.exists()) {
                                String role = documentSnapshot.getString("role");
                                Toast.makeText(LoginActivity.this, "Welcome Back", Toast.LENGTH_SHORT).show();
                                
                                Intent intent;
                                if ("Teacher".equals(role)) {
                                    intent = new Intent(LoginActivity.this, TeacherDashboardActivity.class);
                                } else if ("PT Admin".equals(role)) {
                                    intent = new Intent(LoginActivity.this, PtAdminDashboardActivity.class);
                                } else if ("Principal".equals(role)) {
                                    intent = new Intent(LoginActivity.this, PrincipalDashboardActivity.class);
                                } else {
                                    intent = new Intent(LoginActivity.this, StudentDashboardActivity.class);
                                }
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "User details not found. Please register.", Toast.LENGTH_LONG).show();
                                mAuth.signOut();
                            }
                        })
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                            btnLogin.setEnabled(true);
                            Toast.makeText(LoginActivity.this, "Error fetching details: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                } else {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
    }
}
