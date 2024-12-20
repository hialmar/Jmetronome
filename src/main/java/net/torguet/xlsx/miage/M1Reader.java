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

    private final int[] debutCours = new int[6];
    private final int[] debutCoursHoraires = new int[6];
    private int nbDebutCours = 0;
    private int[] debutTD = new int[6];
    private String[] groupeTDNames = new String[6];
    private int[] debutTP = new int[6];
    private String[] groupeTPNames = new String[6];
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

        calculInfoHoraires();
        calculGroupesTDs();
        calculGroupesTPs();

        colNumber = 3;
        date = searchStartDate();
        if (date != null) {
            calendar.setStartDate(date);
            do {
                semaine = recupSemaine();
                if (semaine != null) {
                    calendar.addSemaine(semaine);
                }
                colNumber++;
            } while (semaine != null);
        }
        return calendar;
    }

    private void calculGroupesTDs() {
        colNumber = 1;
        rowNumber = debutCours[0];
        int idxTD = 0;
        var cell = sheet.getRow(rowNumber).getCell(colNumber);
        while (cell != null && cell.getCellType() == STRING) {
            var value = cell.getStringCellValue();
            if (value != null && value.startsWith("TD")) {
                System.out.println("Groupe "+value);
                debutTD[idxTD] = rowNumber - debutCours[0];
                groupeTDNames[idxTD] = value;
                nbGroupesTD++;
                idxTD++;
                rowNumber++;
                cell = sheet.getRow(rowNumber).getCell(colNumber);
            } else {
                break;
            }
        }
        System.out.println("Nb TDs "+nbGroupesTD);
    }

    private void calculGroupesTPs() {
        colNumber = 2;
        rowNumber = debutCours[0];
        int idxTD = 0;
        var cell = sheet.getRow(rowNumber).getCell(colNumber);
        while (cell != null && cell.getCellType() == STRING) {
            var value = cell.getStringCellValue();
            if (value != null && value.startsWith("TP")) {
                System.out.println("Groupe "+value);
                // si on retrouve le premier groupe on a fini
                if (nbGroupesTP>0 && value.equalsIgnoreCase(groupeTPNames[0])) {
                    break;
                }
                debutTP[idxTD] = rowNumber - debutCours[0];
                groupeTPNames[idxTD] = value;
                nbGroupesTP++;
                idxTD++;
                rowNumber++;
                cell = sheet.getRow(rowNumber).getCell(colNumber);
            } else {
                break;
            }
        }
        System.out.println("Nb TPs "+nbGroupesTP);
    }

    private static final String [] joursSemaine = {"LUNDI", "MAR", "MER", "JEU", "VEN"};

    private static final String timeMatcher = "^([0-9]+)h([0-9]*)";

    private void calculInfoHoraires() {
        colNumber = 0;
        rowNumber = 2;
        int idJour = 0;
        while(idJour < joursSemaine.length) {
            chercheJourSemaine(idJour);
            XSSFCell cell;

            int debutCoursIdx = 0;

            System.out.println(joursSemaine[idJour]);

            if (idJour == 0) {
                // horaires cours
                cell = sheet.getRow(rowNumber).getCell(colNumber);
                while (cell != null && cell.getCellType() == STRING) {
                    String value = chercheHoraire(cell);
                    debutCours[debutCoursIdx] = rowNumber-1;
                    int idxH = value.indexOf("h");
                    int heure = 0;
                    int minute = 0;
                    if (idxH != -1) {
                        heure = Integer.parseInt(value.substring(0, idxH));
                    }
                    if (idxH > value.length()) {
                        minute = Integer.parseInt(value.substring(idxH+1));
                    }
                    debutCoursHoraires[debutCoursIdx] = heure*100+minute;
                    System.out.println(debutCours[debutCoursIdx]);
                    debutCoursIdx++;
                    rowNumber++;
                    cell = sauteCaseVide();
                    if (cell.getStringCellValue().equalsIgnoreCase(joursSemaine[idJour+1])) {
                        nbDebutCours = debutCoursIdx-1;
                        break;
                    }
                }
            }
            idJour++;
        }
    }

    private void chercheJourSemaine(int idJour) {
        var cell = sheet.getRow(rowNumber).getCell(colNumber);
        if (cell != null && cell.getCellType() == STRING) {
            String value = cell.getStringCellValue();
            while (!value.equalsIgnoreCase(joursSemaine[idJour])) {
                cell = sheet.getRow(rowNumber).getCell(colNumber);
                if (cell != null && cell.getCellType() == STRING) {
                    value = cell.getStringCellValue();
                }
                rowNumber++;
            }
        }
    }

    private XSSFCell sauteCaseVide() {
        XSSFCell cell;
        cell = sheet.getRow(rowNumber).getCell(colNumber);

        while (cell != null && cell.getCellType() != STRING) {
            rowNumber++;
            cell = sheet.getRow(rowNumber).getCell(colNumber);
        }
        return cell;
    }

    private String chercheHoraire(XSSFCell cell) {
        String value = cell.getStringCellValue();
        while (!value.matches(timeMatcher)) {
            cell = sheet.getRow(rowNumber).getCell(colNumber);
            if (cell != null && cell.getCellType() == STRING) {
                value = cell.getStringCellValue();
            }
            rowNumber++;
        }
        return value;
    }

    private Semaine recupSemaine() {
        int numeroSemaine;
        rowNumber=1;
        // le numéro de semaine est sur la ligne 1
        var cell = sheet.getRow(rowNumber).getCell(colNumber);

        if (cell == null) {
            return null;
        }

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
                date = date.plusDays(1);
                System.out.println(date);
                Jour jour = recupJour(nbJour);
                if (jour != null) {
                    semaine.addJour(jour);
                }
                nbJour++;
                if(nbJour>=5) {
                    break;
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


    private Jour recupJour(int nbJour) {
        Jour jour = new Jour(date);
        int nbCours = 0;
        for(int coursIdx = 0; coursIdx <nbDebutCours ; coursIdx++) {
            // créneau Cours ou TD1 ou TP1
            rowNumber = debutCours[coursIdx];
            var cell = sheet.getRow(rowNumber).getCell(colNumber);
            if (cell != null && cell.getCellType() == STRING) {
                Cours cours = recupCours(cell);
                if (cours != null) {
                    jour.addCours(cours);
                    nbCours++;
                }
            }
            if (nbGroupesTD > 1) {
                for (int tdIdx=1; tdIdx<nbGroupesTD; tdIdx++) {
                    nbCours = recupTDTP(jour, nbCours, coursIdx, tdIdx, debutTD);
                }
            }
            if (nbGroupesTP>nbGroupesTD) {
                for (int tpIdx=nbGroupesTD; tpIdx<nbGroupesTP; tpIdx++) {
                    nbCours = recupTDTP(jour, nbCours, coursIdx, tpIdx, debutTP);
                }
            }
        }
        if (nbCours == 0) {
            return null;
        }
        return jour;
    }

    private int recupTDTP(Jour jour, int nbCours, int coursIdx, int tpIdx, int[] debutTP) {
        XSSFCell cell;
        rowNumber = debutCours[coursIdx]+ debutTP[tpIdx];
        cell = sheet.getRow(rowNumber).getCell(colNumber);
        if (cell != null && cell.getCellType() == STRING) {
            Cours cours = recupCours(cell);
            if (cours != null) {
                jour.addCours(cours);
                nbCours++;
            }
        }
        return nbCours;
    }

    private Cours recupCours(XSSFCell cell) {
        Cours cours = null;
        System.out.println("recupCours");

        String value = cell.getStringCellValue();

        String [] split = value.split("[-\\n]");

        if (split.length > 1) {
            cours = new Cours(split[1]);
            cours.setCodeApogee(split[0]);
            if (split.length > 2) {
                switch (split[2]) {
                    case "Cours":
                        cours.setType(TypeCours.TYPE_COURS);
                        break;
                    case "TD":
                        cours.setType(TypeCours.TYPE_TD);
                        break;
                    case "TP":
                        cours.setType(TypeCours.TYPE_TP);
                        break;
                    default:
                        cours.setType(TypeCours.TYPE_AUTRE);
                }
            }
            if (split.length > 3) {
                cours.setEnseignant(split[3]);
            }

            // calcul horaire
            for(int debutC = 0; debutC<nbDebutCours; debutC++) {
                if (debutC == nbDebutCours-1) {
                    setHoraireCours(debutC, cours);
                }
                else if (debutCours[debutC]<=rowNumber && debutCours[debutC+1]>rowNumber) {
                    setHoraireCours(debutC, cours);
                    break;
                }
            }
        }

        System.out.println(cours);

        return cours;
    }

    private void setHoraireCours(int debutC, Cours cours) {
        System.out.println(debutCoursHoraires[debutC]);
        ZonedDateTime dateCours = date.plusHours(debutCoursHoraires[debutC]/100);
        dateCours = dateCours.plusMinutes(debutCoursHoraires[debutC +1]%100);
        cours.setDebut(dateCours);
        cours.setDuree(2.0f); // A Modifier !!!!

        if(cours.getType()==TypeCours.TYPE_TD) {
            for (int debTD = 0; debTD < nbGroupesTD; debTD++) {
                if (debutCours[debutC]+debutTD[debTD]==rowNumber) {
                    cours.setGroupe(groupeTDNames[debTD]);
                }
            }
        }
        if (cours.getType()==TypeCours.TYPE_TP) {
            for (int debTP = 0; debTP < nbGroupesTP; debTP++) {
                if (debutCours[debutC]+debutTP[debTP]==rowNumber) {
                    cours.setGroupe(groupeTPNames[debTP]);
                }
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
