package net.torguet.cal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
class CoursTest {
    Cours coursAA;
    Cours coursBB;
    Cours coursCC;

    @BeforeEach
    void setUp() {
        coursAA = new Cours("Cours AA (KRTX9AA1)");
        coursAA.setDuree(2.0f);
        coursAA.setEnseignant("AA");
        coursAA.setGroupe("G1");
        coursAA.setType(TypeCours.TYPE_COURS);
        coursAA.setDebut(ZonedDateTime.of(2024,1,1, 10, 0, 0, 0, ZoneId.systemDefault()));
        coursBB = new Cours("Cours BB (KRTX9AA2)");
        coursBB.setDuree(4.0f);
        coursBB.setEnseignant("AA");
        coursBB.setGroupe("G2");
        coursBB.setType(TypeCours.TYPE_TD);
        coursBB.setDebut(ZonedDateTime.of(2024,1,1, 10, 0, 0, 0, ZoneId.systemDefault()));
        coursCC = new Cours("Cours CC (KRTX9AA3)");
        coursCC.setDuree(2.0f);
        coursCC.setEnseignant("AA/RK");
        coursCC.setGroupe("G1");
        coursCC.setType(TypeCours.TYPE_TP);
        coursCC.setDebut(ZonedDateTime.of(2024,1,1, 10, 0, 0, 0, ZoneId.systemDefault()));
    }

    @Test
    void match() {
        assertTrue(coursAA.match(coursBB, true), "devrait matcher car enseignant identique");
        assertFalse(coursAA.match(coursBB, false), "ne matche pas car seul enseignant identique"); // le reste est différent
        assertTrue(coursAA.match(coursCC, true), "devrait matcher car durée idem et enseignant inclus"); // durée et enseignant
        assertFalse(coursAA.match(coursCC, false), "ne matche pas car seul enseignant et durée matchent"); // le reste est différent

        Cours matcher = new Cours("KRTX9AA");
        assertTrue(coursAA.match(matcher, true), "devrait matcher car code apogée partiel"); // code apogée partiel
        assertTrue(coursAA.match(matcher, false), "devrait matcher car code apogée partiel et reste null"); // le reste est null

        Cours matcher2 = new Cours(null);
        assertFalse(coursAA.match(matcher2, true), "devrait ne pas matcher car matcher vide"); // matcher vide
        assertFalse(coursAA.match(matcher2, false), "devrait ne pas matcher car matcher vide"); // matcher vide

        matcher2.setType(TypeCours.TYPE_COURS);
        assertTrue(coursAA.match(matcher2, true), "devrait matcher car type cours identique");
        assertTrue(coursAA.match(matcher2, false), "devrait matcher car enseignant identique et reste vide");

        matcher2.setDuree(4.0f);
        assertTrue(coursAA.match(matcher2, true), "devrait matcher car type cours identique");
        assertFalse(coursAA.match(matcher2, false), "devrait ne pas matcher car enseignant identique mais durée différentes");

        matcher2.setEnseignant("AA");
        assertTrue(coursBB.match(matcher2, true), "devrait matcher car enseignant identique");
        assertFalse(coursBB.match(matcher2, false), "devrait ne pas matcher car type cours différent");

        assertTrue(coursCC.match(matcher2, true), "devrait matcher car enseignant inclus");
        assertFalse(coursCC.match(matcher2, false), "devrait ne pas matcher car type cours et durée différents");

        matcher2.setDuree(2.0f);
        assertTrue(coursAA.match(matcher2, true), "devrait matcher car type cours, durée idem et enseignant idem");
        assertTrue(coursAA.match(matcher2, false), "devrait matcher car tout est idem");

        matcher2.setGroupe("G1");
        assertTrue(coursAA.match(matcher2, false), "devrait matcher car tout est idem");
        assertFalse(coursBB.match(matcher2, false), "devrait ne pas matcher car durée différente");
    }
}