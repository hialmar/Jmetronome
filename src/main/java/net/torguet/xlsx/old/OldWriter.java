package net.torguet.xlsx.old;

import net.torguet.cal.Calendrier;
import net.torguet.cal.Cours;
import net.torguet.cal.Jour;
import net.torguet.cal.Semaine;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.Locale;

public class OldWriter {
    XSSFWorkbook workbook;
    XSSFSheet spreadsheet;
    Calendrier calendrier;
    int currentRowNumber = 0;
    XSSFRow rowHaut;
    XSSFRow rowBas;
    XSSFCellStyle dateStyle;

    public OldWriter(Calendrier calendrier) {
        this.calendrier = calendrier;
        workbook = new XSSFWorkbook();
        spreadsheet = workbook.createSheet("old");
        spreadsheet.setColumnWidth(0, 10 * 256);
        spreadsheet.setColumnWidth(1, 10 * 256);
        for(int i=2; i<100; i++) {
            spreadsheet.setColumnWidth(i, 4*256);
        }
        dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(16); // d-mmm
    }

    public void generate(File file) throws IOException {
        for(var semaine : calendrier.getSemaines()) {
            generateSemaineDebut(semaine);
            for (var jour : semaine.getJours()) {
                generateJourDebut(jour);
                for (var cours : jour.getCours()) {
                    generateCours(cours);
                }
            }
        }

        FileOutputStream out = new FileOutputStream(file);
        workbook.write(out);
        out.close();
        System.out.println("workbook written successfully");
    }

    private void generateCours(Cours cours) {
        if (cours.isEnParallele()) {
            generateCoursGroupe(cours);
        } else {
            int startCell = computeStartCell(cours);
            XSSFCell cell = rowHaut.createCell(startCell);
            cell.setCellValue(cours.getIntitule());

            XSSFCell cellBas = rowBas.createCell(startCell);
            cellBas.setCellValue(cours.getEnseignant());

            int cellSalle = startCell + (int)(cours.getDuree()*4) - 2;
            XSSFCell salle = rowBas.createCell(cellSalle);
            salle.setCellValue(cours.getSalle());
        }
    }

    private int computeStartCell(Cours cours) {
        ZonedDateTime date = cours.getDebut();
        int heure = date.getHour();
        int minute = date.getMinute();
        int startCell = 2; // 2 : 7h45
        if (heure > 7) {
            startCell = 3 + (heure - 8)*4;
            startCell += (minute / 15);
        }
        return startCell;
    }

    private void generateCoursGroupe(Cours cours) {
        if (cours.getGroupe().equals("1")) {
            int startCell = computeStartCell(cours);
            XSSFCell cell = rowHaut.createCell(startCell);
            cell.setCellValue(cours.getIntitule());
        } else {
            int startCell = computeStartCell(cours);
            XSSFCell cell = rowBas.createCell(startCell);
            cell.setCellValue(cours.getIntitule());
        }
    }

    private void generateJourDebut(Jour jour) {
        rowHaut = spreadsheet.createRow(currentRowNumber++);
        rowBas = spreadsheet.createRow(currentRowNumber ++);
        ZonedDateTime date = jour.getDate();
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        if (dayOfWeek == DayOfWeek.MONDAY) {
            XSSFCell cell = rowHaut.createCell(0);
            cell.setCellValue(LocalDateTime.from(date));
            cell.setCellStyle(dateStyle);
        }
        XSSFCell cell = rowHaut.createCell(1);
        cell.setCellValue(dayOfWeek.getDisplayName(TextStyle.FULL_STANDALONE, Locale.FRANCE));
    }

    private final int [] heuresDebut = {745, 800, 900, 1000, 1300, 1330, 1545, 1615, 1800};
    private final int [] debutsCours = {2, 3, 7, 11, 23, 25, 34, 36, 43};


    private void generateSemaineDebut(Semaine semaine) {
        XSSFRow row = spreadsheet.createRow(currentRowNumber++);
        int cellNumber = 1;
        XSSFCell cell = (XSSFCell) row.createCell(cellNumber++);
        int numSemaine = semaine.getSemaine();
        cell.setCellValue(numSemaine);
        XSSFCellStyle style = workbook.createCellStyle();
        //style.setAlignment(HorizontalAlignment.FILL);
        for (int i = 745; i < 2000; i+=15) {
            if (i%100==60) i += 40;
            cell = (XSSFCell) row.createCell(cellNumber++);
            cell.setCellStyle(style);
            if(i%100 == 0) {
                cell.setCellValue(""+i/100+"h");
            } else {
                // style
            }
        }
    }


    public static void main(String[] args)throws Exception {
        OldReader oldReader = new OldReader("EDT S5 STRI 1A L3 2024-2025.xlsx", 0);

        Calendrier calendrier = oldReader.traiterFichier();

        oldReader.close();


        OldWriter oldWriter = new OldWriter(calendrier);

        oldWriter.generate(new File("old.xlsx"));


    }

}
