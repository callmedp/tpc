package dsa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 4 — CYCLIC SORT (index-as-key)                                   │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * WHEN TO RECOGNIZE
 *   • Array contains numbers in a SMALL RANGE (1..n or 0..n).
 *   • You need to find missing / duplicate / first missing positive
 *     in O(n) time and O(1) EXTRA space.
 *   • Hashing would work, but the constraints scream "do it without
 *     using a HashSet".
 *
 * THE IDEA
 *   Repeatedly swap nums[i] into its "rightful slot" (index nums[i]-1
 *   for 1..n, or index nums[i] for 0..n-1) until every element is in
 *   place OR can't be placed (out of range / duplicate). Then scan for
 *   the first slot whose value doesn't match its index → that's the
 *   answer.
 *
 * COMPLEXITY  — O(n) total (each swap fixes one element).
 *
 * Worked problems in this file:
 *   1. LC 268  Missing Number               (range 0..n)
 *   2. LC 448  Find All Numbers Disappeared (range 1..n, can repeat)
 *   3. LC 442  Find All Duplicates          (range 1..n, each at most twice)
 *   4. LC 287  Find the Duplicate Number    (range 1..n with one repeat) — Floyd alt
 *   5. LC 41   First Missing Positive       (range 1..n+1; the classic)
 *   6. LC 645  Set Mismatch                 (one duplicate + one missing)
 */
public class Module04_CyclicSort {

    // ─────────────────────────────────────────────────────────────────────────
    // helper: place nums[i] in its rightful slot. Used by every problem below.
    //   variant A — range 1..n  → slot = nums[i] - 1
    //   variant B — range 0..n  → slot = nums[i]   (skip when nums[i] == n)
    // ─────────────────────────────────────────────────────────────────────────

    // ─────────────────────────────────────────────────────────────────────────
    // 1. LC 268 — Missing Number  (range 0..n, one missing)
    //    XOR trick is also fine; we show the cyclic-sort version for the pattern.
    // ─────────────────────────────────────────────────────────────────────────
    static int missingNumber(int[] nums) {
        int i = 0;
        while (i < nums.length) {
            if (nums[i] < nums.length && nums[i] != i) {
                int j = nums[i];
                int t = nums[i]; nums[i] = nums[j]; nums[j] = t;
            } else i++;
        }
        for (int k = 0; k < nums.length; k++) if (nums[k] != k) return k;
        return nums.length;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. LC 448 — Find All Numbers Disappeared (range 1..n)
    // ─────────────────────────────────────────────────────────────────────────
    static List<Integer> findDisappeared(int[] nums) {
        cyclicSort1ToN(nums);
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < nums.length; i++) if (nums[i] != i + 1) out.add(i + 1);
        return out;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. LC 442 — Find All Duplicates (range 1..n; each appears once or twice)
    // ─────────────────────────────────────────────────────────────────────────
    static List<Integer> findDuplicates(int[] nums) {
        cyclicSort1ToN(nums);
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < nums.length; i++) if (nums[i] != i + 1) out.add(nums[i]);
        return out;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. LC 287 — Find the Duplicate Number
    //    Cyclic sort would mutate the array, which LC forbids. The canonical
    //    O(n)/O(1) read-only solution is FLOYD's TORTOISE-AND-HARE applied to
    //    the function f(i) = nums[i]: a cycle exists at the duplicate.
    // ─────────────────────────────────────────────────────────────────────────
    static int findDuplicate(int[] nums) {
        int slow = nums[0], fast = nums[0];
        do {                                        // phase 1 — find a meeting point
            slow = nums[slow];
            fast = nums[nums[fast]];
        } while (slow != fast);
        slow = nums[0];                             // phase 2 — cycle entrance == duplicate
        while (slow != fast) {
            slow = nums[slow];
            fast = nums[fast];
        }
        return slow;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. LC 41 — First Missing Positive  (CLASSIC interview question)
    //    Answer is in [1, n+1]. Cyclic-sort values in that range; first slot
    //    where nums[i] != i+1 is the answer.
    // ─────────────────────────────────────────────────────────────────────────
    static int firstMissingPositive(int[] nums) {
        int i = 0, n = nums.length;
        while (i < n) {
            int v = nums[i];
            if (v >= 1 && v <= n && nums[v - 1] != v) {
                int t = nums[v - 1]; nums[v - 1] = nums[i]; nums[i] = t;
            } else i++;
        }
        for (int k = 0; k < n; k++) if (nums[k] != k + 1) return k + 1;
        return n + 1;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. LC 645 — Set Mismatch (one duplicate + one missing, range 1..n)
    // ─────────────────────────────────────────────────────────────────────────
    static int[] findErrorNums(int[] nums) {
        cyclicSort1ToN(nums);
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] != i + 1) return new int[]{nums[i], i + 1};
        }
        return new int[]{-1, -1};
    }

    private static void cyclicSort1ToN(int[] nums) {
        int i = 0, n = nums.length;
        while (i < n) {
            int v = nums[i];
            if (v >= 1 && v <= n && nums[v - 1] != v) {
                int t = nums[v - 1]; nums[v - 1] = nums[i]; nums[i] = t;
            } else i++;
        }
    }

    public static void main(String[] args) {
        System.out.println("missingNumber([3,0,1])         = " + missingNumber(new int[]{3, 0, 1}));                 // 2
        System.out.println("findDisappeared               = " + findDisappeared(new int[]{4, 3, 2, 7, 8, 2, 3, 1})); // [5,6]
        System.out.println("findDuplicates                = " + findDuplicates(new int[]{4, 3, 2, 7, 8, 2, 3, 1}));  // [2,3]
        System.out.println("findDuplicate (Floyd)         = " + findDuplicate(new int[]{1, 3, 4, 2, 2}));             // 2
        System.out.println("firstMissingPositive          = " + firstMissingPositive(new int[]{3, 4, -1, 1}));        // 2
        System.out.println("findErrorNums                 = " + Arrays.toString(findErrorNums(new int[]{1, 2, 2, 4}))); // [2,3]
    }

    /*
     * PRACTICE SET
     *   • LC 26   Remove Duplicates (technically two-pointer, but same flavour)
     *   • LC 1539 Kth Missing Positive Number
     *   • LC 765  Couples Holding Hands              (cyclic placement on pairs)
     *   • LC 217  Contains Duplicate                 (HashSet — comparison baseline)
     *   • LC 2154 Keep Multiplying Until ≤ k
     */
}
