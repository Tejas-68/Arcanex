package com.procollegia.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.procollegia.repository.DashboardRepository;

public class TeacherDashboardViewModel extends ViewModel {

    private final DashboardRepository repository;
    private LiveData<String> userNameLiveData;
    private LiveData<Integer> pendingLeavesLiveData;

    public TeacherDashboardViewModel() {
        repository = new DashboardRepository();
    }

    public LiveData<String> getUserName(String userId) {
        if (userNameLiveData == null) {
            userNameLiveData = repository.getUserName(userId);
        }
        return userNameLiveData;
    }

    public LiveData<Integer> getPendingLeavesCount() {
        if (pendingLeavesLiveData == null) {
            pendingLeavesLiveData = repository.getPendingLeaveCount();
        }
        return pendingLeavesLiveData;
    }
}
