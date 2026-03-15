package com.procollegia.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "students")
public class StudentEntity {

    @PrimaryKey
    @NonNull
    public String id;
    
    public String name;
    public String rollNumber;
    public String course;
    public String batch;

    public StudentEntity(@NonNull String id, String name, String rollNumber, String course, String batch) {
        this.id = id;
        this.name = name;
        this.rollNumber = rollNumber;
        this.course = course;
        this.batch = batch;
    }
}
