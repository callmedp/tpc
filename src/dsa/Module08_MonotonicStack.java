package dsa;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 8 — MONOTONIC STACK / DEQUE                                      │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * WHEN TO RECOGNIZE
 *   • "For each element, find the NEXT GREATER / NEXT SMALLER / PREVIOUS …"
 *   • "Largest rectangle in histogram", "Trapping rain water"
 *   • "Sliding window maximum"
 *   • "Stock span", "Daily temperatures"
 *   • Pattern phrase: "the answer to index i depends on the nearest j>i (or j<i)
 *     where some monotone condition first breaks."
 *
 * THE TEMPLATE (Next-Greater-Element, indices stored)
 *
 *      Deque<Integer> st = new ArrayDeque<>();
 *      for (int i = 0; i < n; i++) {
 *          while (!st.isEmpty() && nums[st.peek()] < nums[i]) {
 *              int j = st.pop();
 *              result[j] = nums[i];          // i is j's NGE
 *          }
 *          st.push(i);
 *      }
 *
 * KEY VARIANTS
 *   • Stack stores INDICES (lets you compute distances) or VALUES.
 *   • Strict vs non-strict inequality changes equal-element behaviour.
 *   • Forward sweep gives NEXT-greater; backward sweep gives PREV-greater.
 *
 * Worked problems in this file:
 *   1. LC 496  Next Greater Element I
 *   2. LC 503  Next Greater Element II (circular array)
 *   3. LC 739  Daily Temperatures
 *   4. LC 84   Largest Rectangle in Histogram          (the canonical hard one)
 *   5. LC 42   Trapping Rain Water (stack variant)
 *   6. LC 239  Sliding Window Maximum (monotonic deque)
 *   7. LC 901  Online Stock Span                       (pair on stack)
 */
public class Module08_MonotonicStack {

    // ─────────────────────────────────────────────────────────────────────────
    // 1. LC 496 — Next Greater Element I
    //    Pre-compute NGE for nums2; answer for nums1[i] is a lookup.
    // ─────────────────────────────────────────────────────────────────────────
    static int[] nextGreaterElement(int[] nums1, int[] nums2) {
        Map<Integer, Integer> nge = new HashMap<>();
        Deque<Integer> st = new ArrayDeque<>();
        for (int x : nums2) {
            while (!st.isEmpty() && st.peek() < x) nge.put(st.pop(), x);
            st.push(x);
        }
        int[] out = new int[nums1.length];
        for (int i = 0; i < nums1.length; i++) out[i] = nge.getOrDefault(nums1[i], -1);
        return out;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. LC 503 — Next Greater Element II  (circular)
    //    Walk indices 0..2n-1, use i % n.
    // ─────────────────────────────────────────────────────────────────────────
    static int[] nextGreaterCircular(int[] nums) {
        int n = nums.length;
        int[] out = new int[n];
        Arrays.fill(out, -1);
        Deque<Integer> st = new ArrayDeque<>();
        for (int i = 0; i < 2 * n; i++) {
            int x = nums[i % n];
            while (!st.isEmpty() && nums[st.peek()] < x) out[st.pop()] = x;
            if (i < n) st.push(i);
        }
        return out;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. LC 739 — Daily Temperatures (distance to next warmer day)
    // ─────────────────────────────────────────────────────────────────────────
    static int[] dailyTemperatures(int[] t) {
        int n = t.length;
        int[] out = new int[n];
        Deque<Integer> st = new ArrayDeque<>();
        for (int i = 0; i < n; i++) {
            while (!st.isEmpty() && t[st.peek()] < t[i]) {
                int j = st.pop();
                out[j] = i - j;
            }
            st.push(i);
        }
        return out;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. LC 84 — Largest Rectangle in Histogram (CLASSIC interview problem)
    //    Stack holds indices of bars in increasing height order.
    //    When the new bar is shorter, pop and compute area assuming the popped
    //    bar was the LIMITING height. Sentinel: append a 0 at the end.
    // ─────────────────────────────────────────────────────────────────────────
    static int largestRectangleArea(int[] h) {
        int n = h.length, best = 0;
        Deque<Integer> st = new ArrayDeque<>();
        for (int i = 0; i <= n; i++) {
            int cur = (i == n) ? 0 : h[i];
            while (!st.isEmpty() && h[st.peek()] > cur) {
                int height = h[st.pop()];
                int left   = st.isEmpty() ? -1 : st.peek();
                int width  = i - left - 1;
                best = Math.max(best, height * width);
            }
            st.push(i);
        }
        return best;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. LC 42 — Trapping Rain Water (stack version)
    //    Stack holds indices of bars in decreasing height. On a higher bar,
    //    pop and compute trapped water for the popped bottom.
    // ─────────────────────────────────────────────────────────────────────────
    static int trap(int[] h) {
        Deque<Integer> st = new ArrayDeque<>();
        int water = 0;
        for (int i = 0; i < h.length; i++) {
            while (!st.isEmpty() && h[st.peek()] < h[i]) {
                int bottom = st.pop();
                if (st.isEmpty()) break;
                int left = st.peek();
                int width = i - left - 1;
                int bounded = Math.min(h[left], h[i]) - h[bottom];
                water += width * bounded;
            }
            st.push(i);
        }
        return water;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. LC 239 — Sliding Window Maximum  (monotonic DEQUE, not stack)
    //    Deque holds indices, values STRICTLY DECREASING. Front = window max.
    // ─────────────────────────────────────────────────────────────────────────
    static int[] maxSlidingWindow(int[] nums, int k) {
        Deque<Integer> dq = new ArrayDeque<>();
        int[] out = new int[nums.length - k + 1];
        for (int i = 0; i < nums.length; i++) {
            while (!dq.isEmpty() && dq.peekFirst() <= i - k) dq.pollFirst();
            while (!dq.isEmpty() && nums[dq.peekLast()] < nums[i]) dq.pollLast();
            dq.offerLast(i);
            if (i >= k - 1) out[i - k + 1] = nums[dq.peekFirst()];
        }
        return out;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. LC 901 — Online Stock Span
    //    Stack of (price, span). On a new price, pop while top.price ≤ price;
    //    accumulate their spans.
    // ─────────────────────────────────────────────────────────────────────────
    static class StockSpanner {
        private final Deque<int[]> st = new ArrayDeque<>();
        int next(int price) {
            int span = 1;
            while (!st.isEmpty() && st.peek()[0] <= price) span += st.pop()[1];
            st.push(new int[]{price, span});
            return span;
        }
    }

    public static void main(String[] args) {
        System.out.println("NGE I             = " + Arrays.toString(nextGreaterElement(new int[]{4, 1, 2}, new int[]{1, 3, 4, 2})));
        System.out.println("NGE II (circular) = " + Arrays.toString(nextGreaterCircular(new int[]{1, 2, 1})));
        System.out.println("dailyTemperatures = " + Arrays.toString(dailyTemperatures(new int[]{73, 74, 75, 71, 69, 72, 76, 73})));
        System.out.println("largestRectangle  = " + largestRectangleArea(new int[]{2, 1, 5, 6, 2, 3}));
        System.out.println("trap (stack)      = " + trap(new int[]{0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1}));
        System.out.println("slidingMax        = " + Arrays.toString(maxSlidingWindow(new int[]{1, 3, -1, -3, 5, 3, 6, 7}, 3)));

        StockSpanner spn = new StockSpanner();
        List<Integer> spans = new ArrayList<>();
        for (int p : new int[]{100, 80, 60, 70, 60, 75, 85}) spans.add(spn.next(p));
        System.out.println("stockSpan         = " + spans);
    }

    /*
     * PRACTICE SET
     *   • LC 85    Maximal Rectangle               (build histograms row by row → LC 84)
     *   • LC 402   Remove K Digits                 (monotonic stack, greedy)
     *   • LC 316   Remove Duplicate Letters
     *   • LC 1019  Next Greater Node In Linked List
     *   • LC 962   Maximum Width Ramp
     *   • LC 907   Sum of Subarray Minimums
     *   • LC 1856  Maximum Subarray Min·Product
     *   • LC 1944  Number of Visible People in a Queue
     *   • LC 456   132 Pattern                     (clever monotonic stack)
     */
}
