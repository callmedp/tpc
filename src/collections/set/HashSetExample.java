package collections.set;

import java.util.*;

/**
 * HashSet — backed by HashMap. O(1) avg add/contains/remove.
 * No order guarantee. Allows one null. NOT thread-safe.
 */
public class HashSetExample {

    public static void main(String[] args) {
        HashSet<String> s = new HashSet<>();

        // ---------- Add ----------
        System.out.println(s.add("apple"));     // true
        System.out.println(s.add("apple"));     // false — duplicate rejected
        s.add("banana"); s.add("cherry"); s.add(null);
        System.out.println("set: " + s);

        // ---------- Contains / Remove ----------
        System.out.println("contains apple: " + s.contains("apple"));
        s.remove("banana");
        System.out.println("after remove: " + s);

        // ---------- Set algebra ----------
        Set<Integer> a = new HashSet<>(List.of(1,2,3,4));
        Set<Integer> b = new HashSet<>(List.of(3,4,5,6));

        Set<Integer> union = new HashSet<>(a); union.addAll(b);
        Set<Integer> inter = new HashSet<>(a); inter.retainAll(b);
        Set<Integer> diff  = new HashSet<>(a); diff.removeAll(b);
        System.out.println("union=" + union + " inter=" + inter + " diff=" + diff);

        // ---------- Mutability hazard ----------
        Set<MutableKey> bad = new HashSet<>();
        MutableKey k = new MutableKey(1);
        bad.add(k);
        k.id = 999;                              // mutated AFTER insertion!
        System.out.println("contains k? " + bad.contains(k));  // false — object lost
        System.out.println("but it's still in: " + bad);

        // ---------- Equals vs hashCode contract demo ----------
        Set<Person> people = new HashSet<>();
        people.add(new Person("Alice", 30));
        people.add(new Person("Alice", 30));     // duplicate iff equals + hashCode are correct
        System.out.println("people size: " + people.size());   // 1
    }

    static class MutableKey {
        int id;
        MutableKey(int id) { this.id = id; }
        @Override public int hashCode() { return Integer.hashCode(id); }
        @Override public boolean equals(Object o) {
            return o instanceof MutableKey mk && mk.id == id;
        }
    }

    static record Person(String name, int age) {} // records auto-generate equals/hashCode
}