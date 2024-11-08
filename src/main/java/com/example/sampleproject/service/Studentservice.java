package com.example.sampleproject.service;

import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.sampleproject.entity.Entity;
import com.example.sampleproject.repository.Repository;
import com.example.sampleproject.util.ExcelHelper;

@Service
public class Studentservice {
    @Autowired
    Repository repo;

    public List<Entity> findallstudents() {
        return repo.findAll();
    }

    public String savestudent(Entity entity) {
        repo.save(entity);
        return "Student saved successfully with the id " + entity.getId();
    }

    public String deleteById(Long id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
            return "Student deleted successfully";
        } else {
            return "Student not found with the id " + id;
        }
    }

    public String save(Entity entity) {
        repo.save(entity);
        return "Updated successfully with the id " + entity.getId();
    }

    // New method for saving students from Excel file
    public void saveFromExcel(MultipartFile file) {
        try {
            List<Entity> students = ExcelHelper.excelToStudents(file.getInputStream());
            repo.saveAll(students);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store excel data: " + e.getMessage());
        }
    }
}