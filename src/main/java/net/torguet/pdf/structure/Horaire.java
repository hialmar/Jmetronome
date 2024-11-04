package net.torguet.pdf.structure;

import java.util.ArrayList;

public class Horaire extends Element {
    private final ArrayList<Element> elements = new ArrayList<>();

    public ArrayList<Element> getElements() {
        return elements;
    }

    public void addElement(Element element) {
        elements.add(element);
    }
}
