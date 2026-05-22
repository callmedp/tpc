package collections.map;

import java.util.*;

/**
 * HashMap — array of buckets; chains turn into red-black trees at length 8 (Java 8+).
 * O(1) avg put/get/remove. Allows 1 null key and null values. NOT thread-safe.
 */
public class HashMapExample {

    public static void main(String[] args) {

        // ---------- Construction ----------
        HashMap<String, Integer> m = new HashMap<>();        // cap 16, load 0.75
        HashMap<String, Integer> sized = new HashMap<>(64, 0.9f);

        // ---------- Basic ops ----------
        m.put("a", 1);
        m.put("b", 2);
        m.put("c", 3);
        m.put(null, 999);                                   // null key allowed (1)
        m.put("d", null);                                   // null value allowed
        System.out.println(m);

        System.out.println("get a: " + m.get("a"));
        System.out.println("contains b: " + m.containsKey("b"));
        System.out.println("size: " + m.size());

        Integer old = m.put("a", 10);                       // returns previous
        System.out.println("replaced a, old=" + old);

        m.remove("c");
        System.out.println("after remove: " + m);

        // ---------- Java 8 default methods ----------

        // getOrDefault
        int v = m.getOrDefault("missing", -1);
        System.out.println("getOrDefault: " + v);

        // putIfAbsent
        m.putIfAbsent("e", 5);
        m.putIfAbsent("a", 999);                            // ignored, a exists
        System.out.println("after putIfAbsent: " + m);

        // computeIfAbsent — lazy init (perfect for multimap)
        Map<String, List<Integer>> mm = new HashMap<>();
        for (int i = 1; i <= 10; i++) {
            String bucket = (i % 2 == 0) ? "even" : "odd";
            mm.computeIfAbsent(bucket, k -> new ArrayList<>()).add(i);
        }
        System.out.println("multimap: " + mm);

        // merge — counter pattern
        Map<String, Integer> counts = new HashMap<>();
        for (String w : List.of("a", "b", "a", "c", "b", "a"))
            counts.merge(w, 1, Integer::sum);
        System.out.println("word counts: " + counts);       // {a=3, b=2, c=1}

        // compute — full control; return null to remove
        counts.compute("a", (k, val) -> val == null ? 1 : val + 100);
        counts.compute("c", (k, val) -> null);              // removes c
        System.out.println("after compute: " + counts);

        // replaceAll
        counts.replaceAll((k, val) -> val * 10);
        System.out.println("replaceAll *10: " + counts);

        // forEach
        counts.forEach((k, val) -> System.out.println("  " + k + "->" + val));

        // ---------- Iteration ----------
        for (Map.Entry<String, Integer> e : m.entrySet()) {
            // safe remove via iterator only
        }
        Iterator<Map.Entry<String, Integer>> it = m.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> e = it.next();
            if (e.getKey() != null && e.getKey().equals("b")) it.remove();
        }
        System.out.println("after iterator.remove: " + m);

        // ---------- ConcurrentModificationException ----------
        try {
            HashMap<String, Integer> cme = new HashMap<>();
            cme.put("x", 1); cme.put("y", 2);
            for (String k : cme.keySet()) cme.put("z", 3);
        } catch (ConcurrentModificationException e) {
            System.out.println("CME during iteration + structural modification");
        }

        // ---------- Mutability hazard ----------
        Map<MutableKey, String> bad = new HashMap<>();
        MutableKey k = new MutableKey(1);
        bad.put(k, "value");
        k.id = 999;                                          // mutated key!
        System.out.println("get after mutation: " + bad.get(k));   // null — entry lost
    }

    static class MutableKey {
        int id;
        MutableKey(int id) { this.id = id; }
        @Override public int hashCode() { return Integer.hashCode(id); }
        @Override public boolean equals(Object o) {
            return o instanceof MutableKey mk && mk.id == id;
        }
    }
}