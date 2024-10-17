package net.torguet.xlsx.old;

import java.io.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

public class ReadOldExcelFile {

    /**
     * Get the cell range of the merged cell in all the merged cells
     * if the given cell is in a merged cell.
     * Otherwise, it will return null.
     *
     * @param sheet  The Sheet object
     * @param row    The row number of this cell
     * @param column The column number of this cell
     * @return The cell range or null
     */
    private static CellRangeAddress getCellRangeIfCellIsInMergedCells(Sheet sheet, int row, int column) {
        int numberOfMergedRegions = sheet.getNumMergedRegions();

        for (int i = 0; i < numberOfMergedRegions; i++) {
            CellRangeAddress mergedCell = sheet.getMergedRegion(i);

            if (mergedCell.isInRange(row, column)) {
                return mergedCell;
            }
        }

        return null;
    }

    /**
     * Get the value from a merged cell
     *
     * @param sheet       The Sheet object
     * @param mergedCells The {@link CellRangeAddress} object fetched from {@link Sheet#getMergedRegion(int)} method
     * @return The content in this merged cell
     */
    private static String readContentFromMergedCells(Sheet sheet, CellRangeAddress mergedCells) {

        if (mergedCells.getFirstRow() != mergedCells.getLastRow()) {
            return null;
        }

        return sheet.getRow(mergedCells.getFirstRow()).getCell(mergedCells.getFirstColumn()).getStringCellValue();
    }



    public static void main(String[] args) throws IOException {
        FileInputStream fis = new FileInputStream("2024-2025 Master.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(fis);
        XSSFSheet spreadsheet = workbook.getSheetAt(1);

        var cell = spreadsheet.getRow(6).getCell(0);

        System.out.println(cell.getCellType() + " " + cell.getDateCellValue());
        printInfos(cell, spreadsheet);


        recupJour(spreadsheet, 6);

        recupJour(spreadsheet, 8);

        /*
        Iterator<Row> rowIterator = spreadsheet.iterator();


        while (rowIterator.hasNext()) {
            var row = (XSSFRow) rowIterator.next();
            Iterator <Cell>  cellIterator = row.cellIterator();

            while ( cellIterator.hasNext()) {
                var cell = cellIterator.next();

                System.out.println(cell.getCellStyle().getBorderLeft().getCode());

                switch (cell.getCellType()) {
                    case NUMERIC:
                        System.out.print(cell.getNumericCellValue() + " \t\t ");
                        break;

                    case STRING:
                        System.out.print(
                                cell.getStringCellValue() + " \t\t ");
                        break;
                }
            }
            System.out.println();
        }*/
        fis.close();
    }

    private static void recupJour(XSSFSheet spreadsheet, int startRow) {
        System.out.println("recupJour");
        var cell = spreadsheet.getRow(startRow).getCell(1);

        printInfos(cell, spreadsheet);

        // Créneau 7h45
        recupInfosCours(spreadsheet, startRow, 2);

        // Créneau 10h
        recupInfosCours(spreadsheet, startRow, 11);

        // Créneau 13h30
        recupInfosCours(spreadsheet, startRow, 25);

        // Créneau 13h30
        recupInfosCours(spreadsheet, startRow, 34);
    }

    private static void recupInfosCours(XSSFSheet spreadsheet, int startRow, int startColumn) {
        System.out.println("recupInfosCours");
        XSSFCell cell;
        cell = spreadsheet.getRow(startRow).getCell(startColumn);

        printInfos(cell, spreadsheet);

        cell = spreadsheet.getRow(startRow+1).getCell(startColumn);

        printInfos(cell, spreadsheet);

        cell = spreadsheet.getRow(startRow+1).getCell(startColumn+6);

        printInfos(cell, spreadsheet);
    }

    private static void printInfos(XSSFCell cell, XSSFSheet spreadsheet) {
        switch (cell.getCellType()) {
            case NUMERIC:
                System.out.print(cell.getNumericCellValue() + " \t\t ");
                break;

            case STRING:
                System.out.print(
                        cell.getStringCellValue() + " \t\t ");
                break;
        }
        System.out.println(cell.getCellType() + " " +
                cell.getCellStyle().getBorderTop() + " " +
                cell.getCellStyle().getBorderBottom() + " " +
                cell.getCellStyle().getBorderLeft() + " " +
                cell.getCellStyle().getBorderRight() + " ");

        if (getCellRangeIfCellIsInMergedCells(spreadsheet, cell.getRowIndex(), cell.getColumnIndex())!=null)
            System.out.println("Merged");
        else
            System.out.println("Not Merged");
    }
}
