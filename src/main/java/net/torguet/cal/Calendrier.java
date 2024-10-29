package net.torguet.cal;

import java.time.ZonedDateTime;
import java.util.ArrayList;

public class Calendrier {
    private final ArrayList<Semaine> semaines = new ArrayList<>();
    private ZonedDateTime startDate;

    public Calendrier() { }

    public void addSemaine(Semaine semaine) {
        semaines.add(semaine);
    }

    public ArrayList<Semaine> getSemaines() {
        return semaines;
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(ZonedDateTime startDate) {
        this.startDate = startDate;
    }
}
