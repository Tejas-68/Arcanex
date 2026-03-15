package com.procollegia.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class DashboardRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<String> getUserName(String userId) {
        MutableLiveData<String> liveData = new MutableLiveData<>();
        db.collection("users").document(userId).addSnapshotListener((doc, e) -> {
            if (e != null) {
                liveData.setValue("Error");
                return;
            }
            if (doc != null && doc.exists()) {
                String name = doc.getString("name");
                if (name != null) liveData.setValue(name.split(" ")[0]);
            }
        });
        return liveData;
    }

    public LiveData<Integer> getAttendancePercentage(String studentId) {
        MutableLiveData<Integer> liveData = new MutableLiveData<>();
        db.collection("attendance_logs")
                .whereEqualTo("studentId", studentId)
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) {
                        return; // Handle error or ignore
                    }
                    if (snap.isEmpty()) {
                        liveData.setValue(0);
                        return;
                    }
                    long total = snap.size();
                    long present = 0;
                    for (QueryDocumentSnapshot doc : snap) {
                        if ("present".equalsIgnoreCase(doc.getString("status"))) present++;
                    }
                    int pct = (int) (total > 0 ? (present * 100.0 / total) : 0);
                    liveData.setValue(pct);
                });
        return liveData;
    }

    public LiveData<Integer> getPendingLeaveCount() {
        MutableLiveData<Integer> liveData = new MutableLiveData<>();
        db.collection("leave_requests")
                .whereEqualTo("status", "pending")
                .addSnapshotListener((snap, e) -> {
                    if (e == null && snap != null) liveData.setValue(snap.size());
                });
        return liveData;
    }

    public LiveData<Integer> getEquipmentRequestCount(String status) {
        MutableLiveData<Integer> liveData = new MutableLiveData<>();
        db.collection("equipment_requests").whereEqualTo("status", status)
                .addSnapshotListener((snap, e) -> {
                    if (e == null && snap != null) liveData.setValue(snap.size());
                });
        return liveData;
    }

    public LiveData<Integer> getTotalUsersCount(String roleFilter, boolean isExactMatch) {
        MutableLiveData<Integer> liveData = new MutableLiveData<>();
        if (isExactMatch) {
            db.collection("users").whereEqualTo("role", roleFilter)
                    .addSnapshotListener((snap, e) -> {
                        if (e == null && snap != null) liveData.setValue(snap.size());
                    });
        } else {
            db.collection("users").whereNotEqualTo("role", roleFilter)
                    .addSnapshotListener((snap, e) -> {
                        if (e == null && snap != null) liveData.setValue(snap.size());
                    });
        }
        return liveData;
    }

    public LiveData<Integer> getCollegeAvgAttendance() {
        MutableLiveData<Integer> liveData = new MutableLiveData<>();
        db.collection("attendance_logs").addSnapshotListener((snap, e) -> {
            if (e != null || snap == null) return;
            if (snap.isEmpty()) { liveData.setValue(0); return; }
            long total = snap.size(), present = 0;
            for (QueryDocumentSnapshot doc : snap) {
                if ("present".equalsIgnoreCase(doc.getString("status"))) present++;
            }
            liveData.setValue((int)(total > 0 ? (present * 100.0 / total) : 0));
        });
        return liveData;
    }

    public LiveData<Integer> getCollegePassPercentage() {
        MutableLiveData<Integer> liveData = new MutableLiveData<>();
        db.collection("internal_marks").addSnapshotListener((snap, e) -> {
            if (e != null || snap == null) return;
            if (snap.isEmpty()) { liveData.setValue(0); return; }
            long total = snap.size(), passed = 0;
            for (QueryDocumentSnapshot doc : snap) {
                Long marks = doc.getLong("marks");
                if (marks != null && marks >= 40) passed++;
            }
            liveData.setValue((int)(total > 0 ? (passed * 100.0 / total) : 0));
        });
        return liveData;
    }
}
