package collections.map;

import java.util.*;

/**
 * LinkedHashMap — HashMap + doubly linked list across entries.
 * Insertion order by default; access order if constructed with accessOrder=true.
 * The classic Java way to implement an LRU cache.
 */
public class LinkedHashMapExample {

    public static void main(String[] args) {

        // ---------- Insertion order ----------
        LinkedHashMap<String, Integer> m = new LinkedHashMap<>();
        m.put("c", 3); m.put("a", 1); m.put("b", 2);
        System.out.println("insertion order: " + m);  // {c=3, a=1, b=2}

        // ---------- Access order ----------
        LinkedHashMap<String, Integer> ao = new LinkedHashMap<>(16, 0.75f, true);
        ao.put("a", 1); ao.put("b", 2); ao.put("c", 3);
        ao.get("a");                                  // moves 'a' to tail
        ao.get("b");
        System.out.println("access order: " + ao);   // {c=3, a=1, b=2}

        // ---------- LRU cache ----------
        LRUCache<String, String> cache = new LRUCache<>(3);
        cache.put("k1", "v1");
        cache.put("k2", "v2");
        cache.put("k3", "v3");
        cache.get("k1");                               // k1 becomes most-recent
        cache.put("k4", "v4");                         // evicts k2 (now eldest)
        System.out.println("LRU cache: " + cache);     // {k3=v3, k1=v1, k4=v4}
    }

    /** Bounded LRU cache backed by LinkedHashMap. */
    static class LRUCache<K, V> extends LinkedHashMap<K, V> {
        private final int capacity;
        LRUCache(int capacity) {
            super(capacity, 0.75f, true);              // access-order
            this.capacity = capacity;
        }
        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > capacity;
        }
    }
}