package net.torguet.cal;

import java.time.ZonedDateTime;

public class Cours {
    private String intitule;
    private String enseignant;
    private TypeCours type;
    private ZonedDateTime debut;
    private float duree;
    private boolean enParallele;
    public Cours(String intitule) {
        this.intitule = intitule;
        this.type = TypeCours.TYPE_AUTRE;
        this.debut = null;
        this.duree = 0;
        this.enParallele = false;
    }

    public String getIntitule() {
        return intitule;
    }

    public void setIntitule(String intitule) {
        this.intitule = intitule;
    }

    public TypeCours getType() {
        return type;
    }

    public void setType(TypeCours type) {
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

    public boolean isEnParallele() {
        return enParallele;
    }

    public void setEnParallele(boolean enParallele) {
        this.enParallele = enParallele;
    }

    public String getEnseignant() {
        return enseignant;
    }

    public void setEnseignant(String enseignant) {
        this.enseignant = enseignant;
    }

    @Override
    public String toString() {
        return "Cours{" +
                "intitule='" + intitule + '\'' +
                ", enseignant='" + enseignant + '\'' +
                ", type=" + type +
                ", debut=" + debut +
                ", duree=" + duree +
                ", enParallele=" + enParallele +
                '}';
    }
}