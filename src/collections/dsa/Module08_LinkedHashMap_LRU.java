package collections.dsa;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 8 — LINKEDHASHMAP & THE LRU CACHE                                │
 * │  Prereq:  Module 5                                                       │
 * │  Goal:    insertion-order vs access-order; the 10-line LRU implementation │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * LinkedHashMap is a HashMap + a doubly-linked list of entries. The link
 * order is one of:
 *    • insertion-order  (default)  — iteration = order keys were first added
 *    • access-order               — get/put MOVES the touched entry to the tail
 *
 * Combine access-order with `removeEldestEntry` and you get an O(1) LRU
 * cache (LeetCode 146) with practically zero code.
 *
 * What you'll learn:
 *   • Constructor (initialCapacity, loadFactor, accessOrder)
 *   • removeEldestEntry override
 *   • Difference vs HashMap & TreeMap
 *   • Idiomatic LRU pattern
 */
public class Module08_LinkedHashMap_LRU {

    public static void main(String[] args) {

        // ─────────────────────────────────────────────────────────────────────
        // 1. Default (insertion order) — useful when you want deterministic iteration
        // ─────────────────────────────────────────────────────────────────────
        Map<String, Integer> insertion = new LinkedHashMap<>();
        insertion.put("c", 3); insertion.put("a", 1); insertion.put("b", 2);
        System.out.println("insertion order  = " + insertion);   // {c=3, a=1, b=2}

        // ─────────────────────────────────────────────────────────────────────
        // 2. Access-order — touched entries move to the tail
        //     constructor:  (initialCapacity, loadFactor, accessOrder=true)
        // ─────────────────────────────────────────────────────────────────────
        LinkedHashMap<String, Integer> access = new LinkedHashMap<>(16, 0.75f, true);
        access.put("a", 1); access.put("b", 2); access.put("c", 3);
        access.get("a");                                            // touches 'a'
        System.out.println("access order     = " + access);          // {b=2, c=3, a=1}

        // ─────────────────────────────────────────────────────────────────────
        // 3. LRU Cache (LC 146) — 10 lines, zero external libraries
        // ─────────────────────────────────────────────────────────────────────
        LRU<Integer, Integer> lru = new LRU<>(3);
        lru.put(1, 10);
        lru.put(2, 20);
        lru.put(3, 30);
        lru.get(1);              // 1 is now most-recently-used
        lru.put(4, 40);          // capacity exceeded → evicts LRU = 2
        System.out.println("LRU state        = " + lru);             // {3=30, 1=10, 4=40}

        // ─────────────────────────────────────────────────────────────────────
        // 4. Comparison cheat
        //     HashMap         — no order, fastest
        //     LinkedHashMap   — predictable iteration order (insertion or access)
        //     TreeMap         — keys sorted, log-n, range queries
        // ─────────────────────────────────────────────────────────────────────

        // ─────────────────────────────────────────────────────────────────────
        // Practice problems:
        //   • LC 146   LRU Cache                — exactly this pattern
        //   • LC 460   LFU Cache                — combine with a frequency bucket map
        //   • LC 1429  First Unique Number (stream) — LinkedHashMap to keep order
        // ─────────────────────────────────────────────────────────────────────
    }

    /** Tiny LRU cache. Capacity-bound, O(1) get and put. */
    static class LRU<K, V> extends LinkedHashMap<K, V> {
        private final int capacity;
        LRU(int capacity) {
            super(capacity, 0.75f, true);     // access-order = true
            this.capacity = capacity;
        }
        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > capacity;          // evict eldest when over the limit
        }
    }
}