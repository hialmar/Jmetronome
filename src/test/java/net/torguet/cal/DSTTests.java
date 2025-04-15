package net.torguet.cal;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.zone.ZoneRules;

public class DSTTests {

    @Test
    public void test() {
        ZonedDateTime now = ZonedDateTime.now( ZoneId.of( "Europe/Paris" ) );
        now = now.plusDays( 200 );
        ZoneId z = now.getZone();
        ZoneRules zoneRules = z.getRules();
        boolean isDst = zoneRules.isDaylightSavings( now.toInstant() );
        System.out.println( now + " " + isDst );
    }
}
