package com.example.sampleproject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.sampleproject.entity.StudentEntity;
import com.example.sampleproject.repository.Repository;
import com.example.sampleproject.service.Studentservice;
import com.example.sampleproject.util.ExcelHelper;

import jakarta.validation.Valid;

import java.io.ByteArrayInputStream;
import java.util.List;

@Controller
@RequestMapping("/students")
public class StudentController {

    @Autowired
    private Repository repo;

    @Autowired
    private Studentservice studentservice;

    // Display all students
    @GetMapping
    public String getAllStudents(Model model) {
        model.addAttribute("students", studentservice.findallstudents());
        model.addAttribute("student", new StudentEntity());
        return "students"; // Refers to students.jsp
    }

    // Save new student
    @PostMapping("/save")
    public String saveStudent(@Valid @ModelAttribute("student") StudentEntity entity, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("students", studentservice.findallstudents());
            return "students"; // Return to the same page with errors
        }
        studentservice.savestudent(entity);
        return "redirect:/students"; // Redirect to /students to show the updated list
    }

    // Update student details
    @PostMapping("/update/{id}")
    public String updateStudent(@PathVariable Long id, @ModelAttribute StudentEntity student) {
        StudentEntity existingStudent = repo.findById(id).orElse(null);
        if (existingStudent != null) {
            existingStudent.setName(student.getName());
            existingStudent.setAddress(student.getAddress());
            existingStudent.setImage(student.getImage());
            studentservice.savestudent(existingStudent);
        }
        return "redirect:/students"; // Redirect to /students to show the updated list
    }

    // Delete student by ID
    @GetMapping("/delete/{id}")
    public String deleteStudent(@PathVariable Long id) {
        studentservice.deleteById(id);
        return "redirect:/students"; // Redirect to /students after deletion
    }


    // Download template for students data (empty Excel template)
    @GetMapping("/download-template")
    public ResponseEntity<Resource> downloadTemplate() {
        // Define the filename for the template
        String filename = "students_template.xlsx";
        
        // Generate an empty Excel file as a template using an empty list
        // Passing an empty list to the helper method creates only headers with no data
        ByteArrayInputStream bis = ExcelHelper.studentsToExcel(List.of());
        
        // Wrap the ByteArrayInputStream in an InputStreamResource for download
        InputStreamResource file = new InputStreamResource(bis);

        // Return a ResponseEntity containing the Excel file as a downloadable resource
        // - Sets Content-Disposition to "attachment" with the specified filename
        // - Specifies the content type as Excel format (application/vnd.ms-excel)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
            .body(file);
    }

    // Download all students in an Excel file
    @GetMapping("/download-all")
    public ResponseEntity<Resource> downloadAllStudents() {
        // Retrieve all student entities from the database
        List<StudentEntity> students = studentservice.findallstudents();
        
        // Generate an Excel file containing all student data
        // The helper method converts the list of students into an Excel file format
        ByteArrayInputStream bis = ExcelHelper.studentsToExcel(students);
        
        // Wrap the ByteArrayInputStream in an InputStreamResource for download
        InputStreamResource file = new InputStreamResource(bis);

        // Define the filename for the file containing all  student data
        String filename = "all_students.xlsx";

        // Return a ResponseEntity containing the Excel file as a downloadable resource
        // - Sets Content-Disposition to "attachment" with the specified filename
        // - Specifies the content type as Excel format (application/vnd.ms-excel)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
            .body(file);
    }

    // Upload Excel file containing student data
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        // Check if the uploaded file is an Excel file by its MIME type
        if (ExcelHelper.hasExcelFormat(file)) {
            try {
                // If the file is in the correct format, process it to save data
                studentservice.saveFromExcel(file);
                
                // Add a success message to be displayed on the redirected page
                redirectAttributes.addFlashAttribute("message", "Uploaded the file successfully!");
            } catch (Exception e) {
                // If an error occurs during processing, add an error message for display
                redirectAttributes.addFlashAttribute("error", "Could not upload the file: " + e.getMessage());
            }
        } else {
            // If the file format is incorrect, display an error message
            redirectAttributes.addFlashAttribute("error", "Please upload an Excel file!");
        }
        // Redirect back to the students page after upload attempt
        return "redirect:/students";
    }
}