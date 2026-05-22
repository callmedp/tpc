package multithreading.basics;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SynchronizedExample {

    // ----------------------------------------------------------------
    // WITHOUT synchronized — race condition
    // Multiple threads read-modify-write counter at the same time
    // Result: unpredictable, less than 1000
    // ----------------------------------------------------------------
    static class UnsafeCounter {
        int count = 0;

        void increment() {
            count++; // NOT atomic: read -> add 1 -> write (3 steps, any thread can cut in)
        }
    }

    // ----------------------------------------------------------------
    // WITH synchronized — race condition prevented
    // Only one thread can execute increment() at a time
    // Result: always exactly 1000
    // ----------------------------------------------------------------
    static class SafeCounter {
        int count = 0;

        synchronized void increment() {
            count++; // only one thread executes this at a time
        }
    }

    public static void run() throws InterruptedException {

        // --- Unsafe: show race condition ---
        UnsafeCounter unsafe = new UnsafeCounter();
        ExecutorService executor1 = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 1000; i++) {
            executor1.execute(unsafe::increment);
        }
        executor1.shutdown();
        executor1.awaitTermination(5, TimeUnit.SECONDS);

        System.out.println("[Without synchronized] Expected: 1000 | Actual: " + unsafe.count);

        // --- Safe: synchronized fixes it ---
        SafeCounter safe = new SafeCounter();
        ExecutorService executor2 = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 1000; i++) {
            executor2.execute(safe::increment);
        }
        executor2.shutdown();
        executor2.awaitTermination(5, TimeUnit.SECONDS);

        System.out.println("[With synchronized]    Expected: 1000 | Actual: " + safe.count);
    }
}
