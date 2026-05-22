package dsa;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 3 — PREFIX SUM & DIFFERENCE ARRAY                                │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * WHEN TO RECOGNIZE
 *   • Many range-sum queries on an immutable array  → 1-D prefix sum.
 *   • Many sub-matrix sum queries                    → 2-D prefix sum.
 *   • Subarray-sum / subarray-count problems         → prefix sum + HashMap.
 *   • Many range UPDATES, then ONE read              → difference array.
 *
 * THE CORE IDENTITY
 *      sum(i, j) = pre[j + 1] - pre[i]
 *   where pre[k] = nums[0] + nums[1] + … + nums[k-1] and pre[0] = 0.
 *
 * THE "PREFIX SUM + HASHMAP" PATTERN  (LC 560, LC 974, LC 525, LC 523, LC 1248)
 *      We walk the array, maintaining the running prefix sum P.
 *      For each P, the number of valid sub-arrays ending HERE equals the
 *      number of earlier prefixes Q such that  P - Q = target.
 *      → look up freq.get(P - target).
 *      → then do  freq[P]++.
 *
 * Worked problems in this file:
 *   1. LC 303  Range Sum Query — Immutable               (1-D prefix sum)
 *   2. LC 304  Range Sum Query 2D — Immutable            (2-D prefix sum)
 *   3. LC 560  Subarray Sum Equals K                     (prefix + map)
 *   4. LC 974  Subarray Sums Divisible by K              (prefix + map of mod)
 *   5. LC 525  Contiguous Array (equal 0s and 1s)        (prefix + map; -1/+1 trick)
 *   6. LC 1109 Corporate Flight Bookings                 (difference array)
 *   7. LC 1248 Count Number of Nice Subarrays            (atMost trick OR prefix+map)
 */
public class Module03_PrefixSum {

    // ─────────────────────────────────────────────────────────────────────────
    // 1. LC 303 — Range Sum Query (Immutable)
    // ─────────────────────────────────────────────────────────────────────────
    static class NumArray {
        private final int[] pre;
        NumArray(int[] nums) {
            pre = new int[nums.length + 1];
            for (int i = 0; i < nums.length; i++) pre[i + 1] = pre[i] + nums[i];
        }
        int sumRange(int l, int r) { return pre[r + 1] - pre[l]; }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. LC 304 — Range Sum Query 2D
    //    pre[i][j] = sum of rectangle (0,0) — (i-1, j-1)
    //    sum(r1,c1,r2,c2) = pre[r2+1][c2+1] - pre[r1][c2+1] - pre[r2+1][c1] + pre[r1][c1]
    // ─────────────────────────────────────────────────────────────────────────
    static class NumMatrix {
        private final int[][] pre;
        NumMatrix(int[][] m) {
            int R = m.length, C = m[0].length;
            pre = new int[R + 1][C + 1];
            for (int i = 0; i < R; i++)
                for (int j = 0; j < C; j++)
                    pre[i + 1][j + 1] = m[i][j] + pre[i][j + 1] + pre[i + 1][j] - pre[i][j];
        }
        int sumRegion(int r1, int c1, int r2, int c2) {
            return pre[r2 + 1][c2 + 1] - pre[r1][c2 + 1] - pre[r2 + 1][c1] + pre[r1][c1];
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. LC 560 — Subarray Sum Equals K
    //    For each running sum, count earlier prefixes equal to (sum - k).
    // ─────────────────────────────────────────────────────────────────────────
    static int subarraySum(int[] nums, int k) {
        Map<Integer, Integer> freq = new HashMap<>();
        freq.put(0, 1);                       // empty prefix
        int sum = 0, count = 0;
        for (int x : nums) {
            sum += x;
            count += freq.getOrDefault(sum - k, 0);
            freq.merge(sum, 1, Integer::sum);
        }
        return count;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. LC 974 — Subarray Sums Divisible by K
    //    Two prefixes with the same  (sum mod k)  enclose a sub-array divisible by k.
    //    Java's % can be negative → use Math.floorMod.
    // ─────────────────────────────────────────────────────────────────────────
    static int subarraysDivByK(int[] nums, int k) {
        Map<Integer, Integer> freq = new HashMap<>();
        freq.put(0, 1);
        int sum = 0, count = 0;
        for (int x : nums) {
            sum += x;
            int r = Math.floorMod(sum, k);
            count += freq.getOrDefault(r, 0);
            freq.merge(r, 1, Integer::sum);
        }
        return count;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. LC 525 — Contiguous Array
    //    Replace 0 → -1. Then "equal #0s and #1s" ⇔ running sum = 0.
    //    Track first index of each running sum → longest sub-array.
    // ─────────────────────────────────────────────────────────────────────────
    static int findMaxLength(int[] nums) {
        Map<Integer, Integer> firstIdx = new HashMap<>();
        firstIdx.put(0, -1);                  // empty prefix at virtual index -1
        int sum = 0, best = 0;
        for (int i = 0; i < nums.length; i++) {
            sum += nums[i] == 0 ? -1 : 1;
            if (firstIdx.containsKey(sum)) best = Math.max(best, i - firstIdx.get(sum));
            else firstIdx.put(sum, i);
        }
        return best;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. LC 1109 — Corporate Flight Bookings (range updates, one read)
    //    For booking (first, last, seats):  diff[first] += seats; diff[last+1] -= seats.
    //    Final answer = prefix sum of diff.
    // ─────────────────────────────────────────────────────────────────────────
    static int[] corpFlightBookings(int[][] bookings, int n) {
        int[] diff = new int[n + 1];
        for (int[] b : bookings) {
            diff[b[0] - 1] += b[2];
            diff[b[1]]      -= b[2];
        }
        int[] out = new int[n];
        int run = 0;
        for (int i = 0; i < n; i++) { run += diff[i]; out[i] = run; }
        return out;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. LC 1248 — Count Number of Nice Subarrays
    //    "Nice" = exactly k odd numbers. Convert to "prefix count of odds";
    //    answer for each i = freq[oddCount - k] then bump freq[oddCount].
    // ─────────────────────────────────────────────────────────────────────────
    static int numberOfSubarrays(int[] nums, int k) {
        Map<Integer, Integer> freq = new HashMap<>();
        freq.put(0, 1);
        int odd = 0, count = 0;
        for (int x : nums) {
            if ((x & 1) == 1) odd++;
            count += freq.getOrDefault(odd - k, 0);
            freq.merge(odd, 1, Integer::sum);
        }
        return count;
    }

    public static void main(String[] args) {
        NumArray  na  = new NumArray(new int[]{-2, 0, 3, -5, 2, -1});
        NumMatrix nm  = new NumMatrix(new int[][]{{3, 0, 1, 4}, {5, 6, 3, 2}, {1, 2, 0, 1}});
        System.out.println("sumRange(0,2)         = " + na.sumRange(0, 2));       // 1
        System.out.println("sumRegion(0,0,1,2)    = " + nm.sumRegion(0, 0, 1, 2)); // 18
        System.out.println("subarraySum([1,1,1],2)= " + subarraySum(new int[]{1, 1, 1}, 2));         // 2
        System.out.println("subarraysDivByK       = " + subarraysDivByK(new int[]{4, 5, 0, -2, -3, 1}, 5));   // 7
        System.out.println("findMaxLength         = " + findMaxLength(new int[]{0, 1, 0, 0, 1, 1, 0}));        // 6
        System.out.println("corpFlightBookings    = " + Arrays.toString(
                corpFlightBookings(new int[][]{{1, 2, 10}, {2, 3, 20}, {2, 5, 25}}, 5)));            // [10,55,45,25,25]
        System.out.println("numberOfSubarrays     = " + numberOfSubarrays(new int[]{1, 1, 2, 1, 1}, 3));      // 2
    }

    /*
     * PRACTICE SET
     *   • LC 238  Product of Array Except Self     (prefix/suffix products, no division)
     *   • LC 523  Continuous Subarray Sum (mod k, length ≥ 2)
     *   • LC 1248 Count Nice Subarrays              (also via atMost trick)
     *   • LC 363  Max Sum Rectangle ≤ K             (2-D prefix + TreeSet)
     *   • LC 1314 Matrix Block Sum                  (2-D prefix sum)
     *   • LC 370  Range Addition                    (difference array)
     *   • LC 1893 Check if All Integers Covered     (difference array)
     *   • LC 2017 Grid Game                         (prefix + suffix)
     */
}
