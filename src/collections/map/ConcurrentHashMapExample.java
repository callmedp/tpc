package collections.map;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * ConcurrentHashMap — thread-safe HashMap replacement.
 * Lock-free reads, fine-grained writes (per-bin synchronized + CAS).
 * Disallows null keys and null values.
 */
public class ConcurrentHashMapExample {

    public static void main(String[] args) throws InterruptedException {

        ConcurrentHashMap<String, Integer> cm = new ConcurrentHashMap<>();
        cm.put("a", 1); cm.put("b", 2);

        // ---------- Atomic compound ops ----------
        cm.putIfAbsent("c", 3);
        cm.computeIfAbsent("d", k -> 4);
        cm.merge("a", 10, Integer::sum);              // atomic increment-by-N
        System.out.println("after atomic ops: " + cm);

        // ---------- null rejected ----------
        try { cm.put(null, 1); }
        catch (NullPointerException e) { System.out.println("CHM rejects null key"); }
        try { cm.put("x", null); }
        catch (NullPointerException e) { System.out.println("CHM rejects null value"); }

        // ---------- Concurrent counter — classic merge pattern ----------
        ConcurrentHashMap<String, Integer> counts = new ConcurrentHashMap<>();
        ExecutorService pool = Executors.newFixedThreadPool(4);
        String[] words = {"a", "b", "a", "c", "b", "a", "d", "a"};
        for (String w : words) {
            pool.submit(() -> counts.merge(w, 1, Integer::sum));
        }
        pool.shutdown(); pool.awaitTermination(2, TimeUnit.SECONDS);
        System.out.println("concurrent counts: " + counts);

        // ---------- LongAdder values for hot keys ----------
        ConcurrentHashMap<String, LongAdder> hot = new ConcurrentHashMap<>();
        Runnable hit = () -> hot.computeIfAbsent("hot", k -> new LongAdder()).increment();
        ExecutorService p2 = Executors.newFixedThreadPool(8);
        for (int i = 0; i < 10_000; i++) p2.submit(hit);
        p2.shutdown(); p2.awaitTermination(2, TimeUnit.SECONDS);
        System.out.println("hot key count: " + hot.get("hot").sum());

        // ---------- Weakly-consistent iteration (never throws CME) ----------
        ConcurrentHashMap<Integer, Integer> live = new ConcurrentHashMap<>();
        for (int i = 0; i < 1000; i++) live.put(i, i);
        ExecutorService p3 = Executors.newFixedThreadPool(2);
        p3.submit(() -> { for (int i = 1000; i < 2000; i++) live.put(i, i); });
        p3.submit(() -> { int sum = 0; for (Integer v : live.values()) sum += v; System.out.println("iter sum (approx): " + sum); });
        p3.shutdown(); p3.awaitTermination(2, TimeUnit.SECONDS);

        // ---------- mappingCount vs size ----------
        System.out.println("size:         " + live.size());
        System.out.println("mappingCount: " + live.mappingCount());   // long, preferred for huge maps

        // ---------- Bulk parallel ops (Java 8+) ----------
        long sum = live.reduceValuesToLong(1000, Integer::longValue, 0L, Long::sum);
        Integer max = live.reduceValues(1000, Integer::max);
        live.forEach(1000, (k, v) -> { /* parallel */ });
        System.out.println("bulk sum=" + sum + " max=" + max);

        // ---------- Concurrent Set view ----------
        Set<String> setView = ConcurrentHashMap.newKeySet();
        setView.add("x"); setView.add("y");
        System.out.println("CHM-backed set: " + setView);
    }
}