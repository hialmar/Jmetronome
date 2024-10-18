package net.torguet.cal;

import java.util.ArrayList;
import java.util.Date;

public class Jour {
    private Date date;
    private ArrayList<Cours> cours;

    public Jour(Date date) {
        this.date = date;
        this.cours = new ArrayList<>();
    }
    public Date getDate() {
        return date;
    }

    public ArrayList<Cours> getCours() {
        return cours;
    }

    public void addCours(Cours cours) {
        this.cours.add(cours);
    }
}
