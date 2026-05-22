package dsa;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 5 — BINARY SEARCH                                                │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * WHEN TO RECOGNIZE
 *   • Input is sorted, OR sorted-rotated.
 *   • You need an O(log n) lookup.
 *   • You can phrase the problem as a MONOTONIC PREDICATE on an integer
 *     ANSWER SPACE  (binary search on answer):
 *         "smallest capacity such that we can finish in D days?"
 *         "smallest speed Koko can eat at and still finish in H hours?"
 *
 * THE CANONICAL TEMPLATE — "find the SMALLEST index where p(x) is true"
 *
 *      int lo = 0, hi = n;            // hi is EXCLUSIVE
 *      while (lo < hi) {
 *          int mid = lo + (hi - lo) / 2;   // overflow-safe
 *          if (p(mid)) hi = mid;            // mid may be the answer; stay
 *          else        lo = mid + 1;
 *      }
 *      // lo == hi  is the answer (or hi if none satisfies)
 *
 * Use the same template for everything by re-phrasing the predicate.
 *
 * Worked problems in this file:
 *   1. LC 704  Binary Search                       (textbook)
 *   2. LC 35   Search Insert Position              (lower_bound)
 *   3. LC 34   Find First and Last Position        (lower_bound twice)
 *   4. LC 33   Search in Rotated Sorted Array      (decide which half is sorted)
 *   5. LC 153  Find Minimum in Rotated Sorted Arr  (compare mid with hi)
 *   6. LC 875  Koko Eating Bananas                 (binary search on answer)
 *   7. LC 1011 Capacity to Ship Packages           (binary search on answer)
 *   8. LC 410  Split Array Largest Sum             (binary search on answer)
 */
public class Module05_BinarySearch {

    // ─────────────────────────────────────────────────────────────────────────
    // 1. LC 704 — Binary Search (textbook)
    // ─────────────────────────────────────────────────────────────────────────
    static int search(int[] a, int target) {
        int lo = 0, hi = a.length;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (a[mid] >= target) hi = mid;
            else                  lo = mid + 1;
        }
        return lo < a.length && a[lo] == target ? lo : -1;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. LC 35 — Search Insert Position  (lower_bound)
    // ─────────────────────────────────────────────────────────────────────────
    static int searchInsert(int[] a, int target) {
        int lo = 0, hi = a.length;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (a[mid] >= target) hi = mid;
            else                  lo = mid + 1;
        }
        return lo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. LC 34 — Find First and Last Position
    //    first = lower_bound(target);  last = lower_bound(target + 1) - 1
    // ─────────────────────────────────────────────────────────────────────────
    static int[] searchRange(int[] a, int target) {
        int first = lowerBound(a, target);
        if (first == a.length || a[first] != target) return new int[]{-1, -1};
        int last  = lowerBound(a, target + 1) - 1;
        return new int[]{first, last};
    }
    private static int lowerBound(int[] a, int t) {
        int lo = 0, hi = a.length;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (a[mid] >= t) hi = mid;
            else             lo = mid + 1;
        }
        return lo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. LC 33 — Search in Rotated Sorted Array
    //    Each step, one half [lo..mid] or [mid..hi] is guaranteed sorted.
    //    Decide which sorted half holds the target.
    // ─────────────────────────────────────────────────────────────────────────
    static int searchRotated(int[] a, int target) {
        int lo = 0, hi = a.length - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            if (a[mid] == target) return mid;
            if (a[lo] <= a[mid]) {                      // left half sorted
                if (target >= a[lo] && target < a[mid]) hi = mid - 1;
                else                                    lo = mid + 1;
            } else {                                    // right half sorted
                if (target > a[mid] && target <= a[hi]) lo = mid + 1;
                else                                    hi = mid - 1;
            }
        }
        return -1;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. LC 153 — Find Minimum in Rotated Sorted Array
    //    The pivot is the smallest element. Compare a[mid] with a[hi].
    // ─────────────────────────────────────────────────────────────────────────
    static int findMin(int[] a) {
        int lo = 0, hi = a.length - 1;
        while (lo < hi) {
            int mid = lo + (hi - lo) / 2;
            if (a[mid] > a[hi]) lo = mid + 1;
            else                hi = mid;
        }
        return a[lo];
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. LC 875 — Koko Eating Bananas (binary search on answer)
    //    Predicate(speed): can finish in ≤ h hours?  Monotonic in speed.
    // ─────────────────────────────────────────────────────────────────────────
    static int minEatingSpeed(int[] piles, int h) {
        int lo = 1, hi = 1;
        for (int p : piles) hi = Math.max(hi, p);
        while (lo < hi) {
            int speed = lo + (hi - lo) / 2;
            long hours = 0;
            for (int p : piles) hours += (p + speed - 1) / speed;   // ceil(p/speed)
            if (hours <= h) hi = speed;
            else            lo = speed + 1;
        }
        return lo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. LC 1011 — Capacity to Ship Packages Within D Days
    //    Predicate(cap): can ship in ≤ D days? Monotonic in cap.
    //    lo = max(weights) — each item must fit; hi = sum(weights).
    // ─────────────────────────────────────────────────────────────────────────
    static int shipWithinDays(int[] w, int days) {
        int lo = 0, hi = 0;
        for (int x : w) { lo = Math.max(lo, x); hi += x; }
        while (lo < hi) {
            int cap = lo + (hi - lo) / 2;
            int d = 1, run = 0;
            for (int x : w) {
                if (run + x > cap) { d++; run = 0; }
                run += x;
            }
            if (d <= days) hi = cap;
            else           lo = cap + 1;
        }
        return lo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 8. LC 410 — Split Array Largest Sum (same pattern as LC 1011)
    // ─────────────────────────────────────────────────────────────────────────
    static int splitArray(int[] nums, int m) {
        int lo = 0, hi = 0;
        for (int x : nums) { lo = Math.max(lo, x); hi += x; }
        while (lo < hi) {
            int cap = lo + (hi - lo) / 2;
            int parts = 1, run = 0;
            for (int x : nums) {
                if (run + x > cap) { parts++; run = 0; }
                run += x;
            }
            if (parts <= m) hi = cap;
            else            lo = cap + 1;
        }
        return lo;
    }

    public static void main(String[] args) {
        System.out.println("search([-1,0,3,5,9,12],9)      = " + search(new int[]{-1, 0, 3, 5, 9, 12}, 9));
        System.out.println("searchInsert([1,3,5,6],5)      = " + searchInsert(new int[]{1, 3, 5, 6}, 5));
        System.out.println("searchRange([5,7,7,8,8,10],8)  = " + java.util.Arrays.toString(searchRange(new int[]{5, 7, 7, 8, 8, 10}, 8)));
        System.out.println("searchRotated([4,5,6,7,0,1,2],0)= " + searchRotated(new int[]{4, 5, 6, 7, 0, 1, 2}, 0));
        System.out.println("findMin([3,4,5,1,2])           = " + findMin(new int[]{3, 4, 5, 1, 2}));
        System.out.println("minEatingSpeed([3,6,7,11],8)   = " + minEatingSpeed(new int[]{3, 6, 7, 11}, 8));
        System.out.println("shipWithinDays([1..10],5)      = " + shipWithinDays(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 5));
        System.out.println("splitArray([7,2,5,10,8],2)     = " + splitArray(new int[]{7, 2, 5, 10, 8}, 2));
    }

    /*
     * PRACTICE SET
     *   • LC 74    Search a 2D Matrix
     *   • LC 240   Search a 2D Matrix II                 (staircase descent)
     *   • LC 162   Find Peak Element
     *   • LC 154   Find Min in Rotated Sorted Array II   (duplicates allowed)
     *   • LC 278   First Bad Version
     *   • LC 540   Single Element in a Sorted Array
     *   • LC 1283  Smallest Divisor Given a Threshold    (binary search on answer)
     *   • LC 1539  Kth Missing Positive Number
     *   • LC 1482  Min Days to Make m Bouquets           (binary search on answer)
     *   • LC 4     Median of Two Sorted Arrays           (advanced binary search)
     */
}
