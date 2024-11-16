package net.torguet.xlsx;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.model.ThemesTable;
import org.apache.poi.xssf.usermodel.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Iterator;

import static org.apache.poi.ss.usermodel.CellType.*;

public class OldReader {

    private final FileInputStream fis;
    private final XSSFSheet sheet;

    private Row row;
    private ZonedDateTime date;
    private int numeroSemaine;
    private XSSFWorkbook workbook;

    public OldReader(String fileName, int sheetNumber) throws IOException {
        fis = new FileInputStream(fileName);
        workbook = new XSSFWorkbook(fis);
        sheet = workbook.getSheetAt(sheetNumber);
    }

    public void close() throws IOException {
        fis.close();
    }

    public void traiterFichier() {
        Iterator<Row> rowIterator = sheet.iterator();
        date = searchStartDate(rowIterator);
        if (date != null) {
            boolean ok = true;
            do {
                ok = recupSemaine(rowIterator);
            } while (ok);
        }
    }

    private boolean recupSemaine(Iterator<Row> rowIterator) {
        numeroSemaine = 0;
        // le numéro de semaine est au-dessus et à droite
        var cell = sheet.getRow(row.getRowNum()-1).getCell(1);

        while (cell == null || (cell.getCellType() != NUMERIC && cell.getCellType() != FORMULA)) {
            if (!rowIterator.hasNext())
                return false;
            row = rowIterator.next();
            var prevRow = sheet.getRow(row.getRowNum()-1);
            if (prevRow != null) {
                cell = prevRow.getCell(1);
            }
        }

        if(cell.getCellType() == NUMERIC || cell.getCellType() == FORMULA) {
            numeroSemaine = (int) cell.getNumericCellValue();

            System.out.println("Semaine "+ cell.getNumericCellValue());
            int nbJour = 0;
            while (rowIterator.hasNext()) {
                boolean jour = recupJour(row);
                if (jour) {
                    date = date.plusDays(1);
                    System.out.println(date);
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
        return true;
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

    private boolean recupJour(Row row) {
        boolean ok = true;
        var cell = row.getCell(1);
        if (cell != null && cell.getCellType() == STRING) {
            String jourSemaine  = cell.getStringCellValue();
            System.out.println(jourSemaine);

            for(int colCours : debutsCours) {
                ok = recupCours(colCours, false);
            }

            //if (parallele) {
                // recherche groupe 2
                this.row = sheet.getRow(row.getRowNum()+1);

                for(int colCours : debutsCours) {
                    ok = recupCours(colCours, true);
                }
            //}
        }
        return ok;
    }

    private boolean recupCours(int colCours, boolean bas) {
        System.out.println("recupInfosCours");
        boolean ok = false;

        // intitulé cours
        var cellIntitule = row.getCell(colCours);
        var prevCell = row.getCell(colCours-1);

        if (cellIntitule != null && cellIntitule.getCellType() == STRING &&
                (hasLeftBorder(cellIntitule) || hasRightBorder(prevCell)))
        {
            String intitule = cellIntitule.getStringCellValue();

            System.out.println(intitule);

            ok = true;

            System.out.println("minuit : "+date);

            int heure = debutCoursToHeuresDebut(colCours);
            var d = date.plusHours(heure);
            int minutes = debutCoursToMinutesDebut(colCours);
            d = d.plusMinutes(minutes);

            System.out.println("début cours : " + d);

            gestionTypeCours(intitule, ok, cellIntitule);
            gestionEnseignantEtSalle(colCours, cellIntitule, ok, intitule, bas);
        }

        System.out.println(ok);

        return ok;
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

    private void gestionEnseignantEtSalle(int colCours, Cell cellIntitule, boolean cours, String intitule, boolean bas) {
        if (bas) {
            Row prevRow = sheet.getRow(row.getRowNum() - 1);
            var aboveCell = prevRow.getCell(colCours);
            // est-ce que c'est un créneau en //
            if (hasTopBorder(cellIntitule) || hasBottomBorder(aboveCell))
            {
                gestionEnsSalleParallele(colCours, cours, intitule);
            }
        } else {
            // enseignant
            Row nextRow = sheet.getRow(row.getRowNum() + 1);
            var cellEnseignant = nextRow.getCell(colCours);

            // est-ce que c'est un créneau en //
            if (hasBottomBorder(cellIntitule) || hasTopBorder(cellEnseignant))
            {
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

    private void gestionEnsSalleFull(int colCours, boolean cours, Cell cellEnseignant, Row nextRow) {
        // l'enseignant est à part
        if (cellEnseignant.getCellType() == STRING)
            System.out.println("Ens : "+cellEnseignant.getStringCellValue());

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
            }

            if (cellFin == null) {
                System.err.println("Impossible de trouver la fin");
                fini = true;
                sansFin = true;
                System.out.println("Durée par défaut : 2h");
            }

            colFinCours++;
        }

        // Recherche salle
        fini = false;
        int colSalle = colCours + 1;
        while(!fini) {
            var cellSalle = nextRow.getCell(colSalle);
            if (cellSalle != null && cellSalle.getCellType() == STRING) {
                System.out.println("Salle : "+cellSalle.getStringCellValue());
                fini = true;

                if (sansFin){
                    long duree = colSalle + 1 - colCours + 1;
                    System.out.println("Durée "+duree + " en heures : " + duree/4.f);
                }
            }
            colSalle ++;
            if (colSalle > colFinCours) {
                // pas de salle
                fini = true;
            }
        }
    }

    private void gestionEnsSalleParallele(int colCours, boolean cours, String intitule) {
        // tout est dans l'intitulé
        int debutEnseignant = intitule.indexOf('(');
        if (debutEnseignant != -1) {
            int finEnseignant = intitule.indexOf(')', debutEnseignant);
            if (finEnseignant == -1) {
                finEnseignant = intitule.length();
            }

            String enseignant = intitule.substring(debutEnseignant+1, finEnseignant);
            System.out.println("Ens: "+enseignant);
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
            }

            if (cellFin == null) {
                System.err.println("Impossible de trouver la fin");
                fini = true;
                System.out.println("Durée par défaut : 2h");
                sansFin = true;
            }

            colFinCours++;
        }

        fini = false;
        int colSalle = colCours + 1;
        while(!fini) {
            var cellSalle = row.getCell(colSalle);
            if (cellSalle != null && cellSalle.getCellType() == STRING) {
                System.out.println("Salle: "+cellSalle.getStringCellValue());
                fini = true;

                if (sansFin){
                    long duree = colSalle + 1 - colCours + 1;
                    System.out.println("Durée : " + duree + " en heures : " + duree/4.f);
                }
            }
            colSalle ++;
            if (colSalle > colFinCours) {
                // pas de salle
                fini = true;
            }
        }
    }

    private void gestionTypeCours(String intitule, boolean cours, Cell cellIntitule) {

        // Contrôle
        if (cellIntitule.getCellStyle() != null)
        {
            XSSFColor colorBg = (XSSFColor)cellIntitule.getCellStyle().getFillBackgroundColorColor();
            XSSFColor colorFg = (XSSFColor)cellIntitule.getCellStyle().getFillForegroundColorColor();
            if (colorBg != null && colorFg != null) {
                int indexFg = colorFg.getIndex();
                int indexBg = colorBg.getIndex();
                int fill = cellIntitule.getCellStyle().getFillPattern().getCode();


                if (fill == FillPatternType.SOLID_FOREGROUND.getCode()) {
                    System.out.println("Couleur de " + intitule + " : " + cellIntitule.getCellStyle().getFillBackgroundColor());

                    int numCellStyle = workbook.getNumCellStyles();
                    for(int i=0; i< numCellStyle; i++) {
                        XSSFCellStyle style = workbook.getCellStyleAt(i);
                        if (style == cellIntitule.getCellStyle()) {
                            System.out.println("C'est le style "+i);
                        }
                    }

                    StylesTable stylesTable = workbook.getStylesSource();

                    numCellStyle = stylesTable.getNumCellStyles();

                    for(int i=0; i< numCellStyle; i++) {
                        XSSFCellStyle style = stylesTable.getStyleAt(i);
                        if (style == cellIntitule.getCellStyle()) {
                            System.out.println("C'est le style "+i);
                        }
                    }

                    IndexedColorMap indexedColorMap = stylesTable.getIndexedColors();

                    byte[] bg = indexedColorMap.getRGB(indexBg);
                    byte[] fg = indexedColorMap.getRGB(indexFg);

                    System.out.println("BG : " + bg);
                    System.out.println("FG : " + fg);

                    ThemesTable themesTable = workbook.getTheme();

                    XSSFColor colorBg2 = themesTable.getThemeColor(indexBg);
                    XSSFColor colorFg2 = themesTable.getThemeColor(indexFg);

                    if (colorBg2 != null)
                        bg = colorBg2.getRGB();
                    if (colorFg2 != null)
                        fg = colorFg2.getRGB();
                    System.out.println("BG2 : " + bg);
                    System.out.println("FG2 : " + fg);

                    for (IndexedColors c : IndexedColors.values()) {
                        if (c.index == indexFg){
                            System.out.println("Color: " + c.name());
                        }
                        if (c.index == indexBg){
                            System.out.println("Color: " + c.name());
                        }
                    }

                }
            }
        }
    }

    private ZonedDateTime searchStartDate(Iterator<Row> rowIterator) {
        while (rowIterator.hasNext()) {
            if(row == null) {
                row = rowIterator.next();
            }
            Cell cell = row.getCell(0);

            if (cell != null && cell.getCellType() == NUMERIC) {
                try {
                    ZoneId zoneId = ZoneId.of("Europe/Paris");
                    return ZonedDateTime.ofInstant(cell.getDateCellValue().toInstant(), zoneId);
                } catch (Exception e) {
                    return null;
                }
            }
            row = rowIterator.next();
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        OldReader oldReader = new OldReader("EDT S5 STRI 1A L3 2024-2025.xlsx", 0);
        //L3 : OldReader oldReader = new OldReader("EDT S5 STRI 1A L3 2024-2025.xlsx", 0);
        //M1 : OldReader oldReader = new OldReader("2024-2025 M1.xlsx", 0);
        //M2 : OldReader oldReader = new OldReader("2024-2025 Master.xlsx", 1);

        oldReader.traiterFichier();

        oldReader.close();
    }
}
