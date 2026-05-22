package collections.utilities;

import java.util.*;
import java.util.stream.*;

/**
 * java.util.Arrays — every important static helper demonstrated.
 */
public class ArraysClassExample {

    public static void main(String[] args) {

        // ---------- Sort (in-place) ----------
        int[] primitives = {5, 2, 8, 1, 3};
        Arrays.sort(primitives);                          // dual-pivot quicksort
        System.out.println("sorted primitives: " + Arrays.toString(primitives));

        String[] objects = {"banana", "apple", "cherry"};
        Arrays.sort(objects);                              // TimSort (stable)
        Arrays.sort(objects, Comparator.reverseOrder());
        System.out.println("sorted objects desc: " + Arrays.toString(objects));

        // Range sort
        int[] r = {9, 8, 7, 6, 5};
        Arrays.sort(r, 1, 4);                              // sort indices [1..4)
        System.out.println("range-sorted: " + Arrays.toString(r));

        // Parallel sort — Fork/Join
        int[] big = new Random(1).ints(100_000, 0, 1_000_000).toArray();
        Arrays.parallelSort(big);

        // ---------- Search ----------
        int[] s = {1, 3, 5, 7, 9, 11};
        System.out.println("indexOf 7 (binarySearch): " + Arrays.binarySearch(s, 7));
        System.out.println("indexOf 8 (insertion -ve): " + Arrays.binarySearch(s, 8));

        // ---------- Fill ----------
        int[] f = new int[5];
        Arrays.fill(f, 7);
        System.out.println("fill: " + Arrays.toString(f));
        Arrays.fill(f, 1, 4, 0);
        System.out.println("range fill: " + Arrays.toString(f));

        // ---------- copyOf / copyOfRange ----------
        int[] src = {1, 2, 3, 4, 5};
        int[] longer = Arrays.copyOf(src, 8);             // padded with 0
        int[] slice  = Arrays.copyOfRange(src, 1, 4);     // [2,3,4]
        System.out.println("copyOf: " + Arrays.toString(longer));
        System.out.println("slice:  " + Arrays.toString(slice));

        // ---------- equals vs deepEquals ----------
        int[][] m1 = {{1,2}, {3,4}};
        int[][] m2 = {{1,2}, {3,4}};
        System.out.println("Arrays.equals nested: " + Arrays.equals(m1, m2));        // false (shallow)
        System.out.println("Arrays.deepEquals:    " + Arrays.deepEquals(m1, m2));    // true

        // ---------- toString / deepToString ----------
        System.out.println("toString:     " + Arrays.toString(m1));        // [[I@... ...]
        System.out.println("deepToString: " + Arrays.deepToString(m1));    // [[1, 2], [3, 4]]

        // ---------- hashCode ----------
        System.out.println("hashCode:     " + Arrays.hashCode(src));
        System.out.println("deepHashCode: " + Arrays.deepHashCode(m1));

        // ---------- setAll (functional fill, Java 8) ----------
        int[] squares = new int[5];
        Arrays.setAll(squares, i -> i * i);
        System.out.println("squares: " + Arrays.toString(squares));

        // ---------- parallelPrefix — cumulative reduction ----------
        int[] cumSum = {1, 2, 3, 4, 5};
        Arrays.parallelPrefix(cumSum, Integer::sum);
        System.out.println("cumulative sum: " + Arrays.toString(cumSum));  // [1, 3, 6, 10, 15]

        // ---------- Array <-> List <-> Stream conversions ----------
        // int[] → List<Integer>
        int[] ints = {1, 2, 3};
        List<Integer> li = Arrays.stream(ints).boxed().collect(Collectors.toList());

        // List<Integer> → int[]
        int[] back = li.stream().mapToInt(Integer::intValue).toArray();

        // Integer[] → List<Integer> (fixed-size view)
        Integer[] boxed = {10, 20, 30};
        List<Integer> view = Arrays.asList(boxed);
        // view.add(40); // UnsupportedOperationException
        view.set(0, 99);                                  // OK — writes through to array
        System.out.println("backing array after view.set: " + Arrays.toString(boxed));

        // Trap: Arrays.asList(int[]) gives List<int[]> of size 1
        List<int[]> trap = Arrays.asList(new int[]{1,2,3});
        System.out.println("trap size: " + trap.size());          // 1
    }
}