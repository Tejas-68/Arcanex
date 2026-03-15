package com.procollegia;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class RoleDetectionActivity extends AppCompatActivity {

    private ImageView ivArcSpinner;
    private List<TextView> dots;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int dotIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_detection);

        ivArcSpinner = findViewById(R.id.iv_arc_spinner);
        dots = new ArrayList<>();
        dots.add(findViewById(R.id.dot1));
        dots.add(findViewById(R.id.dot2));
        dots.add(findViewById(R.id.dot3));

        startSpinnerAnimation();
        startDotAnimation();
        fetchUserRole();
    }

    private void startSpinnerAnimation() {
        ObjectAnimator spin = ObjectAnimator.ofFloat(ivArcSpinner, "rotation", 0f, 360f);
        spin.setDuration(1500);
        spin.setRepeatCount(ObjectAnimator.INFINITE);
        spin.setInterpolator(new LinearInterpolator());
        spin.start();
    }

    private void startDotAnimation() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < dots.size(); i++) {
                    dots.get(i).setAlpha(i == dotIndex ? 1.0f : 0.3f);
                }
                dotIndex = (dotIndex + 1) % dots.size();
                handler.postDelayed(this, 450);
            }
        }, 450);
    }

    private void fetchUserRole() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    handler.removeCallbacksAndMessages(null);
                    if (doc.exists()) {
                        String role = doc.getString("role");
                        routeByRole(role);
                    } else {
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    handler.removeCallbacksAndMessages(null);
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                });
    }

    private void routeByRole(String role) {
        Intent intent;
        if (role == null) role = "";
        switch (role) {
            case "teacher":
                intent = new Intent(this, TeacherHostActivity.class);
                break;
            case "pt":
            case "pt_admin":
                intent = new Intent(this, PtHostActivity.class);
                break;
            case "principal":
                intent = new Intent(this, PrincipalHostActivity.class);
                break;
            case "student":
            default:
                intent = new Intent(this, StudentHostActivity.class);
                break;
        }
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
