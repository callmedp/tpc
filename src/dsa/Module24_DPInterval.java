package dsa;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 24 — INTERVAL DP                                                 │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * SIGNS THAT IT'S INTERVAL DP
 *   • The answer for range [i, j] depends on splitting it into [i, k] and [k+1, j]
 *     (or [i+1, j-1] joined with a decision about endpoints).
 *   • Order: fill dp[i][j] by INCREASING length, OR walk i top-down / j bottom-up.
 *
 * THE TEMPLATE
 *
 *      int[][] dp = new int[n][n];
 *      for (int len = 2; len <= n; len++)
 *          for (int i = 0; i + len <= n; i++) {
 *              int j = i + len - 1;
 *              dp[i][j] = INIT;
 *              for (int k = i; k < j; k++)
 *                  dp[i][j] = combine(dp[i][j], dp[i][k], dp[k+1][j], cost(i,k,j));
 *          }
 *      return dp[0][n-1];
 *
 *   Complexity is typically O(n³) — split point + 2-D table.
 *
 * Worked problems in this file:
 *   1. LC 312   Burst Balloons                       (classic O(n³) interval DP)
 *   2. LC 1000  Minimum Cost to Merge Stones          (interval DP w/ K-way join)
 *   3. LC 1547  Minimum Cost to Cut a Stick           (classic split DP)
 *   4. LC 87    Scramble String                       (split + recursion + memo)
 *   5. LC 664   Strange Printer                       (interval DP on chars)
 *   6. Matrix Chain Multiplication                    (textbook example)
 */
public class Module24_DPInterval {

    // 1. LC 312 — Burst Balloons
    //    Trick: think of k as the LAST balloon burst in (i..j). Adjacent balloons
    //    then are nums[i-1] and nums[j+1]. Pad with 1s at the ends.
    static int maxCoins(int[] nums) {
        int n = nums.length;
        int[] a = new int[n + 2];
        a[0] = a[n + 1] = 1;
        for (int i = 0; i < n; i++) a[i + 1] = nums[i];
        int[][] dp = new int[n + 2][n + 2];
        for (int len = 1; len <= n; len++)
            for (int i = 1; i + len - 1 <= n; i++) {
                int j = i + len - 1;
                for (int k = i; k <= j; k++) {
                    int gain = a[i - 1] * a[k] * a[j + 1] + dp[i][k - 1] + dp[k + 1][j];
                    if (gain > dp[i][j]) dp[i][j] = gain;
                }
            }
        return dp[1][n];
    }

    // 2. LC 1000 — Min Cost to Merge Stones (K-way merge)
    //    Possible iff (n - 1) % (K - 1) == 0. dp[i][j] = min cost to merge a[i..j] into
    //    1 pile IF (j - i) % (K - 1) == 0, else into ≤ K-1 piles.
    static int mergeStones(int[] stones, int K) {
        int n = stones.length;
        if ((n - 1) % (K - 1) != 0) return -1;
        int[] pre = new int[n + 1];
        for (int i = 0; i < n; i++) pre[i + 1] = pre[i] + stones[i];
        int[][] dp = new int[n][n];
        for (int len = K; len <= n; len++)
            for (int i = 0; i + len <= n; i++) {
                int j = i + len - 1;
                dp[i][j] = Integer.MAX_VALUE;
                for (int k = i; k < j; k += K - 1)
                    dp[i][j] = Math.min(dp[i][j], dp[i][k] + dp[k + 1][j]);
                if ((j - i) % (K - 1) == 0) dp[i][j] += pre[j + 1] - pre[i];
            }
        return dp[0][n - 1];
    }

    // 3. LC 1547 — Min Cost to Cut a Stick
    //    Pad cuts with 0 and n; dp[i][j] = min cost to make every cut strictly between c[i] and c[j].
    //    Cost of cut k = (c[j] - c[i]). Split on k between i+1 and j-1.
    static int minCostCutStick(int n, int[] cuts) {
        int m = cuts.length;
        int[] c = new int[m + 2];
        c[0] = 0; c[m + 1] = n;
        System.arraycopy(cuts, 0, c, 1, m);
        java.util.Arrays.sort(c);
        int len = c.length;
        int[][] dp = new int[len][len];
        for (int width = 2; width < len; width++)
            for (int i = 0; i + width < len; i++) {
                int j = i + width;
                dp[i][j] = Integer.MAX_VALUE;
                for (int k = i + 1; k < j; k++)
                    dp[i][j] = Math.min(dp[i][j], dp[i][k] + dp[k][j] + (c[j] - c[i]));
            }
        return dp[0][len - 1];
    }

    // 4. LC 87 — Scramble String (memoised recursion using interval split)
    static java.util.Map<String, Boolean> memo = new java.util.HashMap<>();
    static boolean isScramble(String a, String b) {
        if (a.equals(b)) return true;
        if (a.length() != b.length()) return false;
        String key = a + "#" + b;
        if (memo.containsKey(key)) return memo.get(key);
        int[] cnt = new int[26];
        for (int i = 0; i < a.length(); i++) { cnt[a.charAt(i) - 'a']++; cnt[b.charAt(i) - 'a']--; }
        for (int c : cnt) if (c != 0) { memo.put(key, false); return false; }
        int n = a.length();
        for (int i = 1; i < n; i++) {
            if (isScramble(a.substring(0, i), b.substring(0, i))
             && isScramble(a.substring(i),   b.substring(i))) { memo.put(key, true); return true; }
            if (isScramble(a.substring(0, i), b.substring(n - i))
             && isScramble(a.substring(i),   b.substring(0, n - i))) { memo.put(key, true); return true; }
        }
        memo.put(key, false);
        return false;
    }

    // 5. LC 664 — Strange Printer (print one contiguous run at a time)
    //    dp[i][j] = min turns for s[i..j].
    //    If s[i] == s[k] for some k in (i, j], we can "extend" the i-th run to k,
    //    saving 1 step: dp[i][j] = min(dp[i][k-1] + dp[k+1][j], …).
    static int strangePrinter(String s) {
        int n = s.length();
        int[][] dp = new int[n][n];
        for (int i = 0; i < n; i++) dp[i][i] = 1;
        for (int len = 2; len <= n; len++)
            for (int i = 0; i + len <= n; i++) {
                int j = i + len - 1;
                dp[i][j] = dp[i + 1][j] + 1;
                for (int k = i + 1; k <= j; k++)
                    if (s.charAt(i) == s.charAt(k))
                        dp[i][j] = Math.min(dp[i][j],
                                dp[i + 1][k - 1] + (k + 1 <= j ? dp[k + 1][j] : 0));
            }
        return dp[0][n - 1];
    }

    // 6. Matrix Chain Multiplication — textbook interval DP
    //    Given dimensions p[0..n], min scalar multiplications to multiply A1·A2·…·An.
    static int matrixChainOrder(int[] p) {
        int n = p.length - 1;
        int[][] dp = new int[n + 1][n + 1];
        for (int len = 2; len <= n; len++)
            for (int i = 1; i + len - 1 <= n; i++) {
                int j = i + len - 1;
                dp[i][j] = Integer.MAX_VALUE;
                for (int k = i; k < j; k++)
                    dp[i][j] = Math.min(dp[i][j], dp[i][k] + dp[k + 1][j] + p[i - 1] * p[k] * p[j]);
            }
        return dp[1][n];
    }

    public static void main(String[] args) {
        System.out.println("maxCoins [3,1,5,8]      = " + maxCoins(new int[]{3, 1, 5, 8}));                    // 167
        System.out.println("mergeStones [3,2,4,1],2 = " + mergeStones(new int[]{3, 2, 4, 1}, 2));               // 20
        System.out.println("minCostCutStick 7,[1,3,4,5]= " + minCostCutStick(7, new int[]{1, 3, 4, 5}));         // 16
        System.out.println("isScramble great/rgeat  = " + isScramble("great", "rgeat"));                          // true
        System.out.println("strangePrinter (aaabbb) = " + strangePrinter("aaabbb"));                              // 2
        System.out.println("matrixChainOrder        = " + matrixChainOrder(new int[]{10, 30, 5, 60}));            // 4500
    }

    /*
     * PRACTICE SET
     *   • LC 95    Unique BST II                        (enumerate subtrees over range)
     *   • LC 96    Unique BST count (Catalan numbers)
     *   • LC 375   Guess Number Higher or Lower II
     *   • LC 546   Remove Boxes                          (interval DP w/ extra dim)
     *   • LC 730   Count Different Palindromic Subsequences
     *   • LC 813   Largest Sum of Averages              (split DP)
     *   • LC 1043  Partition Array for Maximum Sum
     *   • LC 1444  Number of Ways of Cutting a Pizza
     */
}
