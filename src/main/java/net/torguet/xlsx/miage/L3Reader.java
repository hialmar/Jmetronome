package net.torguet.xlsx.miage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.torguet.cal.*;
import net.torguet.xlsx.list.ListWriter;
import org.apache.poi.xssf.usermodel.XSSFCell;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class L3Reader extends M1Reader {
    public L3Reader(String fileName, int sheetNumber) throws Exception {
        super(fileName, sheetNumber);
    }

    Cours recupCours(XSSFCell cell) {
        Cours cours;
        System.out.println("recupCours");

        String value = cell.getStringCellValue();

        String [] split = value.strip().split("[-\\n]");
        // remove empty strings
        List<String> list = new ArrayList<>(Arrays.asList(split));
        list.removeIf(item -> item == null || item.isEmpty());
        split = list.toArray(new String[0]);

        if (split.length > 1) {
            // TODO
            cours = new Cours(split[0]);
        } else if (split.length > 0) {
            // une seule chaine
            cours = new Cours(split[0]);
            setTypeCours(split[0], cours);
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

    void setTypeCours(String chaine, Cours cours) {
        chaine = chaine.trim();
        String [] tab = chaine.split(" ");
        if (tab.length==0) {
            return;
        }
        switch (tab[0].toLowerCase()) {
            case "cm":
                cours.setType(TypeCours.TYPE_COURS);
                break;
            case "td":
            case "anglais":
                cours.setType(TypeCours.TYPE_TD);
                break;
            case "tp":
                cours.setType(TypeCours.TYPE_TP);
                break;
            case "examen":
            case "remise projet":
            case "cc1":
            case "cc2":
            case "cc3":
                cours.setType(TypeCours.TYPE_CONTROLE);
                break;
            case "projet":
                cours.setType(TypeCours.TYPE_PROJET);
            default:
                cours.setType(TypeCours.TYPE_AUTRE);
        }
    }


    public static void main(String[] args) throws Exception {
        L3Reader reader;

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

        reader = new L3Reader("L3_MIAGE_S6_25_26_V0.1.xlsx", 0);

        Calendrier calendrier;

        if (reader == null) {
            // re-read JSON file
            FileReader fileReader = new FileReader("calendrierL3Miage.json");
            calendrier = gson.fromJson(fileReader, Calendrier.class);
            fileReader.close();
        } else {
            calendrier = reader.traiterFichier();
            reader.close();

            String json = gson.toJson(calendrier);
            FileWriter fileWriter = new FileWriter("calendrierL3Miage.json");
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

            ICSGenerator generator = new ICSGenerator(calendrier);

            generator.generate(matcher, matchAny, "cal"+i+".ics");

            ListWriter listWriter = new ListWriter(calendrier);

            listWriter.generate(new File("m1miage"+i+".xlsx"), all?null:matcher, matchAny, numSemaines, joursSemaine);

            StatisticsGenerator statisticsGenerator = new StatisticsGenerator(calendrier);

            statisticsGenerator.generate(matcher, matchAny);
            i++;
        }
    }
}
