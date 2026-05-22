package dsa;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 26 — DP ON TREES                                                 │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * IDEA
 *   Tree DP is post-order DFS that returns ONE OR MORE values from each
 *   subtree, which the parent combines.  Many problems return a small
 *   tuple (often an int[] or a custom record).
 *
 * COMMON SHAPE
 *
 *      int[] dfs(TreeNode n) {
 *          if (n == null) return BASE;
 *          int[] L = dfs(n.left);
 *          int[] R = dfs(n.right);
 *          // 1. update GLOBAL answer using L, R, n.val
 *          // 2. return the tuple needed by the PARENT
 *      }
 *
 *   The thing returned to the parent is usually NOT the same as the thing
 *   you ultimately want — keep that distinction clear.
 *
 * Worked problems in this file:
 *   1. LC 124  Binary Tree Maximum Path Sum
 *   2. LC 543  Diameter of Binary Tree
 *   3. LC 337  House Robber III
 *   4. LC 968  Binary Tree Cameras
 *   5. LC 1372 Longest ZigZag Path
 *   6. LC 250  Count Univalued Subtrees
 */
public class Module26_DPTrees {

    static class TreeNode {
        int val; TreeNode left, right;
        TreeNode(int v) { val = v; }
        TreeNode(int v, TreeNode l, TreeNode r) { val = v; left = l; right = r; }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. LC 124 — Binary Tree Maximum Path Sum
    //    Return to parent: best "downward" path starting at n  (= max single side).
    //    Update global with sum that USES n as the join point: L + R + n.val.
    // ─────────────────────────────────────────────────────────────────────────
    static int maxPathSum(TreeNode root) {
        int[] best = {Integer.MIN_VALUE};
        gain(root, best);
        return best[0];
    }
    private static int gain(TreeNode n, int[] best) {
        if (n == null) return 0;
        int L = Math.max(0, gain(n.left, best));   // negative paths are skipped
        int R = Math.max(0, gain(n.right, best));
        best[0] = Math.max(best[0], n.val + L + R);
        return n.val + Math.max(L, R);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. LC 543 — Diameter of Binary Tree   (return = depth; global = L + R)
    // ─────────────────────────────────────────────────────────────────────────
    static int diameterOfBinaryTree(TreeNode root) {
        int[] best = {0};
        depth(root, best);
        return best[0];
    }
    private static int depth(TreeNode n, int[] best) {
        if (n == null) return 0;
        int L = depth(n.left, best);
        int R = depth(n.right, best);
        best[0] = Math.max(best[0], L + R);
        return 1 + Math.max(L, R);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. LC 337 — House Robber III
    //    Return a pair {robThis, skipThis} — parent decides locally.
    //      robThis  = n.val + skipChild.left  + skipChild.right
    //      skipThis = max(...) + max(...)
    // ─────────────────────────────────────────────────────────────────────────
    static int robTree(TreeNode root) {
        int[] r = robHelper(root);
        return Math.max(r[0], r[1]);
    }
    private static int[] robHelper(TreeNode n) {
        if (n == null) return new int[]{0, 0};
        int[] L = robHelper(n.left);
        int[] R = robHelper(n.right);
        int robThis  = n.val + L[1] + R[1];
        int skipThis = Math.max(L[0], L[1]) + Math.max(R[0], R[1]);
        return new int[]{robThis, skipThis};
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. LC 968 — Binary Tree Cameras
    //    States returned per node:
    //       0 = NEEDS coverage  (we have no camera; parent must cover us)
    //       1 = COVERED (a child has a camera that covers us)
    //       2 = HAS a camera
    //    Global counter increments whenever we place a camera.
    // ─────────────────────────────────────────────────────────────────────────
    static int minCameraCover(TreeNode root) {
        int[] cams = {0};
        int s = dfsCam(root, cams);
        return cams[0] + (s == 0 ? 1 : 0);              // if root itself still uncovered → place one
    }
    private static int dfsCam(TreeNode n, int[] cams) {
        if (n == null) return 1;                         // null = covered (nothing to cover)
        int L = dfsCam(n.left, cams);
        int R = dfsCam(n.right, cams);
        if (L == 0 || R == 0) { cams[0]++; return 2; }   // a child needs cover → place camera here
        if (L == 2 || R == 2) return 1;                  // covered by a child's camera
        return 0;                                        // both kids are merely covered → I am uncovered
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. LC 1372 — Longest ZigZag Path
    //    Return {bestGoingLeft, bestGoingRight} from this node.
    //    A "zigzag step" alternates direction.
    // ─────────────────────────────────────────────────────────────────────────
    static int longestZigZag(TreeNode root) {
        int[] best = {0};
        zz(root, best);
        return best[0];
    }
    private static int[] zz(TreeNode n, int[] best) {
        if (n == null) return new int[]{-1, -1};
        int[] L = zz(n.left, best);
        int[] R = zz(n.right, best);
        int goLeft  = L[1] + 1;          // from left child, go right step → zig
        int goRight = R[0] + 1;          // from right child, go left step → zag
        best[0] = Math.max(best[0], Math.max(goLeft, goRight));
        return new int[]{goLeft, goRight};
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. LC 250 — Count Univalued Subtrees
    // ─────────────────────────────────────────────────────────────────────────
    static int countUnivalSubtrees(TreeNode root) {
        int[] count = {0};
        isUnival(root, count);
        return count[0];
    }
    private static boolean isUnival(TreeNode n, int[] count) {
        if (n == null) return true;
        boolean l = isUnival(n.left,  count);
        boolean r = isUnival(n.right, count);
        if (!l || !r) return false;
        if (n.left  != null && n.left.val  != n.val) return false;
        if (n.right != null && n.right.val != n.val) return false;
        count[0]++;
        return true;
    }

    public static void main(String[] args) {
        //          10
        //         /  \
        //        2   10
        //       / \    \
        //      20  1   -25
        //              / \
        //             3   4
        TreeNode root = new TreeNode(10,
                new TreeNode(2, new TreeNode(20), new TreeNode(1)),
                new TreeNode(10, null, new TreeNode(-25, new TreeNode(3), new TreeNode(4))));
        System.out.println("maxPathSum                = " + maxPathSum(root));                   // 42
        System.out.println("diameterOfBinaryTree      = " + diameterOfBinaryTree(root));         // 6
        // rob tree
        TreeNode robTree = new TreeNode(3,
                new TreeNode(2, null, new TreeNode(3)),
                new TreeNode(3, null, new TreeNode(1)));
        System.out.println("robTree                   = " + robTree(robTree));                    // 7
        // cameras
        TreeNode cameras = new TreeNode(0, new TreeNode(0, new TreeNode(0), new TreeNode(0)), null);
        System.out.println("minCameraCover            = " + minCameraCover(cameras));             // 1
        // zigzag
        TreeNode zz = new TreeNode(1,
                null,
                new TreeNode(1,
                        new TreeNode(1, new TreeNode(1), new TreeNode(1, new TreeNode(1), new TreeNode(1))),
                        new TreeNode(1)));
        System.out.println("longestZigZag             = " + longestZigZag(zz));
        // univalued
        TreeNode uv = new TreeNode(5,
                new TreeNode(1, new TreeNode(5), new TreeNode(5)),
                new TreeNode(5, null, new TreeNode(5)));
        System.out.println("countUnivalSubtrees       = " + countUnivalSubtrees(uv));             // 4
    }

    /*
     * PRACTICE SET
     *   • LC 110   Balanced Binary Tree                  (post-order returning -1 on imbalance)
     *   • LC 297   Serialize/Deserialize Tree            (Module 11)
     *   • LC 333   Largest BST Subtree                   (return {isBST,size,lo,hi})
     *   • LC 437   Path Sum III                           (prefix-sum map on DFS path)
     *   • LC 666   Path Sum IV                            (encoded tree)
     *   • LC 834   Sum of Distances in Tree              (rerooting technique)
     *   • LC 979   Distribute Coins in Binary Tree
     *   • LC 1372  Longest ZigZag Path (this file)
     *   • LC 1530  Number of Good Leaf Nodes Pairs
     *   • LC 2477  Minimum Fuel Cost to Report
     */
}
