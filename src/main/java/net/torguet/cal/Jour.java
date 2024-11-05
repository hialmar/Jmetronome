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

    public ArrayList<Cours> getCoursAt(ZonedDateTime zonedDateTime) {
        ArrayList<Cours> coursArrayList = new ArrayList<>();
        for(var c : cours) {
            if (c.getDebut().compareTo(zonedDateTime)>0 && c.getDebut().plusMinutes((long)(c.getDuree()*60.0)).compareTo(zonedDateTime)<=0) {
                coursArrayList.add(c);
            }
        }
        return coursArrayList;
    }
}
