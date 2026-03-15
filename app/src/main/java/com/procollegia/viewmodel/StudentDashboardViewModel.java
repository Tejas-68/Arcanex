package com.procollegia.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.procollegia.repository.DashboardRepository;

public class StudentDashboardViewModel extends ViewModel {

    private final DashboardRepository repository;
    private LiveData<String> userNameLiveData;
    private LiveData<Integer> attendancePctLiveData;

    public StudentDashboardViewModel() {
        repository = new DashboardRepository();
    }

    public LiveData<String> getUserName(String userId) {
        if (userNameLiveData == null) {
            userNameLiveData = repository.getUserName(userId);
        }
        return userNameLiveData;
    }

    public LiveData<Integer> getAttendancePercentage(String studentId) {
        if (attendancePctLiveData == null) {
            attendancePctLiveData = repository.getAttendancePercentage(studentId);
        }
        return attendancePctLiveData;
    }
}
