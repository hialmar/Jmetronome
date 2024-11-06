package net.torguet.xlsx.display;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.ss.usermodel.FillPatternType;

public class CellStyle {
    public static void main(String[] args)throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet spreadsheet = workbook.createSheet("cellstyle");
        XSSFRow row = spreadsheet.createRow((short) 1);
        row.setHeight((short) 800);
        XSSFCell cell = (XSSFCell) row.createCell((short) 1);
        cell.setCellValue("test of merging");

        //MEARGING CELLS
        //this statement for merging cells

        spreadsheet.addMergedRegion(
                new CellRangeAddress(
                        1, //first row (0-based)
                        1, //last row (0-based)
                        1, //first column (0-based)
                        4 //last column (0-based)
                )
        );

        //CELL Alignment
        row = spreadsheet.createRow(5);
        cell = (XSSFCell) row.createCell(0);
        row.setHeight((short) 800);

        // Top Left alignment
        XSSFCellStyle style1 = workbook.createCellStyle();
        spreadsheet.setColumnWidth(0, 8000);
        style1.setAlignment(HorizontalAlignment.LEFT);
        style1.setVerticalAlignment(VerticalAlignment.TOP);
        cell.setCellValue("Top Left");
        cell.setCellStyle(style1);
        row = spreadsheet.createRow(6);
        cell = (XSSFCell) row.createCell(1);
        row.setHeight((short) 800);

        // Center Align Cell Contents
        XSSFCellStyle style2 = workbook.createCellStyle();
        style2.setAlignment(HorizontalAlignment.CENTER);
        style2.setVerticalAlignment(VerticalAlignment.CENTER);
        cell.setCellValue("Center Aligned");
        cell.setCellStyle(style2);
        row = spreadsheet.createRow(7);
        cell = (XSSFCell) row.createCell(2);
        row.setHeight((short) 800);

        // Bottom Right alignment
        XSSFCellStyle style3 = workbook.createCellStyle();
        style3.setAlignment(HorizontalAlignment.RIGHT);
        style3.setVerticalAlignment(VerticalAlignment.BOTTOM);
        cell.setCellValue("Bottom Right");
        cell.setCellStyle(style3);
        row = spreadsheet.createRow(8);
        cell = (XSSFCell) row.createCell(3);

        // Justified Alignment
        XSSFCellStyle style4 = workbook.createCellStyle();
        style4.setAlignment(HorizontalAlignment.JUSTIFY);
        style4.setVerticalAlignment(VerticalAlignment.JUSTIFY);
        cell.setCellValue("Contents are Justified in Alignment");
        cell.setCellStyle(style4);

        //CELL BORDER
        row = spreadsheet.createRow((short) 9);
        row.setHeight((short) 800);
        cell = (XSSFCell) row.createCell((short) 1);
        cell.setCellValue("BORDER");

        XSSFCellStyle style5 = workbook.createCellStyle();
        style5.setBorderBottom(BorderStyle.THICK);
        style5.setBottomBorderColor(IndexedColors.BLUE.getIndex());
        style5.setBorderLeft(BorderStyle.DOUBLE);
        style5.setLeftBorderColor(IndexedColors.GREEN.getIndex());
        style5.setBorderRight(BorderStyle.HAIR);
        style5.setRightBorderColor(IndexedColors.RED.getIndex());
        style5.setBorderTop(BorderStyle.DOTTED);
        style5.setTopBorderColor(IndexedColors.CORAL.getIndex());
        cell.setCellStyle(style5);

        //Fill Colors
        //background color
        row = spreadsheet.createRow((short) 10 );
        cell = (XSSFCell) row.createCell((short) 1);

        XSSFCellStyle style6 = workbook.createCellStyle();
        style6.setFillBackgroundColor(IndexedColors.LIME.index);
        style6.setFillPattern(FillPatternType.LESS_DOTS);
        style6.setAlignment(HorizontalAlignment.FILL);
        spreadsheet.setColumnWidth(1,8000);
        cell.setCellValue("FILL BACKGROUNG/FILL PATTERN");
        cell.setCellStyle(style6);

        //Foreground color
        row = spreadsheet.createRow((short) 12);
        cell = (XSSFCell) row.createCell((short) 1);

        XSSFCellStyle style7 = workbook.createCellStyle();
        style7.setFillForegroundColor(IndexedColors.BLUE.index);
        style7.setFillPattern( FillPatternType.LESS_DOTS);
        style7.setAlignment(HorizontalAlignment.FILL);
        cell.setCellValue("FILL FOREGROUND/FILL PATTERN");
        cell.setCellStyle(style7);

        FileOutputStream out = new FileOutputStream(new File("cellstyle.xlsx"));
        workbook.write(out);
        out.close();
        System.out.println("cellstyle.xlsx written successfully");
    }
}