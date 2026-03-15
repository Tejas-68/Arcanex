package com.procollegia.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.procollegia.repository.DashboardRepository;

public class PrincipalDashboardViewModel extends ViewModel {

    private final DashboardRepository repository;
    private LiveData<Integer> totalStudents;
    private LiveData<Integer> totalStaff;
    private LiveData<Integer> avgAttendance;
    private LiveData<Integer> passPercentage;

    public PrincipalDashboardViewModel() {
        repository = new DashboardRepository();
    }

    public LiveData<Integer> getTotalStudents() {
        if (totalStudents == null) {
            totalStudents = repository.getTotalUsersCount("student", true);
        }
        return totalStudents;
    }

    public LiveData<Integer> getTotalStaff() {
        if (totalStaff == null) {
            // Fetch users where role is NOT student (aka teachers/pts)
            totalStaff = repository.getTotalUsersCount("student", false);
        }
        return totalStaff;
    }

    public LiveData<Integer> getAvgAttendance() {
        if (avgAttendance == null) {
            avgAttendance = repository.getCollegeAvgAttendance();
        }
        return avgAttendance;
    }

    public LiveData<Integer> getPassPercentage() {
        if (passPercentage == null) {
            passPercentage = repository.getCollegePassPercentage();
        }
        return passPercentage;
    }
}
