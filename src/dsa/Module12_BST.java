package dsa;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 12 — BINARY SEARCH TREE (BST) PATTERNS                            │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 *   THE BST PROPERTY  — for every node n:
 *      all keys in n.left  < n.val < all keys in n.right
 *
 *   IMMEDIATE CONSEQUENCE  — IN-ORDER traversal gives keys in SORTED order.
 *   Most BST problems are really "use the in-order order" in disguise.
 *
 *   FOUR THINGS TO RECOGNIZE
 *     • Search / Insert / Delete                — O(h) using property
 *     • Kth smallest / largest                   — in-order with a counter
 *     • LCA in BST                               — descend based on values
 *     • Validate / Recover BST                   — in-order monotonicity
 *
 * Worked problems in this file:
 *   1. LC 700  Search in a BST
 *   2. LC 701  Insert into a BST
 *   3. LC 450  Delete Node in a BST
 *   4. LC 98   Validate BST
 *   5. LC 230  Kth Smallest Element in a BST
 *   6. LC 235  LCA of a BST
 *   7. LC 108  Convert Sorted Array to BST
 *   8. LC 173  BST Iterator                       (controlled in-order)
 */
public class Module12_BST {

    static class TreeNode {
        int val; TreeNode left, right;
        TreeNode(int v) { val = v; }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. LC 700 — Search in BST
    // ─────────────────────────────────────────────────────────────────────────
    static TreeNode search(TreeNode root, int target) {
        while (root != null && root.val != target) {
            root = target < root.val ? root.left : root.right;
        }
        return root;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. LC 701 — Insert into BST
    // ─────────────────────────────────────────────────────────────────────────
    static TreeNode insert(TreeNode root, int v) {
        if (root == null) return new TreeNode(v);
        if (v < root.val) root.left  = insert(root.left, v);
        else              root.right = insert(root.right, v);
        return root;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. LC 450 — Delete Node in a BST
    //    If two children: replace with INORDER SUCCESSOR (min of right subtree).
    // ─────────────────────────────────────────────────────────────────────────
    static TreeNode delete(TreeNode root, int key) {
        if (root == null) return null;
        if      (key < root.val) root.left  = delete(root.left, key);
        else if (key > root.val) root.right = delete(root.right, key);
        else {
            if (root.left  == null) return root.right;
            if (root.right == null) return root.left;
            TreeNode succ = root.right;
            while (succ.left != null) succ = succ.left;
            root.val = succ.val;
            root.right = delete(root.right, succ.val);
        }
        return root;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. LC 98 — Validate BST
    //    Pass [lo, hi] bounds down; every node must be in its range.
    // ─────────────────────────────────────────────────────────────────────────
    static boolean isValidBST(TreeNode root) { return valid(root, Long.MIN_VALUE, Long.MAX_VALUE); }
    private static boolean valid(TreeNode n, long lo, long hi) {
        if (n == null) return true;
        if (n.val <= lo || n.val >= hi) return false;
        return valid(n.left, lo, n.val) && valid(n.right, n.val, hi);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. LC 230 — Kth Smallest (controlled in-order with a counter)
    // ─────────────────────────────────────────────────────────────────────────
    static int kthSmallest(TreeNode root, int k) {
        Deque<TreeNode> st = new ArrayDeque<>();
        TreeNode cur = root;
        while (cur != null || !st.isEmpty()) {
            while (cur != null) { st.push(cur); cur = cur.left; }
            cur = st.pop();
            if (--k == 0) return cur.val;
            cur = cur.right;
        }
        return -1;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. LC 235 — Lowest Common Ancestor of a BST
    //    Descend until p and q split (one ≤ node ≤ other), or node equals one of them.
    // ─────────────────────────────────────────────────────────────────────────
    static TreeNode lcaBST(TreeNode root, int p, int q) {
        while (root != null) {
            if      (p < root.val && q < root.val) root = root.left;
            else if (p > root.val && q > root.val) root = root.right;
            else return root;
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. LC 108 — Convert Sorted Array to height-balanced BST
    // ─────────────────────────────────────────────────────────────────────────
    static TreeNode sortedArrayToBST(int[] nums) { return build(nums, 0, nums.length - 1); }
    private static TreeNode build(int[] a, int lo, int hi) {
        if (lo > hi) return null;
        int mid = lo + (hi - lo) / 2;
        TreeNode n = new TreeNode(a[mid]);
        n.left  = build(a, lo, mid - 1);
        n.right = build(a, mid + 1, hi);
        return n;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 8. LC 173 — BST Iterator  (next / hasNext, O(h) memory, amortised O(1) next)
    // ─────────────────────────────────────────────────────────────────────────
    static class BSTIterator {
        private final Deque<TreeNode> st = new ArrayDeque<>();
        BSTIterator(TreeNode root) { pushLeft(root); }
        boolean hasNext() { return !st.isEmpty(); }
        int next() {
            TreeNode n = st.pop();
            pushLeft(n.right);
            return n.val;
        }
        private void pushLeft(TreeNode n) {
            while (n != null) { st.push(n); n = n.left; }
        }
    }

    public static void main(String[] args) {
        TreeNode root = null;
        for (int v : new int[]{5, 3, 7, 2, 4, 6, 8}) root = insert(root, v);

        System.out.println("search(4) hit       = " + (search(root, 4) != null));
        System.out.println("isValidBST          = " + isValidBST(root));
        System.out.println("kthSmallest(3)      = " + kthSmallest(root, 3));     // 4
        System.out.println("LCA(2,4)            = " + lcaBST(root, 2, 4).val);   // 3
        System.out.println("LCA(2,8)            = " + lcaBST(root, 2, 8).val);   // 5

        root = delete(root, 3);
        System.out.println("kthSmallest(3) post = " + kthSmallest(root, 3));     // 5 (after deleting 3)

        TreeNode bal = sortedArrayToBST(new int[]{-10, -3, 0, 5, 9});
        System.out.println("sortedToBST root    = " + bal.val);                    // 0

        BSTIterator it = new BSTIterator(bal);
        StringBuilder sb = new StringBuilder("[");
        while (it.hasNext()) { if (sb.length() > 1) sb.append(','); sb.append(it.next()); }
        System.out.println("BST iterator        = " + sb.append(']'));
    }

    /*
     * PRACTICE SET
     *   • LC 99    Recover Binary Search Tree           (in-order finds 2 swapped nodes)
     *   • LC 270   Closest BST Value
     *   • LC 285   Inorder Successor in BST
     *   • LC 333   Largest BST Subtree                   (post-order returns {isBST,size,lo,hi})
     *   • LC 426   Convert BST to Sorted Doubly LL
     *   • LC 538   Convert BST to Greater Tree           (reverse in-order)
     *   • LC 653   Two Sum IV — Input is a BST
     *   • LC 776   Split BST
     *   • LC 938   Range Sum of BST
     *   • LC 1382  Balance a BST
     */
}
