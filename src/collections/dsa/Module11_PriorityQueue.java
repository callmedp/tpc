package collections.dsa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 11 — PRIORITYQUEUE (heap)                                        │
 * │  Prereq:  Modules 5, 10                                                  │
 * │  Goal:    Top-K, K-smallest, Dijkstra, merge K sorted, two-heap median   │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 *   java.util.PriorityQueue is a binary HEAP.
 *     • offer / add        — O(log n)
 *     • poll / remove()    — O(log n) (removes the head)
 *     • peek               — O(1)
 *     • remove(Object)     — O(n)     (avoid)
 *     • contains(Object)   — O(n)
 *
 *   Default: MIN-heap (smallest at head).
 *   Max-heap: pass `Comparator.reverseOrder()` or a custom Comparator.
 *
 * What you'll learn:
 *   PATTERN 1 — Top-K largest    (LC 215 Kth Largest)        — min-heap of size K
 *   PATTERN 2 — Top-K frequent   (LC 347)                    — heap of entries
 *   PATTERN 3 — Merge K sorted lists/streams (LC 23)
 *   PATTERN 4 — Dijkstra shortest path
 *   PATTERN 5 — Find Median from Data Stream (LC 295) — two heaps
 *   PATTERN 6 — Task scheduler / meeting rooms (LC 253, 621)
 */
public class Module11_PriorityQueue {

    public static void main(String[] args) {

        // ─────────────────────────────────────────────────────────────────────
        // 1. Basics
        // ─────────────────────────────────────────────────────────────────────
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();
        minHeap.offer(3); minHeap.offer(1); minHeap.offer(2);
        minHeap.peek();          // 1
        minHeap.poll();          // 1
        minHeap.size();   minHeap.isEmpty();

        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
        maxHeap.offer(3); maxHeap.offer(1); maxHeap.offer(2);
        maxHeap.peek();          // 3

        // ─────────────────────────────────────────────────────────────────────
        // 2. PATTERN 1 — Kth Largest Element (LC 215)
        //    Idea: keep a MIN-heap of size K. Whenever it grows past K, evict.
        //    The head is the k-th largest after one pass.
        // ─────────────────────────────────────────────────────────────────────
        int[] nums = {3, 2, 1, 5, 6, 4};
        int k = 2;
        PriorityQueue<Integer> topK = new PriorityQueue<>();
        for (int n : nums) {
            topK.offer(n);
            if (topK.size() > k) topK.poll();
        }
        System.out.println("kth largest (k=2) = " + topK.peek());           // 5

        // ─────────────────────────────────────────────────────────────────────
        // 3. PATTERN 2 — Top-K Frequent Elements (LC 347)
        //    Combine HashMap (frequency) + heap of entries by frequency.
        // ─────────────────────────────────────────────────────────────────────
        Map<Integer, Integer> freq = new HashMap<>();
        for (int n : new int[]{1, 1, 1, 2, 2, 3}) freq.merge(n, 1, Integer::sum);
        PriorityQueue<Map.Entry<Integer, Integer>> kHeap =
                new PriorityQueue<>(Map.Entry.comparingByValue());          // min-heap by count
        for (var e : freq.entrySet()) {
            kHeap.offer(e);
            if (kHeap.size() > 2) kHeap.poll();
        }
        List<Integer> mostFreq = new ArrayList<>();
        while (!kHeap.isEmpty()) mostFreq.add(0, kHeap.poll().getKey());    // reverse-insert
        System.out.println("top-2 frequent     = " + mostFreq);

        // ─────────────────────────────────────────────────────────────────────
        // 4. PATTERN 3 — Merge K Sorted Lists (LC 23)
        //    Heap holds one element from each list. Pop the smallest, push the next.
        //    Here, lists are int[].
        // ─────────────────────────────────────────────────────────────────────
        int[][] lists = {
                {1, 4, 7},
                {2, 5, 8},
                {3, 6, 9}};
        PriorityQueue<int[]> merge =
                new PriorityQueue<>(Comparator.comparingInt(x -> x[0]));    // [value, listIdx, posInList]
        for (int i = 0; i < lists.length; i++) {
            if (lists[i].length > 0) merge.offer(new int[]{lists[i][0], i, 0});
        }
        List<Integer> merged = new ArrayList<>();
        while (!merge.isEmpty()) {
            int[] top = merge.poll();
            merged.add(top[0]);
            int li = top[1], pos = top[2] + 1;
            if (pos < lists[li].length) merge.offer(new int[]{lists[li][pos], li, pos});
        }
        System.out.println("merged k-sorted    = " + merged);

        // ─────────────────────────────────────────────────────────────────────
        // 5. PATTERN 4 — Dijkstra's shortest path (sketch)
        //    PQ of (distance, node), pop smallest dist, relax neighbours.
        // ─────────────────────────────────────────────────────────────────────
        PriorityQueue<int[]> dij = new PriorityQueue<>(Comparator.comparingInt(x -> x[1]));
        dij.offer(new int[]{0, 0});                  // {node, dist=0}
        int[] dist = new int[5];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[0] = 0;
        // Map<Integer, List<int[]>> graph: node -> list of {neighbour, weight}
        // while (!dij.isEmpty()) { ... }            // omitted — pattern shown

        // ─────────────────────────────────────────────────────────────────────
        // 6. PATTERN 5 — Find Median from Data Stream (LC 295)
        //    Two heaps: maxHeap for lower half, minHeap for upper half.
        //    Invariants:  |lo| ∈ {|hi|, |hi|+1}      and    lo.peek() ≤ hi.peek()
        // ─────────────────────────────────────────────────────────────────────
        MedianFinder mf = new MedianFinder();
        for (int x : new int[]{1, 2, 3, 4, 5}) mf.add(x);
        System.out.println("median             = " + mf.median());          // 3.0
        mf.add(6);
        System.out.println("median after 6     = " + mf.median());          // 3.5

        // ─────────────────────────────────────────────────────────────────────
        // 7. PATTERN 6 — Meeting Rooms II (LC 253)
        //    Sort intervals by start.  Min-heap of END times.
        //    For each interval: if heap.peek() ≤ start, reuse (poll); push end.
        //    Heap size at end = number of rooms needed.
        // ─────────────────────────────────────────────────────────────────────
        int[][] meetings = {{0, 30}, {5, 10}, {15, 20}};
        Arrays.sort(meetings, Comparator.comparingInt(x -> x[0]));
        PriorityQueue<Integer> rooms = new PriorityQueue<>();
        for (int[] m : meetings) {
            if (!rooms.isEmpty() && rooms.peek() <= m[0]) rooms.poll();
            rooms.offer(m[1]);
        }
        System.out.println("min meeting rooms  = " + rooms.size());         // 2

        // ─────────────────────────────────────────────────────────────────────
        // Tips & traps:
        //   • iterating a PQ does NOT give sorted order — only poll() does.
        //   • remove(Object) is O(n); for "lazy deletion" patterns, store
        //     stale entries and skip them when polled.
        //   • For ints, prefer  Comparator.comparingInt(...)  over  (a,b) -> a-b
        //     to avoid overflow when values are near Integer.MAX_VALUE.
        // ─────────────────────────────────────────────────────────────────────

        // ─────────────────────────────────────────────────────────────────────
        // Practice problems:
        //   • LC 215   Kth Largest Element in an Array
        //   • LC 347   Top K Frequent Elements
        //   • LC 23    Merge k Sorted Lists
        //   • LC 295   Find Median from Data Stream
        //   • LC 253   Meeting Rooms II
        //   • LC 621   Task Scheduler
        //   • LC 743   Network Delay Time (Dijkstra)
        //   • LC 973   K Closest Points to Origin
        //   • LC 1167  Minimum Cost to Connect Sticks
        // ─────────────────────────────────────────────────────────────────────
    }

    /** Streaming median via two heaps. */
    static class MedianFinder {
        private final PriorityQueue<Integer> lo = new PriorityQueue<>(Comparator.reverseOrder());
        private final PriorityQueue<Integer> hi = new PriorityQueue<>();
        void add(int x) {
            lo.offer(x);
            hi.offer(lo.poll());                                  // shuffle so lo ≤ hi
            if (hi.size() > lo.size()) lo.offer(hi.poll());        // keep |lo| ∈ {|hi|, |hi|+1}
        }
        double median() {
            return lo.size() > hi.size() ? lo.peek() : (lo.peek() + hi.peek()) / 2.0;
        }
    }
}