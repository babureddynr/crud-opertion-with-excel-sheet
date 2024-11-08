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
import com.example.sampleproject.entity.Entity;

public class ExcelHelper {
    public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    static String[] HEADERS = { "Name", "Address", "Image" };
    static String SHEET = "Students";

    public static boolean hasExcelFormat(MultipartFile file) {
        return TYPE.equals(file.getContentType());
    }

    public static ByteArrayInputStream studentsToExcel(List<Entity> students) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(SHEET);

            // Header
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < HEADERS.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(HEADERS[col]);
            }

            int rowIdx = 1;
            for (Entity student : students) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(student.getName());
                row.createCell(1).setCellValue(student.getAddress());
                row.createCell(2).setCellValue(student.getImage());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to import data to Excel file: " + e.getMessage());
        }
    }

    public static List<Entity> excelToStudents(InputStream is) {
        try (Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheet(SHEET);
            Iterator<Row> rows = sheet.iterator();
            List<Entity> students = new ArrayList<>();

            // Skip header
            rows.next();

            while (rows.hasNext()) {
                Row currentRow = rows.next();
                Entity student = new Entity();

                try {
                    // Added null checks and error handling
                    Cell nameCell = currentRow.getCell(0);
                    Cell addressCell = currentRow.getCell(1);
                    Cell imageCell = currentRow.getCell(2);

                    student.setName(nameCell != null ? nameCell.getStringCellValue() : "");
                    student.setAddress(addressCell != null ? addressCell.getStringCellValue() : "");
                    student.setImage(imageCell != null ? imageCell.getStringCellValue() : "");

                    students.add(student);
                } catch (Exception e) {
                    // Log the error but continue processing other rows
                    System.err.println("Error processing row " + currentRow.getRowNum() + ": " + e.getMessage());
                }
            }

            return students;
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage());
        }
    }
}