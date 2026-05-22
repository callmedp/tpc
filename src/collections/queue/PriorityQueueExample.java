package collections.queue;

import java.util.*;

/**
 * PriorityQueue — min-heap by default. offer/poll O(log n), peek O(1).
 * Iteration is NOT in sorted order — poll until empty to get sorted output.
 */
public class PriorityQueueExample {

    public static void main(String[] args) {

        // ---------- Default min-heap ----------
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();
        minHeap.addAll(List.of(5, 1, 9, 3, 7, 2));
        System.out.println("internal order (heap, NOT sorted): " + minHeap);

        System.out.print("polling (sorted): ");
        while (!minHeap.isEmpty()) System.out.print(minHeap.poll() + " ");
        System.out.println();

        // ---------- Max-heap via Comparator ----------
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
        maxHeap.addAll(List.of(5, 1, 9, 3, 7));
        System.out.println("max-heap peek: " + maxHeap.peek());   // 9

        // ---------- Top-K largest pattern ----------
        int K = 3;
        PriorityQueue<Integer> topK = new PriorityQueue<>();      // min-heap of size K
        int[] data = {4, 1, 7, 3, 9, 2, 6, 8, 5};
        for (int x : data) {
            topK.offer(x);
            if (topK.size() > K) topK.poll();
        }
        System.out.println("top-" + K + ": " + topK);             // contains {7,8,9} in some heap order

        // ---------- Custom object with priority ----------
        PriorityQueue<Task> tasks = new PriorityQueue<>(Comparator.comparingInt(Task::priority));
        tasks.offer(new Task("low",  10));
        tasks.offer(new Task("crit",  1));
        tasks.offer(new Task("med",   5));
        System.out.println("next task: " + tasks.poll());          // crit

        // ---------- null rejected ----------
        try { new PriorityQueue<Integer>().offer(null); }
        catch (NullPointerException e) { System.out.println("PriorityQueue rejects null"); }

        // ---------- remove(Object) is O(n) ----------
        PriorityQueue<Integer> q = new PriorityQueue<>(List.of(5,3,8,1));
        q.remove(3);                        // scans heap for 3
        System.out.println("after remove 3: " + q);
    }

    static record Task(String name, int priority) {}
}