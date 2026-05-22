package dsa;

import java.util.Arrays;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 25 — BITMASK DP                                                  │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * WHEN TO RECOGNIZE
 *   • n ≤ ~20.  (2^20 ≈ 1e6 states is fine, 2^25 is not.)
 *   • "Visit every node exactly once" / "use each item once".
 *   • "Partition into K groups" — represent assignment via mask.
 *   • "All-subsets" optimisation, Travelling Salesman, SOS-DP.
 *
 * THE TEMPLATE
 *
 *      int[] dp = new int[1 << n];     // OR int[1 << n][n]   for "last visited"
 *      // base cases
 *      for mask in 0..(1<<n)-1:
 *          for i in 0..n-1:
 *              if (mask & (1 << i)) != 0:
 *                  // dp[mask][i] depends on dp[mask ^ (1<<i)][j] for j neighbours
 *
 * Worked problems in this file:
 *   1. LC 698   Partition to K Equal Sum Subsets   (bitmask + canonical DP)
 *   2. LC 526   Beautiful Arrangement               (positions × mask of used numbers)
 *   3. LC 847   Shortest Path Visiting All Nodes    (BFS over (node, mask) states)
 *   4. LC 1125  Smallest Sufficient Team            (mask of required skills)
 *   5. Classic — Held-Karp TSP                       (bitmask DP, O(n²·2ⁿ))
 */
public class Module25_DPBitmask {

    // 1. LC 698 — Partition to K Equal Sum Subsets
    //    dp[mask] = remaining capacity of the CURRENT bucket given items chosen by `mask`,
    //    or -1 if unreachable.  Each transition: add an unused item if it fits.
    //    A bucket completes when remainder == 0; we then move on with a fresh bucket.
    static boolean canPartitionKSubsets(int[] nums, int k) {
        int sum = 0; for (int n : nums) sum += n;
        if (sum % k != 0) return false;
        int target = sum / k;
        Arrays.sort(nums);
        if (nums[nums.length - 1] > target) return false;
        int n = nums.length;
        int[] dp = new int[1 << n];
        Arrays.fill(dp, -1);
        dp[0] = 0;
        for (int mask = 0; mask < (1 << n); mask++) {
            if (dp[mask] < 0) continue;
            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) continue;
                if (dp[mask] + nums[i] > target) continue;
                int next = mask | (1 << i);
                if (dp[next] < 0) dp[next] = (dp[mask] + nums[i]) % target;
            }
        }
        return dp[(1 << n) - 1] == 0;
    }

    // 2. LC 526 — Beautiful Arrangement
    //    dp[mask] = number of ways to fill positions 1..k (k = popcount(mask)) using
    //    numbers indicated by mask.  Place a number `i` at position pos == popcount(mask)+1
    //    if it satisfies the divisibility condition.
    static int countArrangement(int n) {
        int[] dp = new int[1 << n];
        dp[0] = 1;
        for (int mask = 0; mask < (1 << n); mask++) {
            if (dp[mask] == 0) continue;
            int pos = Integer.bitCount(mask) + 1;
            for (int i = 1; i <= n; i++) {
                if ((mask & (1 << (i - 1))) != 0) continue;
                if (pos % i == 0 || i % pos == 0) dp[mask | (1 << (i - 1))] += dp[mask];
            }
        }
        return dp[(1 << n) - 1];
    }

    // 3. LC 847 — Shortest Path Visiting All Nodes (BFS on (node, mask))
    //    Start with one BFS layer per node (mask = bit for that node).
    //    Goal mask = (1 << n) - 1.
    static int shortestPathLength(int[][] graph) {
        int n = graph.length, full = (1 << n) - 1;
        boolean[][] seen = new boolean[n][1 << n];
        java.util.Queue<int[]> q = new java.util.ArrayDeque<>();
        for (int i = 0; i < n; i++) { seen[i][1 << i] = true; q.offer(new int[]{i, 1 << i, 0}); }
        while (!q.isEmpty()) {
            int[] cur = q.poll();
            int u = cur[0], mask = cur[1], d = cur[2];
            if (mask == full) return d;
            for (int v : graph[u]) {
                int nm = mask | (1 << v);
                if (!seen[v][nm]) { seen[v][nm] = true; q.offer(new int[]{v, nm, d + 1}); }
            }
        }
        return 0;
    }

    // 4. LC 1125 — Smallest Sufficient Team
    //    Encode each person's skills as a bitmask. dp[mask] = smallest team to achieve `mask`.
    static int[] smallestSufficientTeam(String[] req, java.util.List<java.util.List<String>> people) {
        java.util.Map<String, Integer> idx = new java.util.HashMap<>();
        for (int i = 0; i < req.length; i++) idx.put(req[i], i);
        int m = req.length, full = (1 << m) - 1;
        int[] pMask = new int[people.size()];
        for (int i = 0; i < people.size(); i++) {
            int b = 0;
            for (String s : people.get(i)) if (idx.containsKey(s)) b |= 1 << idx.get(s);
            pMask[i] = b;
        }
        java.util.List<Integer>[] dp = new java.util.List[1 << m];
        dp[0] = new java.util.ArrayList<>();
        for (int mask = 0; mask <= full; mask++) {
            if (dp[mask] == null) continue;
            for (int i = 0; i < pMask.length; i++) {
                int nm = mask | pMask[i];
                if (nm == mask) continue;
                if (dp[nm] == null || dp[nm].size() > dp[mask].size() + 1) {
                    var team = new java.util.ArrayList<>(dp[mask]);
                    team.add(i);
                    dp[nm] = team;
                }
            }
        }
        int[] out = new int[dp[full].size()];
        for (int i = 0; i < out.length; i++) out[i] = dp[full].get(i);
        return out;
    }

    // 5. Classic Held-Karp TSP (single source 0; min Hamiltonian cycle cost)
    //    dp[mask][i] = min cost to start at 0, visit nodes in `mask`, end at i.
    static int tsp(int[][] d) {
        int n = d.length, full = (1 << n) - 1;
        int[][] dp = new int[1 << n][n];
        int INF = Integer.MAX_VALUE / 2;
        for (int[] row : dp) Arrays.fill(row, INF);
        dp[1][0] = 0;
        for (int mask = 1; mask <= full; mask++) {
            if ((mask & 1) == 0) continue;             // must contain node 0
            for (int u = 0; u < n; u++) {
                if ((mask & (1 << u)) == 0 || dp[mask][u] == INF) continue;
                for (int v = 0; v < n; v++) {
                    if ((mask & (1 << v)) != 0) continue;
                    int nm = mask | (1 << v);
                    dp[nm][v] = Math.min(dp[nm][v], dp[mask][u] + d[u][v]);
                }
            }
        }
        int best = INF;
        for (int i = 1; i < n; i++) best = Math.min(best, dp[full][i] + d[i][0]);
        return best;
    }

    public static void main(String[] args) {
        System.out.println("canPartitionKSubsets [4,3,2,3,5,2,1],4 = " + canPartitionKSubsets(new int[]{4, 3, 2, 3, 5, 2, 1}, 4)); // true
        System.out.println("countArrangement(4)               = " + countArrangement(4));               // 8
        System.out.println("shortestPathLength               = " + shortestPathLength(new int[][]{
                {1, 2, 3}, {0}, {0}, {0}}));                                                            // 4
        System.out.println("smallestSufficientTeam           = " + Arrays.toString(
                smallestSufficientTeam(new String[]{"java", "nodejs", "reactjs"},
                        java.util.List.of(
                                java.util.List.of("java"),
                                java.util.List.of("nodejs"),
                                java.util.List.of("nodejs", "reactjs")))));
        System.out.println("tsp                              = " + tsp(new int[][]{
                {0, 10, 15, 20},
                {10, 0, 35, 25},
                {15, 35, 0, 30},
                {20, 25, 30, 0}}));                                                                      // 80
    }

    /*
     * PRACTICE SET
     *   • LC 691   Stickers to Spell Word                (bitmask over target chars)
     *   • LC 943   Find the Shortest Superstring         (bitmask DP)
     *   • LC 1349  Maximum Students Taking Exam          (bitmask over seats per row)
     *   • LC 1434  Number of Ways to Wear Hats           (40 hats × 10 people)
     *   • LC 1494  Parallel Courses II                   (bitmask of taken courses)
     *   • LC 1723  Find Minimum Time to Finish All Jobs
     *   • LC 1879  Minimum XOR Sum of Two Arrays
     *   • LC 2305  Fair Distribution of Cookies
     */
}
