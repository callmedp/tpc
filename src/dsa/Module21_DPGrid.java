package dsa;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 21 — DP ON 2-D GRIDS                                              │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * SIGNS THAT IT'S A GRID DP
 *   • Input is an M×N grid (matrix of values, booleans, characters).
 *   • Each cell's answer depends on a small number of NEIGHBOURS.
 *   • Classic moves: → (right) and ↓ (down) for path problems;
 *     all 8 neighbours for "maximal square / rectangle".
 *
 * THE TEMPLATE
 *
 *      int[][] dp = new int[R][C];
 *      // initialise edge row / column
 *      for r in 1..R-1:
 *          for c in 1..C-1:
 *              dp[r][c] = f(dp[r-1][c], dp[r][c-1], …)
 *
 *   Space optimisation: most grid DPs can be reduced to O(min(R, C))
 *   by keeping only the previous row.
 *
 * Worked problems in this file:
 *   1. LC 62   Unique Paths                          (basic count, 1-D opt)
 *   2. LC 63   Unique Paths II (with obstacles)
 *   3. LC 64   Minimum Path Sum
 *   4. LC 221  Maximal Square
 *   5. LC 174  Dungeon Game                          (DP from bottom-right)
 *   6. LC 931  Minimum Falling Path Sum
 *   7. LC 120  Triangle (in-place bottom-up)
 */
public class Module21_DPGrid {

    // 1. LC 62 — Unique Paths   dp[r][c] = dp[r-1][c] + dp[r][c-1]
    static int uniquePaths(int m, int n) {
        int[] f = new int[n];
        java.util.Arrays.fill(f, 1);
        for (int r = 1; r < m; r++)
            for (int c = 1; c < n; c++) f[c] += f[c - 1];
        return f[n - 1];
    }

    // 2. LC 63 — Unique Paths II
    static int uniquePathsII(int[][] g) {
        int R = g.length, C = g[0].length;
        int[] f = new int[C];
        f[0] = g[0][0] == 0 ? 1 : 0;
        for (int c = 1; c < C; c++) f[c] = g[0][c] == 0 ? f[c - 1] : 0;
        for (int r = 1; r < R; r++) {
            f[0] = g[r][0] == 0 ? f[0] : 0;
            for (int c = 1; c < C; c++) f[c] = g[r][c] == 0 ? f[c] + f[c - 1] : 0;
        }
        return f[C - 1];
    }

    // 3. LC 64 — Minimum Path Sum
    static int minPathSum(int[][] g) {
        int R = g.length, C = g[0].length;
        int[] f = new int[C];
        f[0] = g[0][0];
        for (int c = 1; c < C; c++) f[c] = f[c - 1] + g[0][c];
        for (int r = 1; r < R; r++) {
            f[0] += g[r][0];
            for (int c = 1; c < C; c++) f[c] = Math.min(f[c], f[c - 1]) + g[r][c];
        }
        return f[C - 1];
    }

    // 4. LC 221 — Maximal Square
    //    dp[r][c] = side length of largest all-1 square with bottom-right corner at (r,c).
    //    = 1 + min(dp[r-1][c-1], dp[r-1][c], dp[r][c-1])  if matrix[r][c]=='1'.
    static int maximalSquare(char[][] m) {
        if (m.length == 0) return 0;
        int R = m.length, C = m[0].length;
        int[][] dp = new int[R + 1][C + 1];
        int best = 0;
        for (int r = 1; r <= R; r++)
            for (int c = 1; c <= C; c++)
                if (m[r - 1][c - 1] == '1') {
                    dp[r][c] = 1 + Math.min(dp[r - 1][c - 1], Math.min(dp[r - 1][c], dp[r][c - 1]));
                    best = Math.max(best, dp[r][c]);
                }
        return best * best;
    }

    // 5. LC 174 — Dungeon Game  (compute MIN HEALTH required entering each cell;
    //    work BACKWARD from goal because we don't know future drops/gains.)
    static int calculateMinimumHP(int[][] d) {
        int R = d.length, C = d[0].length;
        int[][] dp = new int[R + 1][C + 1];
        for (int[] row : dp) java.util.Arrays.fill(row, Integer.MAX_VALUE);
        dp[R][C - 1] = dp[R - 1][C] = 1;
        for (int r = R - 1; r >= 0; r--)
            for (int c = C - 1; c >= 0; c--) {
                int need = Math.min(dp[r + 1][c], dp[r][c + 1]) - d[r][c];
                dp[r][c] = Math.max(1, need);
            }
        return dp[0][0];
    }

    // 6. LC 931 — Minimum Falling Path Sum  (from row above, three neighbours)
    static int minFallingPathSum(int[][] m) {
        int n = m.length;
        int[] prev = m[0].clone();
        for (int r = 1; r < n; r++) {
            int[] cur = new int[n];
            for (int c = 0; c < n; c++) {
                int best = prev[c];
                if (c > 0)     best = Math.min(best, prev[c - 1]);
                if (c < n - 1) best = Math.min(best, prev[c + 1]);
                cur[c] = m[r][c] + best;
            }
            prev = cur;
        }
        int min = Integer.MAX_VALUE;
        for (int v : prev) min = Math.min(min, v);
        return min;
    }

    // 7. LC 120 — Triangle (bottom-up, in-place on bottom row)
    static int minimumTotal(java.util.List<java.util.List<Integer>> tri) {
        int n = tri.size();
        int[] f = new int[n];
        for (int i = 0; i < n; i++) f[i] = tri.get(n - 1).get(i);
        for (int r = n - 2; r >= 0; r--)
            for (int c = 0; c <= r; c++)
                f[c] = tri.get(r).get(c) + Math.min(f[c], f[c + 1]);
        return f[0];
    }

    public static void main(String[] args) {
        System.out.println("uniquePaths(3,7)        = " + uniquePaths(3, 7));                           // 28
        System.out.println("uniquePathsII           = " + uniquePathsII(new int[][]{
                {0, 0, 0}, {0, 1, 0}, {0, 0, 0}}));                                                     // 2
        System.out.println("minPathSum              = " + minPathSum(new int[][]{
                {1, 3, 1}, {1, 5, 1}, {4, 2, 1}}));                                                     // 7
        System.out.println("maximalSquare           = " + maximalSquare(new char[][]{
                {'1', '0', '1', '0', '0'},
                {'1', '0', '1', '1', '1'},
                {'1', '1', '1', '1', '1'},
                {'1', '0', '0', '1', '0'}}));                                                           // 4
        System.out.println("calculateMinimumHP      = " + calculateMinimumHP(new int[][]{
                {-2, -3, 3}, {-5, -10, 1}, {10, 30, -5}}));                                              // 7
        System.out.println("minFallingPathSum       = " + minFallingPathSum(new int[][]{
                {2, 1, 3}, {6, 5, 4}, {7, 8, 9}}));                                                     // 13
        System.out.println("triangleMinimumTotal    = " + minimumTotal(java.util.List.of(
                java.util.List.of(2),
                java.util.List.of(3, 4),
                java.util.List.of(6, 5, 7),
                java.util.List.of(4, 1, 8, 3))));                                                       // 11
    }

    /*
     * PRACTICE SET
     *   • LC 85    Maximal Rectangle                  (build histograms + LC 84)
     *   • LC 304   Range Sum 2D Immutable             (prefix sum — Module 3)
     *   • LC 363   Max Sum Rectangle ≤ k
     *   • LC 542   01 Matrix                          (BFS — Module 14)
     *   • LC 980   Unique Paths III                   (backtracking, count all paths visiting every cell)
     *   • LC 1314  Matrix Block Sum
     *   • LC 1402  Reducing Dishes
     *   • LC 1463  Cherry Pickup II                   (3-D state for two robots)
     */
}
