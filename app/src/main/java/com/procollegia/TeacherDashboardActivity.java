package com.procollegia;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.procollegia.fragments.TeacherAttendanceFragment;
import com.procollegia.fragments.TeacherHomeFragment;
import com.procollegia.fragments.TeacherHonorScoreFragment;
import com.procollegia.fragments.TeacherProfileFragment;
import com.procollegia.fragments.TeacherTournamentFragment;

/** Teacher Dashboard - Main host activity for teachers. */
public class TeacherDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new TeacherHomeFragment();
            } else if (itemId == R.id.nav_attendance) {
                selectedFragment = new TeacherAttendanceFragment();
            } else if (itemId == R.id.nav_tournament) {
                selectedFragment = new TeacherTournamentFragment();
            } else if (itemId == R.id.nav_honor_score) {
                selectedFragment = new TeacherHonorScoreFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new TeacherProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, selectedFragment)
                    .commit();
            }
            return true;
        });

        // Load the home fragment by default
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }
}
