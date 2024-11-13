package com.example.sampleproject.util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.example.sampleproject.entity.StudentEntity;

public class ExcelValidationResult {
    private int totalRecords;
    private int successCount;
    private int failureCount;
    private StringBuilder errorDetails;
    private List<StudentEntity> validStudents;

    public ExcelValidationResult() {
        this.totalRecords = 0;
        this.successCount = 0;
        this.failureCount = 0;
        this.errorDetails = new StringBuilder();
        this.validStudents = new ArrayList<>();
    }

    // Getters and Setters
    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public StringBuilder getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(StringBuilder errorDetails) {
        this.errorDetails = errorDetails;
    }

    public List<StudentEntity> getValidStudents() {
        return validStudents;
    }

    public void setValidStudents(List<StudentEntity> validStudents) {
        this.validStudents = validStudents;
    }

    // Helper methods
    public void addValidStudent(StudentEntity student) {
        this.validStudents.add(student);
    }

    public void incrementSuccess() {
        this.successCount++;
        this.totalRecords++;
    }

    public void incrementFailure() {
        this.failureCount++;
        this.totalRecords++;
    }

    public String getSummary() {
        return String.format("Processed %d records: %d successful, %d failed", 
            totalRecords, successCount, failureCount);
    }

    // Static Validator Class
    public static class StudentValidator {

        /**
         * Validates a student's data.
         *
         * @param student The student entity to validate.
         * @param result The ExcelValidationResult object to store error details.
         * @param rowNumber The row number in the Excel file where the student data is located.
         * @return True if the student's data is valid, false otherwise.
         */
        public static boolean isValid(StudentEntity student, ExcelValidationResult result, int rowNumber) {
            boolean isValid = true;  // Assume data is valid initially
            
            // Name validation
            if (student.getName() == null || student.getName().trim().isEmpty()) {
                result.errorDetails.append("Row ").append(rowNumber).append(": Name is required\n");
                isValid = false;
            } else if (student.getName().length() > 50) {
                result.errorDetails.append("Row ").append(rowNumber).append(": Name cannot exceed 50 characters\n");
                isValid = false;
            }

            // Address validation
            if (student.getAddress() == null || student.getAddress().trim().isEmpty()) {
                result.errorDetails.append("Row ").append(rowNumber).append(": Address is required\n");
                isValid = false;
            }

            // Image URL validation (optional)
            if (student.getImage() != null && !student.getImage().trim().isEmpty()) {
                if (!student.getImage().startsWith("http://") && !student.getImage().startsWith("https://")) {
                    result.errorDetails.append("Row ").append(rowNumber).append(": Invalid image URL format\n");
                    isValid = false;
                }
            }

            // Salary validation
            if (student.getSalary() == null || student.getSalary() <= 0) {
                result.errorDetails.append("Row ").append(rowNumber).append(": Salary must be a positive value\n");
                isValid = false;
            }

            // DOB (Date of Birth) validation
            if (student.getDob() == null || student.getDob().isAfter(LocalDate.now())) {
                result.errorDetails.append("Row ").append(rowNumber).append(": Date of Birth must be in the past or today\n");
                isValid = false;
            }

            // isActive (Active/Inactive) validation
            if (student.getIsActive() == null) {
                result.errorDetails.append("Row ").append(rowNumber).append(":  status must be specified\n");
                isValid = false;
            }

            return isValid;
        }
    }
}
