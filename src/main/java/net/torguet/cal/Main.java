package net.torguet.cal;


import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.TzId;
import net.fortuna.ical4j.model.parameter.Value;

import java.io.*;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws IOException {
        TzId tzParam = new TzId(TimeZoneRegistry.getGlobalZoneId("Europe/Paris").getId());

        // Add events, etc..
        Calendar copyCalendar = new Calendar();

        ZonedDateTime start = ZonedDateTime.now().with(ChronoField.DAY_OF_WEEK,
                DayOfWeek.MONDAY.getValue()).withHour(9).withMinute(0).withSecond(0);

        System.out.println(start);

        ZonedDateTime end = start.plusHours(2);

        System.out.println(end);

        VEvent week1UserA = new VEvent(start, end, "Week 1 - User A");
        week1UserA.getRequiredProperty(Property.DTSTART).add(tzParam).add(Value.DATE);
        week1UserA.getRequiredProperty(Property.DTEND).add(tzParam).add(Value.DATE);
        /*
        WeekDayList monToFri = new WeekDayList(MO, TU, WE, TH, FR);
        Recur<ZonedDateTime> week1UserARecur = new Recur.Builder<ZonedDateTime>().frequency(Frequency.WEEKLY)
                .until(end).interval(3).dayList(monToFri).hourList(9).build();
        week1UserA.add(new RRule<>(week1UserARecur)).add(new Uid("000001@modularity.net.au"));

        start = start.plusWeeks(1);
        end = end.plusWeeks(1);

        VEvent week2UserB = new VEvent(start, java.time.Duration.ofHours(8), "Week 2 - User B");
        week2UserB.getRequiredProperty(Property.DTSTART).add(tzParam).add(Value.DATE);

        Recur<ZonedDateTime> week2UserBRecur = new Recur.Builder<ZonedDateTime>().frequency(Frequency.WEEKLY)
                .until(end).interval(3).dayList(monToFri).hourList(9).build();
        week2UserB.add(new RRule<>(week2UserBRecur)).add(new Uid("000002@modularity.net.au"));

        start = start.plusWeeks(1);
        end = end.plusWeeks(1);

        VEvent week3UserC = new VEvent(start, java.time.Duration.ofHours(8), "Week 3 - User C");
        week3UserC.getRequiredProperty(Property.DTSTART).add(tzParam);

        Recur<ZonedDateTime> week3UserCRecur = new Recur.Builder<ZonedDateTime>().frequency(Frequency.WEEKLY)
                .until(end).interval(3).dayList(monToFri).hourList(9).build();
        week3UserC.add(new RRule<>(week3UserCRecur)).add(new Uid("000003@modularity.net.au"));

        copyCalendar.add(week1UserA).add(week2UserB).add(week3UserC);

        System.out.println(copyCalendar);

         */
        copyCalendar.add(week1UserA);

        System.out.println(copyCalendar);

        PrintStream ps = new PrintStream(new FileOutputStream("test.ics"));

        //write operation workbook using file out object
        ps.println(copyCalendar);
        ps.close();


    }
}