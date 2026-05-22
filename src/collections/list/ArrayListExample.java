package collections.list;

import java.util.*;

/**
 * ArrayList — every important method demonstrated.
 * Backing: Object[]. Default capacity 10 (lazy). Growth: 1.5x.
 * get O(1), add at end O(1) amortized, insert/remove O(n).
 */
public class ArrayListExample {

    public static void main(String[] args) {
        // ---------- Construction ----------
        ArrayList<String> a = new ArrayList<>();              // capacity 0, lazy
        ArrayList<String> b = new ArrayList<>(50);            // initial capacity
        ArrayList<String> c = new ArrayList<>(List.of("x","y"));   // from collection

        // ---------- Add ----------
        a.add("one");                       // append
        a.add("two");
        a.add(1, "between");                // insert at index, O(n) shift
        a.addAll(List.of("p", "q"));        // append all
        a.addAll(0, List.of("first"));      // insert all at index
        System.out.println("After adds: " + a);

        // ---------- Get / Set ----------
        String first = a.get(0);
        String old = a.set(0, "FIRST");
        System.out.println("set replaced: " + old);

        // ---------- Search ----------
        System.out.println("contains two: " + a.contains("two"));
        System.out.println("indexOf two: " + a.indexOf("two"));
        System.out.println("lastIndexOf two: " + a.lastIndexOf("two"));

        // ---------- Remove ----------
        a.remove("p");                      // by value, removes first match
        a.remove(0);                        // by index
        a.removeIf(s -> s.startsWith("q")); // Java 8 predicate remove
        // a.removeAll(other), a.retainAll(other), a.clear()

        // ---------- Iteration patterns ----------
        for (String s : a) System.out.print(s + " ");
        System.out.println();

        Iterator<String> it = a.iterator();
        while (it.hasNext()) {
            String s = it.next();
            if ("between".equals(s)) it.remove();  // safe remove during iteration
        }

        a.forEach(s -> System.out.print(s + " "));
        System.out.println();

        // ---------- subList — LIVE view ----------
        ArrayList<Integer> nums = new ArrayList<>(List.of(1,2,3,4,5,6,7,8,9));
        List<Integer> view = nums.subList(2, 7);    // [3,4,5,6,7]
        view.set(0, 99);                            // mutates nums too
        System.out.println("nums after subList.set: " + nums);
        view.clear();                               // deletes [3..7] from nums
        System.out.println("nums after subList.clear: " + nums);

        // ---------- Sort ----------
        ArrayList<Integer> n = new ArrayList<>(List.of(5,2,8,1,3));
        n.sort(null);                               // natural order
        n.sort(Comparator.reverseOrder());          // custom
        System.out.println("sorted desc: " + n);

        // ---------- toArray ----------
        Object[] o1 = n.toArray();
        Integer[] o2 = n.toArray(new Integer[0]);   // typed
        Integer[] o3 = n.toArray(Integer[]::new);   // Java 11

        // ---------- Capacity management ----------
        ArrayList<Integer> big = new ArrayList<>();
        big.ensureCapacity(10_000);                 // preallocate to avoid growth
        for (int i = 0; i < 10_000; i++) big.add(i);
        big.trimToSize();                           // shrink backing array to size

        // ---------- Demonstrate ConcurrentModificationException ----------
        try {
            ArrayList<Integer> cme = new ArrayList<>(List.of(1,2,3));
            for (Integer v : cme) cme.add(99);
        } catch (ConcurrentModificationException e) {
            System.out.println("CME during for-each + structural mod");
        }

        // ---------- Immutability variants ----------
        List<Integer> immutable = List.of(1, 2, 3);                  // Java 9
        List<Integer> copy = List.copyOf(n);                          // Java 10
        List<Integer> unmod = Collections.unmodifiableList(n);

        System.out.println("immutable=" + immutable + ", copy=" + copy);
    }
}