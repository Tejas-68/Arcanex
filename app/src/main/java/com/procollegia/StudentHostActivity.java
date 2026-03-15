package com.procollegia;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.procollegia.fragment.StudentAcademicsFragment;
import com.procollegia.fragment.StudentAlertsFragment;
import com.procollegia.fragment.StudentHomeFragment;
import com.procollegia.fragment.StudentProfileFragment;
import com.procollegia.fragment.StudentSportsFragment;

public class StudentHostActivity extends AppCompatActivity {

    private int[] tabIds;
    private int currentTabId = R.id.nav_home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_student);

        tabIds = new int[]{R.id.nav_home, R.id.nav_academics, R.id.nav_sports,
                           R.id.nav_alerts, R.id.nav_profile};

        setupNavBar();

        if (savedInstanceState == null) {
            switchTab(new StudentHomeFragment(), R.id.nav_home);
        }
    }

    private void setupNavBar() {
        for (int id : tabIds) {
            View tab = findViewById(id);
            if (tab == null) continue;
            final int tabId = id;
            tab.setOnClickListener(v -> {
                if (currentTabId == tabId) return;
                Fragment f = getFragmentForTab(tabId);
                if (f != null) switchTab(f, tabId);
            });
        }
        tintAllTabs(R.id.nav_home);
    }

    private Fragment getFragmentForTab(int tabId) {
        if (tabId == R.id.nav_home)      return new StudentHomeFragment();
        if (tabId == R.id.nav_academics) return new StudentAcademicsFragment();
        if (tabId == R.id.nav_sports)    return new StudentSportsFragment();
        if (tabId == R.id.nav_alerts)    return new StudentAlertsFragment();
        if (tabId == R.id.nav_profile)   return new StudentProfileFragment();
        return null;
    }

    public void switchTab(Fragment fragment, int activeTabId) {
        currentTabId = activeTabId;
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss();
        tintAllTabs(activeTabId);
    }

    private void tintAllTabs(int activeId) {
        for (int id : tabIds) {
            View tab = findViewById(id);
            if (tab == null || !(tab instanceof android.view.ViewGroup)) continue;
            boolean active = (id == activeId);
            int color = active
                    ? ContextCompat.getColor(this, R.color.accent_blue)
                    : ContextCompat.getColor(this, R.color.text_secondary);
            ColorStateList tint = ColorStateList.valueOf(color);
            android.view.ViewGroup group = (android.view.ViewGroup) tab;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (child instanceof ImageView)
                    ((ImageView) child).setImageTintList(tint);
                else if (child instanceof TextView) {
                    ((TextView) child).setTextColor(color);
                    ((TextView) child).setTypeface(null,
                            active ? android.graphics.Typeface.BOLD
                                   : android.graphics.Typeface.NORMAL);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (currentTabId != R.id.nav_home) {
            switchTab(new StudentHomeFragment(), R.id.nav_home);
        } else {
            moveTaskToBack(true);
        }
    }
}
