package net.torguet.xlsx.old;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.torguet.cal.Calendrier;
import net.torguet.cal.Cours;
import net.torguet.cal.Jour;
import net.torguet.cal.Semaine;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
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
    XSSFCellStyle topLeftStyle;
    XSSFCellStyle topRightStyle;
    XSSFCellStyle topStyle;
    XSSFCellStyle bottomStyle;
    XSSFCellStyle bottomLeftStyle;
    XSSFCellStyle bottomRightStyle;
    XSSFCellStyle roomStyle;
    XSSFCellStyle missingRoomStyle;
    XSSFCellStyle topBottomStyle;
    XSSFCellStyle leftStyle;
    XSSFCellStyle rightStyle;
    XSSFCellStyle simpleRoomStyle;
    XSSFCellStyle simpleMissingRoomStyle;

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
        topLeftStyle = workbook.createCellStyle();
        topLeftStyle.setBorderTop(BorderStyle.MEDIUM);
        topLeftStyle.setBorderLeft(BorderStyle.MEDIUM);
        topRightStyle = workbook.createCellStyle();
        topRightStyle.setBorderTop(BorderStyle.MEDIUM);
        topRightStyle.setBorderRight(BorderStyle.MEDIUM);
        topStyle = workbook.createCellStyle();
        topStyle.setBorderTop(BorderStyle.MEDIUM);
        bottomStyle = workbook.createCellStyle();
        bottomStyle.setBorderBottom(BorderStyle.MEDIUM);
        bottomLeftStyle = workbook.createCellStyle();
        bottomLeftStyle.setBorderLeft(BorderStyle.MEDIUM);
        bottomLeftStyle.setBorderBottom(BorderStyle.MEDIUM);
        bottomRightStyle = workbook.createCellStyle();
        bottomRightStyle.setBorderBottom(BorderStyle.MEDIUM);
        bottomRightStyle.setBorderRight(BorderStyle.MEDIUM);
        bottomRightStyle.setFillBackgroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
        bottomRightStyle.setFillPattern(FillPatternType.LESS_DOTS);
        roomStyle = workbook.createCellStyle();
        roomStyle.setBorderBottom(BorderStyle.MEDIUM);
        roomStyle.setFillBackgroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
        roomStyle.setFillPattern(FillPatternType.LESS_DOTS);
        missingRoomStyle = workbook.createCellStyle();
        missingRoomStyle.setBorderBottom(BorderStyle.MEDIUM);
        missingRoomStyle.setFillBackgroundColor(IndexedColors.RED.getIndex());
        missingRoomStyle.setFillPattern(FillPatternType.LESS_DOTS);
        topBottomStyle = workbook.createCellStyle();
        topBottomStyle.setBorderTop(BorderStyle.MEDIUM);
        topBottomStyle.setBorderBottom(BorderStyle.MEDIUM);
        leftStyle = workbook.createCellStyle();
        leftStyle.setBorderTop(BorderStyle.MEDIUM);
        leftStyle.setBorderBottom(BorderStyle.MEDIUM);
        leftStyle.setBorderLeft(BorderStyle.MEDIUM);
        rightStyle = workbook.createCellStyle();
        rightStyle.setBorderTop(BorderStyle.MEDIUM);
        rightStyle.setBorderBottom(BorderStyle.MEDIUM);
        rightStyle.setBorderRight(BorderStyle.MEDIUM);
        rightStyle.setFillBackgroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
        rightStyle.setFillPattern(FillPatternType.LESS_DOTS);
        simpleRoomStyle = workbook.createCellStyle();
        simpleRoomStyle.setBorderTop(BorderStyle.MEDIUM);
        simpleRoomStyle.setBorderBottom(BorderStyle.MEDIUM);
        simpleRoomStyle.setFillBackgroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
        simpleRoomStyle.setFillPattern(FillPatternType.LESS_DOTS);
        simpleMissingRoomStyle = workbook.createCellStyle();
        simpleMissingRoomStyle.setBorderTop(BorderStyle.MEDIUM);
        simpleMissingRoomStyle.setBorderBottom(BorderStyle.MEDIUM);
        simpleMissingRoomStyle.setFillBackgroundColor(IndexedColors.RED.getIndex());
        simpleMissingRoomStyle.setFillPattern(FillPatternType.LESS_DOTS);
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
            cell.setCellStyle(topLeftStyle);

            XSSFCell cellEnseignant = rowBas.createCell(startCell);
            cellEnseignant.setCellValue(cours.getEnseignant());
            cellEnseignant.setCellStyle(bottomLeftStyle);

            int cellSalle = startCell + (int)(cours.getDuree()*4) - 2;

            for(int i=startCell+1; i < cellSalle; i++) {
                XSSFCell cellTop = rowHaut.createCell(i);
                cellTop.setCellStyle(topStyle);
                XSSFCell cellBottom = rowBas.createCell(i);
                cellBottom.setCellStyle(bottomStyle);
            }

            XSSFCell salle = rowBas.createCell(cellSalle);
            salle.setCellValue(cours.getSalle());
            if (cours.getSalle() == null) {
                salle.setCellStyle(missingRoomStyle);
            } else {
                salle.setCellStyle(roomStyle);
            }
            XSSFCell salle2 = rowBas.createCell(cellSalle+1);
            salle2.setCellStyle(bottomRightStyle);

            XSSFCell topRight1Cell = rowHaut.createCell(cellSalle);
            topRight1Cell.setCellStyle(topStyle);
            XSSFCell topRight2Cell = rowHaut.createCell(cellSalle+1);
            topRight2Cell.setCellStyle(topRightStyle);
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
            generateCoursGroupeHelper(cours, rowHaut);
        } else {
            generateCoursGroupeHelper(cours, rowBas);
        }
    }

    private void generateCoursGroupeHelper(Cours cours, XSSFRow rowHaut) {
        int startCell = computeStartCell(cours);
        XSSFCell cell = rowHaut.createCell(startCell);
        cell.setCellValue(cours.getIntitule());
        cell.setCellStyle(leftStyle);
        int cellSalle = startCell + (int)(cours.getDuree()*4) - 2;
        for(int i=startCell+1; i < cellSalle; i++) {
            XSSFCell cellTop = rowHaut.createCell(i);
            cellTop.setCellStyle(topBottomStyle);
        }
        XSSFCell salle = rowHaut.createCell(cellSalle);
        if (cours.getSalle() == null) {
            salle.setCellStyle(simpleMissingRoomStyle);
        } else {
            salle.setCellStyle(simpleRoomStyle);
        }
        salle.setCellValue(cours.getSalle());
        XSSFCell last = rowHaut.createCell(cellSalle+1);
        last.setCellStyle(rightStyle);
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

    private void generateSemaineDebut(Semaine semaine) {
        XSSFRow row = spreadsheet.createRow(currentRowNumber++);
        int cellNumber = 1;
        XSSFCell cell = row.createCell(cellNumber++);
        int numSemaine = semaine.getSemaine();
        cell.setCellValue(numSemaine);
        XSSFCellStyle style = workbook.createCellStyle();
        //style.setAlignment(HorizontalAlignment.FILL);
        for (int i = 745; i < 2000; i+=15) {
            if (i%100==60) i += 40;
            cell = row.createCell(cellNumber++);
            cell.setCellStyle(style);
            if(i%100 == 0) {
                cell.setCellValue(i/100+"h");
            } else {
                // style
            }
        }
    }


    public static void main(String[] args)throws Exception {
        OldReader oldReader = null;
        Calendrier calendrier = null;
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(ZonedDateTime.class, new TypeAdapter<ZonedDateTime>() {
                    @Override
                    public void write(JsonWriter out, ZonedDateTime value) throws IOException {
                        out.value(value.toString());
                    }

                    @Override
                    public ZonedDateTime read(JsonReader in) throws IOException {
                        return ZonedDateTime.parse(in.nextString());
                    }
                })
                .enableComplexMapKeySerialization()
                .create();

        int level = 7;
        oldReader = switch (level) {
            case 3 -> // L3
                    new OldReader("EDT S5 STRI 1A L3 2024-2025.xlsx", 0);
            case 4 -> // M1
                    new OldReader("2024-2025 M1.xlsx", 0);
            // M2
            case 5 -> new OldReader("2024-2025 Master.xlsx", 1);
            // M2 Celcat
            case 6 -> new OldReader("2025-2026 Celcat.xlsx", 1);
            default -> null;
        };

        if (oldReader == null) {
            // re-read JSON file
            FileReader fileReader = new FileReader("calendrier.json");
            calendrier = gson.fromJson(fileReader, Calendrier.class);
            fileReader.close();
        } else {
            calendrier = oldReader.traiterFichier();
            oldReader.close();
        }

        OldWriter oldWriter = new OldWriter(calendrier);

        oldWriter.generate(new File("old.xlsx"));


    }

}
