package com.procollegia.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.procollegia.repository.DashboardRepository;

public class PtDashboardViewModel extends ViewModel {

    private final DashboardRepository repository;
    private LiveData<Integer> pendingRequests;
    private LiveData<Integer> activeRequests;
    private LiveData<Integer> overdueRequests;

    public PtDashboardViewModel() {
        repository = new DashboardRepository();
    }

    public LiveData<Integer> getPendingRequestsCount() {
        if (pendingRequests == null) {
            pendingRequests = repository.getEquipmentRequestCount("pending");
        }
        return pendingRequests;
    }

    public LiveData<Integer> getActiveRequestsCount() {
        if (activeRequests == null) {
            activeRequests = repository.getEquipmentRequestCount("approved");
        }
        return activeRequests;
    }

    public LiveData<Integer> getOverdueRequestsCount() {
        if (overdueRequests == null) {
            overdueRequests = repository.getEquipmentRequestCount("overdue");
        }
        return overdueRequests;
    }
}
