package net.torguet.cal;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.TzId;
import net.fortuna.ical4j.model.parameter.Value;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.time.ZonedDateTime;

public class ICSGenerator {
    private final Calendrier calendar;

    public ICSGenerator(Calendrier calendar) {
        this.calendar = calendar;
    }

    public void generate() throws FileNotFoundException {
        Calendar copyCalendar = new Calendar();
        TzId tzParam = new TzId(TimeZoneRegistry.getGlobalZoneId("Europe/Paris").getId());

        for(Semaine semaine : calendar.getSemaines()) {
            if (semaine == null)
                break;
            for(Jour jour : semaine.getJours()) {
                if (jour == null)
                    break;
                for (Cours cours : jour.getCours()) {
                    if (cours == null)
                        break;
                    ZonedDateTime start = cours.getDebut();
                    ZonedDateTime end = start.plusHours((long)cours.getDuree());
                    System.out.println(end);

                    String resume = cours.toString();

                    VEvent week1UserA = new VEvent(start, end, resume);
                    week1UserA.getRequiredProperty(Property.DTSTART).add(tzParam).add(Value.DATE);
                    week1UserA.getRequiredProperty(Property.DTEND).add(tzParam).add(Value.DATE);

                    copyCalendar.add(week1UserA);
                }
            }
        }

        System.out.println(copyCalendar);

        PrintStream ps = new PrintStream(new FileOutputStream("test.ics"));

        //write operation workbook using file out object
        ps.println(copyCalendar);
        ps.close();
    }


}
