package net.torguet.cal;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;

public class Calendrier {
    private final ArrayList<Semaine> semaines = new ArrayList<>();
    private ZonedDateTime startDate;
    private final HashMap<ZonedDateTime, Jour> jourHashMap = new HashMap<>();

    public Calendrier() { }

    public void addSemaine(Semaine semaine) {
        semaines.add(semaine);
        for(var jour : semaine.getJours()) {
            jourHashMap.put(jour.getDate(), jour);
        }
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

    public Jour getJour(ZonedDateTime zonedDateTime) {
        return jourHashMap.get(zonedDateTime);
    }
}
