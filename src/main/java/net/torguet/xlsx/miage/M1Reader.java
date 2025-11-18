package net.torguet.xlsx.miage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.torguet.cal.*;
import net.torguet.xlsx.list.ListWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static org.apache.poi.ss.usermodel.CellType.*;

public class M1Reader {
    private final Calendrier calendar;

    private final FileInputStream fis;
    private final XSSFSheet sheet;

    private Row[] rows;

    private int debutCoursLundi = -1;
    private final int[] debutCours = new int[6];
    private final int[] debutCoursHoraires = new int[6];
    private int nbDebutCours = 0;
    private final int[] debutTD = new int[6];
    private final String[] groupeTDNames = new String[6];
    private final int[] debutTP = new int[6];
    private final String[] groupeTPNames = new String[6];
    private int nbGroupesTD = 0;
    private int nbGroupesTP = 0;

    private ZonedDateTime date;
    private Semaine semaine;

    private int rowNumber;
    private int colNumber;
    private int jourSemaine;

    public M1Reader(String fileName, int sheetNumber) throws Exception {
        calendar = new Calendrier();
        fis = new FileInputStream(fileName);
        XSSFWorkbook workbook = new XSSFWorkbook(fis);
        sheet = workbook.getSheetAt(sheetNumber);
    }

    public void close() throws IOException, InterruptedException {
        fis.close();
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
        rowNumber = debutCoursLundi+1;
        int idxTD = 0;
        var cell = sheet.getRow(rowNumber).getCell(colNumber);
        while (cell != null && cell.getCellType() == STRING) {
            var value = cell.getStringCellValue();
            if (value != null && value.startsWith("TD")) {
                System.out.println("Groupe "+value);
                debutTD[idxTD] = rowNumber - debutCoursLundi - 1;
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
        rowNumber = debutCoursLundi+1;
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
                debutTP[idxTD] = rowNumber - debutCoursLundi - 1;
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
            if (debutCoursLundi == -1)
                debutCoursLundi = rowNumber;
            XSSFCell cell;

            int debutCoursIdx = 0;

            System.out.println(joursSemaine[idJour]);

            if (idJour == 0) {
                // horaires cours
                cell = sheet.getRow(rowNumber).getCell(colNumber);
                while (cell != null && cell.getCellType() == STRING) {
                    String value = chercheHoraire(cell);
                    debutCours[debutCoursIdx] = rowNumber-debutCoursLundi;
                    HeureMinute result = getHeureMinute(value);
                    debutCoursHoraires[debutCoursIdx] = result.heure() *100+ result.minute();
                    System.out.println(debutCours[debutCoursIdx]+"->"+debutCoursHoraires[debutCoursIdx]);
                    debutCoursIdx++;
                    rowNumber++;
                    cell = sauteCaseVide();
                    if (cell.getStringCellValue().equalsIgnoreCase(joursSemaine[idJour+1])) {
                        nbDebutCours = debutCoursIdx;
                        break;
                    }
                }
            }
            idJour++;
        }
    }

    private static HeureMinute getHeureMinute(String value) {
        int idxH = value.indexOf("h");
        int heure = 0;
        int minute = 0;
        if (idxH != -1) {
            heure = Integer.parseInt(value.substring(0, idxH));
        }
        if (idxH < value.length()-2) {
            minute = Integer.parseInt(value.substring(idxH+1));
        }
        return new HeureMinute(heure, minute);
    }

    private record HeureMinute(int heure, int minute) {
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
            rowNumber++;
            cell = sheet.getRow(rowNumber).getCell(colNumber);
            if (cell != null && cell.getCellType() == STRING) {
                value = cell.getStringCellValue();
            }
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
            rowNumber = debutCoursLundi;
            jourSemaine = 0;
            while (rowNumber < sheet.getPhysicalNumberOfRows()) {
                System.out.println(date);
                Jour jour = recupJour();
                if (jour != null) {
                    semaine.addJour(jour);
                }
                jourSemaine++;
                date = date.plusDays(1);
                if(jourSemaine>=5) {
                    break;
                }
            }
            date = date.plusDays(1);
            System.out.println(date);
            date = date.plusDays(1);
            System.out.println(date);
        }
        return semaine;
    }


    private Jour recupJour() {
        Jour jour = new Jour(date);
        int nbCours = 0;
        for(int coursIdx = 0; coursIdx < nbDebutCours ; coursIdx++) {
            // créneau Cours ou TD1 ou TP1
            int row = rowNumber+debutCours[coursIdx];
            System.out.println("Cours Ligne :"+row+", jour : "+joursSemaine[jourSemaine]+" horaire : "+debutCoursHoraires[coursIdx]);
            var cell = sheet.getRow(row).getCell(colNumber);
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
        rowNumber += debutCours[nbDebutCours-1]+debutTP[nbGroupesTP-1];
        rowNumber++; // saute le jour de la semaine
        if (nbCours == 0) {
            return null;
        }
        return jour;
    }

    private int recupTDTP(Jour jour, int nbCours, int coursIdx, int idx, int[] debut) {
        XSSFCell cell;
        int row = rowNumber+debutCours[coursIdx]+debut[idx];
        System.out.println("TD/TP Ligne :"+row);
        cell = sheet.getRow(row).getCell(colNumber);
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
        Cours cours;
        System.out.println("recupCours");

        String value = cell.getStringCellValue();

        String [] split = value.trim().split("[-\\n]");
        // remove empty strings
        List<String> list = new ArrayList<>(Arrays.asList(split));
        list.removeIf(item -> item == null || item.isEmpty());
        split = list.toArray(new String[0]);

        if (split.length > 1) {
            cours = new Cours(split[1]);
            // si la premiere chaine est un code apogée
            if (split[0].startsWith("KMIA")) {
                cours.setCodeApogee(split[0]);
                if (split.length > 2) {
                    setTypeCours(split[2], cours);
                }
                if (split.length > 3) {
                    cours.setEnseignant(split[3]);
                }
            } else if (split[0].matches(timeMatcher)) {
                if (split[1].matches(timeMatcher)) {
                    // calcul de la longueur
                    HeureMinute hmDebut = getHeureMinute(split[0]);
                    HeureMinute hmFin = getHeureMinute(split[1]);
                    double longueur = hmFin.heure + hmFin.minute/60.0 - hmDebut.heure - hmDebut.minute/60.0;
                    cours.setDuree((float)longueur);
                    if (split.length > 2) {
                        setTypeCours(split[2], cours);
                    }
                    if (split.length > 3) {
                        cours.setIntitule(split[3]);
                    }
                }
            } else if (split[0].equalsIgnoreCase("Conférence")) {
                if (split.length > 2) {
                    cours.setEnseignant(split[2]);
                    cours.setIntitule(split[0] + " : " + split[1] + " " + split[2]);
                    if (split.length > 3) {
                        cours.setEnseignant(split[3]);
                    }
                } else {
                    cours.setIntitule(split[0] + " : " + split[1]);
                }
            } else { // sinon ce doit être un intitulé
                cours.setIntitule(split[0]);
                setTypeCours(split[1], cours);
                if (split.length > 2) {
                    cours.setEnseignant(split[2]);
                }
            }
        } else if (split.length > 0) {
            // une seule chaine
            cours = new Cours(split[0]);
        } else {
            return null;
        }

        // calcul horaire
        int decalage = cell.getRowIndex() - rowNumber;
        for(int debutC = 0; debutC<nbDebutCours; debutC++) {
            if (debutC == nbDebutCours-1) {
                setHoraireEtGroupeCours(debutC, cours, decalage);
            }
            else if (debutCours[debutC]<=decalage && debutCours[debutC+1]>decalage) {
                setHoraireEtGroupeCours(debutC, cours, decalage);
                break;
            }
        }

        System.out.println(cours);

        return cours;
    }

    private static void setTypeCours(String split, Cours cours) {
        split = split.trim();
        switch (split) {
            case "Cours":
                cours.setType(TypeCours.TYPE_COURS);
                break;
            case "TD":
            case "TD1":
            case "TD2":
            case "Groupe A":
            case "Groupe B":
            case "Groupe A :":
            case "Groupe B :":
                cours.setType(TypeCours.TYPE_TD);
                break;
            case "TP":
                cours.setType(TypeCours.TYPE_TP);
                break;
            case "EXAMEN":
            case "examen":
                cours.setType(TypeCours.TYPE_CONTROLE);
                break;
            default:
                cours.setType(TypeCours.TYPE_AUTRE);
        }
    }

    private void setHoraireEtGroupeCours(int debutC, Cours cours, int decalage) {
        System.out.println(debutCoursHoraires[debutC]);
        ZonedDateTime dateCours = date.plusHours(debutCoursHoraires[debutC]/100);
        dateCours = dateCours.plusMinutes(debutCoursHoraires[debutC]%100);
        cours.setDebut(dateCours);
        if (cours.getDuree() == 0.0)
            cours.setDuree(2.0f); // Durée par défaut

        if(cours.getType()==TypeCours.TYPE_TD) {
            for (int debTD = 0; debTD < nbGroupesTD; debTD++) {
                if (debutCours[debutC]+debutTD[debTD]==decalage) {
                    cours.setGroupe(groupeTDNames[debTD]);
                }
            }
        }
        if (cours.getType()==TypeCours.TYPE_TP) {
            for (int debTP = 0; debTP < nbGroupesTP; debTP++) {
                if (debutCours[debutC]+debutTP[debTP]==decalage) {
                    cours.setGroupe(groupeTPNames[debTP]);
                }
            }
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

        reader = new M1Reader("24-25EDT S8 M1 MIAGE-propo- 1.xlsx", 0);

        Calendrier calendrier;

        if (reader == null) {
            // re-read JSON file
            FileReader fileReader = new FileReader("calendrierM1Miage.json");
            calendrier = gson.fromJson(fileReader, Calendrier.class);
            fileReader.close();
        } else {
            calendrier = reader.traiterFichier();
            reader.close();

            String json = gson.toJson(calendrier);
            FileWriter fileWriter = new FileWriter("calendrierM1Miage.json");
            fileWriter.write(json);
            fileWriter.close();
        }

        Scanner scanner = new Scanner(System.in);

        Cours matcher = new Cours(null);
        boolean matchAny = true;
        boolean numSemaines = true;
        boolean joursSemaine = true;
        int i = 0;
        boolean all = false;
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
                    all = true;
                }
                case 2 -> {
                    System.out.println("Intitulé :");
                    String intitule = scanner.nextLine();
                    matcher.setIntitule(intitule);
                    all = false;
                }
                case 3 -> {
                    System.out.println("Enseignant :");
                    String enseignant = scanner.nextLine();
                    matcher.setEnseignant(enseignant);
                    all = false;
                }
                case 4 -> {
                    System.out.println("Groupe :");
                    String groupe = scanner.nextLine();
                    matcher.setGroupe(groupe);
                    all = false;
                }
                case 5 -> {
                    System.out.println("Types :");
                    for(TypeCours typeCours : TypeCours.values()) {
                        System.out.println(typeCours.toString());
                    }
                    System.out.println("Votre choix :");
                    String type = scanner.nextLine();
                    matcher.setType(TypeCours.valueOf(type));
                    all = false;
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
                default -> System.out.println("Choix non valide");
            }
            System.out.println("Matcher " + matcher);

            //ICSGenerator generator = new ICSGenerator(calendrier);

            //generator.generate(matcher, matchAny, "cal"+i+".ics");

            ListWriter listWriter = new ListWriter(calendrier);

            listWriter.generate(new File("m1miage"+i+".xlsx"), all?null:matcher, matchAny, numSemaines, joursSemaine);

            StatisticsGenerator statisticsGenerator = new StatisticsGenerator(calendrier);

            statisticsGenerator.generate(matcher, matchAny);
            i++;
        }
    }
}
