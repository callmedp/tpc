package multithreading.basics;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.Random;

/**
 * Real-world use case: E-commerce Revenue Calculator
 *
 * Scenario: A platform has 1 million order amounts and needs to:
 *   1. Compute total revenue in parallel           → RecursiveTask (returns a value)
 *   2. Apply a seasonal discount to all orders     → RecursiveAction (no return value)
 *
 * Fork/Join splits the array in half recursively until chunks are small enough
 * to process directly (threshold), then merges results back up the tree.
 */
public class ForkJoinExample {

    // Chunks smaller than this are summed sequentially — avoids overhead of tiny tasks
    private static final int THRESHOLD = 10_000;

    // -------------------------------------------------------------------------
    // RecursiveTask<Double> — divides array, sums each half, merges totals
    // -------------------------------------------------------------------------
    static class RevenueCalculator extends RecursiveTask<Double> {

        private final double[] orders;
        private final int start;
        private final int end;

        RevenueCalculator(double[] orders, int start, int end) {
            this.orders = orders;
            this.start  = start;
            this.end    = end;
        }

        @Override
        protected Double compute() {
            int length = end - start;

            // Base case: chunk is small enough — compute directly
            if (length <= THRESHOLD) {
                double sum = 0;
                for (int i = start; i < end; i++) sum += orders[i];
                return sum;
            }

            int mid = start + length / 2;

            RevenueCalculator leftTask  = new RevenueCalculator(orders, start, mid);
            RevenueCalculator rightTask = new RevenueCalculator(orders, mid,   end);

            rightTask.fork();              // schedule right half on another thread
            double leftResult  = leftTask.compute();   // process left half on THIS thread
            double rightResult = rightTask.join();      // wait for right half to finish

            return leftResult + rightResult;
        }
    }

    // -------------------------------------------------------------------------
    // RecursiveAction — divides array, applies discount to each half in-place
    // -------------------------------------------------------------------------
    static class DiscountApplier extends RecursiveAction {

        private final double[] orders;
        private final int start;
        private final int end;
        private final double discountFactor; // e.g. 0.90 for 10% off

        DiscountApplier(double[] orders, int start, int end, double discountFactor) {
            this.orders         = orders;
            this.start          = start;
            this.end            = end;
            this.discountFactor = discountFactor;
        }

        @Override
        protected void compute() {
            int length = end - start;

            if (length <= THRESHOLD) {
                for (int i = start; i < end; i++) orders[i] *= discountFactor;
                return;
            }

            int mid = start + length / 2;

            // invokeAll forks both subtasks and waits for both to finish
            invokeAll(
                new DiscountApplier(orders, start, mid, discountFactor),
                new DiscountApplier(orders, mid,   end, discountFactor)
            );
        }
    }

    // -------------------------------------------------------------------------
    // Main
    // -------------------------------------------------------------------------
    public static void main(String[] args) {
        int orderCount = 1_000_000;
        double[] orders = generateOrders(orderCount);

        ForkJoinPool pool = ForkJoinPool.commonPool();
        System.out.println("Parallelism (threads): " + pool.getParallelism());
        System.out.printf("Processing %,d orders...%n%n", orderCount);

        // Step 1 — compute total revenue
        long t0 = System.currentTimeMillis();
        double totalRevenue = pool.invoke(new RevenueCalculator(orders, 0, orderCount));
        System.out.printf("Total revenue        : $%,.2f  (%d ms)%n",
                totalRevenue, System.currentTimeMillis() - t0);

        // Step 2 — apply 10% seasonal discount to every order
        t0 = System.currentTimeMillis();
        pool.invoke(new DiscountApplier(orders, 0, orderCount, 0.90));
        System.out.printf("Discount applied in  : %d ms%n", System.currentTimeMillis() - t0);

        // Step 3 — recompute revenue after discount
        double discountedRevenue = pool.invoke(new RevenueCalculator(orders, 0, orderCount));
        System.out.printf("Revenue after 10%% off: $%,.2f%n", discountedRevenue);
        System.out.printf("Ratio (expect ~0.900): %.4f%n", discountedRevenue / totalRevenue);
    }

    private static double[] generateOrders(int count) {
        Random rng = new Random(42);
        double[] orders = new double[count];
        for (int i = 0; i < count; i++) {
            orders[i] = 5.0 + rng.nextDouble() * 495.0; // $5 – $500 per order
        }
        return orders;
    }
}
