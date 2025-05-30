package net.torguet.xlsx.stats;

import net.torguet.cal.*;
import net.torguet.xlsx.old.OldReader;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class StatsWriter {
    XSSFWorkbook workbook;
    XSSFSheet spreadsheet;
    Calendrier calendrier;
    int currentRowNumber = 0;
    ArrayList<String> enseignants = new ArrayList<>();
    ArrayList<String> groupes;

    public StatsWriter(Calendrier calendrier) {
        this.calendrier = calendrier;
        workbook = new XSSFWorkbook();
        spreadsheet = workbook.createSheet("Stats");
    }

    public void generate(File file) throws IOException {
        this.generate(file, null, true);
    }

    public void generate(File file, Cours matcher, boolean matchAny) throws IOException {
        StatisticsGenerator statisticsGenerator = new StatisticsGenerator(calendrier);
        statisticsGenerator.generate(matcher, matchAny);
        groupes = statisticsGenerator.getGroupes();

        var heuresCours = statisticsGenerator.getHeuresCours();
        var heuresTD = statisticsGenerator.getHeuresTD();
        var heuresTP = statisticsGenerator.getHeuresTP();
        var heuresAutres = statisticsGenerator.getHeuresAutres();
        var heuresControle = statisticsGenerator.getHeuresControle();

        generateStatsDebut();

        for(String enseignant : heuresCours.keySet()) {
            if (!enseignants.contains(enseignant)) {
                enseignants.add(enseignant);
            }
            float cours = heuresCours.get(enseignant);
            float td = 0;
            if (heuresTD.containsKey(enseignant))
                td = heuresTD.get(enseignant);
            float tp = 0;
            if (heuresTP.containsKey(enseignant))
                tp = heuresTP.get(enseignant);
            float autres = 0;
            if (heuresAutres.containsKey(enseignant))
                autres = heuresAutres.get(enseignant);
            float controle = 0;
            if (heuresControle.containsKey(enseignant))
                controle = heuresControle.get(enseignant);
            generateStatsEnseignant(enseignant, cours, td, tp, autres, controle);
        }

        for(String enseignant : heuresTD.keySet()) {
            if (!enseignants.contains(enseignant)) {
                enseignants.add(enseignant);
            }
            if (!heuresCours.containsKey(enseignant)) {
                float cours = 0;
                float td = 0;
                if (heuresTD.containsKey(enseignant))
                    td = heuresTD.get(enseignant);
                float tp = 0;
                if (heuresTP.containsKey(enseignant))
                    tp = heuresTP.get(enseignant);
                float autres = 0;
                if (heuresAutres.containsKey(enseignant))
                    autres = heuresAutres.get(enseignant);
                float controle = 0;
                if (heuresControle.containsKey(enseignant))
                    controle = heuresControle.get(enseignant);
                generateStatsEnseignant(enseignant, cours, td, tp, autres, controle);
            }
        }

        for(String enseignant : heuresTP.keySet()) {
            if (!enseignants.contains(enseignant)) {
                enseignants.add(enseignant);
            }
            if (!heuresCours.containsKey(enseignant) && !heuresTD.containsKey(enseignant)) {
                float cours = 0;
                float td = 0;
                float tp = 0;
                if (heuresTP.containsKey(enseignant))
                    tp = heuresTP.get(enseignant);
                float autres = 0;
                if (heuresAutres.containsKey(enseignant))
                    autres = heuresAutres.get(enseignant);
                float controle = 0;
                if (heuresControle.containsKey(enseignant))
                    controle = heuresControle.get(enseignant);
                generateStatsEnseignant(enseignant, cours, td, tp, autres, controle);
            }
        }

        for(String enseignant : heuresAutres.keySet()) {
            if (!enseignants.contains(enseignant)) {
                enseignants.add(enseignant);
            }
            if (!heuresCours.containsKey(enseignant) && !heuresTD.containsKey(enseignant) && !heuresTP.containsKey(enseignant)) {
                float cours = 0;
                float td = 0;
                float tp = 0;
                float autres = 0;
                if (heuresAutres.containsKey(enseignant))
                    autres = heuresAutres.get(enseignant);
                float controle = 0;
                if (heuresControle.containsKey(enseignant))
                    controle = heuresControle.get(enseignant);
                generateStatsEnseignant(enseignant, cours, td, tp, autres, controle);
            }
        }

        for(String enseignant : heuresControle.keySet()) {
            if (!heuresCours.containsKey(enseignant) && !heuresTD.containsKey(enseignant) &&
                    !heuresTP.containsKey(enseignant) && !heuresAutres.containsKey(enseignant)) {
                float cours = 0;
                float td = 0;
                float tp = 0;
                float autres = 0;
                float controle = 0;
                if (heuresControle.containsKey(enseignant))
                    controle = heuresControle.get(enseignant);
                generateStatsEnseignant(enseignant, cours, td, tp, autres, controle);
            }
        }

        FileOutputStream out = new FileOutputStream(file);
        workbook.write(out);
        out.close();
        System.out.println("workbook written successfully");
    }

    private void generateStatsEnseignant(String enseignant, float heuresCours,
                                         float heuresTD, float heuresTP, float heuresAutres, float heuresControle)
    {
        XSSFRow row = spreadsheet.createRow(currentRowNumber++);
        int cellNumber = 0;
        XSSFCell cell = row.createCell(cellNumber++);
        cell.setCellValue(enseignant);
        XSSFCellStyle style = workbook.createCellStyle();
        //style.setAlignment(HorizontalAlignment.FILL);
        cell = row.createCell(cellNumber++);
        cell.setCellStyle(style);
        cell.setCellValue(heuresCours);
        cell = row.createCell(cellNumber++);
        cell.setCellStyle(style);
        cell.setCellValue(heuresTD);
        cell = row.createCell(cellNumber++);
        cell.setCellStyle(style);
        cell.setCellValue(heuresTP);
        cell = row.createCell(cellNumber++);
        cell.setCellStyle(style);
        cell.setCellValue(heuresAutres);
        cell = row.createCell(cellNumber++);
        cell.setCellStyle(style);
        cell.setCellValue(heuresControle);
    }


    private void generateStatsDebut() {
        XSSFRow row = spreadsheet.createRow(currentRowNumber++);
        int cellNumber = 0;
        XSSFCell cell = row.createCell(cellNumber++);
        cell.setCellValue("Enseignant");
        XSSFCellStyle style = workbook.createCellStyle();
        //style.setAlignment(HorizontalAlignment.FILL);
        cell = row.createCell(cellNumber++);
        cell.setCellStyle(style);
        cell.setCellValue("Cours");
        cell = row.createCell(cellNumber++);
        cell.setCellStyle(style);
        cell.setCellValue("TD");
        cell = row.createCell(cellNumber++);
        cell.setCellStyle(style);
        cell.setCellValue("TP");
        cell = row.createCell(cellNumber++);
        cell.setCellStyle(style);
        cell.setCellValue("Autre");
        cell = row.createCell(cellNumber++);
        cell.setCellStyle(style);
        cell.setCellValue("Contrôle");
    }


    public static void main(String[] args)throws Exception {
        OldReader oldReader;

        int level = 6;
        oldReader = switch (level) {
            case 3 -> // L3
                    new OldReader("EDT S5 STRI 1A L3 2024-2025.xlsx", 0);
            case 4 -> // M1
                    new OldReader("2024-2025 M1.xlsx", 0);
            // M2
            case 5 -> new OldReader("2024-2025 Master.xlsx", 1);
            default -> new OldReader("2025-2026 Celcat.xlsx", 1);
        };

        Calendrier calendrier = oldReader.traiterFichier();

        oldReader.close();


        StatsWriter statsWriter = new StatsWriter(calendrier);

        statsWriter.generate(new File("stats.xlsx"));

    }

    public ArrayList<String> getEnseignants() {
        return enseignants;
    }

    public ArrayList<String> getGroupes() {
        return groupes;
    }
}
