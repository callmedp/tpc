package dsa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 16 — UNION-FIND / DISJOINT-SET UNION (DSU)                       │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * WHEN TO RECOGNIZE
 *   • You're MERGING groups and asking "are these two in the same group?"
 *   • Edges arrive online and you want connectivity / cycle detection.
 *   • Kruskal's MST.
 *   • "Equations with variables", "redundant connection", "accounts merge".
 *   • Whenever you'd otherwise re-run DFS over and over → switch to DSU.
 *
 * THE TEMPLATE (path compression + union by rank)
 *
 *      int[] parent = new int[n], rank = new int[n];
 *      // init parent[i] = i;
 *
 *      int find(int x) {
 *          if (parent[x] != x) parent[x] = find(parent[x]);
 *          return parent[x];
 *      }
 *      boolean union(int a, int b) {
 *          int ra = find(a), rb = find(b);
 *          if (ra == rb) return false;                       // already same set
 *          if (rank[ra] < rank[rb]) { int t = ra; ra = rb; rb = t; }
 *          parent[rb] = ra;
 *          if (rank[ra] == rank[rb]) rank[ra]++;
 *          return true;
 *      }
 *
 *   Amortised O(α(n)) per op — effectively O(1).
 *
 * Worked problems in this file:
 *   1. LC 547  Number of Provinces
 *   2. LC 200  Number of Islands (DSU variant)
 *   3. LC 684  Redundant Connection
 *   4. LC 990  Satisfiability of Equality Equations
 *   5. LC 721  Accounts Merge
 *   6. LC 305  Number of Islands II (online)
 *   7. LC 1319 Number of Operations to Make Network Connected
 */
public class Module16_UnionFind {

    static class DSU {
        int[] parent, rank;
        int count;                                    // # of disjoint sets
        DSU(int n) {
            parent = new int[n]; rank = new int[n]; count = n;
            for (int i = 0; i < n; i++) parent[i] = i;
        }
        int find(int x) {
            while (parent[x] != x) { parent[x] = parent[parent[x]]; x = parent[x]; }
            return x;
        }
        boolean union(int a, int b) {
            int ra = find(a), rb = find(b);
            if (ra == rb) return false;
            if (rank[ra] < rank[rb]) { int t = ra; ra = rb; rb = t; }
            parent[rb] = ra;
            if (rank[ra] == rank[rb]) rank[ra]++;
            count--;
            return true;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. LC 547 — Number of Provinces
    // ─────────────────────────────────────────────────────────────────────────
    static int findCircleNum(int[][] m) {
        int n = m.length;
        DSU dsu = new DSU(n);
        for (int i = 0; i < n; i++)
            for (int j = i + 1; j < n; j++)
                if (m[i][j] == 1) dsu.union(i, j);
        return dsu.count;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. LC 200 — Number of Islands (DSU)
    //    Index cells as r*C+c. Union with N/W neighbours when both are land.
    // ─────────────────────────────────────────────────────────────────────────
    static int numIslandsDSU(char[][] g) {
        int R = g.length, C = g[0].length;
        DSU dsu = new DSU(R * C);
        int water = 0;
        for (int r = 0; r < R; r++)
            for (int c = 0; c < C; c++) {
                if (g[r][c] != '1') { water++; continue; }
                if (r > 0 && g[r - 1][c] == '1') dsu.union(r * C + c, (r - 1) * C + c);
                if (c > 0 && g[r][c - 1] == '1') dsu.union(r * C + c,  r * C + c - 1);
            }
        return dsu.count - water;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. LC 684 — Redundant Connection
    //    First edge that creates a cycle (its two endpoints are already in the
    //    same set) is the redundant one.
    // ─────────────────────────────────────────────────────────────────────────
    static int[] findRedundantConnection(int[][] edges) {
        DSU dsu = new DSU(edges.length + 1);
        for (int[] e : edges) if (!dsu.union(e[0], e[1])) return e;
        return new int[]{-1, -1};
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. LC 990 — Satisfiability of Equality Equations
    //    Process "==" first (union), then check "!=" against same-set membership.
    // ─────────────────────────────────────────────────────────────────────────
    static boolean equationsPossible(String[] eqs) {
        DSU dsu = new DSU(26);
        for (String e : eqs) if (e.charAt(1) == '=') dsu.union(e.charAt(0) - 'a', e.charAt(3) - 'a');
        for (String e : eqs)
            if (e.charAt(1) == '!' && dsu.find(e.charAt(0) - 'a') == dsu.find(e.charAt(3) - 'a'))
                return false;
        return true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. LC 721 — Accounts Merge
    //    Each email is a node; union all emails inside the same account.
    //    Then group emails by their DSU root and prepend the owner name.
    // ─────────────────────────────────────────────────────────────────────────
    static List<List<String>> accountsMerge(List<List<String>> accounts) {
        Map<String, Integer> emailId = new HashMap<>();
        Map<String, String>  emailOwner = new HashMap<>();
        for (List<String> acc : accounts) {
            String owner = acc.get(0);
            for (int i = 1; i < acc.size(); i++) {
                emailId.putIfAbsent(acc.get(i), emailId.size());
                emailOwner.put(acc.get(i), owner);
            }
        }
        DSU dsu = new DSU(emailId.size());
        for (List<String> acc : accounts) {
            int first = emailId.get(acc.get(1));
            for (int i = 2; i < acc.size(); i++) dsu.union(first, emailId.get(acc.get(i)));
        }
        Map<Integer, List<String>> groups = new HashMap<>();
        for (var e : emailId.entrySet())
            groups.computeIfAbsent(dsu.find(e.getValue()), k -> new ArrayList<>()).add(e.getKey());

        List<List<String>> out = new ArrayList<>();
        for (var g : groups.values()) {
            g.sort(null);
            List<String> entry = new ArrayList<>();
            entry.add(emailOwner.get(g.get(0)));
            entry.addAll(g);
            out.add(entry);
        }
        return out;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. LC 305 — Number of Islands II (online additions)
    //    Each addLand: increment count; for each of 4 neighbours that are land,
    //    try to union — successful unions decrement count.
    // ─────────────────────────────────────────────────────────────────────────
    static List<Integer> numIslands2(int m, int n, int[][] positions) {
        DSU dsu = new DSU(m * n) {{ count = 0; }};   // start with 0 islands
        boolean[][] land = new boolean[m][n];
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        List<Integer> out = new ArrayList<>();
        for (int[] p : positions) {
            int r = p[0], c = p[1];
            if (land[r][c]) { out.add(dsu.count); continue; }
            land[r][c] = true;
            dsu.count++;
            for (int[] d : dirs) {
                int nr = r + d[0], nc = c + d[1];
                if (nr < 0 || nc < 0 || nr >= m || nc >= n || !land[nr][nc]) continue;
                dsu.union(r * n + c, nr * n + nc);
            }
            out.add(dsu.count);
        }
        return out;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. LC 1319 — Number of Operations to Make Network Connected
    //    Need at least (components - 1) extra cables; have edges - usefulUnions.
    // ─────────────────────────────────────────────────────────────────────────
    static int makeConnected(int n, int[][] connections) {
        if (connections.length < n - 1) return -1;
        DSU dsu = new DSU(n);
        for (int[] c : connections) dsu.union(c[0], c[1]);
        return dsu.count - 1;
    }

    public static void main(String[] args) {
        System.out.println("findCircleNum        = " + findCircleNum(new int[][]{{1, 1, 0}, {1, 1, 0}, {0, 0, 1}}));
        System.out.println("numIslandsDSU        = " + numIslandsDSU(new char[][]{
                {'1','1','0','0','0'},
                {'1','1','0','0','0'},
                {'0','0','1','0','0'},
                {'0','0','0','1','1'}}));
        System.out.println("findRedundant        = " + Arrays.toString(
                findRedundantConnection(new int[][]{{1, 2}, {1, 3}, {2, 3}})));
        System.out.println("equationsPossible   = " + equationsPossible(new String[]{"a==b", "b!=a"}));
        System.out.println("makeConnected(6,..) = " + makeConnected(6, new int[][]{{0, 1}, {0, 2}, {0, 3}, {1, 2}}));
        System.out.println("numIslands2(3x3)    = " + numIslands2(3, 3,
                new int[][]{{0, 0}, {0, 1}, {1, 2}, {2, 1}}));
        System.out.println("accountsMerge       = " + accountsMerge(List.of(
                List.of("John", "j1@m.com", "j2@m.com"),
                List.of("John", "j3@m.com"),
                List.of("John", "j1@m.com", "j3@m.com"),
                List.of("Mary", "mary@m.com"))));
    }

    /*
     * PRACTICE SET
     *   • LC 128   Longest Consecutive Sequence  (set-based normally, also DSU)
     *   • LC 261   Graph Valid Tree
     *   • LC 323   Number of Connected Components
     *   • LC 685   Redundant Connection II        (directed variant — trickier)
     *   • LC 765   Couples Holding Hands
     *   • LC 924   Minimize Malware Spread
     *   • LC 947   Most Stones Removed
     *   • LC 952   Largest Component by Common Factor
     *   • LC 1101  The Earliest Moment When Everyone Becomes Friends
     *   • LC 1584  Min Cost to Connect All Points   (DSU + MST)
     *   • LC 1971  Find if Path Exists in Graph
     */
}
