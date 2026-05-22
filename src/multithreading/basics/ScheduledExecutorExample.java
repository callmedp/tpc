package multithreading.basics;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ScheduledExecutorService — all methods with real-world use cases.
 *
 * Methods covered:
 *   schedule(Runnable),  schedule(Callable)
 *   scheduleAtFixedRate, scheduleAtFixedDelay
 *
 * Key difference:
 *   scheduleAtFixedRate  — period measured from START of previous run
 *   scheduleAtFixedDelay — delay measured from END   of previous run
 */
public class ScheduledExecutorExample {

    static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    static String now() { return LocalTime.now().format(FMT); }

    public static void run() throws Exception {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

        // ── 1. schedule(Runnable, delay, unit) ──────────────────────
        // Run ONCE after a fixed delay. No repeat.
        //
        // Real use: send a follow-up email 5 seconds after user signs up.
        //           (pretend seconds = hours for the demo)
        System.out.println("=== 1. schedule(Runnable) — one-shot delay ===");
        System.out.println("[" + now() + "] User signed up");

        scheduler.schedule(
            () -> System.out.println("[" + now() + "] Follow-up email sent (1s after signup)"),
            1, TimeUnit.SECONDS
        );
        Thread.sleep(1500); // wait for it to fire before next demo

        // ── 2. schedule(Callable, delay, unit) ──────────────────────
        // Run ONCE after a delay, returning a Future<T> with the result.
        //
        // Real use: generate a password-reset token, valid for N seconds.
        System.out.println("\n=== 2. schedule(Callable) — one-shot with result ===");

        ScheduledFuture<String> tokenFuture = scheduler.schedule(
            () -> "RESET-TOKEN-" + System.currentTimeMillis(),
            1, TimeUnit.SECONDS
        );
        String token = tokenFuture.get(); // blocks until the scheduled time fires
        System.out.println("[" + now() + "] Token generated: " + token);

        // ── 3. scheduleAtFixedRate ───────────────────────────────────
        // Fires every `period` measured from the START of the previous execution.
        // If the task takes LONGER than the period, the next run fires immediately
        // after the current one finishes (no overlap, but no gap either).
        //
        // Real use: heartbeat ping every 2s — you want consistent wall-clock timing.
        //
        // Timeline (period = 1s, task ~200ms):
        //   [0.0s] START → run
        //   [1.0s] START → run   (1s from previous START)
        //   [2.0s] START → run
        System.out.println("\n=== 3. scheduleAtFixedRate — heartbeat every 1s ===");
        AtomicInteger heartbeatCount = new AtomicInteger(0);

        ScheduledFuture<?> heartbeat = scheduler.scheduleAtFixedRate(
            () -> System.out.println("[" + now() + "] Heartbeat #" + heartbeatCount.incrementAndGet() + " → server alive"),
            0,              // start immediately
            1,              // then every 1 second from START of previous run
            TimeUnit.SECONDS
        );
        Thread.sleep(3500);
        heartbeat.cancel(false); // stop the repeating task
        System.out.println("[" + now() + "] Heartbeat stopped");

        // ── 4. scheduleAtFixedDelay ──────────────────────────────────
        // Fires `delay` after the END of the previous execution.
        // Always guarantees a gap between runs, regardless of task duration.
        //
        // Real use: poll an external API for a job result — wait for it to
        // finish, then pause before checking again (breathing room).
        //
        // Timeline (delay = 1s, task ~500ms):
        //   [0.0s] START → runs 500ms → END [0.5s]
        //   [1.5s] START → runs 500ms → END [2.0s]   (1s after END)
        //   [3.0s] START → ...
        System.out.println("\n=== 4. scheduleAtFixedDelay — API polling every 1s after completion ===");
        AtomicInteger pollCount = new AtomicInteger(0);

        ScheduledFuture<?> poller = scheduler.scheduleWithFixedDelay(
            () -> {
                int attempt = pollCount.incrementAndGet();
                System.out.println("[" + now() + "] Polling job status... attempt #" + attempt);
                try { Thread.sleep(400); } // simulate the API call taking 400ms
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                System.out.println("[" + now() + "] Poll #" + attempt + " done — job still running");
            },
            0,              // start immediately
            1,              // then 1 second AFTER previous run ENDS
            TimeUnit.SECONDS
        );
        Thread.sleep(4000);
        poller.cancel(false);
        System.out.println("[" + now() + "] Polling stopped\n");

        // ── 5. ScheduledFuture.getDelay() + cancel() ─────────────────
        // ScheduledFuture lets you inspect time remaining and cancel before it fires.
        //
        // Real use: cancel a session-expiry warning if the user acts before it fires.
        System.out.println("=== 5. ScheduledFuture — inspect delay and cancel ===");

        ScheduledFuture<?> expiryWarning = scheduler.schedule(
            () -> System.out.println("Session expiring soon!"),
            5, TimeUnit.SECONDS
        );
        long remaining = expiryWarning.getDelay(TimeUnit.MILLISECONDS);
        System.out.println("[" + now() + "] Session warning fires in " + remaining + "ms");

        // User clicked something — cancel the warning
        boolean cancelled = expiryWarning.cancel(false);
        System.out.println("[" + now() + "] User active — warning cancelled: " + cancelled);

        scheduler.shutdown();
        scheduler.awaitTermination(2, TimeUnit.SECONDS);
        System.out.println("\n=== All done ===");
    }
}
