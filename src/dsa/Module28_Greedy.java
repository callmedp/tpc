package dsa;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 28 — GREEDY                                                      │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 *   "Greedy" means: at every step, take the locally best choice and PROVE
 *   it doesn't ruin the global answer. Two common proof patterns:
 *
 *     • EXCHANGE ARGUMENT
 *       If some optimal solution makes a different choice than the greedy,
 *       show we can swap the two without making things worse → greedy = OPT.
 *
 *     • SORTING + INVARIANT
 *       After a clever sort, scanning left-to-right and maintaining one
 *       running invariant solves it.
 *
 *   If you cannot prove correctness, suspect DP.
 *
 * Worked problems in this file:
 *   1. LC 55    Jump Game                                (track farthest reachable)
 *   2. LC 45    Jump Game II                             (track "current end" / "farthest")
 *   3. LC 134   Gas Station
 *   4. LC 763   Partition Labels                         (last-occurrence map)
 *   5. LC 435   Non-overlapping Intervals               (sort by END)
 *   6. LC 1029  Two City Scheduling                      (sort by cost difference)
 *   7. LC 860   Lemonade Change
 *   8. LC 502   IPO                                       (heap + sort by capital)
 *   9. LC 678   Valid Parenthesis String                  (range of open counts)
 */
public class Module28_Greedy {

    // 1. LC 55 — Jump Game
    static boolean canJump(int[] a) {
        int farthest = 0;
        for (int i = 0; i < a.length; i++) {
            if (i > farthest) return false;
            farthest = Math.max(farthest, i + a[i]);
        }
        return true;
    }

    // 2. LC 45 — Jump Game II (min jumps)
    //    "currentEnd" = farthest you can reach in the current jump count;
    //    when i passes it, you take another jump and update.
    static int jump(int[] a) {
        int jumps = 0, currentEnd = 0, farthest = 0;
        for (int i = 0; i < a.length - 1; i++) {
            farthest = Math.max(farthest, i + a[i]);
            if (i == currentEnd) { jumps++; currentEnd = farthest; }
        }
        return jumps;
    }

    // 3. LC 134 — Gas Station
    //    If total gas ≥ total cost, the answer exists. Greedy start: whenever the
    //    running tank goes negative, restart at i + 1.
    static int canCompleteCircuit(int[] gas, int[] cost) {
        int total = 0, tank = 0, start = 0;
        for (int i = 0; i < gas.length; i++) {
            int d = gas[i] - cost[i];
            total += d; tank += d;
            if (tank < 0) { start = i + 1; tank = 0; }
        }
        return total < 0 ? -1 : start;
    }

    // 4. LC 763 — Partition Labels (greedy with last-occurrence map)
    static java.util.List<Integer> partitionLabels(String s) {
        int[] last = new int[26];
        for (int i = 0; i < s.length(); i++) last[s.charAt(i) - 'a'] = i;
        java.util.List<Integer> out = new java.util.ArrayList<>();
        int start = 0, end = 0;
        for (int i = 0; i < s.length(); i++) {
            end = Math.max(end, last[s.charAt(i) - 'a']);
            if (i == end) { out.add(end - start + 1); start = i + 1; }
        }
        return out;
    }

    // 5. LC 435 — Non-overlapping Intervals  (sort by END, greedy keep)
    static int eraseOverlapIntervals(int[][] intervals) {
        Arrays.sort(intervals, (a, b) -> a[1] - b[1]);
        int kept = 0, lastEnd = Integer.MIN_VALUE;
        for (int[] in : intervals) if (in[0] >= lastEnd) { kept++; lastEnd = in[1]; }
        return intervals.length - kept;
    }

    // 6. LC 1029 — Two City Scheduling
    //    Sort by  cost[A] - cost[B]; first half goes to A, second half to B.
    static int twoCitySchedCost(int[][] costs) {
        Arrays.sort(costs, (a, b) -> (a[0] - a[1]) - (b[0] - b[1]));
        int n = costs.length / 2, sum = 0;
        for (int i = 0; i < n; i++) sum += costs[i][0];
        for (int i = n; i < costs.length; i++) sum += costs[i][1];
        return sum;
    }

    // 7. LC 860 — Lemonade Change
    static boolean lemonadeChange(int[] bills) {
        int five = 0, ten = 0;
        for (int b : bills) {
            if (b == 5) five++;
            else if (b == 10) { if (five == 0) return false; five--; ten++; }
            else {
                if (ten > 0 && five > 0) { ten--; five--; }
                else if (five >= 3)      { five -= 3; }
                else return false;
            }
        }
        return true;
    }

    // 8. LC 502 — IPO (project selection)
    //    Sort projects by capital; min-heap on capital, max-heap on profit.
    //    Each round: move all currently-affordable projects from min-heap to max-heap;
    //    pick the most profitable one; add its profit to capital.
    static int findMaximizedCapital(int k, int W, int[] profits, int[] capital) {
        int n = profits.length;
        Integer[] idx = new Integer[n];
        for (int i = 0; i < n; i++) idx[i] = i;
        Arrays.sort(idx, (a, b) -> capital[a] - capital[b]);
        java.util.PriorityQueue<Integer> max = new java.util.PriorityQueue<>(java.util.Comparator.reverseOrder());
        int i = 0;
        while (k-- > 0) {
            while (i < n && capital[idx[i]] <= W) max.offer(profits[idx[i++]]);
            if (max.isEmpty()) break;
            W += max.poll();
        }
        return W;
    }

    // 9. LC 678 — Valid Parenthesis String
    //    Track [lo, hi] = range of possible open-brace counts.
    //    On '*': lo--; hi++.   On '(': both ++. On ')': both --.
    //    Clamp lo ≥ 0; return false if hi < 0.
    static boolean checkValidString(String s) {
        int lo = 0, hi = 0;
        for (char c : s.toCharArray()) {
            if      (c == '(') { lo++; hi++; }
            else if (c == ')') { lo--; hi--; }
            else               { lo--; hi++; }
            if (hi < 0) return false;
            if (lo < 0) lo = 0;
        }
        return lo == 0;
    }

    public static void main(String[] args) {
        System.out.println("canJump [2,3,1,1,4]       = " + canJump(new int[]{2, 3, 1, 1, 4}));      // true
        System.out.println("jump    [2,3,1,1,4]       = " + jump(new int[]{2, 3, 1, 1, 4}));         // 2
        System.out.println("canCompleteCircuit        = " + canCompleteCircuit(
                new int[]{1, 2, 3, 4, 5}, new int[]{3, 4, 5, 1, 2}));                                  // 3
        System.out.println("partitionLabels           = " + partitionLabels("ababcbacadefegdehijhklij"));
        System.out.println("eraseOverlapIntervals     = " + eraseOverlapIntervals(new int[][]{
                {1, 2}, {2, 3}, {3, 4}, {1, 3}}));                                                    // 1
        System.out.println("twoCitySchedCost          = " + twoCitySchedCost(new int[][]{
                {10, 20}, {30, 200}, {400, 50}, {30, 20}}));                                          // 110
        System.out.println("lemonadeChange            = " + lemonadeChange(new int[]{5, 5, 5, 10, 20}));
        System.out.println("findMaximizedCapital      = " + findMaximizedCapital(
                2, 0, new int[]{1, 2, 3}, new int[]{0, 1, 1}));                                       // 4
        System.out.println("checkValidString          = " + checkValidString("(*))"));                 // true
    }

    /*
     * PRACTICE SET
     *   • LC 11    Container With Most Water           (two-pointer greedy)
     *   • LC 135   Candy                                 (two passes)
     *   • LC 376   Wiggle Subsequence
     *   • LC 452   Min Arrows to Burst Balloons        (intervals, by END)
     *   • LC 605   Can Place Flowers
     *   • LC 621   Task Scheduler                       (heap also works)
     *   • LC 738   Monotone Increasing Digits
     *   • LC 881   Boats to Save People
     *   • LC 945   Min Increment to Make Array Unique
     *   • LC 1029  Two City Scheduling (this file)
     *   • LC 1326  Min Number of Taps to Open
     */
}
