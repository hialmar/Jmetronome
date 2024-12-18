package net.torguet.cal;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;

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
        this.generate(null, true, "test.ics");
    }

    public void generate(Cours matcher, boolean matchAny, String fileName) throws FileNotFoundException {
        Calendar copyCalendar = new Calendar();
        //TzId tzParam = new TzId(TimeZoneRegistry.getGlobalZoneId("Europe/Paris").getId());

        for(Semaine semaine : calendar.getSemaines()) {
            if (semaine == null)
                continue;
            for(Jour jour : semaine.getJours()) {
                if (jour == null)
                    continue;
                for (Cours cours : jour.getCours()) {
                    if (cours == null)
                        continue;
                    if (!cours.match(matcher, matchAny)) {
                        continue;
                    }
                    ZonedDateTime start = cours.getDebut();
                    ZonedDateTime end = start.plusHours((long)cours.getDuree());
                    System.out.println(end);

                    String resume = cours.toString();

                    VEvent event = new VEvent(start, end, resume);
                    //event.getRequiredProperty(Property.DTSTART).add(tzParam).add(Value.DATE);
                    //event.getRequiredProperty(Property.DTEND).add(tzParam).add(Value.DATE);

                    copyCalendar.add(event);
                }
            }
        }

        System.out.println(copyCalendar);

        PrintStream ps = new PrintStream(new FileOutputStream(fileName));

        //write operation workbook using file out object
        ps.println(copyCalendar);
        ps.close();
    }


}
