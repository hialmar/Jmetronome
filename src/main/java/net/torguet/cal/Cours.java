package net.torguet.cal;

import java.time.ZonedDateTime;

public class Cours {
    private String intitule;
    private int type;
    private ZonedDateTime debut;
    private float duree;

    public Cours(String intitule, int type, ZonedDateTime debut, float duree) {
        this.intitule = intitule;
        this.type = type;
        this.debut = debut;
        this.duree = duree;
    }

    public String getIntitule() {
        return intitule;
    }

    public void setIntitule(String intitule) {
        this.intitule = intitule;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public ZonedDateTime getDebut() {
        return debut;
    }

    public void setDebut(ZonedDateTime debut) {
        this.debut = debut;
    }

    public float getDuree() {
        return duree;
    }

    public void setDuree(float duree) {
        this.duree = duree;
    }
}
