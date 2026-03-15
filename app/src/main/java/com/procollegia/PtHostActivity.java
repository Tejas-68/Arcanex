package com.procollegia;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.procollegia.fragment.PtAttendanceFragment;
import com.procollegia.fragment.PtDashboardFragment;
import com.procollegia.fragment.PtInventoryFragment;
import com.procollegia.fragment.PtProfileFragment;
import com.procollegia.fragment.PtTournamentsFragment;

public class PtHostActivity extends AppCompatActivity {

    private int[] tabIds;
    private int currentTabId = R.id.nav_dashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_pt);

        tabIds = new int[]{R.id.nav_dashboard, R.id.nav_attendance, R.id.nav_equipments,
                           R.id.nav_tournaments, R.id.nav_profile};

        setupNavBar();
        if (savedInstanceState == null) {
            switchTab(new PtDashboardFragment(), R.id.nav_dashboard);
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
        tintAllTabs(R.id.nav_dashboard);
    }

    private Fragment getFragmentForTab(int tabId) {
        if (tabId == R.id.nav_dashboard)    return new PtDashboardFragment();
        if (tabId == R.id.nav_attendance)   return new PtAttendanceFragment();
        if (tabId == R.id.nav_equipments)   return new PtInventoryFragment();
        if (tabId == R.id.nav_tournaments)  return new PtTournamentsFragment();
        if (tabId == R.id.nav_profile)      return new PtProfileFragment();
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
                    ? ContextCompat.getColor(this, R.color.accent_orange)
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
        if (currentTabId != R.id.nav_dashboard) {
            switchTab(new PtDashboardFragment(), R.id.nav_dashboard);
        } else {
            moveTaskToBack(true);
        }
    }
}
