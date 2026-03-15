package com.procollegia;

import android.content.Intent;
import android.os.Bundle;
import android.os.Build;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * SplashActivity — Entry point.
 * Uses AuthStateListener to wait for Firebase to restore the persisted session
 * from disk before routing. This guarantees the user is NEVER incorrectly
 * sent back to the login screen when they are already authenticated.
 * Login is required only ONCE; subsequent launches go directly to the dashboard.
 */
public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private boolean mRouted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        createNotificationChannel();
        retrieveFCMToken();

        mAuth = FirebaseAuth.getInstance();

        // AuthStateListener fires immediately when Firebase has confirmed/restored
        // the session from disk. We only act once (mRouted guard prevents duplicate triggers).
        mAuthStateListener = firebaseAuth -> {
            if (mRouted) return;
            mRouted = true;
            Intent intent;
            if (firebaseAuth.getCurrentUser() != null) {
                // Session restored — skip login entirely
                intent = new Intent(SplashActivity.this, RoleDetectionActivity.class);
            } else {
                // No session — user must login
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "ProCollegia Alerts";
            String description = "Channel for important app notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("procollegia_alerts", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void retrieveFCMToken() {
        FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.w("FCM_CONFIG", "Fetching FCM registration token failed", task.getException());
                    return;
                }
                Log.d("FCM_CONFIG", "FCM Token: " + task.getResult());
            });
    }
}
