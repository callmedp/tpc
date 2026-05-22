package multithreading.basics;

import java.util.concurrent.*;

public class CallableVsRunnableExample {

    // ================================================================
    // Simulated services (stand-ins for real DB / email calls)
    // ================================================================

    static double fetchLivePrice(int productId) throws Exception {
        Thread.sleep(500); // simulate network call
        if (productId == 0) throw new Exception("Invalid product ID");
        return productId * 9.99;
    }

    static void sendConfirmationEmail(String email) {
        try { Thread.sleep(300); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        System.out.println("[Email sent to] " + email);
    }

    // ================================================================
    // RUNNABLE — fire and forget
    //
    // Use case: send order confirmation email.
    // The main flow does NOT need to wait for this or know if it succeeded.
    // Returning a result makes no sense here.
    // ================================================================
    static Runnable sendEmailTask(String customerEmail) {
        return () -> sendConfirmationEmail(customerEmail);
    }

    // ================================================================
    // CALLABLE — returns a result, can throw a checked exception
    //
    // Use case: fetch live product price before creating the order.
    // The main flow MUST have this value to proceed.
    // The external call can fail — Callable lets us propagate that as
    // an ExecutionException via future.get().
    // ================================================================
    static Callable<Double> fetchPriceTask(int productId) {
        return () -> fetchLivePrice(productId);
    }

    // ================================================================
    // DEMO
    // ================================================================
    public static void run() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(3);

        System.out.println("=== Order checkout flow ===\n");

        // Step 1: Fetch live price — we NEED the result to build the order
        // Callable<Double> + Future<Double>
        System.out.println("Fetching live price for product #42...");
        Future<Double> priceFuture = executor.submit(fetchPriceTask(42));

        // Step 2: Kick off email in parallel — fire and forget
        // Runnable + execute() — no Future needed
        executor.execute(sendEmailTask("customer@example.com"));

        // Step 3: Block only where we actually need the result
        double price = priceFuture.get(); // waits here; email runs concurrently
        System.out.printf("Price received: $%.2f%n", price);
        System.out.println("Order created for $" + price);

        // --- Show Callable error propagation ---
        System.out.println("\n=== Callable error propagation ===\n");

        Future<Double> badFuture = executor.submit(fetchPriceTask(0)); // will throw
        try {
            badFuture.get();
        } catch (ExecutionException e) {
            // The checked exception from call() is wrapped here
            System.out.println("Could not fetch price: " + e.getCause().getMessage());
            System.out.println("Order aborted.");
        }

        // Runnable swallows exceptions silently — you'd never know it failed
        // executor.execute(() -> { throw new RuntimeException("silent failure"); });
        // ^ This kills the thread quietly — no way to catch it from outside

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }
}