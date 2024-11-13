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
import com.example.sampleproject.util.ExcelValidationResult;

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

            // New fields
            existingStudent.setSalary(student.getSalary());
            existingStudent.setDob(student.getDob());
            existingStudent.setIsActive(student.getIsActive());

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
 // Endpoint to download an empty Excel template file for students data
    @GetMapping("/download-template")
    public ResponseEntity<Resource> downloadTemplate() {
        // Define the filename for the Excel template
        String filename = "students_template.xlsx";

        // Create an empty Excel file with headers for the student data fields.
        // The ExcelHelper.studentsToExcel(List.of()) method takes an empty list to generate the file
        // with only the headers for each field (e.g., Name, Age, ID, etc.) defined in ExcelHelper.
        ByteArrayInputStream bis = ExcelHelper.studentsToExcel(List.of()); // Ensure headers for required fields

        // Wrap the ByteArrayInputStream as an InputStreamResource, which can be sent in the response
        InputStreamResource file = new InputStreamResource(bis);

        // Return the file as a downloadable resource in the response, setting the Content-Disposition header
        // to prompt the user to download it as 'students_template.xlsx' and specifying the content type
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
            .body(file);
    }

    // Endpoint to download an Excel file containing all student data records
    @GetMapping("/download-all")
    public ResponseEntity<Resource> downloadAllStudents() {
        // Retrieve a list of all student records from the studentservice.
        // This list will contain StudentEntity objects that represent each student's data.
        List<StudentEntity> students = studentservice.findallstudents();

        // Convert the list of students into an Excel file using the helper method.
        // The ExcelHelper.studentsToExcel method creates an Excel file with each student's data
        // in separate rows, including any new fields.
        ByteArrayInputStream bis = ExcelHelper.studentsToExcel(students); // Add fields as needed in helper

        // Prepare the Excel file as an InputStreamResource to be sent as the response body
        InputStreamResource file = new InputStreamResource(bis);

        // Define the filename for the downloaded file
        String filename = "all_students.xlsx";

        // Set up the HTTP response to send the file, specifying the Content-Disposition
        // to trigger download and setting the content type to Excel format
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
            .body(file);
    }

    // Endpoint to upload an Excel file containing student data
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        // Check if the uploaded file is in the correct Excel format (.xlsx).
        // The ExcelHelper.hasExcelFormat method verifies that the uploaded file is in the .xlsx format.
        if (ExcelHelper.hasExcelFormat(file)) {
            try {
                // Process the uploaded Excel file and save the student data.
                // The saveFromExcel method parses the file, validates data, and saves it to the database.
                // The method returns an ExcelValidationResult object containing a summary of the results
                // and any error details if validation fails for some records.
                ExcelValidationResult result = studentservice.saveFromExcel(file); // Ensure new fields are handled

                // Store a success message in the redirect attributes to display after redirection
                redirectAttributes.addFlashAttribute("message", result.getSummary());

                // If there were validation errors, add an error message with detailed information
                if (result.getFailureCount() > 0) {
                    redirectAttributes.addFlashAttribute("error", 
                        "Validation errors:\n" + result.getErrorDetails().toString());
                }
            } catch (Exception e) {
                // If an exception occurs during file processing, store an error message to display
                redirectAttributes.addFlashAttribute("error", 
                    "Could not upload the file: " + e.getMessage());
            }
        } else {
            // If the file is not in Excel format, store an error message to inform the user
            redirectAttributes.addFlashAttribute("error", "Please upload an Excel file (.xlsx)!");
        }

        // Redirect to the '/students' page to show messages or results
        return "redirect:/students";
    }
}
