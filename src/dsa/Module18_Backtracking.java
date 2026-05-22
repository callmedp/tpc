package dsa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 18 — BACKTRACKING                                                │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * WHEN TO RECOGNIZE
 *   • "Find ALL …" / "Generate all …" / "Count all …"
 *   • Combinatorial: subsets, permutations, combinations, partitions.
 *   • Constraint satisfaction: N-Queens, Sudoku, Word Search.
 *   • Search where pruning is required to avoid TLE.
 *
 * THE TEMPLATE — choose, explore, un-choose
 *
 *      void bt(state, partial):
 *          if isGoal(state):
 *              emit(partial); return
 *          if violates(state): return                  // prune
 *          for choice in choices(state):
 *              if !valid(choice): continue              // prune
 *              apply(choice)
 *              bt(state', partial + choice)
 *              undo(choice)
 *
 *   Two perennial questions:
 *     • Are duplicates allowed? Sort and skip when nums[i]==nums[i-1] && !chosen[i-1].
 *     • Order matters (permutations) vs not (combinations) ⇒ pass `start` index.
 *
 * Worked problems in this file:
 *   1. LC 78   Subsets
 *   2. LC 90   Subsets II (with duplicates)
 *   3. LC 46   Permutations
 *   4. LC 47   Permutations II (with duplicates)
 *   5. LC 39   Combination Sum (reuse allowed)
 *   6. LC 40   Combination Sum II (one-time use, duplicates)
 *   7. LC 22   Generate Parentheses
 *   8. LC 79   Word Search (board DFS)
 *   9. LC 131  Palindrome Partitioning
 *  10. LC 51   N-Queens
 */
public class Module18_Backtracking {

    // ─────────────────────────────────────────────────────────────────────────
    // 1. LC 78 — Subsets
    // ─────────────────────────────────────────────────────────────────────────
    static List<List<Integer>> subsets(int[] nums) {
        List<List<Integer>> out = new ArrayList<>();
        btSubsets(0, nums, new ArrayList<>(), out);
        return out;
    }
    private static void btSubsets(int i, int[] nums, List<Integer> path, List<List<Integer>> out) {
        out.add(new ArrayList<>(path));
        for (int k = i; k < nums.length; k++) {
            path.add(nums[k]);
            btSubsets(k + 1, nums, path, out);
            path.remove(path.size() - 1);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. LC 90 — Subsets II (with duplicates)
    //    Sort. At each level, skip nums[k] when nums[k] == nums[k-1] && k > start.
    // ─────────────────────────────────────────────────────────────────────────
    static List<List<Integer>> subsetsWithDup(int[] nums) {
        Arrays.sort(nums);
        List<List<Integer>> out = new ArrayList<>();
        btSubsetsDup(0, nums, new ArrayList<>(), out);
        return out;
    }
    private static void btSubsetsDup(int i, int[] nums, List<Integer> path, List<List<Integer>> out) {
        out.add(new ArrayList<>(path));
        for (int k = i; k < nums.length; k++) {
            if (k > i && nums[k] == nums[k - 1]) continue;
            path.add(nums[k]);
            btSubsetsDup(k + 1, nums, path, out);
            path.remove(path.size() - 1);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. LC 46 — Permutations
    // ─────────────────────────────────────────────────────────────────────────
    static List<List<Integer>> permute(int[] nums) {
        List<List<Integer>> out = new ArrayList<>();
        btPerm(nums, new boolean[nums.length], new ArrayList<>(), out);
        return out;
    }
    private static void btPerm(int[] nums, boolean[] used, List<Integer> path, List<List<Integer>> out) {
        if (path.size() == nums.length) { out.add(new ArrayList<>(path)); return; }
        for (int i = 0; i < nums.length; i++) {
            if (used[i]) continue;
            used[i] = true; path.add(nums[i]);
            btPerm(nums, used, path, out);
            used[i] = false; path.remove(path.size() - 1);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. LC 47 — Permutations II (with duplicates)
    //    Sort. Skip nums[i] when nums[i] == nums[i-1] && !used[i-1].
    // ─────────────────────────────────────────────────────────────────────────
    static List<List<Integer>> permuteUnique(int[] nums) {
        Arrays.sort(nums);
        List<List<Integer>> out = new ArrayList<>();
        btPermU(nums, new boolean[nums.length], new ArrayList<>(), out);
        return out;
    }
    private static void btPermU(int[] nums, boolean[] used, List<Integer> path, List<List<Integer>> out) {
        if (path.size() == nums.length) { out.add(new ArrayList<>(path)); return; }
        for (int i = 0; i < nums.length; i++) {
            if (used[i]) continue;
            if (i > 0 && nums[i] == nums[i - 1] && !used[i - 1]) continue;
            used[i] = true; path.add(nums[i]);
            btPermU(nums, used, path, out);
            used[i] = false; path.remove(path.size() - 1);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. LC 39 — Combination Sum (each candidate may be reused)
    // ─────────────────────────────────────────────────────────────────────────
    static List<List<Integer>> combinationSum(int[] cand, int target) {
        Arrays.sort(cand);
        List<List<Integer>> out = new ArrayList<>();
        btCS(0, cand, target, new ArrayList<>(), out);
        return out;
    }
    private static void btCS(int start, int[] cand, int target, List<Integer> path, List<List<Integer>> out) {
        if (target == 0) { out.add(new ArrayList<>(path)); return; }
        for (int i = start; i < cand.length; i++) {
            if (cand[i] > target) break;
            path.add(cand[i]);
            btCS(i, cand, target - cand[i], path, out);
            path.remove(path.size() - 1);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. LC 40 — Combination Sum II (each used once, candidates may repeat)
    // ─────────────────────────────────────────────────────────────────────────
    static List<List<Integer>> combinationSum2(int[] cand, int target) {
        Arrays.sort(cand);
        List<List<Integer>> out = new ArrayList<>();
        btCS2(0, cand, target, new ArrayList<>(), out);
        return out;
    }
    private static void btCS2(int start, int[] cand, int target, List<Integer> path, List<List<Integer>> out) {
        if (target == 0) { out.add(new ArrayList<>(path)); return; }
        for (int i = start; i < cand.length; i++) {
            if (i > start && cand[i] == cand[i - 1]) continue;
            if (cand[i] > target) break;
            path.add(cand[i]);
            btCS2(i + 1, cand, target - cand[i], path, out);
            path.remove(path.size() - 1);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. LC 22 — Generate Parentheses
    //    Invariants: open ≤ n, close ≤ open.
    // ─────────────────────────────────────────────────────────────────────────
    static List<String> generateParenthesis(int n) {
        List<String> out = new ArrayList<>();
        btParens(0, 0, n, new StringBuilder(), out);
        return out;
    }
    private static void btParens(int open, int close, int n, StringBuilder sb, List<String> out) {
        if (sb.length() == 2 * n) { out.add(sb.toString()); return; }
        if (open < n) { sb.append('('); btParens(open + 1, close, n, sb, out); sb.deleteCharAt(sb.length() - 1); }
        if (close < open) { sb.append(')'); btParens(open, close + 1, n, sb, out); sb.deleteCharAt(sb.length() - 1); }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 8. LC 79 — Word Search (board DFS w/ backtracking)
    // ─────────────────────────────────────────────────────────────────────────
    static boolean exist(char[][] b, String word) {
        for (int r = 0; r < b.length; r++)
            for (int c = 0; c < b[0].length; c++)
                if (dfsWord(b, r, c, word, 0)) return true;
        return false;
    }
    private static boolean dfsWord(char[][] b, int r, int c, String w, int k) {
        if (k == w.length()) return true;
        if (r < 0 || c < 0 || r >= b.length || c >= b[0].length || b[r][c] != w.charAt(k)) return false;
        char saved = b[r][c]; b[r][c] = '#';
        boolean found =
                dfsWord(b, r + 1, c, w, k + 1) || dfsWord(b, r - 1, c, w, k + 1) ||
                dfsWord(b, r, c + 1, w, k + 1) || dfsWord(b, r, c - 1, w, k + 1);
        b[r][c] = saved;
        return found;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 9. LC 131 — Palindrome Partitioning
    // ─────────────────────────────────────────────────────────────────────────
    static List<List<String>> partition(String s) {
        List<List<String>> out = new ArrayList<>();
        btPart(0, s, new ArrayList<>(), out);
        return out;
    }
    private static void btPart(int i, String s, List<String> path, List<List<String>> out) {
        if (i == s.length()) { out.add(new ArrayList<>(path)); return; }
        for (int j = i + 1; j <= s.length(); j++) {
            if (isPal(s, i, j - 1)) {
                path.add(s.substring(i, j));
                btPart(j, s, path, out);
                path.remove(path.size() - 1);
            }
        }
    }
    private static boolean isPal(String s, int l, int r) {
        while (l < r) if (s.charAt(l++) != s.charAt(r--)) return false;
        return true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 10. LC 51 — N-Queens
    //    Row-by-row placement. Track used columns and the two diagonals via sets.
    //    For diagonals: r + c (anti-diag), r - c (diag).
    // ─────────────────────────────────────────────────────────────────────────
    static List<List<String>> solveNQueens(int n) {
        List<List<String>> out = new ArrayList<>();
        boolean[] cols = new boolean[n], d1 = new boolean[2 * n], d2 = new boolean[2 * n];
        int[] queen = new int[n];
        btQ(0, n, queen, cols, d1, d2, out);
        return out;
    }
    private static void btQ(int r, int n, int[] queen, boolean[] cols, boolean[] d1, boolean[] d2, List<List<String>> out) {
        if (r == n) { out.add(render(queen, n)); return; }
        for (int c = 0; c < n; c++) {
            int x = r + c, y = r - c + n;
            if (cols[c] || d1[x] || d2[y]) continue;
            queen[r] = c; cols[c] = d1[x] = d2[y] = true;
            btQ(r + 1, n, queen, cols, d1, d2, out);
            cols[c] = d1[x] = d2[y] = false;
        }
    }
    private static List<String> render(int[] queen, int n) {
        List<String> board = new ArrayList<>();
        for (int r = 0; r < n; r++) {
            char[] row = new char[n];
            Arrays.fill(row, '.');
            row[queen[r]] = 'Q';
            board.add(new String(row));
        }
        return board;
    }

    public static void main(String[] args) {
        System.out.println("subsets([1,2,3])       = " + subsets(new int[]{1, 2, 3}));
        System.out.println("subsetsWithDup([1,2,2])= " + subsetsWithDup(new int[]{1, 2, 2}));
        System.out.println("permute([1,2,3])       = " + permute(new int[]{1, 2, 3}));
        System.out.println("permuteUnique([1,1,2]) = " + permuteUnique(new int[]{1, 1, 2}));
        System.out.println("combinationSum([2,3,6,7],7) = " + combinationSum(new int[]{2, 3, 6, 7}, 7));
        System.out.println("combinationSum2([10,1,2,7,6,1,5],8) = " + combinationSum2(new int[]{10, 1, 2, 7, 6, 1, 5}, 8));
        System.out.println("generateParenthesis(3) = " + generateParenthesis(3));
        char[][] board = {{'A','B','C','E'},{'S','F','C','S'},{'A','D','E','E'}};
        System.out.println("exist(ABCCED)          = " + exist(board, "ABCCED"));
        System.out.println("palindromePartition    = " + partition("aab"));
        System.out.println("solveNQueens(4) count  = " + solveNQueens(4).size());
    }

    /*
     * PRACTICE SET
     *   • LC 17    Letter Combinations of Phone Number
     *   • LC 37    Sudoku Solver
     *   • LC 52    N-Queens II  (count only)
     *   • LC 77    Combinations
     *   • LC 93    Restore IP Addresses
     *   • LC 216   Combination Sum III
     *   • LC 254   Factor Combinations
     *   • LC 282   Expression Add Operators
     *   • LC 320   Generalized Abbreviation
     *   • LC 425   Word Squares
     *   • LC 526   Beautiful Arrangement (bitmask DP — see Module 25)
     *   • LC 698   Partition K Equal Sum Subsets (bitmask DP — see Module 25)
     */
}
