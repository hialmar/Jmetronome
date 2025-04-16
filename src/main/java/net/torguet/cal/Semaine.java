package net.torguet.cal;

import java.util.ArrayList;

public class Semaine {
    private int semaine;
    private ArrayList<Jour> jours = new ArrayList<>();

    public Semaine(int semaine) {
        this.semaine = semaine;
    }
    public int getSemaine() {
        return semaine;
    }
    public ArrayList<Jour> getJours() {
        return jours;
    }
    public void addJour(Jour jour) {
        jours.add(jour);
    }

    public boolean hasMatchingCours(Cours matcher, boolean matchAny) {
        boolean found = false;
        for(Jour j : this.jours) {
            if (j.hasMatchingCours(matcher, matchAny)) {
                found = true;
            }
        }
        return found;
    }
}
