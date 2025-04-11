package net.torguet.xlsx.old;

import net.torguet.cal.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.Scanner;

import static org.apache.poi.ss.usermodel.CellType.*;

public class OldReader {
    private final Calendrier calendar;

    private final FileInputStream fis;
    private final XSSFSheet sheet;

    private Row row;
    private ZonedDateTime date;
    private Semaine semaine;

    private final ColorReader colorReader;

    private int rowNumber;
    private int colNumber;

    public OldReader(String fileName, int sheetNumber) throws Exception {
        calendar = new Calendrier();
        fis = new FileInputStream(fileName);
        XSSFWorkbook workbook = new XSSFWorkbook(fis);
        sheet = workbook.getSheetAt(sheetNumber);
        colorReader = new ColorReader(fileName, sheetNumber);
    }

    public void close() throws IOException, InterruptedException {
        fis.close();
        colorReader.close();
    }

    public Calendrier traiterFichier() {
        rowNumber = 1;
        colNumber = 1;
        Iterator<Row> rowIterator = sheet.iterator();
        date = searchStartDate(rowIterator);
        if (date != null) {
            calendar.setStartDate(date);

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
        int numeroSemaine;
        // le numéro de semaine est au-dessus et à droite
        var cell = sheet.getRow(row.getRowNum()-1).getCell(1);

        while (cell == null || (cell.getCellType() != NUMERIC && cell.getCellType() != FORMULA)) {
            if (!rowIterator.hasNext())
                return null;
            row = rowIterator.next();
            rowNumber++;
            var prevRow = sheet.getRow(row.getRowNum()-1);
            if (prevRow != null) {
                cell = prevRow.getCell(1);
            }
        }

        if(cell.getCellType() == NUMERIC || cell.getCellType() == FORMULA) {
            numeroSemaine = (int) cell.getNumericCellValue();
            if (semaine != null && numeroSemaine != semaine.getSemaine() + 1) {
                System.out.println("Numero de semaine incorrect");
                // il faut recalculer le jour
                date = searchStartDate(rowIterator);
                System.out.println("Nouvelle date "+date);
            }
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
                rowNumber++;
            }
            date = date.plusDays(1);
            System.out.println(date);
            date = date.plusDays(1);
            System.out.println(date);
        }
        return semaine;
    }

    private final int [] heuresDebut = {745, 800, 845, 900, 1000, 1300, 1330, 1545, 1615, 1700, 1800};
    private final int [] debutsCours = {2, 3, 6, 7, 11, 23, 25, 34, 36, 39, 43};

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
        colNumber = 1;
        if (cell != null && cell.getCellType() == STRING) {
            String jourSemaine  = cell.getStringCellValue();
            System.out.println(jourSemaine);
            jour = new Jour(date);

            // recup cours
            for(int colCours : debutsCours) {
                colNumber = colCours+1;
                Cours cours = recupCours(colCours, false);
                if (cours != null) {
                    jour.addCours(cours);
                }
            }


                // recherche groupe 2
                this.row = sheet.getRow(row.getRowNum()+1);
                this.rowNumber++;

                for(int colCours : debutsCours) {
                    colNumber = colCours+1;
                    Cours cours = recupCours(colCours, true);
                    if (cours != null) {
                        if (cours.isEnParallele())
                            jour.addCours(cours);
                    }
                }
                this.rowNumber--;

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

            if (intitule.length()==1) { // only 1 character (bug copy/paste from Google)
                System.out.println(intitule);
                return null;
            }

            cours = new Cours(intitule);

            // Code apogée
            int debutCode = intitule.indexOf('(');
            if (debutCode != -1) {
                int finCode = intitule.indexOf(')', debutCode);
                if (finCode == -1) {
                    finCode = intitule.length();
                }

                String code = intitule.substring(debutCode+1, finCode);

                if (code.startsWith("KRT") || code.startsWith("EIRT")) {
                    // c'est un code Apogée
                    cours.setCodeApogee(code);
                }
            }

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

            if (cellFin != null && nextCell != null && (hasRightBorder(cellFin) || hasLeftBorder(nextCell)))
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

            if (enseignant.startsWith("KRT") || enseignant.startsWith("EIR")) {
                // c'est un code Apogée
                if (cours.getCodeApogee() == null)
                    cours.setCodeApogee(enseignant);

                // on recherche l'enseignant à nouveau
                debutEnseignant = intitule.indexOf('(', finEnseignant);
                if (debutEnseignant != -1) {
                    finEnseignant = intitule.indexOf(')', debutEnseignant);
                    if (finEnseignant == -1) {
                        finEnseignant = intitule.length();
                    }
                    enseignant = intitule.substring(debutEnseignant+1, finEnseignant);
                    cours.setEnseignant(enseignant);
                }
            } else {
                cours.setEnseignant(enseignant);
            }
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

    private void gestionTypeCours(String intitule, Cours cours, Cell ignoredCellIntitule) {
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
        String couleur = colorReader.getColor(rowNumber, colNumber);
        if (couleur != null && ! couleur.equals("0")) {

            switch (couleur) {
                case "FFFFC000": // Ingé
                case "9": // Ingé autre tinte de vert :(
                case "7": // Ingé M1
                    cours.setType(TypeCours.TYPE_INGE);
                    break;
                case "FF92D050": // L3 IRT
                    cours.setType(TypeCours.TYPE_IRT);
                    break;
                case "FFFF85FE": // Alternance M1
                    cours.setType(TypeCours.TYPE_ALTERNANCE);
                    break;
                case "8": // Non alts M1
                    cours.setType(TypeCours.TYPE_NONALT);
                    break;
                case "FFFFFF00": // Contrôle
                    cours.setType(TypeCours.TYPE_CONTROLE);
                    break;
                case "FFFF0000":
                    cours.setType(TypeCours.TYPE_PROBLEME);
                    break;
                case "FF00FA00":
                    cours.setType(TypeCours.TYPE_BE);
                    break;
                case "FF00FFFF": // dev durable M1/M2
                case "FFFF40FF": // M1 RT
                case "FFFFFFFF": // blanc
                    cours.setType(TypeCours.TYPE_AUTRE);
                    break;
                default:
                    cours.setType(TypeCours.TYPE_AUTRE);
                    System.err.println("WARNING Nouvelle Couleur pour " + intitule + " : " + couleur);
            }
            System.out.println("Couleur de " + intitule + " : " + couleur);
        }


        //if (cellIntitule.getCellStyle() != null)
        //{
            //XSSFColor colorBg = (XSSFColor)cellIntitule.getCellStyle().getFillBackgroundColorColor();
//            //XSSFColor colorFg = (XSSFColor)cellIntitule.getCellStyle().getFillForegroundColorColor();
//            //if (colorBg != null && colorFg != null) {
//                int indexFg = colorFg.getIndex(); // not working currently
//                int indexBg = colorBg.getIndex(); // not working currently
//                int fill = cellIntitule.getCellStyle().getFillPattern().getCode();
//
//                //if (fill == FillPatternType.SOLID_FOREGROUND.getCode()) {
//
//                //}
//            }
//        }
    }

    private ZonedDateTime searchStartDate(Iterator<Row> rowIterator) {
        while (rowIterator.hasNext()) {
            if(row == null) {
                row = rowIterator.next();
                rowNumber=1;
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
            rowNumber++;
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
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




        Scanner scanner = new Scanner(System.in);

        Cours matcher = new Cours(null);
        boolean matchAny = true;
        boolean numSemaines = true;
        boolean joursSemaine = true;
        int i = 0;

        while(true) {
            System.out.println("0 - fin");
            System.out.println("1 - tout");
            System.out.println("2 - intitulé");
            System.out.println("3 - enseignant");
            System.out.println("4 - groupe");
            System.out.println("5 - type");
            System.out.println("6 - switch matchAny : "+matchAny);
            System.out.println("7 - switch affiche numSemaines : "+numSemaines);
            System.out.println("8 - switch affiche joursSemaine : "+joursSemaine);
            System.out.println("Votre choix :");
            int choice = scanner.nextInt();
            scanner.nextLine();
            switch(choice) {
                case 0 -> {
                    return;
                }
                case 1 -> {
                    matcher = new Cours(null);
                }
                case 2 -> {
                    System.out.println("Intitulé :");
                    String intitule = scanner.nextLine();
                    matcher.setIntitule(intitule);
                }
                case 3 -> {
                    System.out.println("Enseignant :");
                    String enseignant = scanner.nextLine();
                    matcher.setEnseignant(enseignant);
                }
                case 4 -> {
                    System.out.println("Groupe :");
                    String groupe = scanner.nextLine();
                    matcher.setGroupe(groupe);
                }
                case 5 -> {
                    System.out.println("Types :");
                    for(TypeCours typeCours : TypeCours.values()) {
                        System.out.println(typeCours.toString());
                    }
                    System.out.println("Votre choix :");
                    String type = scanner.nextLine();
                    matcher.setType(TypeCours.valueOf(type));
                }
                case 6 -> {
                    matchAny = !matchAny;
                    System.out.println("Switch matchAny maintenant : "+matchAny);
                }
                case 7 -> {
                    numSemaines = !numSemaines;
                    System.out.println("Switch numSemaines maintenant : "+numSemaines);
                }
                case 8 -> {
                    joursSemaine = !joursSemaine;
                    System.out.println("Switch joursSemaine maintenant : "+joursSemaine);
                }
                default -> {
                    System.out.println("Choix non valide");
                }
            }
            System.out.println("Matcher " + matcher);

            ICSGenerator generator = new ICSGenerator(calendrier);

            generator.generate(matcher, matchAny, "cal"+i+".ics");

            StatisticsGenerator statisticsGenerator = new StatisticsGenerator(calendrier);

            statisticsGenerator.generate(matcher, matchAny);
            i++;
        }
    }
}
