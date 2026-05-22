package dsa;

import java.util.Arrays;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 20 — DYNAMIC PROGRAMMING (1-D LINEAR)                             │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * THE FIRST DP YOU INTERNALISE — every later DP module is a generalisation.
 *
 * SIGNS THAT IT'S LINEAR DP
 *   • One index moves through the input.
 *   • Each state depends on a constant number of PREVIOUS states.
 *   • Order: f(i) depends on f(i-1), f(i-2), ...
 *
 * HOW TO ATTACK
 *   1. Define the STATE precisely:  "f(i) = …".
 *   2. Write the RECURRENCE  (how does f(i) compose from smaller f(?)?).
 *   3. Write BASE CASES.
 *   4. Decide bottom-up (array) or top-down (memoised recursion).
 *   5. Optimise space (often O(1) — just keep the last few values).
 *
 * Worked problems in this file:
 *   1. LC 70   Climbing Stairs                       (Fibonacci shape)
 *   2. LC 198  House Robber                          (take vs skip)
 *   3. LC 213  House Robber II  (circular)
 *   4. LC 300  Longest Increasing Subsequence        (O(n²) DP and O(n log n) tails)
 *   5. LC 322  Coin Change  (min coins)              (unbounded knapsack 1-D form)
 *   6. LC 91   Decode Ways
 *   7. LC 53   Maximum Subarray  (Kadane's)
 *   8. LC 152  Maximum Product Subarray              (track max & min)
 */
public class Module20_DPLinear {

    // 1. LC 70 — Climbing Stairs.  f(n) = f(n-1) + f(n-2), f(0)=f(1)=1.
    static int climbStairs(int n) {
        if (n <= 1) return 1;
        int a = 1, b = 1;
        for (int i = 2; i <= n; i++) { int c = a + b; a = b; b = c; }
        return b;
    }

    // 2. LC 198 — House Robber. f(i) = max(f(i-1), f(i-2) + nums[i]).
    static int rob(int[] nums) {
        int prev2 = 0, prev1 = 0;
        for (int n : nums) {
            int cur = Math.max(prev1, prev2 + n);
            prev2 = prev1; prev1 = cur;
        }
        return prev1;
    }

    // 3. LC 213 — House Robber II (circular).
    //    Two passes: rob houses[0..n-2] and houses[1..n-1]; take the max.
    static int robCircular(int[] nums) {
        if (nums.length == 1) return nums[0];
        return Math.max(robRange(nums, 0, nums.length - 2),
                        robRange(nums, 1, nums.length - 1));
    }
    private static int robRange(int[] nums, int l, int r) {
        int prev2 = 0, prev1 = 0;
        for (int i = l; i <= r; i++) {
            int cur = Math.max(prev1, prev2 + nums[i]);
            prev2 = prev1; prev1 = cur;
        }
        return prev1;
    }

    // 4a. LC 300 — Longest Increasing Subsequence (DP, O(n²))
    //     f(i) = 1 + max f(j) for j<i, nums[j]<nums[i].
    static int lengthOfLIS_DP(int[] nums) {
        int n = nums.length, best = 0;
        int[] f = new int[n];
        Arrays.fill(f, 1);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < i; j++) if (nums[j] < nums[i]) f[i] = Math.max(f[i], f[j] + 1);
            best = Math.max(best, f[i]);
        }
        return best;
    }

    // 4b. LC 300 — LIS in O(n log n) via "patience sorting" tails[]
    static int lengthOfLIS_Patience(int[] nums) {
        int[] tails = new int[nums.length];
        int size = 0;
        for (int x : nums) {
            int lo = 0, hi = size;
            while (lo < hi) {
                int mid = lo + (hi - lo) / 2;
                if (tails[mid] >= x) hi = mid;
                else                 lo = mid + 1;
            }
            tails[lo] = x;
            if (lo == size) size++;
        }
        return size;
    }

    // 5. LC 322 — Coin Change (min coins to make amount)
    //    f(a) = 1 + min f(a - c)  over all coins c ≤ a.
    static int coinChange(int[] coins, int amount) {
        int[] f = new int[amount + 1];
        Arrays.fill(f, amount + 1);
        f[0] = 0;
        for (int a = 1; a <= amount; a++)
            for (int c : coins)
                if (c <= a) f[a] = Math.min(f[a], f[a - c] + 1);
        return f[amount] > amount ? -1 : f[amount];
    }

    // 6. LC 91 — Decode Ways
    //    f(i) counts decodings of s[0..i-1].
    //    f(i) += f(i-1) if s[i-1] is '1'..'9';
    //    f(i) += f(i-2) if s[i-2..i-1] forms 10..26.
    static int numDecodings(String s) {
        int n = s.length();
        int[] f = new int[n + 1];
        f[0] = 1;
        f[1] = s.charAt(0) == '0' ? 0 : 1;
        for (int i = 2; i <= n; i++) {
            if (s.charAt(i - 1) != '0') f[i] += f[i - 1];
            int two = Integer.parseInt(s.substring(i - 2, i));
            if (two >= 10 && two <= 26) f[i] += f[i - 2];
        }
        return f[n];
    }

    // 7. LC 53 — Maximum Subarray (Kadane)
    //    f(i) = max(nums[i], f(i-1) + nums[i]).  Track global max.
    static int maxSubArray(int[] nums) {
        int best = nums[0], cur = nums[0];
        for (int i = 1; i < nums.length; i++) {
            cur = Math.max(nums[i], cur + nums[i]);
            best = Math.max(best, cur);
        }
        return best;
    }

    // 8. LC 152 — Maximum Product Subarray
    //    Track current max AND current min (negatives flip signs).
    static int maxProduct(int[] nums) {
        int best = nums[0], mx = nums[0], mn = nums[0];
        for (int i = 1; i < nums.length; i++) {
            int x = nums[i];
            int newMax = Math.max(x, Math.max(mx * x, mn * x));
            int newMin = Math.min(x, Math.min(mx * x, mn * x));
            mx = newMax; mn = newMin;
            best = Math.max(best, mx);
        }
        return best;
    }

    public static void main(String[] args) {
        System.out.println("climbStairs(5)        = " + climbStairs(5));                               // 8
        System.out.println("rob                   = " + rob(new int[]{2, 7, 9, 3, 1}));                // 12
        System.out.println("robCircular           = " + robCircular(new int[]{2, 3, 2}));              // 3
        System.out.println("LIS DP                = " + lengthOfLIS_DP(new int[]{10, 9, 2, 5, 3, 7, 101, 18})); // 4
        System.out.println("LIS Patience          = " + lengthOfLIS_Patience(new int[]{10, 9, 2, 5, 3, 7, 101, 18}));
        System.out.println("coinChange [1,2,5],11 = " + coinChange(new int[]{1, 2, 5}, 11));           // 3
        System.out.println("numDecodings(226)     = " + numDecodings("226"));                          // 3
        System.out.println("maxSubArray (Kadane)  = " + maxSubArray(new int[]{-2, 1, -3, 4, -1, 2, 1, -5, 4})); // 6
        System.out.println("maxProduct            = " + maxProduct(new int[]{2, 3, -2, 4}));            // 6
    }

    /*
     * PRACTICE SET
     *   • LC 746   Min Cost Climbing Stairs
     *   • LC 740   Delete and Earn                       (rob in disguise)
     *   • LC 198/213 House Robber I/II already in
     *   • LC 337   House Robber III (tree) → Module 26
     *   • LC 1143  LCS → Module 23
     *   • LC 718   Maximum Length of Repeated Subarray
     *   • LC 416   Partition Equal Subset Sum → Module 22 (knapsack)
     *   • LC 935   Knight Dialer
     *   • LC 871   Min Refueling Stops
     *   • LC 873   Length of Longest Fibonacci Subseq
     *   • LC 1262  Greatest Sum Divisible by Three
     *   • LC 1493  Longest Subarray of 1s After Deleting One
     */
}
