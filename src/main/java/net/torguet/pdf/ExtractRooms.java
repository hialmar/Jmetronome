package net.torguet.pdf;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import net.torguet.cal.Calendrier;
import net.torguet.cal.Jour;
import net.torguet.pdf.structure.Element;
import net.torguet.pdf.structure.Horaire;
import net.torguet.pdf.structure.JourSemaine;
import net.torguet.xlsx.old.OldReader;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This is an example on how to get some x/y coordinates of text.
 *
 * @author Ben Litchfield
 */
public class ExtractRooms extends PDFTextStripper
{
    private static final String [] semTab = {"lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi"};
    private static final String dateMatcher = "^([0-9]{2})\\/([0-9]{2})\\/([0-9]{4})";
    private static final String timeMatcher = "^([0-9]{2})\\:([0-9]{2})";

    private final Calendrier calendrier;

    private final ArrayList<JourSemaine> joursSemaine = new ArrayList<>();
    private final ArrayList<Horaire> horaires = new ArrayList<>();
    private final ArrayList<Element> elements = new ArrayList<>();
    private final ArrayList<Element> dates = new ArrayList<>();
    private final ArrayList<String> semaine = new ArrayList<>();

    private boolean suite = false;


    /**
     * Instantiate a new PDFTextStripper object.
     *
     * @throws IOException If there is an error loading the properties.
     */
    public ExtractRooms(Calendrier calendrier) throws IOException
    {
        this.calendrier = calendrier;
        Collections.addAll(semaine, semTab);
    }

    /**
     * This will print the documents data.
     *
     * @param args The command line arguments.
     *
     * @throws IOException If there is an error parsing the document.
     */
    public static void main( String[] args ) throws Exception
    {
        OldReader oldReader = new OldReader("2024-2025 Master.xlsx", 1);

        Calendrier calendrier = oldReader.traiterFichier();

        oldReader.close();

        try (PDDocument document = Loader.loadPDF(new File("edt_3A_2024_05_28.pdf")))
        {
            PDFTextStripper stripper = new ExtractRooms(calendrier);
            stripper.setSortByPosition( true );
            stripper.setStartPage( 3 ); // 0 );
            stripper.setEndPage( 3 ); // document.getNumberOfPages() );

            Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
            stripper.writeText(document, dummy);
        }
    }

    /**
     * Override the default functionality of PDFTextStripper.
     */
    @Override
    protected void writeString(String string, List<TextPosition> textPositions) throws IOException
    {
        System.out.println(string);

        float x1 = 1000, y1 = 1000, x2 = -1, y2 = -1;

        float height = -1;

        int i = 0;

        for (TextPosition text : textPositions)
        {
            if (i==0) {
                System.out.println( "String[" + text.getXDirAdj() + "," +
                        text.getYDirAdj() + " fs=" + text.getFontSize() + " xscale=" +
                        text.getXScale() +  " yscale=" +
                        text.getYScale() + " height=" + text.getHeightDir() + " space=" +
                        text.getWidthOfSpace() + " width=" +
                        text.getWidthDirAdj() + "]" + text.getUnicode() );
                i++;
            }

            if(height == -1) {
                height = text.getPageHeight();
                System.out.println("Page Height = "+height);
            }

            if (text.getXDirAdj() < x1) {
                x1 = text.getXDirAdj();
            }
            if (text.getYDirAdj() < y1) {
                y1 = text.getYDirAdj();
            }
            if (text.getXDirAdj() + text.getWidthDirAdj() > x2) {
                x2 = text.getXDirAdj() + text.getWidthDirAdj() ;
            }
            if (text.getYDirAdj() + text.getHeightDir()  > y2) {
                y2 = text.getYDirAdj() + text.getHeightDir();
            }
        }

        System.out.println("x1 : "+x1+" y1 : "+y1+" x2 : "+x2+" y2 : "+y2);

        Element element;

        if (semaine.contains(string)) {
            element = new JourSemaine();
            element.setX1(height - x2);
            element.setX2(height - x1);
            joursSemaine.add((JourSemaine) element);
            System.out.println("Jour Semaine " +string);
        } else if (string.matches(timeMatcher)) {
            element = new Horaire();
            element.setX1(x1);
            element.setX2(x2);
            System.out.println("Horaire : "+string);
            horaires.add((Horaire) element);
        } else {
            element = new Element();

            if(string.matches(dateMatcher)) { // date
                System.out.println("Date : "+string);
                element.setX1(height - x2);
                element.setX2(height - x1);
                dates.add(element);
            } else {
                System.out.println("Autre : "+string);
                element.setX1(x1);
                element.setX2(x2);
                elements.add(element);
            }
        }
        element.setChaine(string);
        element.setY1(y1);
        element.setY2(y2);
    }

    /**
     * Start a new page. Default implementation is to do nothing. Subclasses may provide additional information.
     *
     * @param page The page we are about to process.
     *
     * @throws IOException If there is any error writing to the stream.
     */
    protected void startPage(PDPage page) throws IOException
    {
        System.out.println("Page begins");
        if (!this.suite) {
            elements.clear();
            joursSemaine.clear();
            System.out.println("Nouvelle semaine");
        }
    }

    /**
     * End a page. Default implementation is to do nothing. Subclasses may provide additional information.
     *
     * @param page The page we are about to process.
     *
     * @throws IOException If there is any error writing to the stream.
     */
    protected void endPage(PDPage page) throws IOException
    {
        System.out.println("Page ends");

        boolean fini = true;

        for(var date : dates) {
            for (var js : joursSemaine) {
                if (date.overlapsX(js)) {
                    System.out.println("La date : "+date+" correspond avec le jour "+js);
                    ZoneId zoneId = ZoneId.of("Europe/Paris");
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(zoneId);
                    ZonedDateTime zonedDateTime = ZonedDateTime.parse(date.getChaine()+" 00:00:00", formatter);
                    js.setJour(new Jour(zonedDateTime));
                    System.out.println("Jour : "+js.getJour().getDate());
                    // La date est plus étendue que le jour de la semaine
                    js.setX1(date.getX1());
                    js.setX2(date.getX2());
                    js.setY1(date.getY1());
                    js.setY2(date.getY2());
                }
            }
        }

        for(var e : elements) {
            for (var js : joursSemaine) {
                if (js.overlaps(e)) {
                    // System.out.println("L'élément : "+e+" correspond avec le jour "+js);
                    js.addElement(e);
                }
            }
            for (var h : horaires) {
                if (e.overlapsX(h)) {
                    // System.out.println("L'élément : "+e+" correspond en X avec l'horaire "+h);
                    e.addHoraire(h);
                }
            }

            if (e.getChaine().contains("suite")) {
                System.out.println("Suite de la semaine");
                fini = false;
            }
        }

        for (var js : joursSemaine) {
            Jour jour = calendrier.getJour(js.getJour().getDate());
            System.out.println(js.getJour().getDate());
            System.out.println(jour.getDate());
            for (var cours : jour.getCours()) {
                System.out.println("On travaille sur le cours "+cours);
                long heureDebut = cours.getDebut().getHour();
                long minuteDebut = cours.getDebut().getMinute();
                float hDebut = heureDebut + minuteDebut / 60.0f;
                System.out.println("Heure début : "+hDebut);
                ZonedDateTime fin = cours.getDebut().plusMinutes((long)(cours.getDuree()*60));
                long heureFin = fin.getHour();
                long minuteFin = fin.getMinute();
                float hFin = heureFin + minuteFin / 60.0f;
                System.out.println("Heure fin : "+hFin);
                boolean trouve = false;
                for(var e : js.getElements()) {
                    if (e.isAmphi() || e.isSalle()) {
                        for (var h : e.getHoraires()) {
                            float hH = h.getHeures() + h.getMinutes() / 60.0f;
                            // System.out.println("Horaire "+hH);
                            if (hH >= hDebut && hH <= hFin) {
                                trouve = true;
                                System.out.println("Salle trouvée : "+e.getSalleCours());
                                if (cours.getSalle() == null) {
                                    cours.setSalle(e.getSalleCours());
                                    break;
                                }
                            }
                        }
                        if (trouve) {
                            break;
                        }
                    }
                }

                if (!trouve) {
                    System.err.println("Pas de salle trouvée pour le cours : "+cours);
                }
            }
        }

        this.suite = ! fini;
    }


    /**
     * This will print the usage for this document.
     */
    private static void usage()
    {
        System.err.println( "Usage: java " + ExtractRooms.class.getName() + " <input-pdf>" );
    }
}