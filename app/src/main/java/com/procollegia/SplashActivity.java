package com.procollegia;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply dark mode immediately from preferences
        SharedPreferences prefs = getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        setContentView(R.layout.activity_splash);

        // Minimum splash delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                // User is already logged in, fetch role and route to dashboard
                FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String role = documentSnapshot.getString("role");
                            Intent intent;
                            if ("Teacher".equals(role)) {
                                intent = new Intent(SplashActivity.this, TeacherDashboardActivity.class);
                            } else if ("PT Admin".equals(role)) {
                                intent = new Intent(SplashActivity.this, PtAdminDashboardActivity.class);
                            } else if ("Principal".equals(role)) {
                                intent = new Intent(SplashActivity.this, PrincipalDashboardActivity.class);
                            } else {
                                intent = new Intent(SplashActivity.this, StudentDashboardActivity.class);
                            }
                            startActivity(intent);
                            finish();
                        } else {
                            // Document missing, re-login required
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(SplashActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        finish();
                    });
            } else {
                // No user logged in, go to Login
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            }
        }, 1500);
    }
}
