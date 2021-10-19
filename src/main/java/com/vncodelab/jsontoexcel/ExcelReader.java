package com.vncodelab.jsontoexcel;

import com.vncodelab.model.ctdt.HocPhan;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class ExcelReader<T> {
    public static void main(String[] args) {
        ExcelReader excelReader = new ExcelReader<HocPhan>();

        try {
            excelReader.readExcel("/Users/xuanlam/Google Drive/Thông tin.xlsx", 0);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public ArrayList<HashMap<String, Object>> readExcel(String filename, int sheetNo) throws IOException {

        FileInputStream fis = new FileInputStream(filename);
        XSSFWorkbook wb = new XSSFWorkbook(fis);
        XSSFSheet sheet = wb.getSheetAt(sheetNo);
        FormulaEvaluator formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();
        int stt = 0;
        ArrayList<HashMap<String, Object>> list = new ArrayList<>();
        HashMap<String, Object> header = new HashMap<>();
        for (Row row : sheet) {
            Cell cell = null;
            int col = 0;
            if (stt++ == 0) {  //Header
                do {
                    cell = row.getCell(col++);
                    if (cell != null) {
                        String cellValue = getValue(formulaEvaluator, cell).toString();
                        header.put("" + col, cellValue);
                    }
                } while (cell != null);
            } else { //Rows
                HashMap<String, Object> line = new HashMap<>();
                do {
                    cell = row.getCell(col++);
                    if (cell != null) {
                        try {
                            Object cellValue = getValue(formulaEvaluator, cell);
                            line.put((String) header.get("" + col), cellValue);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                } while (cell != null);
                list.add(line);
            }
        }
        for (Object o : list) {
            System.out.println(o);
        }
        return list;
    }





    int getIntegerValue(FormulaEvaluator formulaEvaluator, Cell cell) {
        if (formulaEvaluator.evaluateInCell(cell) == null)
            return 0;
        else
            switch (formulaEvaluator.evaluateInCell(cell).getCellTypeEnum()) {
                case NUMERIC:
                    return (int) Math.round(cell.getNumericCellValue());
                case STRING:
                    if (cell.getStringCellValue().isEmpty())
                        return 0;
                    else
                        return Integer.parseInt(cell.getStringCellValue());
            }
        return 0;
    }

    Double getDoubleValue(FormulaEvaluator formulaEvaluator, Cell cell) {
        if (formulaEvaluator.evaluateInCell(cell) == null)
            return null;
        else
            switch (formulaEvaluator.evaluateInCell(cell).getCellTypeEnum()) {
                case NUMERIC:
                    return cell.getNumericCellValue();
                case STRING:
                    if (cell.getStringCellValue().trim().isEmpty())
                        return 0.0;
                    else
                        return Double.parseDouble(cell.getStringCellValue());
                case BLANK:
                    return 0.0;
            }
        return null;
    }

    Serializable getValue(FormulaEvaluator formulaEvaluator, Cell cell) {
        switch (cell.getCellTypeEnum()) {
            case FORMULA:
                switch (cell.getCachedFormulaResultTypeEnum()) {
                    case NUMERIC:
                        Double value = cell.getNumericCellValue();
                        if (value.doubleValue() % 1 == 0)
                            return value.intValue();
                        else
                            return value;
                    case STRING:
                        return cell.getStringCellValue();
                    default:
                        return null;
                }
            case NUMERIC:
                Double value = cell.getNumericCellValue();
                if (value.doubleValue() % 1 == 0)
                    return value.intValue();
                else
                    return value;
            case STRING:
                return cell.getStringCellValue();
        }
        return null;
    }
}
