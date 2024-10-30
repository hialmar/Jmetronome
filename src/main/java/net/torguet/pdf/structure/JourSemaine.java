package net.torguet.pdf.structure;

import net.torguet.cal.Jour;

public class JourSemaine extends Element {
    private Jour jour;

    public JourSemaine() {
    }

    public Jour getJour() {
        return jour;
    }

    public void setJour(Jour jour) {
        this.jour = jour;
    }

    public boolean overlapsX(Element e) {
        return ((x1 < e.y1 && e.y1 < x2) || (x1 < e.y2 && e.y2 < x2));
    }

    public boolean overlapsY(Element e) {
        return ((y1 < e.x1 && e.x1 < y2) || (y1 < e.x2 && e.x2 < y2));
    }
}
