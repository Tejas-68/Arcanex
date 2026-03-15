package com.procollegia;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.google.firebase.auth.FirebaseAuth;
import android.content.Intent;

public class StudentProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);
        
        ImageView ivBack = findViewById(R.id.iv_back);
        AppCompatButton btnLogout = findViewById(R.id.btn_logout);
        
        ivBack.setOnClickListener(v -> onBackPressed());
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
        
        BottomNavigationHelper.setup(this, R.id.nav_profile, "student");
    }
}
