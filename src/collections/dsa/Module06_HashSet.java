package collections.dsa;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 6 — HASHSET (dedup, O(1) membership, set algebra)                │
 * │  Prereq:  Module 5                                                       │
 * │  Goal:    set operations + the LeetCode patterns that need them          │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * If you only need to ask "have I seen this before?" use a HashSet, not a
 * HashMap with dummy values.
 *
 * What you'll learn:
 *   • add / remove / contains / size / isEmpty / clear
 *   • addAll / retainAll / removeAll / removeIf       — UNION / INTERSECTION / DIFFERENCE
 *   • Set.of / Set.copyOf
 *   • LinkedHashSet — preserves insertion order, dedup-with-order pattern
 *   • Pattern: contains-duplicate, intersection, longest consecutive, happy number
 */
public class Module06_HashSet {

    public static void main(String[] args) {

        // ─────────────────────────────────────────────────────────────────────
        // 1. Basics
        // ─────────────────────────────────────────────────────────────────────
        Set<Integer> set = new HashSet<>();
        set.add(1); set.add(2); set.add(3);
        set.add(1);                       // duplicate — silently ignored, returns false
        set.contains(2);
        set.remove(3);
        set.size();    set.isEmpty();

        // add() returns boolean — false if already present (LC 217 idiom)
        boolean wasNew = set.add(4);

        // ─────────────────────────────────────────────────────────────────────
        // 2. Set algebra — the "bulk" methods are the magic
        // ─────────────────────────────────────────────────────────────────────
        Set<Integer> a = new HashSet<>(Set.of(1, 2, 3, 4));
        Set<Integer> b = new HashSet<>(Set.of(3, 4, 5, 6));

        Set<Integer> union = new HashSet<>(a);  union.addAll(b);                  // {1..6}
        Set<Integer> inter = new HashSet<>(a);  inter.retainAll(b);               // {3,4}
        Set<Integer> diff  = new HashSet<>(a);  diff.removeAll(b);                // {1,2}
        Set<Integer> symm  = new HashSet<>(a);  symm.addAll(b); symm.removeAll(inter); // {1,2,5,6}

        // ─────────────────────────────────────────────────────────────────────
        // 3. Iteration  (HashSet has NO defined order — use LinkedHashSet if you need one)
        // ─────────────────────────────────────────────────────────────────────
        for (int x : set) { /* unordered */ }
        set.forEach(x -> { /* lambda */ });
        set.removeIf(x -> x > 100);

        // ─────────────────────────────────────────────────────────────────────
        // 4. LinkedHashSet — dedup while preserving FIRST-SEEN order
        // ─────────────────────────────────────────────────────────────────────
        Set<Integer> ordered = new LinkedHashSet<>();
        for (int x : new int[]{3, 1, 4, 1, 5, 9, 2, 6, 5, 3}) ordered.add(x);
        // ordered = [3, 1, 4, 5, 9, 2, 6]

        // ─────────────────────────────────────────────────────────────────────
        // 5. PATTERN — Contains Duplicate (LC 217)
        // ─────────────────────────────────────────────────────────────────────
        int[] nums = {1, 2, 3, 1};
        boolean hasDup = false;
        Set<Integer> seen = new HashSet<>();
        for (int n : nums) {
            if (!seen.add(n)) { hasDup = true; break; }   // .add() returns false on dup
        }

        // ─────────────────────────────────────────────────────────────────────
        // 6. PATTERN — Intersection of two arrays (LC 349)
        // ─────────────────────────────────────────────────────────────────────
        Set<Integer> sa = new HashSet<>(List.of(1, 2, 2, 3));
        Set<Integer> sb = new HashSet<>(List.of(2, 3, 4));
        sa.retainAll(sb);                                       // sa now = {2, 3}

        // ─────────────────────────────────────────────────────────────────────
        // 7. PATTERN — Longest Consecutive Sequence (LC 128)
        //    Trick: only start counting from a number with no predecessor in the set
        // ─────────────────────────────────────────────────────────────────────
        Set<Integer> all = new HashSet<>(List.of(100, 4, 200, 1, 3, 2));
        int longest = 0;
        for (int n : all) {
            if (!all.contains(n - 1)) {                   // n is the start of a streak
                int cur = n, len = 1;
                while (all.contains(cur + 1)) { cur++; len++; }
                longest = Math.max(longest, len);
            }
        }

        // ─────────────────────────────────────────────────────────────────────
        // 8. PATTERN — Happy Number (LC 202) — cycle detection via Set
        // ─────────────────────────────────────────────────────────────────────
        int n = 19;
        Set<Integer> visited = new HashSet<>();
        boolean happy = true;
        while (n != 1) {
            if (!visited.add(n)) { happy = false; break; }
            int sum = 0;
            while (n > 0) { sum += (n % 10) * (n % 10); n /= 10; }
            n = sum;
        }

        // ─────────────────────────────────────────────────────────────────────
        // 9. Output
        // ─────────────────────────────────────────────────────────────────────
        System.out.println("union     = " + union);
        System.out.println("inter     = " + inter);
        System.out.println("diff      = " + diff);
        System.out.println("symm      = " + symm);
        System.out.println("ordered   = " + ordered);
        System.out.println("hasDup    = " + hasDup);
        System.out.println("sa∩sb     = " + sa);
        System.out.println("longest   = " + longest);
        System.out.println("happy(19) = " + happy);
        System.out.println("addedNew? = " + wasNew);

        // ─────────────────────────────────────────────────────────────────────
        // Practice problems:
        //   • LC 217   Contains Duplicate
        //   • LC 349   Intersection of Two Arrays
        //   • LC 350   Intersection of Two Arrays II (multiset — use HashMap)
        //   • LC 128   Longest Consecutive Sequence
        //   • LC 202   Happy Number
        //   • LC 141   Linked List Cycle (alternative to Floyd's)
        // ─────────────────────────────────────────────────────────────────────
    }
}