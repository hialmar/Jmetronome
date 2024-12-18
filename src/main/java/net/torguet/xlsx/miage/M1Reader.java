package net.torguet.xlsx.miage;

import net.torguet.cal.*;
import net.torguet.xlsx.old.ColorReader;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.Scanner;

import static org.apache.poi.ss.usermodel.CellType.*;

public class M1Reader {
    private final Calendrier calendar;

    private final FileInputStream fis;
    private final XSSFSheet sheet;

    private Row[] rows;

    private int[] debutCours = new int[6];
    private int[] debutTD = new int[4];
    private int[] debutTP = new int[4];
    private int nbGroupesTD = 0;
    private int nbGroupesTP = 0;

    private ZonedDateTime date;
    private Semaine semaine;

    private final ColorReader colorReader;

    private int rowNumber;
    private int colNumber;

    public M1Reader(String fileName, int sheetNumber) throws Exception {
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
        rowNumber = 0;
        Iterator<Row> rowIterator = sheet.iterator();

        rows = new Row[sheet.getPhysicalNumberOfRows()];

        for(int i=0; i<sheet.getPhysicalNumberOfRows(); i++) {
            rows[i] = rowIterator.next();
        }

        calculInfoLignes();

        colNumber = 0;
        date = searchStartDate();
        if (date != null) {
            calendar.setStartDate(date);
            do {
                semaine = recupSemaine();
                if (semaine != null) {
                    calendar.addSemaine(semaine);
                }
            } while (semaine != null);
        }
        return calendar;
    }

    private static final String [] joursSemaine = {"LUNDI", "MAR", "MER", "JEU", "VEN"};

    private static final String timeMatcher = "^([0-9]{1,})h([0-9]*)";

    private void calculInfoLignes() {
        colNumber = 0;
        rowNumber = 2;
        for (String jourSemaine : joursSemaine) {
            var cell = sheet.getRow(rowNumber).getCell(colNumber);

            if (cell != null && cell.getCellType() == STRING) {
                String value = cell.getStringCellValue();
                while (!value.equalsIgnoreCase(jourSemaine)) {
                    cell = sheet.getRow(rowNumber).getCell(colNumber);
                    if (cell != null && cell.getCellType() == STRING) {
                        value = cell.getStringCellValue();
                    }
                    rowNumber++;
                }
            }

            int debutCoursIdx = 0;

            if (jourSemaine.equals("LUNDI")) {
                System.out.println("LUNDI");
                rowNumber++;
                // horaires cours
                cell = sheet.getRow(rowNumber).getCell(colNumber);
                while (cell != null && cell.getCellType() == STRING) {
                    String value = cell.getStringCellValue();
                    while (!value.matches(timeMatcher)) {
                        cell = sheet.getRow(rowNumber).getCell(colNumber);
                        if (cell != null && cell.getCellType() == STRING) {
                            value = cell.getStringCellValue();
                        }
                        rowNumber++;
                    }
                    System.out.println("LUNDI " + value);
                    debutCours[debutCoursIdx] = rowNumber-1;
                    System.out.println(debutCours[debutCoursIdx]);
                    debutCoursIdx++;

                    rowNumber++;
                    cell = sheet.getRow(rowNumber).getCell(colNumber);

                    while (cell != null && cell.getCellType() != STRING) {
                        rowNumber++;
                        cell = sheet.getRow(rowNumber).getCell(colNumber);
                    }
                }

            }



        }
    }

    private Semaine recupSemaine() {
        int numeroSemaine;
        rowNumber=1;
        // le numéro de semaine est sur la ligne 1
        var cell = sheet.getRow(rowNumber).getCell(colNumber);

        if(cell.getCellType() == NUMERIC || cell.getCellType() == FORMULA) {
            numeroSemaine = (int) cell.getNumericCellValue();
            if (semaine != null && numeroSemaine != semaine.getSemaine() + 1) {
                System.out.println("Numero de semaine incorrect");
                // il faut recalculer le jour
                date = searchStartDate();
                System.out.println("Nouvelle date "+date);
            }
            semaine = new Semaine((int) cell.getNumericCellValue());

            System.out.println("Semaine "+ cell.getNumericCellValue());
            int nbJour = 0;
            while (rowNumber < sheet.getPhysicalNumberOfRows()) {
                Jour jour = recupJour();
                if (jour != null) {
                    date = date.plusDays(1);
                    System.out.println(date);
                    semaine.addJour(jour);
                    nbJour++;
                    if(nbJour>=5) {
                        break;
                    }
                }
                rowNumber++;
            }
            date = date.plusDays(1);
            System.out.println(date);
            date = date.plusDays(1);
            System.out.println(date);
        }
        return semaine;
    }


    private Jour recupJour() {
        Jour jour = null;
        var cell = rows[rowNumber].getCell(colNumber);
        if (cell != null && cell.getCellType() == STRING) {
            String jourSemaine  = cell.getStringCellValue();
            System.out.println(jourSemaine);
            jour = new Jour(date);

            // recup cours
            //for(int colCours : debutsCours) {
                //colNumber = colCours+1;
//                Cours cours = recupCours(colCours, false);
//                if (cours != null) {
//                    jour.addCours(cours);
//                }
            //}
            this.rowNumber--;
        }
        return jour;
    }

    private Cours recupCours(int colCours, boolean bas) {
        Cours cours = null;
        System.out.println("recupInfosCours");

        // intitulé cours
//        var cellIntitule = row.getCell(colCours);
//        var prevCell = row.getCell(colCours-1);
//
//        if (cellIntitule != null && cellIntitule.getCellType() == STRING &&
//                (hasLeftBorder(cellIntitule) || hasRightBorder(prevCell)))
//        {
//            String intitule = cellIntitule.getStringCellValue();
//
//            if (intitule.length()==1) { // only 1 character (bug copy/paste from Google)
//                System.out.println(intitule);
//                return null;
//            }
//
//            cours = new Cours(intitule);
//
//            System.out.println("minuit : "+date);
//
//            int heure = debutCoursToHeuresDebut(colCours);
//            var d = date.plusHours(heure);
//            int minutes = debutCoursToMinutesDebut(colCours);
//            d = d.plusMinutes(minutes);
//
//            System.out.println("début cours : " + d);
//
//            cours.setDebut(d);
//
//            gestionTypeCours(intitule, cours, cellIntitule);
//            gestionEnseignantEtSalle(colCours, cellIntitule, cours, intitule, bas);
//        }

        System.out.println(cours);

        return cours;
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
        if (couleur != null && !couleur.equals("0")) {

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
    }

    private ZonedDateTime searchStartDate() {
        rowNumber=2;
        Cell cell = rows[rowNumber].getCell(colNumber);
        do {
            if (cell != null && cell.getCellType() == NUMERIC) {
                try {
                    ZoneId zoneId = ZoneId.of("Europe/Paris");
                    return ZonedDateTime.ofInstant(cell.getDateCellValue().toInstant(), zoneId);
                } catch (Exception e) {
                    return null;
                }
            }
            colNumber++;
            cell = rows[rowNumber].getCell(colNumber);
        } while(cell == null);
        return null;
    }

    public static void main(String[] args) throws Exception {
        M1Reader reader;

        reader = new M1Reader("24-25EDT S8 M1 MIAGE-propo- 1.xlsx", 0);

        Calendrier calendrier = reader.traiterFichier();

        reader.close();

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
