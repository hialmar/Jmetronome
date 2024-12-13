package net.torguet.cal;

import java.util.HashMap;

public class StatisticsGenerator {
    private final Calendrier calendar;
    private final HashMap<String, Float> heuresCours = new HashMap<>();
    private final HashMap<String, Float> heuresTD = new HashMap<>();
    private final HashMap<String, Float> heuresTP = new HashMap<>();
    private final HashMap<String, Float> heuresAutres = new HashMap<>();
    private final HashMap<String, Float> heuresControle = new HashMap<>();

    public StatisticsGenerator(Calendrier calendar) {
        this.calendar = calendar;
    }

    public HashMap<String, Float> getHeuresCours() {
        return heuresCours;
    }

    public HashMap<String, Float> getHeuresTD() {
        return heuresTD;
    }

    public HashMap<String, Float> getHeuresTP() {
        return heuresTP;
    }

    public HashMap<String, Float> getHeuresAutres() {
        return heuresAutres;
    }

    public HashMap<String, Float> getHeuresControle() {
        return heuresControle;
    }

    public void generate(Cours matcher, boolean matchAny) {
        for(Semaine semaine : calendar.getSemaines()) {
            if (semaine == null)
                continue;
            for(Jour jour : semaine.getJours()) {
                if (jour == null)
                    continue;
                for (Cours cours : jour.getCours()) {
                    if (cours == null)
                        continue;
                    if (!cours.match(matcher, matchAny)) {
                        continue;
                    }
                    String enseignant = cours.getEnseignant();
                    if (enseignant == null) {
                        enseignant = "ZZAnonyme";
                    }
                    switch (cours.getType()) {
                        case TYPE_COURS -> {
                            if (!heuresCours.containsKey(enseignant))
                                heuresCours.put(enseignant, cours.getDuree());
                            else {
                                heuresCours.put(enseignant,
                                        heuresCours.get(enseignant)
                                                + cours.getDuree());
                            }
                        }
                        case TYPE_TD -> {
                            if (!heuresTD.containsKey(enseignant))
                                heuresTD.put(enseignant, cours.getDuree());
                            else {
                                heuresTD.put(enseignant,
                                        heuresTD.get(enseignant)
                                                + cours.getDuree());
                            }
                        }
                        case TYPE_TP -> {
                            if (!heuresTP.containsKey(enseignant))
                                heuresTP.put(enseignant, cours.getDuree());
                            else {
                                heuresTP.put(enseignant,
                                        heuresTP.get(enseignant)
                                                + cours.getDuree());
                            }
                        }
                        case TYPE_CONTROLE -> {
                            if (!heuresControle.containsKey(enseignant))
                                heuresControle.put(enseignant, cours.getDuree());
                            else {
                                heuresControle.put(enseignant,
                                        heuresControle.get(enseignant)
                                                + cours.getDuree());
                            }
                        }
                        default -> {
                            if (!heuresAutres.containsKey(enseignant))
                                heuresAutres.put(enseignant, cours.getDuree());
                            else {
                                heuresAutres.put(enseignant,
                                        heuresAutres.get(enseignant)
                                                + cours.getDuree());
                            }
                        }
                    }
                }
            }
        }

        // Affichage stats
        for(String enseignant : heuresCours.keySet()) {
            System.out.println("Service de "+enseignant);
            System.out.println("Heures de cours "+heuresCours.get(enseignant));
            if (heuresTD.containsKey(enseignant))
                System.out.println("Heures de TD "+heuresTD.get(enseignant));
            if (heuresTP.containsKey(enseignant))
                System.out.println("Heures de TP "+heuresTP.get(enseignant));
            if (heuresAutres.containsKey(enseignant))
                System.out.println("Autres heures "+heuresAutres.get(enseignant));
            if (heuresControle.containsKey(enseignant))
                System.out.println("Heures de contrôle "+heuresControle.get(enseignant));
        }

        for(String enseignant : heuresTD.keySet()) {
            if (!heuresCours.containsKey(enseignant)) {
                System.out.println("Service de "+enseignant);
                System.out.println("Heures de TD "+heuresTD.get(enseignant));
                if (heuresTP.containsKey(enseignant))
                    System.out.println("Heures de TP "+heuresTP.get(enseignant));
                if (heuresAutres.containsKey(enseignant))
                    System.out.println("Autres heures "+heuresAutres.get(enseignant));
                if (heuresControle.containsKey(enseignant))
                    System.out.println("Heures de contrôle "+heuresControle.get(enseignant));
            }
        }

        for(String enseignant : heuresTP.keySet()) {
            if (!heuresCours.containsKey(enseignant)) {
                System.out.println("Service de "+enseignant);
                System.out.println("Heures de TP "+heuresTP.get(enseignant));
                if (heuresAutres.containsKey(enseignant))
                    System.out.println("Autres heures "+heuresAutres.get(enseignant));
                if (heuresControle.containsKey(enseignant))
                    System.out.println("Heures de contrôle "+heuresControle.get(enseignant));
            }
        }

        for(String enseignant : heuresAutres.keySet()) {
            if (!heuresCours.containsKey(enseignant)) {
                System.out.println("Service de "+enseignant);
                System.out.println("Autres heures "+heuresAutres.get(enseignant));
                if (heuresControle.containsKey(enseignant))
                    System.out.println("Heures de contrôle "+heuresControle.get(enseignant));
            }
        }

        for(String enseignant : heuresControle.keySet()) {
            if (!heuresCours.containsKey(enseignant)) {
                System.out.println("Service de "+enseignant);
                System.out.println("Heures de contrôle "+heuresControle.get(enseignant));
            }
        }
    }
}
