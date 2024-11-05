package net.torguet.pdf.structure;

import java.util.ArrayList;

public class Element {
    protected String chaine;
    protected float x1;
    protected float y1;
    protected float x2;
    protected float y2;

    private boolean salle = false;
    private boolean amphi = false;
    private String salleCours;

    private final ArrayList<Horaire> horaires = new ArrayList<>();

    public Element() {
    }

    public String getChaine() {
        return chaine;
    }

    public void setChaine(String chaine) {
        this.chaine = chaine;

        if(chaine.contains("Salle: ")) {
            salleCours = chaine.substring(7);

            if (salleCours.startsWith("Amphi")) {
                if (salleCours.length() != 5) {
                    salleCours = salleCours.substring(5);
                    amphi = true;
                    System.out.println("Amphi trouvé : "+salleCours);
                }
            } else {
                salle = true;
                System.out.println("Salle trouvée : "+salleCours);
            }
        }

        int parentheseDebut = chaine.indexOf('(');
        if (parentheseDebut>0) {
            int parentheseFin = chaine.indexOf(')');
            if (parentheseFin>0 && parentheseFin - parentheseDebut < 5) {
                salleCours = chaine;
                amphi = true;
                System.out.println("Amphi trouvé : " + salleCours);
            }
        }
    }

    public float getX1() {
        return x1;
    }

    public void setX1(float x1) {
        this.x1 = x1;
    }

    public float getY1() {
        return y1;
    }

    public void setY1(float y1) {
        this.y1 = y1;
    }

    public float getX2() {
        return x2;
    }

    public void setX2(float x2) {
        this.x2 = x2;
    }

    public float getY2() {
        return y2;
    }

    public void setY2(float y2) {
        this.y2 = y2;
    }

    public boolean overlapsX(Element e) {
        return ((x1 <= e.x1 && e.x1 <= x2) || (x1 <= e.x2 && e.x2 <= x2));
    }

    public boolean overlapsY(Element e) {
        return ((y1 <= e.y1 && e.y1 <= y2) || (y1 <= e.y2 && e.y2 <= y2));
    }

    public boolean isSalle() {
        return salle;
    }

    public boolean isAmphi() {
        return amphi;
    }

    public String getSalleCours() {
        return salleCours;
    }

    @Override
    public String toString() {
        return "Element{" +
                "chaine='" + chaine + '\'' +
                ", x1=" + x1 +
                ", y1=" + y1 +
                ", x2=" + x2 +
                ", y2=" + y2 +
                '}';
    }

    public ArrayList<Horaire> getHoraires() {
        return horaires;
    }

    public void addHoraire(Horaire horaire) {
        this.horaires.add(horaire);
    }
}
