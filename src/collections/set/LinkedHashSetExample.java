package collections.set;

import java.util.*;

/**
 * LinkedHashSet — maintains insertion order; backed by LinkedHashMap.
 * Use to deduplicate while preserving order.
 */
public class LinkedHashSetExample {

    public static void main(String[] args) {
        LinkedHashSet<String> s = new LinkedHashSet<>();
        s.add("c"); s.add("a"); s.add("b"); s.add("a");  // 'a' second add ignored
        System.out.println("insertion-ordered: " + s);    // [c, a, b]

        // Deduplicate a List while preserving order
        List<Integer> input = List.of(3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5);
        List<Integer> deduped = new ArrayList<>(new LinkedHashSet<>(input));
        System.out.println("deduped (ordered): " + deduped);   // [3, 1, 4, 5, 9, 2, 6]

        // Same Set semantics as HashSet, just with ordering
        System.out.println("contains a: " + s.contains("a"));
        s.remove("c");
        System.out.println("after remove c: " + s);
    }
}