package collections.map;

import java.util.*;

/**
 * TreeMap — NavigableMap backed by red-black tree. O(log n) all ops.
 * Sorted by key (natural or Comparator).
 */
public class TreeMapExample {

    public static void main(String[] args) {

        // ---------- Sorted by key ----------
        TreeMap<Integer, String> t = new TreeMap<>();
        t.put(3, "three"); t.put(1, "one"); t.put(2, "two"); t.put(5, "five");
        System.out.println("sorted: " + t);

        // ---------- Navigation ----------
        System.out.println("firstKey:    " + t.firstKey());
        System.out.println("lastKey:     " + t.lastKey());
        System.out.println("floorKey(4): " + t.floorKey(4));     // 3
        System.out.println("ceilingKey(4): " + t.ceilingKey(4)); // 5
        System.out.println("lowerKey(3): " + t.lowerKey(3));     // 2
        System.out.println("higherKey(3):" + t.higherKey(3));    // 5
        System.out.println("firstEntry:  " + t.firstEntry());
        System.out.println("pollFirst:   " + t.pollFirstEntry());
        System.out.println("after pollFirst: " + t);

        // ---------- Range views (live) ----------
        TreeMap<Integer, String> base = new TreeMap<>(Map.of(1,"a", 2,"b", 3,"c", 4,"d", 5,"e"));
        System.out.println("headMap(<3): " + base.headMap(3));
        System.out.println("tailMap(>=3): " + base.tailMap(3));
        System.out.println("subMap [2,5): " + base.subMap(2, 5));
        System.out.println("descending: " + base.descendingMap());

        // ---------- Custom comparator (reverse) ----------
        TreeMap<String, Integer> r = new TreeMap<>(Comparator.reverseOrder());
        r.put("apple", 1); r.put("banana", 2); r.put("cherry", 3);
        System.out.println("reversed: " + r);

        // ---------- null key rejected ----------
        try { new TreeMap<Integer, String>().put(null, "x"); }
        catch (NullPointerException e) { System.out.println("TreeMap rejects null key"); }
    }
}