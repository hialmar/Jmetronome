package net.torguet.xlsx.old;

import net.torguet.cal.*;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Iterator;

import static org.apache.poi.ss.usermodel.CellType.*;

public class OldReader {
    private Calendrier calendar;

    private FileInputStream fis;
    private XSSFSheet sheet;

    private Row row;
    private Date date;

    public OldReader(String fileName, int sheetNumber) throws IOException {
        calendar = new Calendrier();
        fis = new FileInputStream(fileName);
        XSSFWorkbook workbook = new XSSFWorkbook(fis);
        sheet = workbook.getSheetAt(sheetNumber);
    }

    public void close() throws IOException {
        fis.close();
    }

    public Calendrier traiterFichier() throws IOException {
        Iterator<Row> rowIterator = sheet.iterator();
        date = searchStartDate(rowIterator);
        if (date != null) {
            calendar.setStartDate(date);

            Semaine semaine;
            do {
                semaine = recupSemaine(rowIterator);
                if (semaine != null) {
                    calendar.addSemaine(semaine);
                }
            } while (semaine != null);
        }
        return calendar;
    }

    private Semaine recupSemaine(Iterator<Row> rowIterator) {
        Semaine semaine = null;
        // le numéro de semaine est au-dessus et à droite
        var cell = sheet.getRow(row.getRowNum()-1).getCell(1);

        while (cell == null || (cell.getCellType() != NUMERIC && cell.getCellType() != FORMULA)) {
            if (!rowIterator.hasNext())
                return null;
            row = rowIterator.next();
            cell = sheet.getRow(row.getRowNum()-1).getCell(1);
        }

        if(cell != null && (cell.getCellType() == NUMERIC || cell.getCellType() == FORMULA)) {
            semaine = new Semaine((int) cell.getNumericCellValue());
            System.out.println("Semaine "+ cell.getNumericCellValue());
            int nbJour = 0;
            while (rowIterator.hasNext()) {
                Jour jour = recupJour(row);
                if (jour != null) {
                    date.setTime(date.getTime()+24*60*60*1000);
                    System.out.println(date);
                    semaine.addJour(jour);
                    nbJour++;
                    if(nbJour>=5) {
                        break;
                    }
                }
                row = rowIterator.next();
            }
            date.setTime(date.getTime()+24*60*60*1000);
            System.out.println(date);
            date.setTime(date.getTime()+24*60*60*1000);
            System.out.println(date);
        }
        return semaine;
    }

    private int [] heuresDebut = {745, 800, 900, 1000, 1330, 1545, 1800};
    private int [] debutsCours = {2, 3, 7, 11, 25, 34, 43};

    private int debutCoursToHeuresDebut(int debut) {
        for(int i = 0; i < debutsCours.length; i++) {
            if (debutsCours[i] == debut) {
                return heuresDebut[i]/100;
            }
        }
        return -1;
    }

    private int debutCoursToMinutesDebut(int debut) {
        for(int i = 0; i < debutsCours.length; i++) {
            if (debutsCours[i] == debut) {
                return heuresDebut[i]%100;
            }
        }
        return -1;
    }

    private Jour recupJour(Row row) {
        Jour jour = null;
        var cell = row.getCell(1);
        if (cell != null && cell.getCellType() == STRING) {
            String jourSemaine  = cell.getStringCellValue();
            System.out.println(jourSemaine);
            jour = new Jour(date);

            // recup cours
            for(int colCours : debutsCours) {
                Cours cours = recupCours(colCours);
                if (cours != null) {
                    jour.addCours(cours);
                }
            }
        }
        return jour;
    }

    private Cours recupCours(int colCours) {
        Cours cours = null;
        System.out.println("recupInfosCours");

        // intitulé cours
        var cellIntitule = row.getCell(colCours);

        if (cellIntitule != null && cellIntitule.getCellType() == STRING) {
            String intitule = cellIntitule.getStringCellValue();

            cours = new Cours(intitule);

            ZonedDateTime d = ZonedDateTime.ofInstant(date.toInstant(),
                    ZoneId.systemDefault());

            int heure = debutCoursToHeuresDebut(colCours);
            d = d.plusHours(heure);
            int minutes = debutCoursToMinutesDebut(colCours);
            d = d.plusMinutes(minutes);

            cours.setDebut(d);

            if (intitule.contains(" C"))
                cours.setType(TypeCours.TYPE_COURS);
            else if (intitule.contains(" TD"))
                cours.setType(TypeCours.TYPE_TD);
            else if (intitule.contains(" TP"))
                cours.setType(TypeCours.TYPE_TP);
            else if (intitule.contains(" BE"))
                cours.setType(TypeCours.TYPE_BE);
            else if (intitule.contains(" Projet"))
                cours.setType(TypeCours.TYPE_PROJET);

            // Contrôle
            if (cellIntitule.getCellStyle() != null &&
            cellIntitule.getCellStyle().getFillBackgroundColor() != 64) {
                System.out.println("Couleur de " + intitule + " : " + cellIntitule.getCellStyle().getFillBackgroundColor());
                cours.setType(TypeCours.TYPE_CONTROLE);
            }
            // enseignant
            Row nextRow = sheet.getRow(row.getRowNum()+1);
            var cellEnseignant = nextRow.getCell(colCours);

            // est-ce que c'est un créneau en //
            if ((cellIntitule.getCellStyle() != null &&
                    cellIntitule.getCellStyle().getBorderBottom() != null &&
                    cellIntitule.getCellStyle().getBorderBottom() != BorderStyle.NONE) ||
                    (cellEnseignant.getCellStyle() != null &&
                            cellEnseignant.getCellStyle().getBorderTop() != null &&
                            cellEnseignant.getCellStyle().getBorderTop() != BorderStyle.NONE))
            {
                cours.setEnParallele(true);

                // tout est dans l'intitulé
                int debutEnseignant = intitule.indexOf('(');
                if (debutEnseignant != -1) {
                    int finEnseignant = intitule.indexOf(')', debutEnseignant);
                    if (finEnseignant == -1) {
                        finEnseignant = intitule.length();
                    }

                    String enseignant = intitule.substring(debutEnseignant+1, finEnseignant);
                    cours.setEnseignant(enseignant);
                }
            } else {
                // l'enseignant est à part
                if (cellEnseignant.getCellType() == STRING)
                    cours.setEnseignant(cellEnseignant.getStringCellValue());

                var cellSalle = nextRow.getCell(colCours+6);
                if (cellSalle != null && cellSalle.getCellType() == STRING) {
                    // créneau de 2h
                    cours.setDuree(2);
                } else {
                    cellSalle = nextRow.getCell(colCours+4);
                    if (cellSalle != null && cellSalle.getCellType() == STRING) {
                        // créneau de 1h
                        cours.setDuree(1);
                    } else {
                        cellSalle = nextRow.getCell(colCours+10);
                        if (cellSalle != null && cellSalle.getCellType() == STRING) {
                            // créneau de 3h
                            cours.setDuree(3);
                        }
                    }
                }
            }
        }

        System.out.println(cours);

        return cours;
    }

    private Date searchStartDate(Iterator<Row> rowIterator) {
        while (rowIterator.hasNext()) {
            row = rowIterator.next();

            Cell cell = row.getCell(0);

            if (cell != null && cell.getCellType() == NUMERIC) {
                try {
                    Date date = cell.getDateCellValue();
                    return date;
                } catch (Exception e) {
                }
            }
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        OldReader oldReader = new OldReader("2024-2025 Master.xlsx", 1);

        Calendrier calendrier = oldReader.traiterFichier();

        oldReader.close();

        ICSGenerator generator = new ICSGenerator(calendrier);

        generator.generate();
    }
}
