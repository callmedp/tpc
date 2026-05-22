package multithreading.basics;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ExecutorServiceExample {

    public static void run() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // --- execute() use case ---
        // Use when: fire-and-forget, you don't need a result or to know if it succeeded
        // Real usecase: sending an audit log, triggering a notification
        System.out.println("\n[execute()] Fire-and-forget — logging audit events:");
        for (int i = 1; i <= 3; i++) {
            final int eventId = i;
            executor.execute(() ->
                System.out.println("  Audit log saved for event #" + eventId
                    + " | Thread: " + Thread.currentThread().getName())
            );
        }

        // --- submit() use case ---
        // Use when: you need the result of a task or want to handle its exception
        // Real usecase: calling an API, computing a value, processing a file
        System.out.println("\n[submit()] With result — processing orders:");
        Future<String> order1 = executor.submit(() -> {
            Thread.sleep(100); // simulate DB call
            return "Order #1001 processed";
        });

        Future<String> order2 = executor.submit(() -> {
            Thread.sleep(50);
            return "Order #1002 processed";
        });

        try {
            System.out.println("  " + order1.get()); // blocks until result is ready
            System.out.println("  " + order2.get());
        } catch (Exception e) {
            System.out.println("Task failed: " + e.getMessage());
        }

        executor.shutdown();
        System.out.println("\n[ExecutorService] All tasks done, pool shut down.");
    }
}