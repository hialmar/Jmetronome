package net.torguet.xlsx.base;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;

public class CreateWorkBook {
    public static void main(String[] args)throws Exception {
        //Create Blank workbook
        XSSFWorkbook workbook = new XSSFWorkbook();

        //Create file system using specific name
        FileOutputStream out = new FileOutputStream("createworkbook.xlsx");

        //write operation workbook using file out object
        workbook.write(out);
        out.close();
        System.out.println("createworkbook.xlsx written successfully");
    }
}