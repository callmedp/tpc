package dsa;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Queue;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 11 — BINARY TREE TRAVERSAL & PATTERNS                            │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 *   Almost every binary-tree problem boils down to:
 *      1. DFS  (pre / in / post order)            — recursion
 *      2. BFS  (level order)                       — queue
 *      3. "DFS RETURNS X TO PARENT, COMBINES AT NODE"  — Tree DP (Module 26)
 *
 *   The decision tree:
 *     • Need ROOT-TO-LEAF info?                use top-down DFS w/ parameter
 *     • Need POST-ORDER info (subtree sum)?    bottom-up DFS, return up
 *     • Need LEVEL aggregation (avg / max)?    BFS
 *     • Need to (de)serialise?                 pre-order (DFS) or BFS
 *
 * Worked problems in this file:
 *   1. LC 94/144/145  In/Pre/Post Order Traversal       (recursive + iterative)
 *   2. LC 102  Level Order Traversal                    (BFS)
 *   3. LC 104  Maximum Depth                            (DFS bottom-up)
 *   4. LC 110  Balanced Binary Tree                     (DFS, sentinel -1)
 *   5. LC 226  Invert Binary Tree
 *   6. LC 543  Diameter of Binary Tree                  (post-order + global)
 *   7. LC 236  Lowest Common Ancestor (general tree)
 *   8. LC 297  Serialize / Deserialize Binary Tree      (BFS form)
 */
public class Module11_BinaryTree {

    static class TreeNode {
        int val; TreeNode left, right;
        TreeNode(int v) { val = v; }
        TreeNode(int v, TreeNode l, TreeNode r) { val = v; left = l; right = r; }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. Traversals — recursive (one liner per order)
    // ─────────────────────────────────────────────────────────────────────────
    static void preorder (TreeNode n, List<Integer> out) { if (n == null) return; out.add(n.val); preorder (n.left, out); preorder (n.right, out); }
    static void inorder  (TreeNode n, List<Integer> out) { if (n == null) return; inorder  (n.left, out); out.add(n.val); inorder  (n.right, out); }
    static void postorder(TreeNode n, List<Integer> out) { if (n == null) return; postorder(n.left, out); postorder(n.right, out); out.add(n.val); }

    // Iterative inorder — useful when recursion depth is dangerous
    static List<Integer> inorderIterative(TreeNode root) {
        List<Integer> out = new ArrayList<>();
        Deque<TreeNode> st = new ArrayDeque<>();
        TreeNode cur = root;
        while (cur != null || !st.isEmpty()) {
            while (cur != null) { st.push(cur); cur = cur.left; }
            cur = st.pop();
            out.add(cur.val);
            cur = cur.right;
        }
        return out;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. LC 102 — Level Order Traversal (BFS)
    // ─────────────────────────────────────────────────────────────────────────
    static List<List<Integer>> levelOrder(TreeNode root) {
        List<List<Integer>> out = new ArrayList<>();
        if (root == null) return out;
        Queue<TreeNode> q = new ArrayDeque<>();
        q.offer(root);
        while (!q.isEmpty()) {
            int size = q.size();
            List<Integer> level = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                TreeNode n = q.poll();
                level.add(n.val);
                if (n.left  != null) q.offer(n.left);
                if (n.right != null) q.offer(n.right);
            }
            out.add(level);
        }
        return out;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. LC 104 — Maximum Depth (DFS bottom-up)
    // ─────────────────────────────────────────────────────────────────────────
    static int maxDepth(TreeNode root) {
        if (root == null) return 0;
        return 1 + Math.max(maxDepth(root.left), maxDepth(root.right));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. LC 110 — Balanced Binary Tree
    //    Bottom-up: return -1 as soon as any subtree is unbalanced (short-circuit).
    // ─────────────────────────────────────────────────────────────────────────
    static boolean isBalanced(TreeNode root) { return balancedHeight(root) != -1; }
    private static int balancedHeight(TreeNode n) {
        if (n == null) return 0;
        int L = balancedHeight(n.left);  if (L == -1) return -1;
        int R = balancedHeight(n.right); if (R == -1) return -1;
        if (Math.abs(L - R) > 1) return -1;
        return 1 + Math.max(L, R);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. LC 226 — Invert Binary Tree
    // ─────────────────────────────────────────────────────────────────────────
    static TreeNode invert(TreeNode n) {
        if (n == null) return null;
        TreeNode L = invert(n.left), R = invert(n.right);
        n.left = R; n.right = L;
        return n;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. LC 543 — Diameter of Binary Tree
    //    Post-order returns HEIGHT; we update a global with max(L + R).
    // ─────────────────────────────────────────────────────────────────────────
    static int diameter(TreeNode root) {
        int[] best = new int[1];
        diaH(root, best);
        return best[0];
    }
    private static int diaH(TreeNode n, int[] best) {
        if (n == null) return 0;
        int L = diaH(n.left, best);
        int R = diaH(n.right, best);
        best[0] = Math.max(best[0], L + R);
        return 1 + Math.max(L, R);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. LC 236 — Lowest Common Ancestor (assume p,q exist in tree)
    //    If one of p/q is found in left subtree and the other in right, current node is LCA.
    // ─────────────────────────────────────────────────────────────────────────
    static TreeNode lca(TreeNode root, TreeNode p, TreeNode q) {
        if (root == null || root == p || root == q) return root;
        TreeNode L = lca(root.left, p, q);
        TreeNode R = lca(root.right, p, q);
        if (L != null && R != null) return root;
        return L != null ? L : R;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 8. LC 297 — Serialize / Deserialize Binary Tree (BFS form)
    // ─────────────────────────────────────────────────────────────────────────
    static String serialize(TreeNode root) {
        if (root == null) return "";
        StringBuilder sb = new StringBuilder();
        Queue<TreeNode> q = new java.util.LinkedList<>();   // LinkedList allows null elements
        q.offer(root);
        while (!q.isEmpty()) {
            TreeNode n = q.poll();
            if (n == null) { sb.append("#,"); continue; }
            sb.append(n.val).append(',');
            q.offer(n.left);
            q.offer(n.right);
        }
        return sb.toString();
    }
    static TreeNode deserialize(String s) {
        if (s.isEmpty()) return null;
        String[] tok = s.split(",");
        TreeNode root = new TreeNode(Integer.parseInt(tok[0]));
        Queue<TreeNode> q = new ArrayDeque<>(); q.offer(root);
        int i = 1;
        while (!q.isEmpty() && i < tok.length) {
            TreeNode n = q.poll();
            if (!tok[i].equals("#")) { n.left  = new TreeNode(Integer.parseInt(tok[i])); q.offer(n.left); } i++;
            if (i < tok.length && !tok[i].equals("#")) { n.right = new TreeNode(Integer.parseInt(tok[i])); q.offer(n.right); } i++;
        }
        return root;
    }

    public static void main(String[] args) {
        //        3
        //       / \
        //      9  20
        //         / \
        //        15  7
        TreeNode root = new TreeNode(3,
                new TreeNode(9),
                new TreeNode(20, new TreeNode(15), new TreeNode(7)));

        List<Integer> in = new ArrayList<>(); inorder(root, in);
        System.out.println("inorder         = " + in);
        System.out.println("inorderIter     = " + inorderIterative(root));
        System.out.println("levelOrder      = " + levelOrder(root));
        System.out.println("maxDepth        = " + maxDepth(root));
        System.out.println("isBalanced      = " + isBalanced(root));
        System.out.println("diameter        = " + diameter(root));
        TreeNode inv = invert(root);
        List<Integer> in2 = new ArrayList<>(); inorder(inv, in2);
        System.out.println("inverted in.    = " + in2);

        // serde round-trip
        String s = serialize(root);
        TreeNode round = deserialize(s);
        List<Integer> in3 = new ArrayList<>(); inorder(round, in3);
        System.out.println("serialize       = " + s);
        System.out.println("after round     = " + in3);
    }

    /*
     * PRACTICE SET
     *   • LC 100   Same Tree
     *   • LC 101   Symmetric Tree
     *   • LC 105   Construct Tree from Pre + Inorder
     *   • LC 106   Construct Tree from In + Postorder
     *   • LC 124   Binary Tree Maximum Path Sum         (tree DP — see Module 26)
     *   • LC 199   Binary Tree Right Side View          (BFS take last per level)
     *   • LC 226   Invert Binary Tree
     *   • LC 257   Binary Tree Paths
     *   • LC 437   Path Sum III                         (prefix sum on path)
     *   • LC 572   Subtree of Another Tree
     *   • LC 617   Merge Two Binary Trees
     *   • LC 988   Smallest String Starting From Leaf
     *   • LC 1448  Count Good Nodes in Binary Tree
     */
}
