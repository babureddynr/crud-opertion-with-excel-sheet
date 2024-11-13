package com.example.sampleproject.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;
import com.example.sampleproject.entity.StudentEntity;

public class ExcelHelper {

    // MIME type for Excel files (xlsx format)
    public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    // Headers to be used in the Excel file (columns)
    static String[] HEADERS = { "Name", "Address", "Image", "Salary", "DOB", "Active Status" };

    // The sheet name for the Excel file
    static String SHEET = "Students";

    public static boolean hasExcelFormat(MultipartFile file) {
        return TYPE.equals(file.getContentType());
    }

    /**
     * Converts a list of StudentEntity objects into an Excel file.
     *
     * @param students The list of students to convert
     * @return ByteArrayInputStream containing the Excel file data
     */
    public static ByteArrayInputStream studentsToExcel(List<StudentEntity> students) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Create a new sheet within the workbook
            Sheet sheet = workbook.createSheet(SHEET);

            // Create a header row at the top (index 0) of the sheet
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < HEADERS.length; col++) {
                // Populate each cell in the header row with the corresponding header from HEADERS array
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(HEADERS[col]);
            }

            // Create a dropdown for the "Active Status" column
            Row dummyRow = sheet.createRow(1); // A dummy row is created to facilitate the dropdown placement
            Cell activeStatusCell = dummyRow.createCell(5); // This sets the dropdown for column index 5 (Active Status)

            // Initialize the DataValidationHelper to create a dropdown list of options
            DataValidationHelper validationHelper = sheet.getDataValidationHelper();
            String[] options = {"Active", "Inactive"}; // Define options for the dropdown

            // Create a constraint with the defined options
            DataValidationConstraint constraint = validationHelper.createExplicitListConstraint(options);

            // Set the cell range for the dropdown; this applies to column index 5 for all rows
            CellRangeAddressList addressList = new CellRangeAddressList(1, sheet.getLastRowNum(), 5, 5);

            // Create and apply the validation for the dropdown
            DataValidation validation = validationHelper.createValidation(constraint, addressList);
            sheet.addValidationData(validation);

            // Add rows for each student's data, starting from row index 2 (to skip headers and dummy row)
            int rowIdx = 2;

            // Define date format for date of birth (DOB) column
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            for (StudentEntity student : students) {
                // Create a new row for each student
                Row row = sheet.createRow(rowIdx++);

                // Populate cells for each field in StudentEntity
                row.createCell(0).setCellValue(student.getName()); // Set student name in first cell (index 0)
                row.createCell(1).setCellValue(student.getAddress()); // Set student address in second cell (index 1)
                row.createCell(2).setCellValue(student.getImage()); // Set image path or URL in third cell (index 2)
                row.createCell(3).setCellValue(student.getSalary() != null ? student.getSalary() : 0.0); // Set salary in fourth cell (index 3)
                
                // Format and set DOB in fifth cell (index 4); leave blank if DOB is null
                row.createCell(4).setCellValue(student.getDob() != null ? student.getDob().format(dateFormatter) : "");

                // Set Active Status in sixth cell (index 5) as "Active" or "Inactive"
                Cell activeStatusCell2 = row.createCell(5);
                activeStatusCell2.setCellValue(student.getIsActive() != null && student.getIsActive() ? "Active" : "Inactive");
            }

            // Write the workbook data to the ByteArrayOutputStream
            workbook.write(out);

            // Return the output as a ByteArrayInputStream for use in the response
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            // Handle any I/O exceptions that occur while writing the Excel file
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
        // Create an ExcelValidationResult object to store validation results
        ExcelValidationResult validationResult = new ExcelValidationResult();

        try (Workbook workbook = new XSSFWorkbook(is)) {
            // Get the sheet with the name specified in SHEET constant
            Sheet sheet = workbook.getSheet(SHEET);
            
            // Initialize an iterator to go through the rows in the sheet
            Iterator<Row> rows = sheet.iterator();

            // Skip the header row if present
            if (rows.hasNext()) {
                rows.next();
            }

            // Start reading data from row index 2, as row 1 is the dummy row for dropdown options
            int rowNumber = 2;

            // Set up a date formatter for parsing dates in "yyyy-MM-dd" format
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // Loop through each row in the Excel sheet
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                StudentEntity student = new StudentEntity(); // Create a new StudentEntity for each row

                try {
                    // Extract each cell's data and set to the corresponding fields in StudentEntity

                    // Set the Name field (column 0)
                    student.setName(getCellValueAsString(currentRow.getCell(0)));

                    // Set the Address field (column 1)
                    student.setAddress(getCellValueAsString(currentRow.getCell(1)));

                    // Set the Image field (column 2)
                    student.setImage(getCellValueAsString(currentRow.getCell(2)));

                    // Extract and parse the Salary field (column 3)
                    Cell salaryCell = currentRow.getCell(3);
                    if (salaryCell != null && salaryCell.getCellType() == CellType.NUMERIC) {
                        // If the cell is numeric, set the Salary field
                        student.setSalary(salaryCell.getNumericCellValue());
                    } else {
                        // Set Salary to null if the cell is empty or not numeric
                        student.setSalary(null);
                    }

                    // Extract and parse the DOB field (column 4) as a date string
                    String dobStr = getCellValueAsString(currentRow.getCell(4));
                    if (!dobStr.isEmpty()) {
                        // Parse DOB only if the date string is not empty
                        student.setDob(LocalDate.parse(dobStr, dateFormatter));
                    }

                    // Extract and parse the Active Status field (column 5) as a boolean
                    String isActiveStr = getCellValueAsString(currentRow.getCell(5));
                    if (!isActiveStr.isEmpty()) {
                        // Convert to boolean if not empty ("true" or "false")
                        student.setIsActive(Boolean.parseBoolean(isActiveStr));
                    }

                    // Validate the student data
                    if (ExcelValidationResult.StudentValidator.isValid(student, validationResult, rowNumber)) {
                        // If valid, add the student to valid students and increment success count
                        validationResult.addValidStudent(student);
                        validationResult.incrementSuccess();
                    } else {
                        // Increment failure count if validation fails
                        validationResult.incrementFailure();
                    }
                } catch (Exception e) {
                    // Handle exceptions for any errors while processing the row data
                    validationResult.incrementFailure(); // Increment failure count
                    String errorMessage = String.format("Row %d: Error processing data - %s%n", rowNumber, e.getMessage());
                    validationResult.getErrorDetails().append(errorMessage); // Log detailed error message
                }

                rowNumber++; // Move to the next row
            }
        } catch (IOException e) {
            // Handle I/O exceptions (e.g., if the file cannot be read)
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage());
        }

        // Return the validation result containing success, failure counts, and error details
        return validationResult;
    }

    // Helper method to retrieve cell values as strings for various cell types
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return ""; // Return empty string if cell is null

        // Check if the cell contains a numeric value
        if (cell.getCellType() == CellType.NUMERIC) {
            // If the numeric cell is a date, format it as "yyyy-MM-dd"
            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } else {
                // If not a date, return the numeric value as an integer string
                return String.valueOf((int) cell.getNumericCellValue());
            }
        } else {
            // If the cell type is a string, return the string value, otherwise return an empty string
            return cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : "";
        }
    }
}