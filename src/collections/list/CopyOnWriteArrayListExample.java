package collections.list;

import java.util.*;
import java.util.concurrent.*;

/**
 * CopyOnWriteArrayList — thread-safe; mutations copy the backing array.
 * Iterators are fail-safe snapshots; do NOT support remove/set/add.
 * Best for read-mostly use (e.g., listener lists).
 */
public class CopyOnWriteArrayListExample {

    public static void main(String[] args) throws InterruptedException {
        CopyOnWriteArrayList<String> cow = new CopyOnWriteArrayList<>();
        cow.add("a"); cow.add("b"); cow.add("c");

        // Snapshot iterator — never throws CME, ignores subsequent writes
        Iterator<String> it = cow.iterator();
        cow.add("d");                       // mutation happens AFTER iterator created
        System.out.print("iterator sees (snapshot): ");
        while (it.hasNext()) System.out.print(it.next() + " ");
        System.out.println();
        System.out.println("list now: " + cow);   // includes d

        // Iterator.remove() unsupported
        try {
            Iterator<String> it2 = cow.iterator();
            it2.next();
            it2.remove();
        } catch (UnsupportedOperationException e) {
            System.out.println("COW iterator.remove() unsupported");
        }

        // addIfAbsent — atomic
        boolean added = cow.addIfAbsent("a");
        System.out.println("addIfAbsent 'a': " + added);

        // Demonstrate thread-safety: writers + readers concurrently
        ExecutorService pool = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 4; i++) {
            final int id = i;
            pool.submit(() -> {
                for (int j = 0; j < 100; j++) cow.add("t" + id + "-" + j);
            });
        }
        // Reader running concurrently — never throws CME
        pool.submit(() -> {
            for (String s : cow) { /* read snapshot */ }
        });
        pool.shutdown();
        pool.awaitTermination(2, TimeUnit.SECONDS);
        System.out.println("final size: " + cow.size());
    }
}