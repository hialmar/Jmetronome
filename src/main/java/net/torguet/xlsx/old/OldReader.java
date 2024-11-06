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
import java.util.Iterator;

import static org.apache.poi.ss.usermodel.CellType.*;

public class OldReader {
    private final Calendrier calendar;

    private final FileInputStream fis;
    private final XSSFSheet sheet;

    private Row row;
    private ZonedDateTime date;

    public OldReader(String fileName, int sheetNumber) throws IOException {
        calendar = new Calendrier();
        fis = new FileInputStream(fileName);
        XSSFWorkbook workbook = new XSSFWorkbook(fis);
        sheet = workbook.getSheetAt(sheetNumber);
    }

    public void close() throws IOException {
        fis.close();
    }

    public Calendrier traiterFichier() {
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
            var prevRow = sheet.getRow(row.getRowNum()-1);
            if (prevRow != null) {
                cell = prevRow.getCell(1);
            }
        }

        if(cell.getCellType() == NUMERIC || cell.getCellType() == FORMULA) {
            semaine = new Semaine((int) cell.getNumericCellValue());
            System.out.println("Semaine "+ cell.getNumericCellValue());
            int nbJour = 0;
            while (rowIterator.hasNext()) {
                Jour jour = recupJour(row);
                if (jour != null) {
                    date = date.plusDays(1);
                    System.out.println(date);
                    semaine.addJour(jour);
                    nbJour++;
                    if(nbJour>=5) {
                        break;
                    }
                }
                row = rowIterator.next();
            }
            date = date.plusDays(1);
            System.out.println(date);
            date = date.plusDays(1);
            System.out.println(date);
        }
        return semaine;
    }

    private final int [] heuresDebut = {745, 800, 900, 1000, 1300, 1330, 1545, 1615, 1800};
    private final int [] debutsCours = {2, 3, 7, 11, 23, 25, 34, 36, 43};

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
            boolean parallele = false;
            for(int colCours : debutsCours) {
                Cours cours = recupCours(colCours, false);
                if (cours != null) {
                    if (cours.isEnParallele())
                        parallele = true;
                    jour.addCours(cours);
                }
            }

            //if (parallele) {
                // recherche groupe 2
                this.row = sheet.getRow(row.getRowNum()+1);

                for(int colCours : debutsCours) {
                    Cours cours = recupCours(colCours, true);
                    if (cours != null) {
                        if (cours.isEnParallele())
                            jour.addCours(cours);
                    }
                }
            //}
        }
        return jour;
    }

    private Cours recupCours(int colCours, boolean bas) {
        Cours cours = null;
        System.out.println("recupInfosCours");

        // intitulé cours
        var cellIntitule = row.getCell(colCours);
        var prevCell = row.getCell(colCours-1);

        if (cellIntitule != null && cellIntitule.getCellType() == STRING &&
                (hasLeftBorder(cellIntitule) || hasRightBorder(prevCell)))
        {
            String intitule = cellIntitule.getStringCellValue();

            cours = new Cours(intitule);

            System.out.println("minuit : "+date);

            int heure = debutCoursToHeuresDebut(colCours);
            var d = date.plusHours(heure);
            int minutes = debutCoursToMinutesDebut(colCours);
            d = d.plusMinutes(minutes);

            System.out.println("début cours : " + d);

            cours.setDebut(d);

            gestionTypeCours(intitule, cours, cellIntitule);
            gestionEnseignantEtSalle(colCours, cellIntitule, cours, intitule, bas);
        }

        System.out.println(cours);

        return cours;
    }

    private boolean hasRightBorder(Cell cell) {
        return cell.getCellStyle() != null &&
                cell.getCellStyle().getBorderRight() != null &&
                cell.getCellStyle().getBorderRight() != BorderStyle.NONE;
    }

    private boolean hasLeftBorder(Cell cell) {
        return cell.getCellStyle() != null &&
                cell.getCellStyle().getBorderLeft() != null &&
                cell.getCellStyle().getBorderLeft() != BorderStyle.NONE;
    }

    private void gestionEnseignantEtSalle(int colCours, Cell cellIntitule, Cours cours, String intitule, boolean bas) {
        if (bas) {
            Row prevRow = sheet.getRow(row.getRowNum() - 1);
            var aboveCell = prevRow.getCell(colCours);
            // est-ce que c'est un créneau en //
            if (hasTopBorder(cellIntitule) || hasBottomBorder(aboveCell))
            {
                cours.setEnParallele(true);
                cours.setGroupe("2");

                gestionEnsSalleParallele(colCours, cours, intitule);
            }
        } else {
            // enseignant
            Row nextRow = sheet.getRow(row.getRowNum() + 1);
            var cellEnseignant = nextRow.getCell(colCours);

            // est-ce que c'est un créneau en //
            if (hasBottomBorder(cellIntitule) || hasTopBorder(cellEnseignant))
            {
                cours.setEnParallele(true);
                cours.setGroupe("1");

                gestionEnsSalleParallele(colCours, cours, intitule);
            } else {
                gestionEnsSalleFull(colCours, cours, cellEnseignant, nextRow);
            }
        }
    }

    private boolean hasTopBorder(Cell cell) {
        return cell.getCellStyle() != null &&
                cell.getCellStyle().getBorderTop() != null &&
                cell.getCellStyle().getBorderTop() != BorderStyle.NONE;
    }

    private boolean hasBottomBorder(Cell cell) {
        return cell.getCellStyle() != null &&
                cell.getCellStyle().getBorderBottom() != null &&
                cell.getCellStyle().getBorderBottom() != BorderStyle.NONE;
    }

    private void gestionEnsSalleFull(int colCours, Cours cours, Cell cellEnseignant, Row nextRow) {
        // l'enseignant est à part
        if (cellEnseignant.getCellType() == STRING)
            cours.setEnseignant(cellEnseignant.getStringCellValue());

        // Recherche fin du cours
        boolean fini = false;
        boolean sansFin = false;
        int colFinCours = colCours + 1;
        while(!fini) {
            var cellFin = this.row.getCell(colFinCours);
            var nextCell = this.row.getCell(colFinCours+1);

            if (cellFin != null && (hasRightBorder(cellFin) || hasLeftBorder(nextCell)))
            {
                fini = true;
                int duree = colFinCours + 1 - colCours;
                System.out.println("Durée : " + duree + " en heures : " + duree/4.f);
                cours.setDuree(duree/4.f);
            }

            if (cellFin == null) {
                System.err.println("Impossible de trouver la fin");
                fini = true;
                sansFin = true;
                System.out.println("Durée par défaut : 2h");
                cours.setDuree(2.f);
            }

            colFinCours++;
        }

        // Recherche salle
        fini = false;
        int colSalle = colCours + 1;
        while(!fini) {
            var cellSalle = nextRow.getCell(colSalle);
            if (cellSalle != null && cellSalle.getCellType() == STRING) {
                cours.setSalle(cellSalle.getStringCellValue());
                fini = true;

                if (sansFin){
                    long duree = colSalle + 1 - colCours + 1;
                    if (duree != cours.getDuree()*4) {
                        cours.setDuree(duree/4.f);
                    }
                }
            }
            colSalle ++;
            if (colSalle > colFinCours) {
                // pas de salle
                fini = true;
            }
        }
    }

    private void gestionEnsSalleParallele(int colCours, Cours cours, String intitule) {
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
        // Recherche fin du cours
        boolean fini = false;
        boolean sansFin = false;
        int colFinCours = colCours + 2;
        while(!fini) {
            var cellFin = this.row.getCell(colFinCours);
            var prevCell = this.row.getCell(colFinCours-1);

            if (cellFin != null && (hasRightBorder(cellFin) || hasLeftBorder(prevCell))) {
                fini = true;
                int duree = colFinCours + 1 - colCours;
                System.out.println("Durée : " + duree + " en heures : " + duree/4.f);
                cours.setDuree(duree/4.f);
            }

            if (cellFin == null) {
                System.err.println("Impossible de trouver la fin");
                fini = true;
                System.out.println("Durée par défaut : 2h");
                cours.setDuree(2.f);
                sansFin = true;
            }

            colFinCours++;
        }

        fini = false;
        int colSalle = colCours + 1;
        while(!fini) {
            var cellSalle = row.getCell(colSalle);
            if (cellSalle != null && cellSalle.getCellType() == STRING) {
                cours.setSalle(cellSalle.getStringCellValue());
                fini = true;

                if (sansFin){
                    long duree = colSalle + 1 - colCours + 1;
                    if (duree != cours.getDuree()*4) {
                        cours.setDuree(duree/4.f);
                    }
                }
            }
            colSalle ++;
            if (colSalle > colFinCours) {
                // pas de salle
                fini = true;
            }
        }
    }

    private void gestionTypeCours(String intitule, Cours cours, Cell cellIntitule) {
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
    }

    private ZonedDateTime searchStartDate(Iterator<Row> rowIterator) {
        while (rowIterator.hasNext()) {
            row = rowIterator.next();

            Cell cell = row.getCell(0);

            if (cell != null && cell.getCellType() == NUMERIC) {
                try {
                    ZoneId zoneId = ZoneId.of("Europe/Paris");
                    return ZonedDateTime.ofInstant(cell.getDateCellValue().toInstant(), zoneId);
                } catch (Exception e) {
                    return null;
                }
            }
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        OldReader oldReader = new OldReader("EDT S5 STRI 1A L3 2024-2025.xlsx", 0);

        Calendrier calendrier = oldReader.traiterFichier();

        oldReader.close();

        ICSGenerator generator = new ICSGenerator(calendrier);

        generator.generate();
    }
}
