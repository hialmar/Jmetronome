package net.torguet.pdf.structure;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Horaire extends Element {
    private long heures = 0;
    private long minutes = 0;

    public void setChaine(String s) {
        super.setChaine(s);

        ZoneId zoneId = ZoneId.of("Europe/Paris");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(zoneId);
        ZonedDateTime zonedDateTime = ZonedDateTime.parse("01/01/2024 "+s+":00", formatter);
        heures = zonedDateTime.getHour();
        if (heures<8) heures+=12;
        minutes = zonedDateTime.getMinute();
        if (heures == 8 && minutes == 15) heures+=12;
        if (heures == 11 && minutes == 59) heures+=12;
        System.out.println(s+" => Heures : "+heures+" Minutes : "+minutes);
    }

    public long getHeures() {
        return heures;
    }

    public long getMinutes() {
        return minutes;
    }
}
