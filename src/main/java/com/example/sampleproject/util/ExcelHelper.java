package com.example.sampleproject.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Apache POI imports
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.web.multipart.MultipartFile;
import com.example.sampleproject.entity.StudentEntity;

public class ExcelHelper {
    // Define the MIME type for an Excel file in the .xlsx format
    public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    // Define headers for the Excel columns
    static String[] HEADERS = { "Name", "Address", "Image" };
    
    // Define the name of the Excel sheet
    static String SHEET = "Students";

    // Check if the uploaded file is of Excel format by comparing MIME type
    public static boolean hasExcelFormat(MultipartFile file) {
        return TYPE.equals(file.getContentType());
    }

    // Method to generate an Excel file with student data or an empty template
    public static ByteArrayInputStream studentsToExcel(List<StudentEntity> students) {
        // Initialize workbook and output stream
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Create a sheet with the specified name
            Sheet sheet = workbook.createSheet(SHEET);

            // Header Row - Creates the first row in the sheet for column headers
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < HEADERS.length; col++) {
                // Create a cell for each header and set its value
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(HEADERS[col]);
            }

            // Check if the list contains any student data
            int rowIdx = 1;
            for (StudentEntity student : students) {
                // Create a new row for each student in the list
                Row row = sheet.createRow(rowIdx++);
                
                // Set cell values with student properties
                row.createCell(0).setCellValue(student.getName());
                row.createCell(1).setCellValue(student.getAddress());
                row.createCell(2).setCellValue(student.getImage());
            }

            // Write workbook contents to the ByteArrayOutputStream
            workbook.write(out);
            
            // Return the written Excel content as a ByteArrayInputStream
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to export data to Excel file: " + e.getMessage());
        }
    }

    // Method to read Excel content and convert it to a list of StudentEntity objects
    public static List<StudentEntity> excelToStudents(InputStream is) {
        try (Workbook workbook = new XSSFWorkbook(is)) {
            // Get the specified sheet from the workbook
            Sheet sheet = workbook.getSheet(SHEET);
            Iterator<Row> rows = sheet.iterator();
            List<StudentEntity> students = new ArrayList<>();

            // Skip the header row
            rows.next();

            // Process each row and map it to a StudentEntity object
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                StudentEntity student = new StudentEntity();

                try {
                    // Extract cell data and set values for StudentEntity properties
                    Cell nameCell = currentRow.getCell(0);
                    Cell addressCell = currentRow.getCell(1);
                    Cell imageCell = currentRow.getCell(2);
                    
                  //set values to studentEntity object
                    student.setName(nameCell != null ? nameCell.getStringCellValue() : "");
                    student.setAddress(addressCell != null ? addressCell.getStringCellValue() : "");
                    student.setImage(imageCell != null ? imageCell.getStringCellValue() : "");
                  
                    students.add(student);
                } catch (Exception e) {
                    // Log the error and continue processing subsequent rows
                    System.err.println("Error processing row " + currentRow.getRowNum() + ": " + e.getMessage());
                }
            }

            return students;
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage());
        }
    }
}
