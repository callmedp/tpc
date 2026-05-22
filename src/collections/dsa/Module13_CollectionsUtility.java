package collections.dsa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 13 — COLLECTIONS UTILITY                                         │
 * │  Prereq:  Module 3                                                       │
 * │  Goal:    one-liners for sort / reverse / shuffle / frequency / search   │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 *   The `java.util.Collections` class is a grab-bag of static helpers that
 *   operate on existing collections.  These are not glamorous, but they
 *   come up CONSTANTLY in interviews.
 *
 * What you'll learn:
 *   • sort / reverse / shuffle              — in-place ordering
 *   • max / min / frequency                 — quick aggregates
 *   • binarySearch                          — on sorted Lists
 *   • swap / rotate / fill                  — in-place transforms
 *   • addAll / disjoint                     — bulk helpers
 *   • nCopies / emptyList / singletonList   — immutable constants
 *   • unmodifiableList / synchronizedList   — wrappers (defensive copies & legacy thread-safety)
 *   • reverseOrder                          — natural-order reversed Comparator
 */
public class Module13_CollectionsUtility {

    public static void main(String[] args) {

        List<Integer> list = new ArrayList<>(List.of(3, 1, 4, 1, 5, 9, 2, 6));

        // ─────────────────────────────────────────────────────────────────────
        // 1. Order
        // ─────────────────────────────────────────────────────────────────────
        Collections.sort(list);                                  // ascending  (uses natural order)
        Collections.sort(list, Collections.reverseOrder());      // descending
        Collections.reverse(list);                               // in-place reverse
        Collections.shuffle(list);                               // random   (useful for testing)

        // ─────────────────────────────────────────────────────────────────────
        // 2. Aggregates
        // ─────────────────────────────────────────────────────────────────────
        int max = Collections.max(list);
        int min = Collections.min(list);
        int freqOf1 = Collections.frequency(list, 1);            // count occurrences
        boolean disjoint = Collections.disjoint(list, List.of(100, 200));

        // ─────────────────────────────────────────────────────────────────────
        // 3. Binary search — list MUST be sorted in the same order you'll search
        // ─────────────────────────────────────────────────────────────────────
        Collections.sort(list);
        int hit  = Collections.binarySearch(list, 5);
        int miss = Collections.binarySearch(list, 7);             // negative → -insertionPoint-1

        // ─────────────────────────────────────────────────────────────────────
        // 4. In-place transforms
        // ─────────────────────────────────────────────────────────────────────
        Collections.swap(list, 0, list.size() - 1);              // swap first/last
        Collections.rotate(list, 2);                              // shift right by 2
        // Collections.fill(list, 0);                              // overwrite all with 0

        // ─────────────────────────────────────────────────────────────────────
        // 5. Bulk add — variadic, faster than a manual loop for known elements
        // ─────────────────────────────────────────────────────────────────────
        List<Integer> bag = new ArrayList<>();
        Collections.addAll(bag, 10, 20, 30, 40);

        // ─────────────────────────────────────────────────────────────────────
        // 6. Immutable / shared constants
        // ─────────────────────────────────────────────────────────────────────
        List<Integer> empty     = Collections.emptyList();        // immutable, singleton
        List<Integer> oneOnly   = Collections.singletonList(42);  // immutable, size 1
        List<Integer> threeZero = Collections.nCopies(3, 0);      // [0,0,0]  immutable
        // (Modern alternatives: List.of(), List.of(42), etc.)

        // ─────────────────────────────────────────────────────────────────────
        // 7. Wrappers — read-only view & legacy thread-safety
        // ─────────────────────────────────────────────────────────────────────
        List<Integer> readOnly   = Collections.unmodifiableList(list);   // any write throws UOE
        List<Integer> threadSafe = Collections.synchronizedList(new ArrayList<>());
        // For modern thread-safe lists prefer CopyOnWriteArrayList or ConcurrentHashMap.

        // ─────────────────────────────────────────────────────────────────────
        // 8. PATTERN — "Sort then walk":  Majority Element (LC 169)
        //     Sort the list; the middle element must be the majority (>n/2).
        // ─────────────────────────────────────────────────────────────────────
        List<Integer> votes = new ArrayList<>(List.of(2, 2, 1, 1, 1, 2, 2));
        Collections.sort(votes);
        int majority = votes.get(votes.size() / 2);
        System.out.println("majority element  = " + majority);

        // ─────────────────────────────────────────────────────────────────────
        // 9. Output
        // ─────────────────────────────────────────────────────────────────────
        System.out.printf("list=%s  max=%d  min=%d  freq(1)=%d  disjoint=%b%n",
                list, max, min, freqOf1, disjoint);
        System.out.printf("hit(5)=%d  miss(7)=%d  insertAt=%d%n",
                hit, miss, -miss - 1);
        System.out.println("bag       = " + bag);
        System.out.println("nCopies   = " + threeZero);
        System.out.println("readOnly  = " + readOnly);
        System.out.println("empty/one = " + empty + " / " + oneOnly);
        System.out.println("threadSafe size = " + threadSafe.size());

        // ─────────────────────────────────────────────────────────────────────
        // Practice problems:
        //   • LC 169   Majority Element                — sort + middle
        //   • LC 78    Subsets                         — Collections.swap variants
        //   • LC 189   Rotate Array                    — Collections.rotate trick
        //   • LC 75    Sort Colors                     — three-pointer (no Collections needed,
        //                                                but rotate/swap clarify the pattern)
        //   • LC 31    Next Permutation                — Collections.reverse on tail
        // ─────────────────────────────────────────────────────────────────────
    }
}