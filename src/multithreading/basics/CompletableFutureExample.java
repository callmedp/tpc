package multithreading.basics;

import java.util.concurrent.*;

/**
 * Real-world scenario: E-commerce order checkout pipeline
 *
 * Methods covered:
 *   supplyAsync, runAsync, thenApply, thenAccept, thenRun,
 *   thenCompose, thenCombine, exceptionally, handle,
 *   whenComplete, complete, completeExceptionally, allOf, anyOf
 */
public class CompletableFutureExample {

    // ── Simulated service calls ──────────────────────────────────────

    static String fetchUser(int userId) throws Exception {
        Thread.sleep(200);
        if (userId <= 0) throw new Exception("User not found: " + userId);
        return "Alice";
    }

    static double fetchPrice(int productId) throws Exception {
        Thread.sleep(300);
        return productId * 9.99;
    }

    static int fetchStock(int productId) throws Exception {
        Thread.sleep(300);
        return 42;
    }

    static String fetchInventory(int productId) throws Exception {
        Thread.sleep(400);
        return "Warehouse-B";
    }

    // ────────────────────────────────────────────────────────────────

    public static void run() throws Exception {
        System.out.println("=== CompletableFuture — full method tour ===\n");

        // ── 1. supplyAsync ───────────────────────────────────────────
        // Runs a task on ForkJoinPool and returns CompletableFuture<T>.
        // Use when the task produces a value you need later.
        CompletableFuture<String> userFuture = CompletableFuture
                .supplyAsync(() -> {
                    try { return fetchUser(1); }
                    catch (Exception e) { throw new CompletionException(e); }
                });
        System.out.println("1. supplyAsync   → " + userFuture.get());

        // ── 2. runAsync ──────────────────────────────────────────────
        // Runs a task with no return value (Runnable).
        // Use for fire-and-forget side effects.
        CompletableFuture<Void> auditLog = CompletableFuture
                .runAsync(() -> System.out.println("2. runAsync      → audit log written"));
        auditLog.get(); // wait so output appears in order for this demo

        // ── 3. thenApply ─────────────────────────────────────────────
        // Transform the result with a Function<T, R>.
        // Like map() on a stream — produces a new CompletableFuture<R>.
        CompletableFuture<String> greeting = CompletableFuture
                .supplyAsync(() -> "Alice")
                .thenApply(name -> "Hello, " + name + "!");
        System.out.println("3. thenApply     → " + greeting.get());

        // ── 4. thenAccept ────────────────────────────────────────────
        // Consume the result with a Consumer<T>. Returns CompletableFuture<Void>.
        // Use when you need the value but produce no new value.
        CompletableFuture<Void> print = CompletableFuture
                .supplyAsync(() -> 249.99)
                .thenAccept(price -> System.out.printf("4. thenAccept    → Order total: $%.2f%n", price));
        print.get();

        // ── 5. thenRun ───────────────────────────────────────────────
        // Run a Runnable after completion — no access to the result.
        // Use for "after this step, do cleanup / log / notify" with no need for the value.
        CompletableFuture<Void> done = CompletableFuture
                .supplyAsync(() -> "order-1001")
                .thenRun(() -> System.out.println("5. thenRun       → pipeline complete, releasing resources"));
        done.get();

        // ── 6. thenCompose ───────────────────────────────────────────
        // Chain two async operations where the second depends on the first.
        // Like flatMap() — avoids CompletableFuture<CompletableFuture<T>>.
        // Use when step B itself returns a CompletableFuture.
        CompletableFuture<String> warehouse = CompletableFuture
                .supplyAsync(() -> 42)                              // fetch product ID
                .thenCompose(productId -> CompletableFuture         // use it to start next async call
                        .supplyAsync(() -> {
                            try { return fetchInventory(productId); }
                            catch (Exception e) { throw new CompletionException(e); }
                        }));
        System.out.println("6. thenCompose   → ships from: " + warehouse.get());

        // ── 7. thenCombine ───────────────────────────────────────────
        // Combine two INDEPENDENT futures when both complete.
        // Both run in parallel — you merge results with a BiFunction.
        CompletableFuture<Double> priceFuture = CompletableFuture
                .supplyAsync(() -> { try { return fetchPrice(42); } catch (Exception e) { throw new CompletionException(e); } });

        CompletableFuture<Integer> stockFuture = CompletableFuture
                .supplyAsync(() -> { try { return fetchStock(42); } catch (Exception e) { throw new CompletionException(e); } });

        CompletableFuture<String> orderSummary = priceFuture.thenCombine(stockFuture,
                (price, stock) -> String.format("price=$%.2f, stock=%d — %s",
                        price, stock, stock > 0 ? "CONFIRMED" : "OUT OF STOCK"));

        System.out.println("7. thenCombine   → " + orderSummary.get());

        // ── 8. exceptionally ─────────────────────────────────────────
        // Recover from an exception — provide a fallback value.
        // Only runs if the pipeline threw; skipped on success.
        CompletableFuture<String> recovered = CompletableFuture
                .<String>supplyAsync(() -> { throw new RuntimeException("payment gateway down"); })
                .exceptionally(ex -> "fallback: retry with saved card — " + ex.getMessage());
        System.out.println("8. exceptionally → " + recovered.get());

        // ── 9. handle ────────────────────────────────────────────────
        // Always runs — receives (result, exception), one of which is null.
        // Use when you want a single place to deal with both success and failure.
        CompletableFuture<String> handled = CompletableFuture
                .<String>supplyAsync(() -> { throw new RuntimeException("DB timeout"); })
                .handle((result, ex) -> ex != null
                        ? "handled error: " + ex.getMessage()
                        : "success: " + result);
        System.out.println("9. handle        → " + handled.get());

        // ── 10. whenComplete ─────────────────────────────────────────
        // Side-effect hook that runs on both success and failure.
        // Does NOT transform the result — the original value/exception passes through.
        // Use for logging, metrics, cleanup.
        CompletableFuture<Double> withAudit = CompletableFuture
                .supplyAsync(() -> 199.99)
                .whenComplete((result, ex) -> {
                    if (ex == null) System.out.printf("10. whenComplete  → audit: charged $%.2f%n", result);
                    else            System.out.println("10. whenComplete  → audit: charge failed — " + ex.getMessage());
                });
        withAudit.get();

        // ── 11. complete ─────────────────────────────────────────────
        // Manually push a value into a CompletableFuture.
        // Use for cache hits, mocks, or bridging callback-based APIs.
        CompletableFuture<String> manual = new CompletableFuture<>();
        // Simulate a cache hit — no need to call the real service
        manual.complete("Alice (from cache)");
        System.out.println("11. complete     → " + manual.get());

        // ── 12. completeExceptionally ────────────────────────────────
        // Manually fail a CompletableFuture.
        // Use to signal that a condition was not met (e.g., user not authorized).
        CompletableFuture<String> failed = new CompletableFuture<>();
        failed.completeExceptionally(new RuntimeException("user banned"));
        String result12 = failed
                .exceptionally(ex -> "blocked: " + ex.getMessage())
                .get();
        System.out.println("12. completeExceptionally → " + result12);

        // ── 13. allOf ────────────────────────────────────────────────
        // Wait for ALL futures to complete (parallel fan-out).
        // Returns CompletableFuture<Void> — join each individually for values.
        CompletableFuture<Void> email = CompletableFuture.runAsync(() -> {
            try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            System.out.println("13. allOf        → email sent");
        });
        CompletableFuture<Void> sms = CompletableFuture.runAsync(() -> {
            try { Thread.sleep(150); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            System.out.println("13. allOf        → SMS sent");
        });
        CompletableFuture<Void> push = CompletableFuture.runAsync(() -> {
            try { Thread.sleep(80); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            System.out.println("13. allOf        → push notification sent");
        });
        CompletableFuture.allOf(email, sms, push).get(); // waits for all three
        System.out.println("13. allOf        → all notifications dispatched");

        // ── 14. anyOf ────────────────────────────────────────────────
        // Complete as soon as ANY future finishes (pick the fastest).
        // Use for redundant calls to multiple servers — take whichever replies first.
        CompletableFuture<Object> warehouseA = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return "Warehouse-A";
        });
        CompletableFuture<Object> warehouseB = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return "Warehouse-B"; // wins — fastest
        });
        CompletableFuture<Object> warehouseC = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(300); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return "Warehouse-C";
        });
        Object fastest = CompletableFuture.anyOf(warehouseA, warehouseB, warehouseC).get();
        System.out.println("14. anyOf        → fulfilling from: " + fastest);

        System.out.println("\n=== All done ===");
    }
}