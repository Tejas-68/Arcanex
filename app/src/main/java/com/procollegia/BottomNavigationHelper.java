package com.procollegia;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

/**
 * Helper to set up bottom navigation bar highlighting and click routing.
 * Each role has its own nav bar layout included via <include>.
 *
 * Highlights the active tab with accent_blue; all others use text_secondary.
 */
public class BottomNavigationHelper {

    public static void setup(Activity activity, int activeNavId, String role) {
        if ("student".equals(role)) {
            setupStudentNav(activity, activeNavId);
        } else if ("teacher".equals(role)) {
            setupTeacherNav(activity, activeNavId);
        } else if ("pt".equals(role)) {
            setupPtNav(activity, activeNavId);
        } else if ("principal".equals(role)) {
            setupPrincipalNav(activity, activeNavId);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Student Nav: Home | Academics | Sports | Alerts | Profile
    // ──────────────────────────────────────────────────────────────────────────
    private static void setupStudentNav(Activity a, int activeId) {
        int[] tabIds = {R.id.nav_home, R.id.nav_academics, R.id.nav_sports, R.id.nav_alerts, R.id.nav_profile};

        for (int id : tabIds) {
            View tab = a.findViewById(id);
            if (tab == null) continue;
            boolean isActive = (id == activeId);
            tintTab(a, tab, isActive);
            final int tabId = id;
            tab.setOnClickListener(v -> {
                if (activeId == tabId) return;
                Intent intent = null;
                if (tabId == R.id.nav_home)      intent = new Intent(a, StudentDashboardActivity.class);
                else if (tabId == R.id.nav_academics) intent = new Intent(a, StudentAttendanceActivity.class);
                else if (tabId == R.id.nav_sports)    intent = new Intent(a, StudentSportsHomeActivity.class);
                else if (tabId == R.id.nav_alerts)    intent = new Intent(a, StudentAlertsActivity.class);
                else if (tabId == R.id.nav_profile)   intent = new Intent(a, StudentProfileActivity.class);
                if (intent != null) { a.startActivity(intent); a.finish(); }
            });
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Teacher Nav: Dashboard | Attendance | Leaves | Marks | Profile
    // ──────────────────────────────────────────────────────────────────────────
    private static void setupTeacherNav(Activity a, int activeId) {
        int[] tabIds = {R.id.nav_home, R.id.nav_attendance, R.id.nav_leaves, R.id.nav_marks, R.id.nav_profile};

        for (int id : tabIds) {
            View tab = a.findViewById(id);
            if (tab == null) continue;
            boolean isActive = (id == activeId);
            tintTab(a, tab, isActive);
            final int tabId = id;
            tab.setOnClickListener(v -> {
                if (activeId == tabId) return;
                Intent intent = null;
                if (tabId == R.id.nav_home)          intent = new Intent(a, TeacherDashboardActivity.class);
                else if (tabId == R.id.nav_attendance) intent = new Intent(a, TeacherAttendanceActivity.class);
                else if (tabId == R.id.nav_leaves)     intent = new Intent(a, TeacherLeaveRequestsActivity.class);
                else if (tabId == R.id.nav_marks)      intent = new Intent(a, TeacherEditMarksActivity.class);
                else if (tabId == R.id.nav_profile)    intent = new Intent(a, TeacherProfileActivity.class);
                if (intent != null) { a.startActivity(intent); a.finish(); }
            });
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // PT Admin Nav: Dashboard | Attendance | Equipment | Tournaments | Profile
    // ──────────────────────────────────────────────────────────────────────────
    private static void setupPtNav(Activity a, int activeId) {
        int[] tabIds = {R.id.nav_dashboard, R.id.nav_attendance, R.id.nav_equipments, R.id.nav_tournaments, R.id.nav_profile};

        for (int id : tabIds) {
            View tab = a.findViewById(id);
            if (tab == null) continue;
            boolean isActive = (id == activeId);
            tintTab(a, tab, isActive);
            final int tabId = id;
            tab.setOnClickListener(v -> {
                if (activeId == tabId) return;
                Intent intent = null;
                if (tabId == R.id.nav_dashboard)     intent = new Intent(a, PtDashboardActivity.class);
                else if (tabId == R.id.nav_attendance)  intent = new Intent(a, PtAttendanceActivity.class);
                else if (tabId == R.id.nav_equipments)  intent = new Intent(a, PtInventoryActivity.class);
                else if (tabId == R.id.nav_tournaments) intent = new Intent(a, StudentSportsTournamentsActivity.class);
                else if (tabId == R.id.nav_profile)     intent = new Intent(a, PtProfileActivity.class);
                if (intent != null) { a.startActivity(intent); a.finish(); }
            });
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Principal Nav: Home | Staff | Analytics | Alerts | Profile
    // ──────────────────────────────────────────────────────────────────────────
    private static void setupPrincipalNav(Activity a, int activeId) {
        int[] tabIds = {R.id.nav_home, R.id.nav_staffs, R.id.nav_analyze, R.id.nav_alerts, R.id.nav_profile};

        for (int id : tabIds) {
            View tab = a.findViewById(id);
            if (tab == null) continue;
            boolean isActive = (id == activeId);
            tintTab(a, tab, isActive);
            final int tabId = id;
            tab.setOnClickListener(v -> {
                if (activeId == tabId) return;
                Intent intent = null;
                if (tabId == R.id.nav_home)       intent = new Intent(a, PrincipalDashboardActivity.class);
                else if (tabId == R.id.nav_staffs)   intent = new Intent(a, PrincipalStaffsActivity.class);
                else if (tabId == R.id.nav_analyze)  intent = new Intent(a, PrincipalAnalyzeActivity.class);
                else if (tabId == R.id.nav_alerts)   intent = new Intent(a, StudentAlertsActivity.class);
                else if (tabId == R.id.nav_profile)  intent = new Intent(a, PrincipalProfileActivity.class);
                if (intent != null) { a.startActivity(intent); a.finish(); }
            });
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Tint helper — sets icon + label color for a tab LinearLayout
    // ──────────────────────────────────────────────────────────────────────────
    private static void tintTab(Activity a, View tab, boolean active) {
        int color = active
                ? ContextCompat.getColor(a, R.color.accent_blue)
                : ContextCompat.getColor(a, R.color.text_secondary);
        ColorStateList tint = ColorStateList.valueOf(color);

        // Find ImageView child (icon)
        if (tab instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) tab;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (child instanceof ImageView) {
                    ((ImageView) child).setImageTintList(tint);
                } else if (child instanceof TextView) {
                    ((TextView) child).setTextColor(color);
                    if (active) {
                        ((TextView) child).setTypeface(null, android.graphics.Typeface.BOLD);
                    } else {
                        ((TextView) child).setTypeface(null, android.graphics.Typeface.NORMAL);
                    }
                }
            }
        }
    }
}
