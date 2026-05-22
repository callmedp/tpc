package dsa;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 15 — TOPOLOGICAL SORT                                            │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * WHEN TO RECOGNIZE
 *   • "Find an order to complete tasks given prerequisites"
 *   • "Build order", "compile order", "alien dictionary"
 *   • Anything that boils down to ORDERING a DAG so that every edge u → v
 *     puts u BEFORE v.
 *
 * IF THE GRAPH HAS A CYCLE, no topological order exists.
 *
 * THE TWO ALGORITHMS YOU SHOULD KNOW
 *
 *   1. KAHN's BFS  — repeatedly remove a node whose in-degree is 0.
 *        • Compute in-degree of each node.
 *        • Push all 0-in-degree nodes into a queue.
 *        • Pop one, append to result, decrement in-degree of every neighbour;
 *          if it becomes 0, push it too.
 *        • If at the end you didn't emit all N nodes ⇒ CYCLE.
 *
 *   2. DFS POST-ORDER — DFS each node, push to a stack AFTER all its descendants
 *      are processed. Reverse the stack for the order. Use 3-colour to detect cycles.
 *
 *   Pick Kahn for code simplicity and cycle detection in one go.
 *
 * Worked problems in this file:
 *   1. LC 207  Course Schedule              (boolean — can we finish?)
 *   2. LC 210  Course Schedule II           (return the order)
 *   3. LC 269  Alien Dictionary             (derive letter order from sorted words)
 *   4. LC 310  Minimum Height Trees         (trim leaves layer by layer — variant)
 *   5. LC 802  Find Eventual Safe States    (reverse topology)
 *   6. LC 1136 Parallel Courses              (Kahn levelled — semester count)
 */
public class Module15_TopologicalSort {

    // ─────────────────────────────────────────────────────────────────────────
    // 1. LC 207 — Course Schedule (boolean)
    // ─────────────────────────────────────────────────────────────────────────
    static boolean canFinish(int n, int[][] prereqs) {
        List<List<Integer>> g = new ArrayList<>();
        int[] indeg = new int[n];
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        for (int[] p : prereqs) { g.get(p[1]).add(p[0]); indeg[p[0]]++; }
        Queue<Integer> q = new ArrayDeque<>();
        for (int i = 0; i < n; i++) if (indeg[i] == 0) q.offer(i);
        int done = 0;
        while (!q.isEmpty()) {
            int u = q.poll(); done++;
            for (int v : g.get(u)) if (--indeg[v] == 0) q.offer(v);
        }
        return done == n;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. LC 210 — Course Schedule II (return order)
    // ─────────────────────────────────────────────────────────────────────────
    static int[] findOrder(int n, int[][] prereqs) {
        List<List<Integer>> g = new ArrayList<>();
        int[] indeg = new int[n];
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        for (int[] p : prereqs) { g.get(p[1]).add(p[0]); indeg[p[0]]++; }
        Queue<Integer> q = new ArrayDeque<>();
        for (int i = 0; i < n; i++) if (indeg[i] == 0) q.offer(i);
        int[] out = new int[n]; int k = 0;
        while (!q.isEmpty()) {
            int u = q.poll();
            out[k++] = u;
            for (int v : g.get(u)) if (--indeg[v] == 0) q.offer(v);
        }
        return k == n ? out : new int[0];
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. LC 269 — Alien Dictionary
    //    From every pair of adjacent words, find the FIRST mismatching char →
    //    that gives an edge in the letter graph. Then topo-sort the letters.
    //    Edge case: "abc" before "ab" is INVALID (return "").
    // ─────────────────────────────────────────────────────────────────────────
    static String alienOrder(String[] words) {
        Map<Character, Set<Character>> g = new HashMap<>();
        Map<Character, Integer> indeg = new HashMap<>();
        for (String w : words) for (char c : w.toCharArray()) {
            g.putIfAbsent(c, new HashSet<>());
            indeg.putIfAbsent(c, 0);
        }
        for (int i = 0; i + 1 < words.length; i++) {
            String a = words[i], b = words[i + 1];
            int len = Math.min(a.length(), b.length());
            boolean found = false;
            for (int k = 0; k < len; k++) {
                if (a.charAt(k) != b.charAt(k)) {
                    if (g.get(a.charAt(k)).add(b.charAt(k))) indeg.merge(b.charAt(k), 1, Integer::sum);
                    found = true; break;
                }
            }
            if (!found && a.length() > b.length()) return "";
        }
        Queue<Character> q = new ArrayDeque<>();
        for (var e : indeg.entrySet()) if (e.getValue() == 0) q.offer(e.getKey());
        StringBuilder sb = new StringBuilder();
        while (!q.isEmpty()) {
            char c = q.poll(); sb.append(c);
            for (char nb : g.get(c)) if (indeg.merge(nb, -1, Integer::sum) == 0) q.offer(nb);
        }
        return sb.length() == indeg.size() ? sb.toString() : "";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. LC 310 — Minimum Height Trees
    //    Trim LEAVES (degree 1) layer by layer; the last 1 or 2 nodes are MHT roots.
    // ─────────────────────────────────────────────────────────────────────────
    static List<Integer> findMinHeightTrees(int n, int[][] edges) {
        if (n == 1) return List.of(0);
        List<Set<Integer>> g = new ArrayList<>();
        for (int i = 0; i < n; i++) g.add(new HashSet<>());
        for (int[] e : edges) { g.get(e[0]).add(e[1]); g.get(e[1]).add(e[0]); }
        List<Integer> leaves = new ArrayList<>();
        for (int i = 0; i < n; i++) if (g.get(i).size() == 1) leaves.add(i);
        int remaining = n;
        while (remaining > 2) {
            remaining -= leaves.size();
            List<Integer> next = new ArrayList<>();
            for (int leaf : leaves) {
                int nb = g.get(leaf).iterator().next();
                g.get(nb).remove(leaf);
                if (g.get(nb).size() == 1) next.add(nb);
            }
            leaves = next;
        }
        return leaves;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. LC 802 — Find Eventual Safe Nodes
    //    A node is "safe" if every path leads to a terminal node.
    //    Reverse the graph; Kahn-topo from terminals — every popped node is safe.
    // ─────────────────────────────────────────────────────────────────────────
    static List<Integer> eventualSafeNodes(int[][] g) {
        int n = g.length;
        List<List<Integer>> rev = new ArrayList<>();
        int[] outDeg = new int[n];
        for (int i = 0; i < n; i++) rev.add(new ArrayList<>());
        for (int u = 0; u < n; u++) {
            outDeg[u] = g[u].length;
            for (int v : g[u]) rev.get(v).add(u);
        }
        Queue<Integer> q = new ArrayDeque<>();
        for (int i = 0; i < n; i++) if (outDeg[i] == 0) q.offer(i);
        boolean[] safe = new boolean[n];
        while (!q.isEmpty()) {
            int u = q.poll(); safe[u] = true;
            for (int p : rev.get(u)) if (--outDeg[p] == 0) q.offer(p);
        }
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < n; i++) if (safe[i]) out.add(i);
        return out;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. LC 1136 — Parallel Courses (Kahn levelled — count semesters needed)
    // ─────────────────────────────────────────────────────────────────────────
    static int minimumSemesters(int n, int[][] relations) {
        List<List<Integer>> g = new ArrayList<>();
        int[] indeg = new int[n + 1];
        for (int i = 0; i <= n; i++) g.add(new ArrayList<>());
        for (int[] r : relations) { g.get(r[0]).add(r[1]); indeg[r[1]]++; }
        Queue<Integer> q = new ArrayDeque<>();
        for (int i = 1; i <= n; i++) if (indeg[i] == 0) q.offer(i);
        int semesters = 0, taken = 0;
        while (!q.isEmpty()) {
            semesters++;
            int size = q.size();
            for (int k = 0; k < size; k++) {
                int u = q.poll(); taken++;
                for (int v : g.get(u)) if (--indeg[v] == 0) q.offer(v);
            }
        }
        return taken == n ? semesters : -1;
    }

    public static void main(String[] args) {
        System.out.println("canFinish(2,[[1,0]])    = " + canFinish(2, new int[][]{{1, 0}}));
        System.out.println("findOrder(4,[[1,0],[2,0],[3,1],[3,2]])= "
                + Arrays.toString(findOrder(4, new int[][]{{1, 0}, {2, 0}, {3, 1}, {3, 2}})));
        System.out.println("alienOrder(wrt,wrf,er,ett,rftt) = "
                + alienOrder(new String[]{"wrt", "wrf", "er", "ett", "rftt"}));
        System.out.println("findMinHeightTrees(4,[[1,0],[1,2],[1,3]]) = "
                + findMinHeightTrees(4, new int[][]{{1, 0}, {1, 2}, {1, 3}}));
        System.out.println("eventualSafeNodes       = " + eventualSafeNodes(new int[][]{
                {1, 2}, {2, 3}, {5}, {0}, {5}, {}, {}}));
        System.out.println("minimumSemesters(3,[[1,3],[2,3]]) = "
                + minimumSemesters(3, new int[][]{{1, 3}, {2, 3}}));
    }

    /*
     * PRACTICE SET
     *   • LC 444   Sequence Reconstruction
     *   • LC 1462  Course Schedule IV
     *   • LC 2050  Parallel Courses III          (Kahn + longest path)
     *   • LC 329   Longest Increasing Path in Matrix  (topo on cells)
     *   • LC 1857  Largest Color Value in DAG     (topo + DP)
     *   • LC 2127  Maximum Employees to Be Invited (functional graph)
     */
}
