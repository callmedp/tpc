package collections.utilities;

import java.util.*;

/**
 * java.util.Collections — every important static helper demonstrated.
 */
public class CollectionsClassExample {

    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>(List.of(5, 1, 4, 2, 3));

        // ---------- Sort / search ----------
        Collections.sort(list);
        System.out.println("sorted: " + list);
        int idx = Collections.binarySearch(list, 4);
        System.out.println("binarySearch 4: " + idx);

        Collections.reverse(list);
        System.out.println("reversed: " + list);

        Collections.shuffle(list, new Random(42));
        System.out.println("shuffled: " + list);

        Collections.swap(list, 0, list.size() - 1);
        System.out.println("after swap: " + list);

        Collections.rotate(list, 2);
        System.out.println("rotated by 2: " + list);

        // ---------- Aggregates ----------
        List<Integer> agg = List.of(3, 1, 4, 1, 5, 9, 2, 6, 5);
        System.out.println("min: " + Collections.min(agg));
        System.out.println("max: " + Collections.max(agg));
        System.out.println("frequency of 5: " + Collections.frequency(agg, 5));
        System.out.println("disjoint? " + Collections.disjoint(agg, List.of(100, 200)));

        // ---------- Fill / copy / nCopies ----------
        List<Integer> dest = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0));
        Collections.copy(dest, List.of(1, 2, 3));
        System.out.println("copy: " + dest);

        List<String> filled = new ArrayList<>(Arrays.asList(null, null, null));
        Collections.fill(filled, "x");
        System.out.println("filled: " + filled);

        List<String> copies = Collections.nCopies(4, "hi");
        System.out.println("nCopies: " + copies);

        // ---------- Empty / singleton (immutable, cached) ----------
        List<String> empty = Collections.emptyList();
        Set<String> emptySet = Collections.emptySet();
        Map<String, Integer> emptyMap = Collections.emptyMap();
        List<String> oneItem = Collections.singletonList("only");
        Map<String, Integer> oneEntry = Collections.singletonMap("k", 1);
        System.out.println("singleton list: " + oneItem);

        // ---------- Unmodifiable wrappers (VIEW, not copy) ----------
        List<Integer> backing = new ArrayList<>(List.of(1, 2, 3));
        List<Integer> unmod = Collections.unmodifiableList(backing);
        backing.add(99);                                        // mutates underlying
        System.out.println("'unmodifiable' view sees: " + unmod);  // [1, 2, 3, 99]

        try { unmod.add(100); }
        catch (UnsupportedOperationException e) {
            System.out.println("unmodifiable rejects direct mutation");
        }

        // ---------- Synchronized wrappers ----------
        List<String> sync = Collections.synchronizedList(new ArrayList<>(List.of("a","b","c")));
        synchronized (sync) {                                   // manual sync for iteration
            for (String s : sync) { /* safe */ }
        }

        // ---------- Checked wrappers — runtime type-check on insertion ----------
        List rawList = new ArrayList();
        List<String> checked = Collections.checkedList(rawList, String.class);
        try { ((List) checked).add(42); }
        catch (ClassCastException e) {
            System.out.println("checkedList caught type violation at runtime");
        }

        // ---------- newSetFromMap, asLifoQueue ----------
        Set<String> setFromMap = Collections.newSetFromMap(new IdentityHashMap<>());
        setFromMap.add("identity-based set");

        Deque<Integer> deque = new ArrayDeque<>();
        Queue<Integer> lifo = Collections.asLifoQueue(deque);
        lifo.offer(1); lifo.offer(2); lifo.offer(3);
        System.out.println("LIFO poll: " + lifo.poll());        // 3
    }
}