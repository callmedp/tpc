package dsa;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 14 — GRAPH TRAVERSAL (DFS / BFS / Grid as Graph)                  │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 *   "Graph problem in disguise" is the most common interview disguise.
 *   Recognise it whenever you have:
 *     • Cells in a grid with adjacency
 *     • Words connected by edit distance
 *     • Courses / dependencies
 *     • Nodes linked by edges, including implicit edges
 *
 *   DFS  — use recursion (or explicit stack). Natural for connected components,
 *           cycle detection, topological order (Module 15), bridges/articulations.
 *   BFS  — use a queue. Natural for SHORTEST PATH in UNWEIGHTED graphs and for
 *           level-by-level processing (multi-source, layered).
 *
 *   GRID AS GRAPH — neighbours = up/down/left/right, sometimes diagonals.
 *      int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
 *
 * Worked problems in this file:
 *   1. LC 200  Number of Islands                (DFS grid)
 *   2. LC 695  Max Area of Island               (DFS grid, return size)
 *   3. LC 133  Clone Graph                      (BFS + map original→copy)
 *   4. LC 207  Course Schedule (cycle detection — DFS 3-colour OR Kahn's)
 *   5. LC 994  Rotting Oranges                  (multi-source BFS)
 *   6. LC 1091 Shortest Path in Binary Matrix   (BFS with 8 neighbours)
 *   7. LC 547  Number of Provinces              (DFS on adjacency matrix)
 */
public class Module14_GraphTraversal {

    private static final int[][] DIRS4 = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    // ─────────────────────────────────────────────────────────────────────────
    // 1. LC 200 — Number of Islands
    // ─────────────────────────────────────────────────────────────────────────
    static int numIslands(char[][] g) {
        int R = g.length, C = g[0].length, count = 0;
        for (int r = 0; r < R; r++)
            for (int c = 0; c < C; c++)
                if (g[r][c] == '1') { count++; dfsIslands(g, r, c); }
        return count;
    }
    private static void dfsIslands(char[][] g, int r, int c) {
        if (r < 0 || c < 0 || r >= g.length || c >= g[0].length || g[r][c] != '1') return;
        g[r][c] = '0';
        for (int[] d : DIRS4) dfsIslands(g, r + d[0], c + d[1]);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. LC 695 — Max Area of Island
    // ─────────────────────────────────────────────────────────────────────────
    static int maxAreaOfIsland(int[][] g) {
        int best = 0;
        for (int r = 0; r < g.length; r++)
            for (int c = 0; c < g[0].length; c++)
                if (g[r][c] == 1) best = Math.max(best, dfsArea(g, r, c));
        return best;
    }
    private static int dfsArea(int[][] g, int r, int c) {
        if (r < 0 || c < 0 || r >= g.length || c >= g[0].length || g[r][c] != 1) return 0;
        g[r][c] = 0;
        int a = 1;
        for (int[] d : DIRS4) a += dfsArea(g, r + d[0], c + d[1]);
        return a;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. LC 133 — Clone Graph  (BFS, map original→copy)
    // ─────────────────────────────────────────────────────────────────────────
    static class Node {
        int val; List<Node> neighbors = new ArrayList<>();
        Node(int v) { val = v; }
    }
    static Node cloneGraph(Node start) {
        if (start == null) return null;
        Map<Node, Node> map = new HashMap<>();
        Queue<Node> q = new ArrayDeque<>();
        map.put(start, new Node(start.val));
        q.offer(start);
        while (!q.isEmpty()) {
            Node n = q.poll();
            for (Node nb : n.neighbors) {
                if (!map.containsKey(nb)) {
                    map.put(nb, new Node(nb.val));
                    q.offer(nb);
                }
                map.get(n).neighbors.add(map.get(nb));
            }
        }
        return map.get(start);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. LC 207 — Course Schedule  (does a topological order exist? = DAG?)
    //    DFS three-colour:  WHITE=unvisited  GRAY=on stack  BLACK=done.
    //    Cycle ⇔ we revisit a GRAY node.
    // ─────────────────────────────────────────────────────────────────────────
    static boolean canFinish(int n, int[][] prereqs) {
        List<List<Integer>> g = new ArrayList<>();
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        for (int[] p : prereqs) g.get(p[1]).add(p[0]);
        int[] colour = new int[n];                       // 0=W 1=G 2=B
        for (int i = 0; i < n; i++) if (colour[i] == 0 && hasCycle(i, g, colour)) return false;
        return true;
    }
    private static boolean hasCycle(int u, List<List<Integer>> g, int[] colour) {
        colour[u] = 1;
        for (int v : g.get(u)) {
            if (colour[v] == 1) return true;
            if (colour[v] == 0 && hasCycle(v, g, colour)) return true;
        }
        colour[u] = 2;
        return false;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. LC 994 — Rotting Oranges (multi-source BFS)
    //    Seed the queue with ALL initial rotten cells, then BFS layer by layer.
    // ─────────────────────────────────────────────────────────────────────────
    static int orangesRotting(int[][] g) {
        Queue<int[]> q = new ArrayDeque<>();
        int fresh = 0;
        for (int r = 0; r < g.length; r++)
            for (int c = 0; c < g[0].length; c++) {
                if (g[r][c] == 2) q.offer(new int[]{r, c, 0});
                else if (g[r][c] == 1) fresh++;
            }
        int t = 0;
        while (!q.isEmpty()) {
            int[] cell = q.poll();
            int r = cell[0], cc = cell[1]; t = cell[2];
            for (int[] d : DIRS4) {
                int nr = r + d[0], nc = cc + d[1];
                if (nr < 0 || nc < 0 || nr >= g.length || nc >= g[0].length) continue;
                if (g[nr][nc] != 1) continue;
                g[nr][nc] = 2; fresh--;
                q.offer(new int[]{nr, nc, t + 1});
            }
        }
        return fresh == 0 ? t : -1;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. LC 1091 — Shortest Path in Binary Matrix (BFS, 8 directions)
    // ─────────────────────────────────────────────────────────────────────────
    static int shortestPathBinaryMatrix(int[][] g) {
        int n = g.length;
        if (g[0][0] == 1 || g[n - 1][n - 1] == 1) return -1;
        int[][] dirs8 = {{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
        Queue<int[]> q = new ArrayDeque<>();
        q.offer(new int[]{0, 0, 1});
        boolean[][] vis = new boolean[n][n]; vis[0][0] = true;
        while (!q.isEmpty()) {
            int[] cur = q.poll();
            int r = cur[0], c = cur[1], d = cur[2];
            if (r == n - 1 && c == n - 1) return d;
            for (int[] dd : dirs8) {
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
    // 7. LC 547 — Number of Provinces (DFS on adjacency matrix)
    // ─────────────────────────────────────────────────────────────────────────
    static int findCircleNum(int[][] m) {
        int n = m.length, count = 0;
        boolean[] vis = new boolean[n];
        for (int i = 0; i < n; i++)
            if (!vis[i]) { count++; dfsProv(i, m, vis); }
        return count;
    }
    private static void dfsProv(int u, int[][] m, boolean[] vis) {
        vis[u] = true;
        for (int v = 0; v < m.length; v++) if (m[u][v] == 1 && !vis[v]) dfsProv(v, m, vis);
    }

    public static void main(String[] args) {
        char[][] g = {
                {'1','1','0','0','0'},
                {'1','1','0','0','0'},
                {'0','0','1','0','0'},
                {'0','0','0','1','1'}};
        System.out.println("numIslands           = " + numIslands(g));

        System.out.println("maxAreaOfIsland      = " + maxAreaOfIsland(new int[][]{
                {1, 1, 0, 0}, {0, 1, 0, 1}, {1, 0, 1, 0}}));

        System.out.println("canFinish            = " + canFinish(2, new int[][]{{1, 0}}));
        System.out.println("canFinish cycle      = " + canFinish(2, new int[][]{{1, 0}, {0, 1}}));

        System.out.println("orangesRotting       = " + orangesRotting(new int[][]{
                {2, 1, 1}, {1, 1, 0}, {0, 1, 1}}));

        System.out.println("shortestPathBinMat   = " + shortestPathBinaryMatrix(new int[][]{
                {0, 0, 0}, {1, 1, 0}, {1, 1, 0}}));

        System.out.println("findCircleNum        = " + findCircleNum(new int[][]{
                {1, 1, 0}, {1, 1, 0}, {0, 0, 1}}));

        // simple clone graph round-trip
        Node a = new Node(1), b = new Node(2), c = new Node(3), d = new Node(4);
        a.neighbors.add(b); a.neighbors.add(d);
        b.neighbors.add(a); b.neighbors.add(c);
        c.neighbors.add(b); c.neighbors.add(d);
        d.neighbors.add(a); d.neighbors.add(c);
        Node copy = cloneGraph(a);
        Set<Integer> seen = new HashSet<>();
        Queue<Node> q = new ArrayDeque<>(); q.offer(copy); seen.add(copy.val);
        while (!q.isEmpty()) {
            Node n = q.poll();
            for (Node nb : n.neighbors) if (seen.add(nb.val)) q.offer(nb);
        }
        System.out.println("cloneGraph nodes seen= " + seen);
    }

    /*
     * PRACTICE SET
     *   • LC 130   Surrounded Regions               (border-flood DFS)
     *   • LC 286   Walls and Gates                  (multi-source BFS)
     *   • LC 417   Pacific Atlantic Water Flow      (two reverse-BFS)
     *   • LC 463   Island Perimeter
     *   • LC 542   01 Matrix                        (multi-source BFS)
     *   • LC 752   Open the Lock                    (BFS on string states)
     *   • LC 909   Snakes and Ladders               (BFS on integer states)
     *   • LC 815   Bus Routes                       (BFS over routes)
     *   • LC 1162  As Far from Land as Possible
     *   • LC 1306  Jump Game III
     *   • LC 127   Word Ladder                       (BFS on word graph)
     */
}
