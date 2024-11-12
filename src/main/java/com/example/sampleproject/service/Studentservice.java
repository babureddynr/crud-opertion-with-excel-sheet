package com.example.sampleproject.service;

import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.sampleproject.entity.StudentEntity;
import com.example.sampleproject.repository.Repository;
import com.example.sampleproject.util.ExcelHelper;
import com.example.sampleproject.util.ExcelValidationResult;

@Service
public class Studentservice {
    @Autowired
    Repository repo;

    public List<StudentEntity> findallstudents() {
        return repo.findAll();
    }

    public String savestudent(StudentEntity entity) {
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

    public String save(StudentEntity entity) {
        repo.save(entity);
        return "Updated successfully with the id " + entity.getId();
    }

    // New method for saving students from Excel file
    public ExcelValidationResult saveFromExcel(MultipartFile file) throws IOException {
        ExcelValidationResult result = ExcelHelper.excelToStudents(file.getInputStream());
        
        // Save only valid students
        if (!result.getValidStudents().isEmpty()) {
            repo.saveAll(result.getValidStudents());
        }
        
        return result;
    }
}