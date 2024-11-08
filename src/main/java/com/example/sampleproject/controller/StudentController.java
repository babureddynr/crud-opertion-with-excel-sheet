package com.example.sampleproject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.sampleproject.entity.Entity;
import com.example.sampleproject.repository.Repository;
import com.example.sampleproject.service.Studentservice;
import com.example.sampleproject.util.ExcelHelper;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

@Controller
@RequestMapping("/students")
public class StudentController {
    @Autowired
    private Repository repo;
    
    @Autowired
    private Studentservice studentservice;

    @GetMapping
    public String getAllStudents(Model model) {
        model.addAttribute("students", studentservice.findallstudents());
        model.addAttribute("student", new Entity());
        return "students";
    }

    @PostMapping("/save")
    public String saveStudent(@ModelAttribute Entity entity) {
        studentservice.savestudent(entity);
        return "redirect:/students";
    }

    @PostMapping("/update/{id}")
    public String updateStudent(@PathVariable Long id, @ModelAttribute Entity student) {
        Entity existingStudent = repo.findById(id).orElse(null);
        if (existingStudent != null) {
            existingStudent.setName(student.getName());
            existingStudent.setAddress(student.getAddress());
            existingStudent.setImage(student.getImage());
            studentservice.savestudent(existingStudent);
        }
        return "redirect:/students";
    }

    @GetMapping("/delete/{id}")
    public String deleteStudent(@PathVariable Long id) {
        studentservice.deleteById(id);
        return "redirect:/students";
    }

    // New endpoints for Excel operations
    @GetMapping("/download-template")
    public ResponseEntity<Resource> downloadTemplate() {
        String filename = "students_template.xlsx";
        ByteArrayInputStream bis = ExcelHelper.studentsToExcel(new ArrayList<>());
        InputStreamResource file = new InputStreamResource(bis);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
            .body(file);
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, 
                           RedirectAttributes redirectAttributes) {
        if (ExcelHelper.hasExcelFormat(file)) {
            try {
                studentservice.saveFromExcel(file);
                redirectAttributes.addFlashAttribute("message", 
                    "Uploaded the file successfully!");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", 
                    "Could not upload the file: " + e.getMessage());
            }
        } else {
            redirectAttributes.addFlashAttribute("error", 
                "Please upload an excel file!");
        }
        return "redirect:/students";
    }
}