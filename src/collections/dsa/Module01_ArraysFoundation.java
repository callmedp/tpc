package collections.dsa;

import java.util.Arrays;
import java.util.List;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 1 — ARRAYS FOUNDATION                                            │
 * │  Prereq:  none                                                           │
 * │  Goal:    master java.util.Arrays — sort, copy, fill, binarySearch, etc. │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * Arrays are the most common DSA input (int[], char[], int[][]).
 * The `Arrays` utility class is your toolbox — every method here shows up
 * in real interviews. Learn this first; everything that follows builds on it.
 *
 * What you'll learn:
 *   • sort, parallelSort (with Comparator for objects)
 *   • copyOf, copyOfRange — for snapshots and resizing
 *   • fill, setAll                  — initialisation patterns
 *   • binarySearch                  — and interpreting the "insertion point"
 *   • equals, deepEquals            — array equality (1-D vs N-D)
 *   • hashCode, deepHashCode        — using arrays as keys (don't!)
 *   • toString, deepToString        — debug printing
 *   • asList, stream                — bridging to higher APIs
 */
public class Module01_ArraysFoundation {

    public static void main(String[] args) {

        // ─────────────────────────────────────────────────────────────────────
        // 1. Sorting
        // ─────────────────────────────────────────────────────────────────────
        int[] primitive = {5, 2, 8, 1, 9, 3};
        Arrays.sort(primitive);                              // ascending, O(n log n)
        Arrays.sort(primitive, 1, 4);                        // sort sub-range [1, 4)
        Arrays.parallelSort(primitive);                      // multi-threaded for huge arrays

        // Descending on int[]: NO direct API. Box, or sort + manual reverse.
        Integer[] boxed = {5, 2, 8, 1, 9, 3};
        Arrays.sort(boxed, (a, b) -> b - a);                 // descending
        // (For values near Integer.MAX_VALUE prefer Integer.compare(b, a) to avoid overflow)

        // ─────────────────────────────────────────────────────────────────────
        // 2. Copying / resizing
        // ─────────────────────────────────────────────────────────────────────
        int[] full      = Arrays.copyOf(primitive, primitive.length);
        int[] grown     = Arrays.copyOf(primitive, primitive.length + 3);  // pads with 0
        int[] window    = Arrays.copyOfRange(primitive, 1, 4);             // [from, to)

        // ─────────────────────────────────────────────────────────────────────
        // 3. Initialising (memoisation, DP)
        // ─────────────────────────────────────────────────────────────────────
        int[] memo = new int[10];
        Arrays.fill(memo, -1);                               // 1-D memo init

        int[][] grid = new int[3][3];
        for (int[] row : grid) Arrays.fill(row, -1);         // 2-D memo init

        // setAll — populate with a function of the index (Java 8)
        int[] squares = new int[5];
        Arrays.setAll(squares, i -> i * i);                  // [0, 1, 4, 9, 16]

        // ─────────────────────────────────────────────────────────────────────
        // 4. Binary search (sorted array required)
        // ─────────────────────────────────────────────────────────────────────
        int[] sorted = {1, 3, 5, 7, 9};
        int hit  = Arrays.binarySearch(sorted, 5);           // 2
        int miss = Arrays.binarySearch(sorted, 4);           // -3   ( -(insertPoint) - 1 )
        int insertPoint = -miss - 1;                          // 2 — where 4 would go

        // ─────────────────────────────────────────────────────────────────────
        // 5. Equality & hashing
        // ─────────────────────────────────────────────────────────────────────
        int[][] a = {{1, 2}, {3, 4}};
        int[][] b = {{1, 2}, {3, 4}};

        Arrays.equals(sorted, new int[]{1, 3, 5, 7, 9});      // true  — element-wise
        Arrays.equals(a, b);                                  // false — only top-level refs compared
        Arrays.deepEquals(a, b);                              // true  — recurses
        Arrays.hashCode(sorted);                              // hash of contents (not identity)
        Arrays.deepHashCode(a);                               // hash of nested contents

        // ─────────────────────────────────────────────────────────────────────
        // 6. Debug printing
        // ─────────────────────────────────────────────────────────────────────
        System.out.println("sorted     = " + Arrays.toString(sorted));        // [1, 3, 5, 7, 9]
        System.out.println("grid       = " + Arrays.deepToString(grid));      // nested
        System.out.println("squares    = " + Arrays.toString(squares));

        // ─────────────────────────────────────────────────────────────────────
        // 7. Bridging to higher APIs
        // ─────────────────────────────────────────────────────────────────────
        List<Integer> view = Arrays.asList(1, 2, 3);          // FIXED-size view (no add/remove)
        int sum = Arrays.stream(sorted).sum();                // IntStream — sum/max/min/avg/count
        int max = Arrays.stream(sorted).max().getAsInt();

        System.out.printf("hit=%d  miss=%d  insertPoint=%d  sum=%d  max=%d%n",
                hit, miss, insertPoint, sum, max);
        System.out.println("window     = " + Arrays.toString(window));
        System.out.println("grown len  = " + grown.length);
        System.out.println("view       = " + view);

        // ─────────────────────────────────────────────────────────────────────
        // 8. Common DSA idiom: sort by a derived key on int[][]
        // ─────────────────────────────────────────────────────────────────────
        int[][] intervals = {{1, 4}, {2, 3}, {1, 6}, {3, 5}};
        Arrays.sort(intervals, (x, y) -> x[0] - y[0]);        // sort by start
        System.out.println("intervals  = " + Arrays.deepToString(intervals));

        // ─────────────────────────────────────────────────────────────────────
        // Practice problems that use ONLY Arrays:
        //   • LC 88   Merge Sorted Array
        //   • LC 169  Majority Element (sort + middle)
        //   • LC 215  Kth Largest (sort, also covered later via heap)
        //   • LC 268  Missing Number (sum/xor)
        //   • LC 287  Find Duplicate (cycle detection — uses array indices)
        // ─────────────────────────────────────────────────────────────────────
    }
}