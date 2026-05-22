package collections.set;

import java.util.*;

/**
 * EnumSet — bit-vector implementation specialized for enum types. Very fast & compact.
 * No public constructor — use static factories.
 */
public class EnumSetExample {

    enum Day { MON, TUE, WED, THU, FRI, SAT, SUN }

    public static void main(String[] args) {
        EnumSet<Day> none      = EnumSet.noneOf(Day.class);
        EnumSet<Day> all       = EnumSet.allOf(Day.class);
        EnumSet<Day> weekend   = EnumSet.of(Day.SAT, Day.SUN);
        EnumSet<Day> weekdays  = EnumSet.complementOf(weekend);
        EnumSet<Day> midweek   = EnumSet.range(Day.TUE, Day.THU);

        System.out.println("all:      " + all);
        System.out.println("weekend:  " + weekend);
        System.out.println("weekdays: " + weekdays);
        System.out.println("midweek:  " + midweek);

        // Normal Set ops
        weekdays.add(Day.SAT);
        weekdays.remove(Day.MON);
        System.out.println("modified weekdays: " + weekdays);

        // Set algebra is fast (single long bitwise ops for small enums)
        EnumSet<Day> u = EnumSet.copyOf(weekend); u.addAll(midweek);
        System.out.println("union weekend ∪ midweek: " + u);

        // Cannot store null
        try {
            weekend.add(null);
        } catch (NullPointerException e) {
            System.out.println("EnumSet rejects null");
        }
    }
}