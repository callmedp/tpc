package collections.overview;

import java.util.*;

/**
 * Demonstrates Iterable, Iterator, ListIterator, fail-fast vs fail-safe behavior,
 * and a custom Iterable implementation.
 */
public class IterableIteratorExample {

    public static void main(String[] args) {

        // 1) Iterator — forward-only
        List<Integer> list = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        Iterator<Integer> it = list.iterator();
        while (it.hasNext()) {
            int v = it.next();
            if (v % 2 == 0) it.remove();   // remove via iterator is safe
        }
        System.out.println("After iterator.remove: " + list);  // [1, 3, 5]

        // 2) ListIterator — bidirectional, can set/add
        List<Integer> nums = new ArrayList<>(List.of(10, 20, 30));
        ListIterator<Integer> lit = nums.listIterator();
        while (lit.hasNext()) {
            int v = lit.next();
            if (v == 20) lit.set(200);     // replace
            if (v == 30) lit.add(999);     // insert after current position
        }
        System.out.println("After ListIterator: " + nums); // [10, 200, 30, 999]

        // walk backwards
        System.out.print("Reverse: ");
        while (lit.hasPrevious()) System.out.print(lit.previous() + " ");
        System.out.println();

        // 3) Fail-fast — throws ConcurrentModificationException
        try {
            List<Integer> ff = new ArrayList<>(List.of(1, 2, 3));
            for (int v : ff) ff.add(4);
        } catch (ConcurrentModificationException e) {
            System.out.println("Fail-fast caught CME (ArrayList during structural change)");
        }

        // 4) Custom Iterable — usable in for-each
        Range r = new Range(1, 4);
        System.out.print("Custom iterable: ");
        for (int v : r) System.out.print(v + " ");
        System.out.println();
    }

    /** Inclusive integer range — implements Iterable so it works in for-each. */
    static class Range implements Iterable<Integer> {
        private final int from, to;
        Range(int from, int to) { this.from = from; this.to = to; }
        @Override
        public Iterator<Integer> iterator() {
            return new Iterator<>() {
                int cur = from;
                @Override public boolean hasNext() { return cur <= to; }
                @Override public Integer next() {
                    if (!hasNext()) throw new NoSuchElementException();
                    return cur++;
                }
            };
        }
    }
}