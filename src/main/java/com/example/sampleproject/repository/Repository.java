package com.example.sampleproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.sampleproject.entity.StudentEntity;  // Import your entity class

public interface Repository extends JpaRepository<StudentEntity, Long> {
    // JpaRepository provides the save method, no need to define it
}
