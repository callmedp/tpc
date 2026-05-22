package collections.dsa;

import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 7 — TREEMAP & TREESET  (ordered, log-n)                          │
 * │  Prereq:  Modules 5–6                                                    │
 * │  Goal:    floor/ceiling/higher/lower — the queries HashMap can't do      │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * If your problem needs:
 *   • the closest key not greater than X (`floorKey`)
 *   • the closest key not less than X    (`ceilingKey`)
 *   • range queries  [lo, hi)
 *   • smallest / largest currently in the structure
 * …then it's TreeMap / TreeSet (Red-Black tree, O(log n) every op).
 *
 * What you'll learn:
 *   • firstKey / lastKey / floorKey / ceilingKey / lowerKey / higherKey
 *   • firstEntry / lastEntry / floorEntry / ceilingEntry / lowerEntry / higherEntry
 *   • pollFirstEntry / pollLastEntry                  — remove min/max
 *   • headMap / tailMap / subMap                      — range views
 *   • descendingMap / descendingKeySet / navigableKeySet
 *   • TreeSet mirrors: floor / ceiling / lower / higher / first / last
 *   • Patterns: My Calendar I, Find Closest, Online Stock Span (variant)
 */
public class Module07_TreeMapTreeSet {

    public static void main(String[] args) {

        // ─────────────────────────────────────────────────────────────────────
        // 1. TreeMap basics
        // ─────────────────────────────────────────────────────────────────────
        TreeMap<Integer, String> tm = new TreeMap<>();
        tm.put(10, "A"); tm.put(20, "B"); tm.put(30, "C"); tm.put(40, "D");

        tm.firstKey();          // 10
        tm.lastKey();           // 40
        tm.firstEntry();        // 10=A
        tm.lastEntry();         // 40=D

        // ─────────────────────────────────────────────────────────────────────
        // 2. Floor / ceiling / lower / higher — the WHOLE reason TreeMap exists
        //    floor   = greatest key ≤ target   (≤)
        //    ceiling = smallest  key ≥ target   (≥)
        //    lower   = greatest key  < target   ( strictly less    )
        //    higher  = smallest  key  > target   ( strictly greater )
        // ─────────────────────────────────────────────────────────────────────
        tm.floorKey(25);        // 20
        tm.ceilingKey(25);      // 30
        tm.lowerKey(20);        // 10
        tm.higherKey(20);       // 30
        tm.floorEntry(25);      // 20=B
        tm.ceilingEntry(25);    // 30=C
        tm.lowerEntry(20);      // 10=A
        tm.higherEntry(20);     // 30=C

        // ─────────────────────────────────────────────────────────────────────
        // 3. Range views — return a LIVE submap backed by the tree
        // ─────────────────────────────────────────────────────────────────────
        tm.headMap(30);                // {10=A, 20=B}              (exclusive end)
        tm.headMap(30, true);          // {10=A, 20=B, 30=C}        (inclusive end)
        tm.tailMap(20);                // {20=B, 30=C, 40=D}        (inclusive start)
        tm.tailMap(20, false);         // {30=C, 40=D}              (exclusive start)
        tm.subMap(20, 40);             // {20=B, 30=C}              [from, to)
        tm.subMap(20, true, 40, true); // {20=B, 30=C, 40=D}        inclusive both

        // ─────────────────────────────────────────────────────────────────────
        // 4. Pop min / pop max — like a sorted priority queue
        // ─────────────────────────────────────────────────────────────────────
        Map.Entry<Integer, String> snapshot = tm.firstEntry();
        // tm.pollFirstEntry();       // remove + return smallest
        // tm.pollLastEntry();        // remove + return largest

        // ─────────────────────────────────────────────────────────────────────
        // 5. Reverse views
        // ─────────────────────────────────────────────────────────────────────
        tm.descendingMap();            // view in reverse-key order
        tm.descendingKeySet();
        tm.navigableKeySet();          // forward navigable view

        // ─────────────────────────────────────────────────────────────────────
        // 6. TreeSet — same toolkit, no values
        // ─────────────────────────────────────────────────────────────────────
        TreeSet<Integer> ts = new TreeSet<>();
        ts.add(10); ts.add(20); ts.add(30); ts.add(40);
        ts.floor(25);     ts.ceiling(25);
        ts.lower(20);     ts.higher(20);
        ts.first();       ts.last();
        ts.headSet(30);   ts.tailSet(20);   ts.subSet(20, 40);
        ts.descendingSet();
        ts.descendingIterator();
        ts.pollFirst();   // remove + return smallest
        ts.pollLast();    // remove + return largest

        // ─────────────────────────────────────────────────────────────────────
        // 7. PATTERN — MyCalendar I (LC 729)
        //    Reject a booking [start, end) if it overlaps any existing booking.
        //    Use TreeMap<start, end>.  Check the booking immediately to our left
        //    (floorEntry) and immediately to our right (ceilingEntry).
        // ─────────────────────────────────────────────────────────────────────
        TreeMap<Integer, Integer> cal = new TreeMap<>();
        boolean ok1 = book(cal, 10, 20);
        boolean ok2 = book(cal, 30, 40);
        boolean ok3 = book(cal, 15, 25);   // overlaps [10,20) → false
        System.out.printf("calendar  : ok1=%b ok2=%b ok3=%b  → %s%n", ok1, ok2, ok3, cal);

        // ─────────────────────────────────────────────────────────────────────
        // 8. PATTERN — Find Closest Element (LC 658 helper)
        //    Closest to `target` is whichever of floor or ceiling is nearer.
        // ─────────────────────────────────────────────────────────────────────
        int target = 27;
        Integer lo = ts.floor(target), hi = ts.ceiling(target);
        Integer closest = lo == null ? hi
                         : hi == null ? lo
                         : (target - lo) <= (hi - target) ? lo : hi;
        System.out.println("closest to 27 in " + ts + " is " + closest);

        // ─────────────────────────────────────────────────────────────────────
        // 9. PATTERN — Sliding-window MAX with TreeMap counts (alt to deque)
        //    Useful when you also need other stats (min, kth) on the window.
        // ─────────────────────────────────────────────────────────────────────
        int[] arr = {1, 3, -1, -3, 5, 3, 6, 7};
        int k = 3;
        TreeMap<Integer, Integer> window = new TreeMap<>();
        java.util.List<Integer> maxima = new java.util.ArrayList<>();
        for (int i = 0; i < arr.length; i++) {
            window.merge(arr[i], 1, Integer::sum);
            if (i >= k) {
                int leaving = arr[i - k];
                if (window.merge(leaving, -1, Integer::sum) == 0) window.remove(leaving);
            }
            if (i >= k - 1) maxima.add(window.lastKey());
        }
        System.out.println("sliding maxima = " + maxima);
        System.out.println("snapshot first = " + snapshot);

        // ─────────────────────────────────────────────────────────────────────
        // Practice problems:
        //   • LC 729   My Calendar I
        //   • LC 731   My Calendar II
        //   • LC 855   Exam Room                     (TreeSet of seats)
        //   • LC 220   Contains Duplicate III        (TreeSet floor/ceiling)
        //   • LC 658   Find K Closest Elements
        //   • LC 1146  Snapshot Array                (TreeMap per index)
        // ─────────────────────────────────────────────────────────────────────
    }

    private static boolean book(TreeMap<Integer, Integer> cal, int start, int end) {
        Map.Entry<Integer, Integer> prev = cal.floorEntry(start);
        Map.Entry<Integer, Integer> next = cal.ceilingEntry(start);
        if (prev != null && prev.getValue() > start) return false;
        if (next != null && next.getKey()   < end)   return false;
        cal.put(start, end);
        return true;
    }
}