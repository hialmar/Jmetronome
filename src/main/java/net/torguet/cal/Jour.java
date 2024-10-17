package net.torguet.cal;

import java.util.ArrayList;

public class Jour {
    private int jour;
    private ArrayList<Cours> cours;

    public Jour(int jour) {
        this.jour = jour;
        this.cours = new ArrayList<>();
    }
    public int getJour() {
        return jour;
    }

    public ArrayList<Cours> getCours() {
        return cours;
    }

    public void addCours(Cours cours) {
        this.cours.add(cours);
    }
}
