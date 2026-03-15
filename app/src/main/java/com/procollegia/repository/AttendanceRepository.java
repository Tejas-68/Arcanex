package com.procollegia.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.procollegia.data.AppDatabase;
import com.procollegia.data.StudentDao;
import com.procollegia.data.StudentEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AttendanceRepository {

    private final StudentDao studentDao;
    private final FirebaseFirestore db;
    private final ExecutorService executor;

    public AttendanceRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        studentDao = database.studentDao();
        db = FirebaseFirestore.getInstance();
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<StudentEntity>> getStudentsByDepartment(String department) {
        MutableLiveData<List<StudentEntity>> liveData = new MutableLiveData<>();

        // 1. Instantly load from local Room Cache
        executor.execute(() -> {
            List<StudentEntity> cached = studentDao.getStudentsByDepartment(department);
            if (!cached.isEmpty()) {
                liveData.postValue(cached);
            }

            // 2. Fetch fresh data from Firebase in background
            db.collection("users").whereEqualTo("role", "student")
                .whereEqualTo("course", department)
                .get()
                .addOnSuccessListener(snap -> {
                    List<StudentEntity> freshStudents = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        freshStudents.add(new StudentEntity(
                                doc.getId(),
                                doc.getString("name"),
                                doc.getString("rollNumber"),
                                doc.getString("course"),
                                doc.getString("batch")
                        ));
                    }
                    // 3. Update Room cache and post new LiveData if successful
                    executor.execute(() -> {
                        studentDao.deleteByDepartment(department);
                        studentDao.insertAll(freshStudents);
                        liveData.postValue(freshStudents);
                    });
                });
        });

        return liveData;
    }
}
