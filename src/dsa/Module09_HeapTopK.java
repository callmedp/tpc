package dsa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 9 — HEAP / TOP-K                                                 │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * WHEN TO RECOGNIZE
 *   • "Find the Kth largest / smallest …"
 *   • "Top K frequent / closest / cheapest …"
 *   • Streaming: get the median / running max
 *   • Merge K sorted lists / streams
 *   • Dijkstra (PQ keyed by distance) — covered separately in Module 17
 *
 * THE THREE CORE IDIOMS
 *
 *   1. KTH LARGEST            min-heap of size K. After scan, peek() = answer.
 *      KTH SMALLEST           max-heap of size K. After scan, peek() = answer.
 *
 *   2. STREAMING MEDIAN       two heaps: max-heap of lower half, min-heap of upper.
 *
 *   3. MERGE K SORTED         heap holds one entry per source; pop smallest, push next.
 *
 * Worked problems in this file:
 *   1. LC 215  Kth Largest Element in an Array
 *   2. LC 347  Top K Frequent Elements
 *   3. LC 973  K Closest Points to Origin
 *   4. LC 23   Merge K Sorted Lists
 *   5. LC 295  Find Median from Data Stream
 *   6. LC 621  Task Scheduler
 *   7. LC 1167 Min Cost to Connect Sticks
 */
public class Module09_HeapTopK {

    // ─────────────────────────────────────────────────────────────────────────
    // 1. LC 215 — Kth Largest Element
    // ─────────────────────────────────────────────────────────────────────────
    static int findKthLargest(int[] nums, int k) {
        PriorityQueue<Integer> min = new PriorityQueue<>();
        for (int n : nums) {
            min.offer(n);
            if (min.size() > k) min.poll();
        }
        return min.peek();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. LC 347 — Top K Frequent Elements
    //    HashMap of counts + min-heap keyed by count, size K.
    // ─────────────────────────────────────────────────────────────────────────
    static int[] topKFrequent(int[] nums, int k) {
        Map<Integer, Integer> freq = new HashMap<>();
        for (int n : nums) freq.merge(n, 1, Integer::sum);
        PriorityQueue<Map.Entry<Integer, Integer>> heap =
                new PriorityQueue<>(Map.Entry.comparingByValue());
        for (var e : freq.entrySet()) {
            heap.offer(e);
            if (heap.size() > k) heap.poll();
        }
        int[] out = new int[k];
        for (int i = k - 1; i >= 0; i--) out[i] = heap.poll().getKey();
        return out;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. LC 973 — K Closest Points to Origin (max-heap size K, ranked by distance²)
    // ─────────────────────────────────────────────────────────────────────────
    static int[][] kClosest(int[][] points, int k) {
        PriorityQueue<int[]> max =
                new PriorityQueue<>((a, b) -> dist(b) - dist(a));
        for (int[] p : points) {
            max.offer(p);
            if (max.size() > k) max.poll();
        }
        return max.toArray(new int[0][]);
    }
    private static int dist(int[] p) { return p[0] * p[0] + p[1] * p[1]; }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. LC 23 — Merge K Sorted Lists (here represented as int[] for simplicity)
    //    Heap of {value, listIdx, position}. Pop smallest, push next from same list.
    // ─────────────────────────────────────────────────────────────────────────
    static int[] mergeKArrays(int[][] lists) {
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(x -> x[0]));
        int total = 0;
        for (int i = 0; i < lists.length; i++) {
            total += lists[i].length;
            if (lists[i].length > 0) pq.offer(new int[]{lists[i][0], i, 0});
        }
        int[] out = new int[total];
        int k = 0;
        while (!pq.isEmpty()) {
            int[] t = pq.poll();
            out[k++] = t[0];
            int li = t[1], pos = t[2] + 1;
            if (pos < lists[li].length) pq.offer(new int[]{lists[li][pos], li, pos});
        }
        return out;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. LC 295 — Median from Data Stream (two heaps)
    //    Invariants:  |lo| ∈ {|hi|, |hi|+1}     lo.peek() ≤ hi.peek()
    // ─────────────────────────────────────────────────────────────────────────
    static class MedianFinder {
        private final PriorityQueue<Integer> lo = new PriorityQueue<>(Comparator.reverseOrder());
        private final PriorityQueue<Integer> hi = new PriorityQueue<>();
        void addNum(int x) {
            lo.offer(x);
            hi.offer(lo.poll());
            if (hi.size() > lo.size()) lo.offer(hi.poll());
        }
        double findMedian() {
            return lo.size() > hi.size() ? lo.peek() : (lo.peek() + hi.peek()) / 2.0;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. LC 621 — Task Scheduler (max-heap of counts + cooldown queue)
    //    Each tick, pop the most-frequent task; deque tasks whose cooldown has expired.
    //    A simpler O(n) formula exists but the heap solution is the pattern teacher.
    // ─────────────────────────────────────────────────────────────────────────
    static int leastInterval(char[] tasks, int n) {
        int[] cnt = new int[26];
        for (char c : tasks) cnt[c - 'A']++;
        PriorityQueue<Integer> max = new PriorityQueue<>(Comparator.reverseOrder());
        for (int c : cnt) if (c > 0) max.offer(c);
        java.util.Queue<int[]> cooldown = new java.util.ArrayDeque<>();  // {count, readyAtTime}
        int time = 0;
        while (!max.isEmpty() || !cooldown.isEmpty()) {
            time++;
            if (!max.isEmpty()) {
                int c = max.poll() - 1;
                if (c > 0) cooldown.offer(new int[]{c, time + n});
            }
            if (!cooldown.isEmpty() && cooldown.peek()[1] == time) max.offer(cooldown.poll()[0]);
        }
        return time;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. LC 1167 — Minimum Cost to Connect Sticks (Huffman-style greedy)
    // ─────────────────────────────────────────────────────────────────────────
    static int connectSticks(int[] sticks) {
        PriorityQueue<Integer> min = new PriorityQueue<>();
        for (int s : sticks) min.offer(s);
        int cost = 0;
        while (min.size() > 1) {
            int merged = min.poll() + min.poll();
            cost += merged;
            min.offer(merged);
        }
        return cost;
    }

    public static void main(String[] args) {
        System.out.println("findKthLargest         = " + findKthLargest(new int[]{3, 2, 1, 5, 6, 4}, 2));
        System.out.println("topKFrequent           = " + Arrays.toString(topKFrequent(new int[]{1, 1, 1, 2, 2, 3}, 2)));
        System.out.println("kClosest               = " + Arrays.deepToString(kClosest(new int[][]{{1, 3}, {-2, 2}, {5, 8}, {0, 1}}, 2)));
        System.out.println("mergeKArrays           = " + Arrays.toString(
                mergeKArrays(new int[][]{{1, 4, 7}, {2, 5, 8}, {3, 6, 9}})));

        MedianFinder mf = new MedianFinder();
        for (int x : new int[]{1, 2, 3, 4, 5}) mf.addNum(x);
        System.out.println("findMedian             = " + mf.findMedian());
        System.out.println("leastInterval(AAABBB,2)= " + leastInterval(new char[]{'A', 'A', 'A', 'B', 'B', 'B'}, 2));
        System.out.println("connectSticks          = " + connectSticks(new int[]{2, 4, 3}));
    }

    /*
     * PRACTICE SET
     *   • LC 378  Kth Smallest in a Sorted Matrix
     *   • LC 692  Top K Frequent Words           (heap with custom Comparator)
     *   • LC 451  Sort Characters By Frequency
     *   • LC 703  Kth Largest in a Stream         (min-heap size K, online)
     *   • LC 502  IPO                              (two heaps)
     *   • LC 1046 Last Stone Weight                (max-heap)
     *   • LC 1631 Path With Minimum Effort        (Dijkstra-like)
     *   • LC 1834 Single-Threaded CPU              (two heaps + sort)
     *   • LC 2402 Meeting Rooms III                (two heaps)
     */
}
