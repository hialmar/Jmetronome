package net.torguet.xlsx.list;

import net.torguet.cal.Calendrier;
import net.torguet.cal.Cours;
import net.torguet.cal.Jour;
import net.torguet.cal.Semaine;
import net.torguet.xlsx.old.OldReader;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.Locale;

public class ListWriter {
    XSSFWorkbook workbook;
    XSSFSheet spreadsheet;
    Calendrier calendrier;
    int currentRowNumber = 0;
    XSSFCellStyle dateStyle;

    public ListWriter(Calendrier calendrier) {
        this.calendrier = calendrier;
        workbook = new XSSFWorkbook();
        spreadsheet = workbook.createSheet("Liste");
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
        int startCell = 0;
        XSSFRow row = spreadsheet.createRow(currentRowNumber++);
        XSSFCell cell = row.createCell(startCell++);
        cell.setCellValue(cours.getIntitule());
        cell = row.createCell(startCell++);
        cell.setCellValue(cours.getEnseignant());
        cell = row.createCell(startCell++);
        cell.setCellValue(cours.getDebut().getHour()+":"+cours.getDebut().getMinute());
        cell = row.createCell(startCell++);
        cell.setCellValue(cours.getDuree());
        cell = row.createCell(startCell++);
        cell.setCellValue(cours.getSalle());
        cell = row.createCell(startCell++);
        cell.setCellValue(cours.getGroupe());
        cell = row.createCell(startCell++);
        cell.setCellValue(cours.getType().toString());
        cell = row.createCell(startCell++);
    }


    private void generateJourDebut(Jour jour) {
        XSSFRow row = spreadsheet.createRow(currentRowNumber++);
        ZonedDateTime date = jour.getDate();
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        XSSFCell cell = row.createCell(0);
        cell.setCellValue(LocalDateTime.from(date));
        cell.setCellStyle(dateStyle);
        cell = row.createCell(1);
        cell.setCellValue(dayOfWeek.getDisplayName(TextStyle.FULL_STANDALONE, Locale.FRANCE));
    }

    private void generateSemaineDebut(Semaine semaine) {
        XSSFRow row = spreadsheet.createRow(currentRowNumber++);
        int cellNumber = 0;
        XSSFCell cell = row.createCell(cellNumber++);
        cell.setCellValue("Semaine : ");
        cell = row.createCell(cellNumber++);
        int numSemaine = semaine.getSemaine();
        cell.setCellValue(numSemaine);
    }


    public static void main(String[] args)throws Exception {
        OldReader oldReader;

        int level = 5;
        oldReader = switch (level) {
            case 3 -> // L3
                    new OldReader("EDT S5 STRI 1A L3 2024-2025.xlsx", 0);
            case 4 -> // M1
                    new OldReader("2024-2025 M1.xlsx", 0); // M2
            default -> new OldReader("2024-2025 Master.xlsx", 1);
        };


        Calendrier calendrier = oldReader.traiterFichier();

        oldReader.close();


        ListWriter listWriter = new ListWriter(calendrier);

        listWriter.generate(new File("Liste.xlsx"));
    }

}
