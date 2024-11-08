package com.example.sampleproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.sampleproject.entity.Entity;  // Import your entity class

public interface Repository extends JpaRepository<Entity, Long> {
    // JpaRepository provides the save method, no need to define it
}
