package collections.set;

import java.util.*;

/**
 * TreeSet — NavigableSet backed by a red-black tree. All ops O(log n).
 * Elements must be Comparable OR a Comparator must be provided.
 * Equality is defined by compareTo, NOT equals.
 */
public class TreeSetExample {

    public static void main(String[] args) {
        // Natural ordering
        TreeSet<Integer> t = new TreeSet<>();
        t.addAll(List.of(5, 1, 8, 3, 9, 2, 7));
        System.out.println("sorted: " + t);            // [1, 2, 3, 5, 7, 8, 9]

        // ---------- Navigation ----------
        System.out.println("first: " + t.first());
        System.out.println("last:  " + t.last());
        System.out.println("floor(6):   " + t.floor(6));     // 5
        System.out.println("ceiling(6): " + t.ceiling(6));   // 7
        System.out.println("lower(5):   " + t.lower(5));     // 3
        System.out.println("higher(5):  " + t.higher(5));    // 7
        System.out.println("pollFirst:  " + t.pollFirst());  // removes 1
        System.out.println("pollLast:   " + t.pollLast());   // removes 9
        System.out.println("after polls: " + t);

        // ---------- Range views (LIVE) ----------
        System.out.println("headSet(<7):  " + t.headSet(7));
        System.out.println("tailSet(>=3): " + t.tailSet(3));
        System.out.println("subSet [3,7): " + t.subSet(3, 7));
        System.out.println("descending: " + t.descendingSet());

        // ---------- Custom comparator ----------
        TreeSet<String> ci = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        ci.add("apple"); ci.add("Banana"); ci.add("APPLE");  // case-insensitive dup
        System.out.println("case-insensitive set: " + ci);

        // ---------- null hazard ----------
        try {
            new TreeSet<Integer>().add(null);
        } catch (NullPointerException e) {
            System.out.println("TreeSet rejects null with natural ordering");
        }

        // ---------- compareTo vs equals trap ----------
        TreeSet<MyPoint> pts = new TreeSet<>(Comparator.comparingInt(p -> p.x));
        pts.add(new MyPoint(1, 100));
        pts.add(new MyPoint(1, 999));     // SAME x → treated as duplicate, NOT added
        System.out.println("treeset size (dup by comparator): " + pts.size());
    }

    static record MyPoint(int x, int y) {}
}