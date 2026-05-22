package collections.map;

import java.util.*;

/**
 * EnumMap — array-backed map for enum keys. Very fast and compact.
 * Maintains natural enum (declaration) order in iteration. Rejects null keys.
 */
public class EnumMapExample {

    enum Day { MON, TUE, WED, THU, FRI, SAT, SUN }

    public static void main(String[] args) {

        EnumMap<Day, Integer> hours = new EnumMap<>(Day.class);
        hours.put(Day.MON, 8);
        hours.put(Day.TUE, 9);
        hours.put(Day.WED, 7);
        hours.put(Day.SAT, 0);
        System.out.println("hours (enum order): " + hours);

        // get / contains / iteration
        System.out.println("MON hours: " + hours.get(Day.MON));
        System.out.println("contains SUN: " + hours.containsKey(Day.SUN));

        // ordered iteration — by enum declaration order
        for (Map.Entry<Day, Integer> e : hours.entrySet())
            System.out.println("  " + e.getKey() + " = " + e.getValue());

        // null key rejected
        try { hours.put(null, 0); }
        catch (NullPointerException e) { System.out.println("EnumMap rejects null key"); }
    }
}