package net.torguet.pdf.structure;

import net.torguet.cal.Jour;

import java.util.ArrayList;

public class JourSemaine extends Element {
    private Jour jour;

    private final ArrayList<Element> elements = new ArrayList<>();

    public JourSemaine() {
    }

    public Jour getJour() {
        return jour;
    }

    public void setJour(Jour jour) {
        this.jour = jour;
    }

    public boolean overlaps(Element e) {
        return ((x1 < e.y1 && e.y1 < x2) || (x1 < e.y2 && e.y2 < x2));
    }

    public ArrayList<Element> getElements() {
        return elements;
    }

    public void addElement(Element element) {
        this.elements.add(element);
    }
}
