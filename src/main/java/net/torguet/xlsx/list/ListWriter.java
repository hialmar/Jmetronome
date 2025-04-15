package net.torguet.xlsx.list;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.torguet.cal.*;
import net.torguet.xlsx.old.OldReader;
import net.torguet.xlsx.stats.StatsWriter;
import org.apache.poi.xssf.usermodel.*;

import java.io.*;
import java.nio.file.Files;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Scanner;

public class ListWriter {
    XSSFWorkbook workbook;
    XSSFSheet spreadsheet;
    Calendrier calendrier;
    int currentRowNumber = 0;
    XSSFCellStyle dateStyle;
    XSSFCellStyle dateTimeStyle;

    public ListWriter(Calendrier calendrier) {
        this.calendrier = calendrier;
    }

    public void generate(File file) throws IOException {
        this.generate(file, null, true, true, true);
    }

    public void generate(File file, Cours matcher, boolean matchAny, boolean numSemaines, boolean joursSemaine) throws IOException {
        workbook = new XSSFWorkbook();
        spreadsheet = workbook.createSheet("Liste");
        dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(16); // d-mmm
        dateTimeStyle = workbook.createCellStyle();
        dateTimeStyle.setDataFormat(22); // m/d/yy h:mm

        currentRowNumber = 0;

        for(var semaine : calendrier.getSemaines()) {
            if (numSemaines)
                generateSemaineDebut(semaine);
            for (var jour : semaine.getJours()) {
                if (joursSemaine)
                    generateJourDebut(jour);
                for (var cours : jour.getCours()) {
                    if (cours.match(matcher, matchAny)) {
                        generateCours(cours, joursSemaine);
                    }
                }
            }
        }

        FileOutputStream out = new FileOutputStream(file);
        workbook.write(out);
        out.close();
        System.out.println("workbook written successfully");
    }

    void generateAllLists(StatsWriter statsWriter) throws IOException {
        Cours matcher = new Cours(null);
        ICSGenerator generator = new ICSGenerator(calendrier);
        for(String ens : statsWriter.getEnseignants()) {
            if (ens.contains("/")) {
                String[] split = ens.split("/");
                for(String e : split) {
                    if(!Files.exists(new File(e+".xlsx").toPath())) {
                        matcher.setEnseignant(e);
                        generate(new File(e + ".xlsx"), matcher, true, true, true);
                        generator.generate(matcher, true, e + ".ics");
                    }
                }
            } else {
                matcher.setEnseignant(ens);
                generate(new File(ens + ".xlsx"), matcher, true, true, true);
                generator.generate(matcher, true, ens + ".ics");
            }
        }
        generate(new File("all.xlsx"), null, true, true, true);
        generator.generate(null, true, "all.ics");
    }

    private void generateCours(Cours cours, boolean joursSemaine) {
        int startCell = 0;
        XSSFRow row = spreadsheet.createRow(currentRowNumber++);
        XSSFCell cell = row.createCell(startCell++);
        cell.setCellValue(cours.getIntitule());
        cell = row.createCell(startCell++);
        cell.setCellValue(cours.getCodeApogee());
        cell = row.createCell(startCell++);
        cell.setCellValue(cours.getEnseignant());
        cell = row.createCell(startCell++);

        if (joursSemaine) {
            cell.setCellValue(cours.getDebut().getHour()+":"+cours.getDebut().getMinute());
        } else {
            cell.setCellValue(LocalDateTime.from(cours.getDebut()));
            cell.setCellStyle(dateTimeStyle);
        }

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

        String json = gson.toJson(calendrier);
        FileWriter fileWriter = new FileWriter("calendrier.json");
        fileWriter.write(json);
        fileWriter.close();

        StatsWriter statsWriter = new StatsWriter(calendrier);

        statsWriter.generate(new File("stats.xlsx"));

        ListWriter listWriter = new ListWriter(calendrier);

        listWriter.generateAllLists(statsWriter);

        Scanner scanner = new Scanner(System.in);

        Cours matcher = new Cours(null);
        boolean matchAny = true;
        boolean numSemaines = true;
        boolean joursSemaine = true;
        boolean all = true;
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
                default -> {
                    System.out.println("Choix non valide");
                }
            }
            System.out.println("Matcher " + matcher);
            listWriter.generate(new File("Liste"+i+".xlsx"), all?null:matcher, matchAny, numSemaines, joursSemaine);
            i++;
        }

    }

}
