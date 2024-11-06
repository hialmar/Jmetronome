package net.torguet.cal;

import java.time.ZonedDateTime;

public class Cours {
    private String intitule;
    private String enseignant;
    private TypeCours type;
    private ZonedDateTime debut;
    private float duree;
    private boolean enParallele;
    private String salle;
    private String groupe;

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

    public String getSalle() {
        return salle;
    }

    public void setSalle(String salle) {
        this.salle = salle;
    }

    public String getGroupe() {
        return groupe;
    }

    public void setGroupe(String groupe) {
        this.groupe = groupe;
    }

    @Override
    public String toString() {
        return  (type == TypeCours.TYPE_CONTROLE ? "Contrôle " : "") +
                intitule +
                (enseignant != null ? ", ens : " + enseignant : "") +
                (groupe != null ? ", grp : " + groupe : "") +
                (salle != null ? ", salle : " + salle : "");
    }
}
