package net.torguet.cal;

import java.util.ArrayList;
import java.util.Date;

public class Calendrier {
    private ArrayList<Semaine> semaines = new ArrayList<>();
    private Date startDate;

    public Calendrier() { }

    public void addSemaine(Semaine semaine) {
        semaines.add(semaine);
    }

    public ArrayList<Semaine> getSemaines() {
        return semaines;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
}
