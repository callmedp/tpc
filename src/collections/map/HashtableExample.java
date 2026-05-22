package collections.map;

import java.util.*;

/**
 * Hashtable — LEGACY synchronized map. Every method is synchronized → coarse contention.
 * Rejects null keys AND null values. Prefer ConcurrentHashMap.
 */
public class HashtableExample {

    public static void main(String[] args) {
        Hashtable<String, Integer> h = new Hashtable<>();
        h.put("a", 1); h.put("b", 2);
        System.out.println("hashtable: " + h);

        try { h.put(null, 3); }
        catch (NullPointerException e) { System.out.println("Hashtable rejects null key"); }
        try { h.put("x", null); }
        catch (NullPointerException e) { System.out.println("Hashtable rejects null value"); }

        // Legacy iteration with Enumeration
        Enumeration<String> keys = h.keys();
        while (keys.hasMoreElements()) System.out.print(keys.nextElement() + " ");
        System.out.println();

        // Properties extends Hashtable<Object,Object> — still used for .properties files
        Properties props = new Properties();
        props.setProperty("db.host", "localhost");
        props.setProperty("db.port", "5432");
        System.out.println("prop: " + props.getProperty("db.host"));
    }
}