package collections.dsa;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Queue;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 10 — QUEUE & DEQUE (via ArrayDeque)                              │
 * │  Prereq:  Module 9                                                       │
 * │  Goal:    BFS templates, sliding-window MAX, monotonic deque             │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 *   Use ArrayDeque as a Queue and as a Deque.  Faster than LinkedList.
 *   (Don't use the bounded BlockingQueue family for plain DSA — those are
 *   for thread-safe producer-consumer pipelines.)
 *
 *   Queue API on Deque:
 *       offer(e)   — add to tail, returns false if full         (==offerLast)
 *       add(e)     — add to tail, THROWS if full
 *       poll()     — remove head, returns null if empty         (==pollFirst)
 *       remove()   — remove head, THROWS if empty
 *       peek()     — look at head, null if empty                (==peekFirst)
 *
 *   Deque-only (both ends):
 *       offerFirst / offerLast / pollFirst / pollLast / peekFirst / peekLast
 *
 * What you'll learn:
 *   PATTERN 1 — BFS template (graph / tree level-order)
 *   PATTERN 2 — Sliding-window MAXIMUM with monotonic deque (LC 239)
 *   PATTERN 3 — Multi-source BFS (LC 994 Rotting Oranges)
 *   PATTERN 4 — 0-1 BFS (deque, push 0-cost to front, 1-cost to back)
 */
public class Module10_QueueAndDeque {

    public static void main(String[] args) {

        // ─────────────────────────────────────────────────────────────────────
        // 1. Queue basics
        // ─────────────────────────────────────────────────────────────────────
        Queue<Integer> q = new ArrayDeque<>();
        q.offer(1); q.offer(2); q.offer(3);
        q.peek();                 // 1
        q.poll();                 // 1
        q.size();   q.isEmpty();

        // ─────────────────────────────────────────────────────────────────────
        // 2. Deque both-ends
        // ─────────────────────────────────────────────────────────────────────
        Deque<Integer> dq = new ArrayDeque<>();
        dq.offerFirst(2);   dq.offerLast(3);   dq.offerFirst(1);
        // dq = [1, 2, 3]
        dq.peekFirst();     dq.peekLast();
        dq.pollFirst();     dq.pollLast();

        // ─────────────────────────────────────────────────────────────────────
        // 3. PATTERN 1 — BFS template
        //    Use case: shortest path in unweighted graph, level-order tree traversal,
        //              grid problems (LC 200 islands, LC 1091 shortest path in matrix).
        // ─────────────────────────────────────────────────────────────────────
        int[][] grid = {
                {0, 1, 0},
                {0, 1, 0},
                {0, 0, 0}};
        int rows = grid.length, cols = grid[0].length;
        boolean[][] vis = new boolean[rows][cols];
        Queue<int[]> bfs = new ArrayDeque<>();
        bfs.offer(new int[]{0, 0, 0});                          // r, c, distance
        vis[0][0] = true;
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        int target = -1;
        while (!bfs.isEmpty()) {
            int[] cell = bfs.poll();
            int r = cell[0], c = cell[1], d = cell[2];
            if (r == rows - 1 && c == cols - 1) { target = d; break; }
            for (int[] dir : dirs) {
                int nr = r + dir[0], nc = c + dir[1];
                if (nr < 0 || nc < 0 || nr >= rows || nc >= cols) continue;
                if (vis[nr][nc] || grid[nr][nc] == 1) continue;
                vis[nr][nc] = true;
                bfs.offer(new int[]{nr, nc, d + 1});
            }
        }
        System.out.println("shortest path length = " + target);

        // ─────────────────────────────────────────────────────────────────────
        // 4. PATTERN 2 — Sliding-window MAXIMUM with monotonic deque (LC 239)
        //    Deque stores INDICES whose values are STRICTLY DECREASING.
        //      • drop indices that fell out of the window (i - k)
        //      • drop smaller values from the back before adding current
        //      • front is always the window's max
        // ─────────────────────────────────────────────────────────────────────
        int[] nums = {1, 3, -1, -3, 5, 3, 6, 7};
        int k = 3;
        Deque<Integer> mono = new ArrayDeque<>();
        List<Integer> maxima = new ArrayList<>();
        for (int i = 0; i < nums.length; i++) {
            while (!mono.isEmpty() && mono.peekFirst() <= i - k) mono.pollFirst();
            while (!mono.isEmpty() && nums[mono.peekLast()] < nums[i]) mono.pollLast();
            mono.offerLast(i);
            if (i >= k - 1) maxima.add(nums[mono.peekFirst()]);
        }
        System.out.println("slidingMax = " + maxima);

        // ─────────────────────────────────────────────────────────────────────
        // 5. PATTERN 3 — Multi-source BFS (LC 994 Rotting Oranges)
        //    Seed the queue with ALL starting cells. Then run normal BFS.
        // ─────────────────────────────────────────────────────────────────────
        int[][] oranges = {
                {2, 1, 1},
                {1, 1, 0},
                {0, 1, 1}};
        Queue<int[]> rot = new ArrayDeque<>();
        int fresh = 0;
        for (int r = 0; r < oranges.length; r++) {
            for (int c = 0; c < oranges[0].length; c++) {
                if (oranges[r][c] == 2) rot.offer(new int[]{r, c, 0});
                else if (oranges[r][c] == 1) fresh++;
            }
        }
        int minutes = 0;
        while (!rot.isEmpty()) {
            int[] cell = rot.poll();
            int r = cell[0], c = cell[1], t = cell[2];
            minutes = Math.max(minutes, t);
            for (int[] d : dirs) {
                int nr = r + d[0], nc = c + d[1];
                if (nr < 0 || nc < 0 || nr >= oranges.length || nc >= oranges[0].length) continue;
                if (oranges[nr][nc] != 1) continue;
                oranges[nr][nc] = 2;
                fresh--;
                rot.offer(new int[]{nr, nc, t + 1});
            }
        }
        System.out.println("rotting oranges: minutes = " + (fresh == 0 ? minutes : -1));

        // ─────────────────────────────────────────────────────────────────────
        // 6. PATTERN 4 — 0-1 BFS sketch (Deque, push 0-cost front, 1-cost back)
        //    Use case: shortest path in 0/1-weighted graph faster than Dijkstra.
        //    (Real implementation needs a distance[] array; this is the shape.)
        // ─────────────────────────────────────────────────────────────────────
        Deque<int[]> zo = new ArrayDeque<>();
        zo.offerFirst(new int[]{0, 0});       // node 0, dist 0
        // while not empty:
        //   pop front; for each edge (v, w):  if dist improves,
        //   if w == 0  zo.offerFirst(...)  else  zo.offerLast(...)

        // ─────────────────────────────────────────────────────────────────────
        System.out.println("oranges final = " + Arrays.deepToString(oranges));

        // ─────────────────────────────────────────────────────────────────────
        // Practice problems:
        //   • LC 102   Binary Tree Level Order Traversal
        //   • LC 199   Right Side View of Binary Tree
        //   • LC 200   Number of Islands (BFS variant)
        //   • LC 994   Rotting Oranges (multi-source BFS)
        //   • LC 1091  Shortest Path in Binary Matrix
        //   • LC 239   Sliding Window Maximum (monotonic deque)
        //   • LC 542   01 Matrix (multi-source BFS)
        //   • LC 752   Open the Lock (BFS over states)
        // ─────────────────────────────────────────────────────────────────────
    }
}