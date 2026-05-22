package dsa;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 17 — SHORTEST PATH (Dijkstra, 0-1 BFS, Bellman-Ford)             │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * PICK THE RIGHT TOOL
 *   • Unweighted graph?                         → BFS                  O(V + E)
 *   • Edge weights ∈ {0, 1}?                    → 0-1 BFS (Deque)      O(V + E)
 *   • Non-negative weights?                     → Dijkstra (heap)      O((V+E) log V)
 *   • Negative weights, no negative cycle?      → Bellman-Ford         O(V·E)
 *   • All-pairs, dense, V ≤ ~500?              → Floyd-Warshall       O(V³)
 *   • DAG? non-trivial weights?                 → Topo-order + relax  O(V + E)
 *
 * DIJKSTRA — THE TEMPLATE
 *
 *      dist[] = +∞; dist[src] = 0
 *      PriorityQueue<int[]> pq sorted by dist
 *      pq.offer({src, 0})
 *      while pq not empty:
 *          [u, d] = pq.poll()
 *          if d > dist[u]: continue          // stale entry
 *          for each (v, w) in adj[u]:
 *              if dist[u] + w < dist[v]:
 *                  dist[v] = dist[u] + w
 *                  pq.offer({v, dist[v]})
 *
 *   Lazy deletion: we never `decrease-key`; we just push the new better entry
 *   and skip stale ones on poll. That's why we re-check `d > dist[u]`.
 *
 * Worked problems in this file:
 *   1. LC 743   Network Delay Time            (single-source Dijkstra)
 *   2. LC 787   Cheapest Flights Within K Stops (Bellman-Ford or modified Dijkstra)
 *   3. LC 1631  Path With Minimum Effort      (Dijkstra on grid)
 *   4. LC 1091  Shortest Path in Binary Matrix (0-1 / plain BFS — for contrast)
 *   5. LC 2812  Find the Safest Path           (Dijkstra "max-min")
 *   6. (bonus) Floyd-Warshall in 5 lines
 */
public class Module17_ShortestPath {

    // ─────────────────────────────────────────────────────────────────────────
    // 1. LC 743 — Network Delay Time
    //    times[i] = {u, v, w}. Return time for the message to reach EVERY node.
    // ─────────────────────────────────────────────────────────────────────────
    static int networkDelayTime(int[][] times, int n, int k) {
        List<int[]>[] adj = new List[n + 1];
        for (int i = 1; i <= n; i++) adj[i] = new ArrayList<>();
        for (int[] t : times) adj[t[0]].add(new int[]{t[1], t[2]});
        int[] dist = new int[n + 1];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[k] = 0;
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(x -> x[1]));
        pq.offer(new int[]{k, 0});
        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            int u = cur[0], d = cur[1];
            if (d > dist[u]) continue;
            for (int[] nb : adj[u]) {
                int v = nb[0], w = nb[1];
                if (d + w < dist[v]) { dist[v] = d + w; pq.offer(new int[]{v, dist[v]}); }
            }
        }
        int max = 0;
        for (int i = 1; i <= n; i++) {
            if (dist[i] == Integer.MAX_VALUE) return -1;
            max = Math.max(max, dist[i]);
        }
        return max;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. LC 787 — Cheapest Flights Within K Stops  (Bellman-Ford, k+1 relaxations)
    //    BF does NOT need "negative edges". It uses K+1 limited relaxation steps,
    //    which is exactly what "at most K stops" means.
    // ─────────────────────────────────────────────────────────────────────────
    static int findCheapestPrice(int n, int[][] flights, int src, int dst, int k) {
        int[] dist = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[src] = 0;
        for (int step = 0; step <= k; step++) {
            int[] next = dist.clone();             // critical: relax from "previous" round only
            for (int[] f : flights) {
                if (dist[f[0]] == Integer.MAX_VALUE) continue;
                next[f[1]] = Math.min(next[f[1]], dist[f[0]] + f[2]);
            }
            dist = next;
        }
        return dist[dst] == Integer.MAX_VALUE ? -1 : dist[dst];
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. LC 1631 — Path With Minimum Effort  (Dijkstra on grid; "weight" = abs diff)
    //    Replace dist[u] + w with max(dist[u], |h[u] - h[v]|) — same algorithm shape.
    // ─────────────────────────────────────────────────────────────────────────
    static int minimumEffortPath(int[][] h) {
        int R = h.length, C = h[0].length;
        int[][] dist = new int[R][C];
        for (int[] row : dist) Arrays.fill(row, Integer.MAX_VALUE);
        dist[0][0] = 0;
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(x -> x[2]));
        pq.offer(new int[]{0, 0, 0});
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            int r = cur[0], c = cur[1], d = cur[2];
            if (r == R - 1 && c == C - 1) return d;
            if (d > dist[r][c]) continue;
            for (int[] dd : dirs) {
                int nr = r + dd[0], nc = c + dd[1];
                if (nr < 0 || nc < 0 || nr >= R || nc >= C) continue;
                int diff = Math.max(d, Math.abs(h[r][c] - h[nr][nc]));
                if (diff < dist[nr][nc]) { dist[nr][nc] = diff; pq.offer(new int[]{nr, nc, diff}); }
            }
        }
        return 0;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. LC 1091 — Shortest Path in Binary Matrix (plain BFS for contrast)
    //    Edges all have weight 1. BFS is faster than Dijkstra here.
    // ─────────────────────────────────────────────────────────────────────────
    static int shortestPathBinaryMatrixBFS(int[][] g) {
        int n = g.length;
        if (g[0][0] == 1 || g[n - 1][n - 1] == 1) return -1;
        Queue<int[]> q = new ArrayDeque<>();
        q.offer(new int[]{0, 0, 1});
        boolean[][] vis = new boolean[n][n]; vis[0][0] = true;
        int[][] D = {{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
        while (!q.isEmpty()) {
            int[] cur = q.poll();
            int r = cur[0], c = cur[1], d = cur[2];
            if (r == n - 1 && c == n - 1) return d;
            for (int[] dd : D) {
                int nr = r + dd[0], nc = c + dd[1];
                if (nr < 0 || nc < 0 || nr >= n || nc >= n) continue;
                if (vis[nr][nc] || g[nr][nc] == 1) continue;
                vis[nr][nc] = true;
                q.offer(new int[]{nr, nc, d + 1});
            }
        }
        return -1;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. LC 2812 — Find the Safest Path  (Dijkstra "max over the min on the path")
    //    First multi-source BFS to compute "distance to nearest thief" per cell.
    //    Then Dijkstra where  cost(u→v) = min(score[u], score[v])  — invert ordering.
    // ─────────────────────────────────────────────────────────────────────────
    static int maximumSafenessFactor(List<List<Integer>> grid) {
        int n = grid.size();
        int[][] dist = new int[n][n];
        for (int[] row : dist) Arrays.fill(row, Integer.MAX_VALUE);
        Deque<int[]> q = new ArrayDeque<>();
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++)
                if (grid.get(r).get(c) == 1) { dist[r][c] = 0; q.offer(new int[]{r, c}); }
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!q.isEmpty()) {
            int[] cur = q.poll();
            for (int[] d : dirs) {
                int nr = cur[0] + d[0], nc = cur[1] + d[1];
                if (nr < 0 || nc < 0 || nr >= n || nc >= n) continue;
                if (dist[nr][nc] > dist[cur[0]][cur[1]] + 1) {
                    dist[nr][nc] = dist[cur[0]][cur[1]] + 1;
                    q.offer(new int[]{nr, nc});
                }
            }
        }
        // Dijkstra-like max-heap maximising min score
        int[][] best = new int[n][n];
        for (int[] row : best) Arrays.fill(row, -1);
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> b[2] - a[2]);
        pq.offer(new int[]{0, 0, dist[0][0]});
        best[0][0] = dist[0][0];
        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            int r = cur[0], c = cur[1], s = cur[2];
            if (r == n - 1 && c == n - 1) return s;
            if (s < best[r][c]) continue;
            for (int[] d : dirs) {
                int nr = r + d[0], nc = c + d[1];
                if (nr < 0 || nc < 0 || nr >= n || nc >= n) continue;
                int ns = Math.min(s, dist[nr][nc]);
                if (ns > best[nr][nc]) { best[nr][nc] = ns; pq.offer(new int[]{nr, nc, ns}); }
            }
        }
        return 0;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. (bonus) Floyd-Warshall — all-pairs shortest paths
    //    O(V³). Practical when V ≤ ~500. Easy 5-line implementation.
    // ─────────────────────────────────────────────────────────────────────────
    static int[][] floydWarshall(int n, int[][] edges) {
        int[][] d = new int[n][n];
        int INF = Integer.MAX_VALUE / 2;
        for (int[] row : d) Arrays.fill(row, INF);
        for (int i = 0; i < n; i++) d[i][i] = 0;
        for (int[] e : edges) d[e[0]][e[1]] = Math.min(d[e[0]][e[1]], e[2]);
        for (int k = 0; k < n; k++)
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    d[i][j] = Math.min(d[i][j], d[i][k] + d[k][j]);
        return d;
    }

    public static void main(String[] args) {
        System.out.println("networkDelayTime         = " + networkDelayTime(
                new int[][]{{2, 1, 1}, {2, 3, 1}, {3, 4, 1}}, 4, 2));
        System.out.println("findCheapestPrice        = " + findCheapestPrice(
                3, new int[][]{{0, 1, 100}, {1, 2, 100}, {0, 2, 500}}, 0, 2, 1));
        System.out.println("minimumEffortPath        = " + minimumEffortPath(new int[][]{
                {1, 2, 2}, {3, 8, 2}, {5, 3, 5}}));
        System.out.println("shortestPathBinMatrix    = " + shortestPathBinaryMatrixBFS(new int[][]{
                {0, 0, 0}, {1, 1, 0}, {1, 1, 0}}));
        int[][] all = floydWarshall(3,
                new int[][]{{0, 1, 5}, {0, 2, 9}, {1, 2, 3}});
        System.out.println("floydWarshall[0]         = " + Arrays.toString(all[0]));  // [0,5,8]
    }

    /*
     * PRACTICE SET
     *   • LC 778   Swim in Rising Water           (Dijkstra: max-on-path)
     *   • LC 1334  City With Smallest # Reachable (Floyd-Warshall)
     *   • LC 1928  Min Cost to Reach Destination in Time
     *   • LC 2045  Second Minimum Time            (modified Dijkstra)
     *   • LC 2290  Minimum Obstacle Removal       (0-1 BFS)
     *   • LC 1976  Number of Ways to Arrive       (Dijkstra + counting)
     *   • LC 2642  Design Graph With Shortest Path Calculator
     */
}
