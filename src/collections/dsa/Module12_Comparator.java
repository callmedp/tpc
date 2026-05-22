package collections.dsa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 12 — COMPARATOR & COMPARABLE                                     │
 * │  Prereq:  Modules 3, 5, 11                                               │
 * │  Goal:    custom ordering — every interview problem that says "sort by…" │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 *   Comparable<T>     — natural order: implement compareTo on the type
 *   Comparator<T>     — external order: a strategy you pass to sort / PQ / TreeMap
 *
 *   Contract:
 *       compare(a, b) < 0   ⇒  a comes BEFORE b
 *       compare(a, b) = 0   ⇒  equal in this ordering
 *       compare(a, b) > 0   ⇒  a comes AFTER b
 *   Must be consistent: anti-symmetric and transitive.
 *
 *   Java 8 factories (use these — DON'T hand-write classic Comparators):
 *       Comparator.comparing       / .comparingInt / .comparingLong / .comparingDouble
 *       Comparator.naturalOrder    / .reverseOrder
 *       Comparator.nullsFirst      / .nullsLast
 *       .thenComparing             / .thenComparingInt
 *       .reversed
 *
 *   The famous trap:
 *       (a, b) -> a - b              ←  can OVERFLOW for big ints. Use Integer.compare.
 */
public class Module12_Comparator {

    public static void main(String[] args) {

        // ─────────────────────────────────────────────────────────────────────
        // 1. The pre-Java-8 way (avoid — verbose)
        // ─────────────────────────────────────────────────────────────────────
        Comparator<Integer> oldStyle = new Comparator<>() {
            @Override public int compare(Integer a, Integer b) { return Integer.compare(a, b); }
        };

        // ─────────────────────────────────────────────────────────────────────
        // 2. The Java-8 way — one expression
        // ─────────────────────────────────────────────────────────────────────
        Comparator<Integer> asc  = Comparator.naturalOrder();
        Comparator<Integer> desc = Comparator.reverseOrder();
        Comparator<Integer> byAbs = Comparator.comparingInt(Math::abs);     // ← lambda-friendly

        // ─────────────────────────────────────────────────────────────────────
        // 3. Sorting OBJECTS — sort people by age, then by name
        // ─────────────────────────────────────────────────────────────────────
        record Person(String name, int age) {}
        List<Person> people = new ArrayList<>(List.of(
                new Person("Amy", 30),
                new Person("Bob", 25),
                new Person("Cal", 30),
                new Person("Dee", 25)));
        people.sort(Comparator.comparingInt(Person::age)
                              .thenComparing(Person::name));
        System.out.println("by age, then name = " + people);

        // ─────────────────────────────────────────────────────────────────────
        // 4. Reversing a multi-key comparator
        //    Sort intervals by start ASC, break ties by end DESC.
        // ─────────────────────────────────────────────────────────────────────
        int[][] intervals = {{1, 4}, {2, 3}, {1, 6}, {3, 5}};
        Arrays.sort(intervals,
                Comparator.<int[]>comparingInt(x -> x[0])
                          .thenComparing((a, b) -> Integer.compare(b[1], a[1])));
        System.out.println("intervals         = " + Arrays.deepToString(intervals));

        // ─────────────────────────────────────────────────────────────────────
        // 5. Sorting Map entries by value DESC, ties by key ASC (Top-K frequent)
        // ─────────────────────────────────────────────────────────────────────
        Map<String, Integer> freq = Map.of("a", 3, "b", 1, "c", 3, "d", 2);
        List<Map.Entry<String, Integer>> ranked = new ArrayList<>(freq.entrySet());
        ranked.sort(Map.Entry.<String, Integer>comparingByValue().reversed()
                .thenComparing(Map.Entry.comparingByKey()));
        System.out.println("ranked entries    = " + ranked);

        // ─────────────────────────────────────────────────────────────────────
        // 6. Null safety
        // ─────────────────────────────────────────────────────────────────────
        List<String> withNulls = new ArrayList<>(Arrays.asList("c", null, "a", "b", null));
        withNulls.sort(Comparator.nullsFirst(Comparator.naturalOrder()));
        System.out.println("nullsFirst        = " + withNulls);

        // ─────────────────────────────────────────────────────────────────────
        // 7. Custom string ordering — sort by length then lex
        // ─────────────────────────────────────────────────────────────────────
        List<String> words = new ArrayList<>(List.of("pear", "fig", "apple", "kiwi"));
        words.sort(Comparator.comparingInt(String::length)
                             .thenComparing(Comparator.naturalOrder()));
        System.out.println("by len then lex   = " + words);

        // ─────────────────────────────────────────────────────────────────────
        // 8. PATTERN — "Largest Number" (LC 179)
        //    Sort strings by which concatenation is larger.
        //    Custom Comparator without overflow risk.
        // ─────────────────────────────────────────────────────────────────────
        String[] parts = {"3", "30", "34", "5", "9"};
        Arrays.sort(parts, (a, b) -> (b + a).compareTo(a + b));
        System.out.println("largest concat    = " + String.join("", parts));   // 9534330

        // ─────────────────────────────────────────────────────────────────────
        // 9. Comparable — making YOUR type sortable natively
        //    record Score(int points) implements Comparable<Score> {
        //        public int compareTo(Score o) { return Integer.compare(points, o.points); }
        //    }
        //    Then:  Collections.sort(list)  — uses the natural order.
        // ─────────────────────────────────────────────────────────────────────

        // ─────────────────────────────────────────────────────────────────────
        // Common interview traps
        //   ✗ (a, b) -> a - b                — overflow for big a, b
        //   ✓ Integer.compare(a, b)
        //
        //   ✗ Comparator.comparing(o -> o.field)            — type inference can break
        //   ✓ Comparator.comparing(Foo::field)              — method reference is cleanest
        //
        //   ✗ PriorityQueue<int[]> pq = new PriorityQueue<>((a,b) -> a[0]-b[0]);  // overflow!
        //   ✓ new PriorityQueue<>(Comparator.comparingInt(x -> x[0]));
        // ─────────────────────────────────────────────────────────────────────

        // ─────────────────────────────────────────────────────────────────────
        // Practice problems:
        //   • LC 56    Merge Intervals
        //   • LC 179   Largest Number
        //   • LC 215   Kth Largest (with custom heap)
        //   • LC 252   Meeting Rooms (sort by start)
        //   • LC 451   Sort Characters By Frequency
        //   • LC 853   Car Fleet (sort by position)
        //   • LC 1366  Rank Teams by Votes
        // ─────────────────────────────────────────────────────────────────────
    }
}