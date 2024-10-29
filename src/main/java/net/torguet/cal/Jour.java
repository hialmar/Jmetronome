package net.torguet.cal;

import java.time.ZonedDateTime;
import java.util.ArrayList;

public class Jour {
    private final ZonedDateTime date;
    private final ArrayList<Cours> cours;



    public Jour(ZonedDateTime date) {
        this.date = date;
        this.cours = new ArrayList<>();
    }
    public ZonedDateTime getDate() {
        return date;
    }

    public ArrayList<Cours> getCours() {
        return cours;
    }

    public void addCours(Cours cours) {
        this.cours.add(cours);
    }
}
