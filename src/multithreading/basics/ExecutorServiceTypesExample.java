package multithreading.basics;

import java.util.concurrent.*;

public class ExecutorServiceTypesExample {

    public static void run() throws InterruptedException, ExecutionException {

        // ----------------------------------------------------------------
        // 1. newFixedThreadPool(n)
        // Use case: you want to limit how many threads run at once
        // Real use: processing 100 orders but only 4 threads allowed (DB connection limit)
        // ----------------------------------------------------------------
        System.out.println("\n--- 1. FixedThreadPool (max 3 threads) ---");
        ExecutorService fixed = Executors.newFixedThreadPool(3);
        for (int i = 1; i <= 6; i++) {
            final int orderId = i;
            fixed.execute(() ->
                System.out.println("Processing order #" + orderId
                    + " | Thread: " + Thread.currentThread().getName())
            );
        }
        fixed.shutdown();// non-blocking — returns immediately
        fixed.awaitTermination(5, TimeUnit.SECONDS);

        // ----------------------------------------------------------------
        // 2. newSingleThreadExecutor()
        // Use case: tasks must run one at a time, in order
        // Real use: writing to a file — concurrent writes would corrupt it
        // ----------------------------------------------------------------
        System.out.println("\n--- 2. SingleThreadExecutor (sequential file writes) ---");
        ExecutorService single = Executors.newSingleThreadExecutor();
        single.execute(() -> System.out.println("Writing line 1 to file | Thread: " + Thread.currentThread().getName()));
        single.execute(() -> System.out.println("Writing line 2 to file | Thread: " + Thread.currentThread().getName()));
        single.execute(() -> System.out.println("Writing line 3 to file | Thread: " + Thread.currentThread().getName()));
        single.shutdown();
        single.awaitTermination(5, TimeUnit.SECONDS);

        // ----------------------------------------------------------------
        // 3. newCachedThreadPool()
        // Use case: many short-lived tasks, thread count grows/shrinks dynamically
        // Real use: handling incoming HTTP requests (burst traffic)
        // Caution: no upper limit — can spin up thousands of threads under heavy load
        // ----------------------------------------------------------------
        System.out.println("\n--- 3. CachedThreadPool (burst HTTP requests) ---");
        ExecutorService cached = Executors.newCachedThreadPool();
        for (int i = 1; i <= 5; i++) {
            final int reqId = i;
            cached.execute(() ->
                System.out.println("Handling request #" + reqId
                    + " | Thread: " + Thread.currentThread().getName())
            );
        }
        cached.shutdown();
        cached.awaitTermination(5, TimeUnit.SECONDS);

        // ----------------------------------------------------------------
        // 4. newScheduledThreadPool(n)
        // Use case: run tasks after a delay, or on a fixed interval
        // Real use: sending a reminder email 10 minutes after signup,
        //           polling an API every 30 seconds
        // ----------------------------------------------------------------
        System.out.println("\n--- 4. ScheduledThreadPool ---");
        ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(2);

        // run once after 1 second delay
        scheduled.schedule(
            () -> System.out.println("Reminder email sent | Thread: " + Thread.currentThread().getName()),
            1, TimeUnit.SECONDS
        );

        // run repeatedly — first after 500ms, then every 1 second
        ScheduledFuture<?> poller = scheduled.scheduleAtFixedRate(
            () -> System.out.println("Polling API... | Thread: " + Thread.currentThread().getName()),
            500, 1000, TimeUnit.MILLISECONDS
        );

        Thread.sleep(3500);      // let it run for 3.5 seconds
        poller.cancel(false);    // stop the repeating task
        scheduled.shutdown();
        scheduled.awaitTermination(5, TimeUnit.SECONDS);

        // ----------------------------------------------------------------
        // 5. newVirtualThreadPerTaskExecutor()  (Java 21+)
        // Use case: massive concurrency with I/O-heavy tasks (DB, API calls)
        // Real use: handling 10,000 simultaneous API calls without 10,000 OS threads
        // Each task gets its own lightweight virtual thread — no pool needed
        // ----------------------------------------------------------------
//        System.out.println("\n--- 5. VirtualThreadPerTaskExecutor (Java 21+) ---");
//        try (ExecutorService virtual = Executors.newVirtualThreadPerTaskExecutor()) {
//            for (int i = 1; i <= 5; i++) {
//                final int taskId = i;
//                virtual.submit(() ->
//                    System.out.println("Virtual thread task #" + taskId
//                        + " | Thread: " + Thread.currentThread().getName()
//                        + " | Virtual: " + Thread.currentThread().isVirtual())
//                );
//            }
//        }
    }
}