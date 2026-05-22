package collections.queue;

import java.util.concurrent.*;

/**
 * BlockingQueue family — used to coordinate producers and consumers.
 * Demonstrates the four operation modes (throws / special / blocks / times out)
 * and the major implementations.
 */
public class BlockingQueueExample {

    public static void main(String[] args) throws Exception {

        // ---------- 1) ArrayBlockingQueue — bounded ----------
        ArrayBlockingQueue<Integer> abq = new ArrayBlockingQueue<>(3);
        abq.put(1); abq.put(2); abq.put(3);
        System.out.println("offer when full (non-blocking): " + abq.offer(4));    // false
        System.out.println("offer with timeout: " + abq.offer(4, 50, TimeUnit.MILLISECONDS)); // false after 50ms
        try { abq.add(4); } catch (IllegalStateException e) { System.out.println("add throws on full"); }
        System.out.println("take: " + abq.take());     // blocking remove

        // ---------- 2) LinkedBlockingQueue — optionally bounded ----------
        LinkedBlockingQueue<String> lbq = new LinkedBlockingQueue<>(100);
        lbq.put("hello");
        System.out.println("LBQ poll: " + lbq.poll());

        // ---------- 3) PriorityBlockingQueue — unbounded + priority ----------
        PriorityBlockingQueue<Integer> pbq = new PriorityBlockingQueue<>();
        pbq.put(5); pbq.put(1); pbq.put(3);
        System.out.println("PBQ take order: " + pbq.take() + ", " + pbq.take() + ", " + pbq.take());

        // ---------- 4) DelayQueue ----------
        DelayQueue<DelayedTask> dq = new DelayQueue<>();
        long now = System.currentTimeMillis();
        dq.put(new DelayedTask("A", now + 100));
        dq.put(new DelayedTask("B", now + 300));
        System.out.println("delay take 1: " + dq.take().name);   // blocks ~100ms
        System.out.println("delay take 2: " + dq.take().name);   // blocks ~200ms more

        // ---------- 5) SynchronousQueue — zero capacity, hand-off ----------
        SynchronousQueue<String> sq = new SynchronousQueue<>();
        ExecutorService pool = Executors.newCachedThreadPool();
        pool.submit(() -> { try { sq.put("hand-off"); } catch (InterruptedException e) {} });
        System.out.println("SQ take: " + sq.take());

        // ---------- 6) Classic Producer / Consumer ----------
        BlockingQueue<Integer> pipeline = new ArrayBlockingQueue<>(5);
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) { pipeline.put(i); System.out.println("produced " + i); }
                pipeline.put(-1);                // poison pill
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        Thread consumer = new Thread(() -> {
            try {
                while (true) {
                    int v = pipeline.take();
                    if (v == -1) break;
                    System.out.println("consumed " + v);
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        producer.start(); consumer.start();
        producer.join(); consumer.join();

        pool.shutdownNow();
    }

    /** A task that becomes "ready" only after its scheduled time. */
    static class DelayedTask implements Delayed {
        final String name;
        final long readyAt;
        DelayedTask(String name, long readyAt) { this.name = name; this.readyAt = readyAt; }
        @Override public long getDelay(TimeUnit unit) {
            return unit.convert(readyAt - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }
        @Override public int compareTo(Delayed o) {
            return Long.compare(this.readyAt, ((DelayedTask) o).readyAt);
        }
    }
}