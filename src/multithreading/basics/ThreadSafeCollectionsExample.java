package multithreading.basics;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe collections — three real-world patterns:
 *
 *  PART 1 — ConcurrentHashMap    : high-concurrency order-status tracker
 *  PART 2 — CopyOnWriteArrayList : read-heavy event-listener registry
 *  PART 3 — LinkedBlockingQueue  : producer-consumer order pipeline
 *
 * Each part also shows the equivalent synchronized collection and its limitation.
 */
public class ThreadSafeCollectionsExample {

    // ==========================================================================
    // PART 1 — Synchronized Map  vs  ConcurrentHashMap
    //
    // Scenario: Multiple services (payment, warehouse, delivery) concurrently
    // update order statuses in a shared map.
    //
    // Problem with synchronizedMap:
    //   Each method call acquires ONE lock on the whole map.
    //   Compound operations (check-then-act) are NOT atomic — need external lock.
    //
    // ConcurrentHashMap solution:
    //   Fine-grained bucket-level locking + CAS for writes.
    //   Atomic compound ops built in: putIfAbsent, computeIfAbsent, merge, etc.
    // ==========================================================================
    static void part1_OrderStatusTracker() throws InterruptedException {
        System.out.println("=== PART 1: Synchronized Map vs ConcurrentHashMap ===\n");

        int totalOrders = 1_000;
        String[] statuses = {"PLACED", "CONFIRMED", "SHIPPED", "DELIVERED"};

        // --- synchronizedMap: each method call locks the whole map ---
        Map<String, String> syncMap = Collections.synchronizedMap(new HashMap<>());

        // --- ConcurrentHashMap: fine-grained locks, built-in atomic ops ---
        Map<String, String> concMap = new ConcurrentHashMap<>();

        // Separate counter map — merge() needs the value type to match the delta
        Map<String, Integer> countMap = new ConcurrentHashMap<>();

        ExecutorService pool = Executors.newFixedThreadPool(4);
        CountDownLatch latch = new CountDownLatch(4);

        for (int t = 0; t < 4; t++) {
            final int tid = t;
            pool.submit(() -> {
                for (int i = tid; i < totalOrders; i += 4) {
                    String key = "ORD-" + i;

                    // synchronizedMap — compound "set if absent" needs an EXTERNAL lock
                    // because containsKey() and put() are two separate synchronized calls
                    synchronized (syncMap) {
                        if (!syncMap.containsKey(key)) {
                            syncMap.put(key, statuses[tid]);
                        }
                    }

                    // ConcurrentHashMap — one atomic call, no external lock needed
                    concMap.putIfAbsent(key, statuses[tid]);

                    // computeIfAbsent — atomically initialise a complex value
                    concMap.computeIfAbsent(
                            "FIRST-" + tid,
                            k -> "Thread-" + tid + " first order: " + key);

                    // merge — atomically update or insert (here: count orders per thread)
                    countMap.merge("COUNT-" + tid, 1,
                            Integer::sum);
                }
                latch.countDown();
            });
        }

        latch.await();
        pool.shutdown();

        System.out.printf("syncMap entries : %d%n", syncMap.size());
        System.out.printf("concMap entries : %d%n", concMap.size());
        for (int t = 0; t < 4; t++) {
            System.out.printf("  Thread-%d handled %d orders | first: %s%n",
                    t, countMap.get("COUNT-" + t), concMap.get("FIRST-" + t));
        }
    }

    // ==========================================================================
    // PART 2 — Synchronized List  vs  CopyOnWriteArrayList
    //
    // Scenario: An event system where many threads fire events (reads) while
    // rarely a thread registers or removes a listener (writes).
    //
    // Problem with synchronizedList:
    //   Iteration is NOT covered by the per-method lock.
    //   Must manually wrap iteration in synchronized(list){...} — coarse, slow.
    //   ConcurrentModificationException if another thread modifies during iteration.
    //
    // CopyOnWriteArrayList solution:
    //   Every write (add/remove) copies the backing array atomically.
    //   Reads iterate over a stable snapshot — completely lock-free.
    //   Best when reads vastly outnumber writes (listener lists, config, etc.)
    // ==========================================================================
    static void part2_EventListenerRegistry() throws InterruptedException {
        System.out.println("\n=== PART 2: Synchronized List vs CopyOnWriteArrayList ===\n");

        // synchronizedList — iteration requires an EXTERNAL lock or risks CME
        List<String> syncList = Collections.synchronizedList(new ArrayList<>());
        syncList.add("EmailNotifier");
        syncList.add("SmsNotifier");

        // CopyOnWriteArrayList — reads iterate a snapshot; zero CME risk
        List<String> cowList = new CopyOnWriteArrayList<>();
        cowList.add("EmailNotifier");
        cowList.add("SmsNotifier");
        cowList.add("PushNotifier");

        ExecutorService pool = Executors.newFixedThreadPool(6);
        AtomicInteger eventCount = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(6);

        // 5 reader threads — fire events by iterating the listener list
        for (int i = 0; i < 5; i++) {
            pool.submit(() -> {
                for (int e = 0; e < 300; e++) {
                    // SAFE: iterates over a snapshot taken when the loop began —
                    // concurrent adds/removes are invisible mid-iteration but never crash
                    for (String listener : cowList) {
                        eventCount.incrementAndGet(); // simulate notifying the listener
                    }
                }
                latch.countDown();
            });
        }

        // 1 writer thread — occasionally registers / deregisters listeners
        pool.submit(() -> {
            try {
                Thread.sleep(5);
                cowList.add("AuditLogger");       // triggers array copy — existing readers unaffected
                syncList.add("AuditLogger");      // synchronizedList: ok for this one call

                Thread.sleep(5);
                cowList.remove("SmsNotifier");    // copy again
                syncList.remove("SmsNotifier");

                Thread.sleep(5);
                cowList.add("AnalyticsTracker");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            latch.countDown();
        });

        latch.await();
        pool.shutdown();

        System.out.println("Final COW listeners  : " + cowList);
        System.out.println("Total events notified: " + eventCount.get());
        System.out.println("Zero ConcurrentModificationException");

        // Show the synchronizedList iteration pitfall
        System.out.println("\nsynchronizedList safe iteration — requires external lock:");
        synchronized (syncList) {            // <-- MUST hold this lock for the whole loop
            for (String l : syncList) {
                System.out.println("  listener: " + l);
            }
        }
        System.out.println("(Forgetting the outer synchronized block can throw CME)");
    }

    // ==========================================================================
    // PART 3 — LinkedBlockingQueue: Producer-Consumer Order Pipeline
    //
    // Scenario: An order intake service (producer) places incoming orders into a
    // queue; a fulfillment service (consumer) picks them up one by one.
    //
    // BlockingQueue mechanics:
    //   put()  — inserts; BLOCKS if queue is at capacity  → natural backpressure
    //   take() — removes; BLOCKS if queue is empty        → consumer waits cleanly
    //   poll(timeout) — removes or returns null after timeout → graceful shutdown
    // ==========================================================================
    static void part3_OrderPipeline() throws InterruptedException {
        System.out.println("\n=== PART 3: LinkedBlockingQueue — Order Pipeline ===\n");

        // Capacity = 5 — producer is throttled if consumer falls behind
        BlockingQueue<String> queue = new LinkedBlockingQueue<>(5);
        AtomicInteger processed = new AtomicInteger();
        int totalOrders = 10;

        // Producer: order intake (faster than consumer — illustrates backpressure)
        Thread intake = new Thread(() -> {
            try {
                for (int i = 1; i <= totalOrders; i++) {
                    String order = String.format("ORD-%03d", i);
                    queue.put(order); // BLOCKS here if queue is full
                    System.out.printf("  [Intake]      Queued    %-8s  queue=%d/5%n",
                            order, queue.size());
                    Thread.sleep(30);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "OrderIntake");

        // Consumer: fulfillment (slower — simulates packing/dispatch work)
        Thread fulfillment = new Thread(() -> {
            try {
                while (processed.get() < totalOrders) {
                    // poll with timeout so thread exits cleanly when producer is done
                    String order = queue.poll(400, TimeUnit.MILLISECONDS);
                    if (order != null) {
                        Thread.sleep(80); // simulate processing
                        System.out.printf("  [Fulfillment] Processed %-8s  done=%d/%d%n",
                                order, processed.incrementAndGet(), totalOrders);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Fulfillment");

        intake.start();
        fulfillment.start();
        intake.join();
        fulfillment.join();

        System.out.printf("%nPipeline complete — all %d orders processed.%n", processed.get());
    }

    // ==========================================================================
    // Main
    // ==========================================================================
    public static void main(String[] args) throws InterruptedException {
        part1_OrderStatusTracker();
        part2_EventListenerRegistry();
        part3_OrderPipeline();

        System.out.println("""

                ╔══════════════════════════════════╦══════════════════════╦══════════════════════════════╗
                ║ Collection                       ║ Locking strategy     ║ Best for                     ║
                ╠══════════════════════════════════╬══════════════════════╬══════════════════════════════╣
                ║ Collections.synchronizedMap/List ║ Whole-object lock    ║ Low concurrency, legacy wrap ║
                ║ ConcurrentHashMap                ║ Bucket-level / CAS   ║ High-concurrency K-V store   ║
                ║ CopyOnWriteArrayList/Set          ║ Copy on write        ║ Read-heavy, rare writes      ║
                ║ LinkedBlockingQueue              ║ Lock per end (head/  ║ Producer-consumer pipelines  ║
                ║                                  ║ tail separate locks) ║                              ║
                ╚══════════════════════════════════╩══════════════════════╩══════════════════════════════╝
                """);
    }
}
