package dsa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 1 — TWO POINTERS                                                 │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * WHEN TO RECOGNIZE
 *   • Input is a SORTED array (or can be sorted) and you're asked to find a
 *     pair / triplet / sum / closest value.
 *   • Need to operate on both ends of an array converging inward.
 *   • Need to compact / partition an array in O(n) with O(1) extra memory.
 *   • Need to find a cycle in a linked list, or "middle" in one pass.
 *
 * THE THREE VARIANTS YOU MUST KNOW
 *   A. Opposite ends  — left = 0, right = n-1, move toward center
 *                       (sorted-pair, container-water, palindrome check)
 *   B. Same direction — slow ≤ fast; slow advances only when keeping
 *                       (move zeroes, remove duplicates, partition)
 *   C. Fast & slow    — fast = slow.next.next; slow = slow.next
 *                       (linked-list cycle, middle, happy number)
 *
 * COMPLEXITY  — almost always O(n) time, O(1) extra space.
 *
 * Worked problems in this file:
 *   1. LC 167  Two Sum II — Input Array Is Sorted          (variant A)
 *   2. LC 11   Container With Most Water                   (variant A)
 *   3. LC 15   3Sum                                        (variant A + sort)
 *   4. LC 283  Move Zeroes                                 (variant B)
 *   5. LC 26   Remove Duplicates from Sorted Array         (variant B)
 *   6. LC 141  Linked List Cycle                           (variant C)
 *   7. LC 876  Middle of the Linked List                   (variant C)
 */
public class Module01_TwoPointers {

    // ─────────────────────────────────────────────────────────────────────────
    // 1. LC 167 — Two Sum II (sorted)
    //    Move left right inward depending on sum vs target.
    // ─────────────────────────────────────────────────────────────────────────
    static int[] twoSumSorted(int[] nums, int target) {
        int l = 0, r = nums.length - 1;
        while (l < r) {
            int sum = nums[l] + nums[r];
            if (sum == target) return new int[]{l + 1, r + 1};   // 1-indexed per LC
            if (sum < target)  l++;
            else               r--;
        }
        return new int[]{-1, -1};
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. LC 11 — Container With Most Water
    //    Width × min(height). Always move the SHORTER side; it can never improve
    //    by staying put (any other move shrinks width AND height ≤ current min).
    // ─────────────────────────────────────────────────────────────────────────
    static int maxArea(int[] h) {
        int l = 0, r = h.length - 1, best = 0;
        while (l < r) {
            best = Math.max(best, (r - l) * Math.min(h[l], h[r]));
            if (h[l] < h[r]) l++; else r--;
        }
        return best;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. LC 15 — 3Sum
    //    Sort + fix one + two-pointer the rest. Skip duplicates explicitly.
    // ─────────────────────────────────────────────────────────────────────────
    static List<List<Integer>> threeSum(int[] nums) {
        Arrays.sort(nums);
        List<List<Integer>> out = new ArrayList<>();
        for (int i = 0; i < nums.length - 2; i++) {
            if (i > 0 && nums[i] == nums[i - 1]) continue;        // skip dup first
            if (nums[i] > 0) break;                               // early exit
            int l = i + 1, r = nums.length - 1;
            while (l < r) {
                int s = nums[i] + nums[l] + nums[r];
                if (s == 0) {
                    out.add(List.of(nums[i], nums[l], nums[r]));
                    while (l < r && nums[l] == nums[l + 1]) l++;  // skip dup second
                    while (l < r && nums[r] == nums[r - 1]) r--;  // skip dup third
                    l++; r--;
                } else if (s < 0) l++;
                else              r--;
            }
        }
        return out;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. LC 283 — Move Zeroes (in-place)
    //    Slow points to next non-zero slot, fast scans.
    // ─────────────────────────────────────────────────────────────────────────
    static void moveZeroes(int[] a) {
        int slow = 0;
        for (int fast = 0; fast < a.length; fast++) {
            if (a[fast] != 0) {
                int tmp = a[slow]; a[slow] = a[fast]; a[fast] = tmp;
                slow++;
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. LC 26 — Remove Duplicates from Sorted Array
    //    Slow = next slot for a unique value; fast scans.
    // ─────────────────────────────────────────────────────────────────────────
    static int removeDuplicates(int[] a) {
        if (a.length == 0) return 0;
        int slow = 1;
        for (int fast = 1; fast < a.length; fast++) {
            if (a[fast] != a[fast - 1]) a[slow++] = a[fast];
        }
        return slow;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6/7. Linked list — cycle detection + middle (one pass each)
    // ─────────────────────────────────────────────────────────────────────────
    static class ListNode {
        int val; ListNode next;
        ListNode(int v) { val = v; }
    }

    // LC 141 — fast/slow; meet iff cycle exists
    static boolean hasCycle(ListNode head) {
        ListNode slow = head, fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
            if (slow == fast) return true;
        }
        return false;
    }

    // LC 876 — when fast reaches end, slow is at middle (lower middle of two)
    static ListNode middleNode(ListNode head) {
        ListNode slow = head, fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }
        return slow;
    }

    public static void main(String[] args) {
        System.out.println("twoSumSorted([2,7,11,15],9) = " + Arrays.toString(twoSumSorted(new int[]{2, 7, 11, 15}, 9)));
        System.out.println("maxArea([1,8,6,2,5,4,8,3,7]) = " + maxArea(new int[]{1, 8, 6, 2, 5, 4, 8, 3, 7}));
        System.out.println("threeSum([-1,0,1,2,-1,-4])   = " + threeSum(new int[]{-1, 0, 1, 2, -1, -4}));

        int[] z = {0, 1, 0, 3, 12};
        moveZeroes(z);
        System.out.println("moveZeroes                   = " + Arrays.toString(z));

        int[] dup = {1, 1, 2, 2, 3};
        int k = removeDuplicates(dup);
        System.out.println("removeDuplicates             = " + Arrays.toString(Arrays.copyOf(dup, k)));

        // cycle: 1 → 2 → 3 → 2 (cycle back)
        ListNode a = new ListNode(1), b = new ListNode(2), c = new ListNode(3);
        a.next = b; b.next = c; c.next = b;
        System.out.println("hasCycle                     = " + hasCycle(a));

        // middle of 1→2→3→4→5
        ListNode h = new ListNode(1); h.next = new ListNode(2); h.next.next = new ListNode(3);
        h.next.next.next = new ListNode(4); h.next.next.next.next = new ListNode(5);
        System.out.println("middleNode                   = " + middleNode(h).val);
    }

    /*
     * PRACTICE SET
     *   • LC 125  Valid Palindrome
     *   • LC 344  Reverse String
     *   • LC 88   Merge Sorted Array (in place, two pointers from back)
     *   • LC 75   Sort Colors (Dutch National Flag — three pointers)
     *   • LC 16   3Sum Closest
     *   • LC 18   4Sum
     *   • LC 42   Trapping Rain Water (two pointers OR monotonic stack)
     *   • LC 142  Linked List Cycle II (find start of cycle)
     *   • LC 234  Palindrome Linked List
     *   • LC 287  Find the Duplicate Number (Floyd's tortoise & hare on array)
     */
}
