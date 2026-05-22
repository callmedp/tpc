package dsa;

import java.util.Arrays;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 22 — DP: KNAPSACK FAMILY                                          │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * THE FAMILY
 *   • 0/1 KNAPSACK         — each item picked at most once.
 *       Loop weight (or "budget") DESCENDING when using 1-D dp.
 *
 *   • UNBOUNDED KNAPSACK   — each item picked any number of times.
 *       Loop weight ASCENDING when using 1-D dp.
 *
 *   • COUNT vs MAX vs MIN  — same skeleton, different recurrence:
 *       count:   dp[w] += dp[w - wt]
 *       max sum: dp[w]  = max(dp[w], dp[w - wt] + val)
 *       min cnt: dp[w]  = min(dp[w], dp[w - wt] + 1)
 *
 * RECOGNITION SIGNS
 *   • "Can we select a subset that sums to S?"  (0/1)
 *   • "Number of ways to reach target with array elements"  (0/1 vs unbounded → check reuse)
 *   • "Min coins to make amount"  (unbounded)
 *
 * Worked problems in this file:
 *   1. Classic 0/1 Knapsack                          (max value, capacity W)
 *   2. LC 416  Partition Equal Subset Sum            (0/1, boolean)
 *   3. LC 494  Target Sum                            (0/1 count, +/- assignment)
 *   4. LC 474  Ones and Zeroes                       (0/1 with 2-D capacity)
 *   5. LC 322  Coin Change                           (unbounded, min count)
 *   6. LC 518  Coin Change II                        (unbounded, count ways)
 *   7. LC 377  Combination Sum IV  (note: PERMUTATIONS, not combinations — outer = target)
 */
public class Module22_DPKnapsack {

    // 1. Classic 0/1 Knapsack — max value within capacity W
    static int knapsack01(int[] wt, int[] val, int W) {
        int[] dp = new int[W + 1];
        for (int i = 0; i < wt.length; i++)
            for (int w = W; w >= wt[i]; w--)        // descending → ensures each item used once
                dp[w] = Math.max(dp[w], dp[w - wt[i]] + val[i]);
        return dp[W];
    }

    // 2. LC 416 — Partition Equal Subset Sum (boolean subset-sum DP)
    static boolean canPartition(int[] nums) {
        int sum = 0; for (int n : nums) sum += n;
        if ((sum & 1) == 1) return false;
        int target = sum / 2;
        boolean[] dp = new boolean[target + 1];
        dp[0] = true;
        for (int n : nums)
            for (int w = target; w >= n; w--)
                dp[w] |= dp[w - n];
        return dp[target];
    }

    // 3. LC 494 — Target Sum: assign +/- to each num to reach S.
    //    Equivalent to: pick a subset P with sum = (sum + S) / 2  ⇒ subset-sum count DP.
    static int findTargetSumWays(int[] nums, int S) {
        int sum = 0; for (int n : nums) sum += n;
        if (Math.abs(S) > sum || ((sum + S) & 1) != 0) return 0;
        int target = (sum + S) / 2;
        int[] dp = new int[target + 1];
        dp[0] = 1;
        for (int n : nums)
            for (int w = target; w >= n; w--) dp[w] += dp[w - n];
        return dp[target];
    }

    // 4. LC 474 — Ones and Zeroes (0/1 with 2-D capacity)
    //    State: dp[i][j] = max strings selected with ≤ i zeros and ≤ j ones.
    static int findMaxForm(String[] strs, int m, int n) {
        int[][] dp = new int[m + 1][n + 1];
        for (String s : strs) {
            int zeros = 0, ones = 0;
            for (char c : s.toCharArray()) if (c == '0') zeros++; else ones++;
            for (int i = m; i >= zeros; i--)
                for (int j = n; j >= ones; j--)
                    dp[i][j] = Math.max(dp[i][j], dp[i - zeros][j - ones] + 1);
        }
        return dp[m][n];
    }

    // 5. LC 322 — Coin Change (min coins; UNBOUNDED)
    //    Outer loop: coins. Inner loop: ascending amount.
    static int coinChangeMin(int[] coins, int amount) {
        int[] dp = new int[amount + 1];
        Arrays.fill(dp, amount + 1);
        dp[0] = 0;
        for (int c : coins)
            for (int a = c; a <= amount; a++)
                dp[a] = Math.min(dp[a], dp[a - c] + 1);
        return dp[amount] > amount ? -1 : dp[amount];
    }

    // 6. LC 518 — Coin Change II (count ways; UNBOUNDED)
    //    Outer coin / inner amount ASCENDING ⇒ COMBINATIONS (order doesn't matter).
    static int coinChangeWays(int amount, int[] coins) {
        int[] dp = new int[amount + 1];
        dp[0] = 1;
        for (int c : coins)
            for (int a = c; a <= amount; a++) dp[a] += dp[a - c];
        return dp[amount];
    }

    // 7. LC 377 — Combination Sum IV  (PERMUTATIONS counted)
    //    Outer target / inner number ⇒ same number can be used multiple times AND
    //    different orderings count separately.
    static int combinationSum4(int[] nums, int target) {
        int[] dp = new int[target + 1];
        dp[0] = 1;
        for (int a = 1; a <= target; a++)
            for (int n : nums)
                if (a >= n) dp[a] += dp[a - n];
        return dp[target];
    }

    public static void main(String[] args) {
        System.out.println("knapsack01([2,3,4],[3,4,5],5)= " + knapsack01(new int[]{2, 3, 4}, new int[]{3, 4, 5}, 5)); // 7
        System.out.println("canPartition [1,5,11,5]      = " + canPartition(new int[]{1, 5, 11, 5}));                  // true
        System.out.println("findTargetSumWays            = " + findTargetSumWays(new int[]{1, 1, 1, 1, 1}, 3));         // 5
        System.out.println("findMaxForm                  = " + findMaxForm(new String[]{"10", "0001", "111001", "1", "0"}, 5, 3)); // 4
        System.out.println("coinChangeMin [1,2,5],11     = " + coinChangeMin(new int[]{1, 2, 5}, 11));                   // 3
        System.out.println("coinChangeWays 5,[1,2,5]     = " + coinChangeWays(5, new int[]{1, 2, 5}));                    // 4
        System.out.println("combinationSum4 [1,2,3],4    = " + combinationSum4(new int[]{1, 2, 3}, 4));                  // 7
    }

    /*
     * PRACTICE SET
     *   • LC 879   Profitable Schemes
     *   • LC 956   Tallest Billboard
     *   • LC 1049  Last Stone Weight II                 (subset-sum minimisation)
     *   • LC 1402  Reducing Dishes
     *   • LC 638   Shopping Offers                       (bounded knapsack with offers)
     *   • Classic: rod cutting, unbounded knapsack variants.
     *   • Classic interview: subset sum with K equal sums (NP-hard → bitmask DP, Module 25).
     */
}
