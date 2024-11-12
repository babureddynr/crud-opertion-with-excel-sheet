package com.example.sampleproject.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;
import com.example.sampleproject.entity.StudentEntity;

public class ExcelHelper {

    // MIME type for Excel files (xlsx format)
    public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    // Headers to be used in the Excel file (columns)
    static String[] HEADERS = { "Name", "Address", "Image" };

    // The sheet name for the Excel file
    static String SHEET = "Students";

    /**
     * Checks if the uploaded file has an Excel format (xlsx).
     *
     * @param file The uploaded file
     * @return True if the file is of type Excel, otherwise false.
     */
    public static boolean hasExcelFormat(MultipartFile file) {
        return TYPE.equals(file.getContentType());
    }

    /**
     * Converts a list of StudentEntity objects into an Excel file (ByteArrayInputStream).
     *
     * @param students The list of students to convert
     * @return ByteArrayInputStream containing the Excel file data
     */
    public static ByteArrayInputStream studentsToExcel(List<StudentEntity> students) {
        try (Workbook workbook = new XSSFWorkbook(); 
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Create a sheet with the given name
            Sheet sheet = workbook.createSheet(SHEET);

            // Create the header row in the first row of the Excel sheet
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < HEADERS.length; col++) {
                // Add headers to the first row
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(HEADERS[col]);
            }

            // Add student data in subsequent rows
            int rowIdx = 1;
            for (StudentEntity student : students) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(student.getName());
                row.createCell(1).setCellValue(student.getAddress());
                row.createCell(2).setCellValue(student.getImage());
            }

            // Write the workbook data to the output stream
            workbook.write(out);
            // Return the ByteArrayInputStream containing the Excel file
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to export data to Excel file: " + e.getMessage());
        }
    }

    /**
     * Parses an Excel file to extract student data and validates it.
     *
     * @param is InputStream of the Excel file
     * @return ExcelValidationResult object containing validation details
     */
    public static ExcelValidationResult excelToStudents(InputStream is) {
        // Create an instance to store the validation results
        ExcelValidationResult validationResult = new ExcelValidationResult();

        try (Workbook workbook = new XSSFWorkbook(is)) {
            // Get the sheet named "Students"
            Sheet sheet = workbook.getSheet(SHEET);
            Iterator<Row> rows = sheet.iterator();

            // Skip the header row (the first row)
            if (rows.hasNext()) {
                rows.next();
            }

            int rowNumber = 2; // Start processing from the second row
            // Iterate through all rows in the sheet
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                StudentEntity student = new StudentEntity();

                try {
                    // Extract data from the cells in the current row
                    Cell nameCell = currentRow.getCell(0);
                    Cell addressCell = currentRow.getCell(1);
                    Cell imageCell = currentRow.getCell(2);

                    // Set student attributes based on the cell values
                    student.setName(nameCell != null ? nameCell.getStringCellValue() : "");
                    student.setAddress(addressCell != null ? addressCell.getStringCellValue() : "");
                    student.setImage(imageCell != null ? imageCell.getStringCellValue() : "");

                    // Validate the student data
                    if (ExcelValidationResult.StudentValidator.isValid(student, validationResult, rowNumber)) {
                        // If valid, add student to the validation result
                        validationResult.addValidStudent(student);
                        validationResult.incrementSuccess();
                    } else {
                        // If validation fails, increment failure count
                        validationResult.incrementFailure();
                    }
                } catch (Exception e) {
                    // In case of any errors, increment failure and log error details
                    validationResult.incrementFailure();
                    String errorMessage = String.format("Row %d: Error processing data - %s%n", 
                        rowNumber, e.getMessage());
                    validationResult.getErrorDetails().append(errorMessage);
                }
                rowNumber++;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage());
        }

        // Return the result of the validation
        return validationResult;
    }
}
