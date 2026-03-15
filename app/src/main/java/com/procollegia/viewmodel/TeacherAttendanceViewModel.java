package com.procollegia.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.procollegia.data.StudentEntity;
import com.procollegia.repository.AttendanceRepository;
import java.util.List;

public class TeacherAttendanceViewModel extends AndroidViewModel {

    private final AttendanceRepository repository;

    public TeacherAttendanceViewModel(@NonNull Application application) {
        super(application);
        repository = new AttendanceRepository(application);
    }

    public LiveData<List<StudentEntity>> getStudentsByDepartment(String department) {
        return repository.getStudentsByDepartment(department);
    }
}
