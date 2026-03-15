package com.procollegia.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface StudentDao {

    @Query("SELECT * FROM students WHERE course = :department")
    List<StudentEntity> getStudentsByDepartment(String department);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<StudentEntity> students);

    @Query("DELETE FROM students WHERE course = :department")
    void deleteByDepartment(String department);
}
