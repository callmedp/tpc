package dsa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 19 — BIT MANIPULATION                                            │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * THE TOOLBOX
 *      n & 1            test lowest bit (parity)
 *      n >> 1           divide by 2 (logical for unsigned use)
 *      n & (n - 1)      turn OFF lowest set bit
 *      n & -n           isolate lowest set bit
 *      n | (1 << i)     set bit i
 *      n & ~(1 << i)    clear bit i
 *      n ^ (1 << i)     toggle bit i
 *      (n >> i) & 1     read bit i
 *      Integer.bitCount(n)              popcount
 *      Integer.numberOfTrailingZeros(n) ctz
 *      Integer.highestOneBit(n)         floor power of 2
 *      Integer.reverse(n)               bit-reverse 32 bits
 *
 * WHY XOR IS INTERVIEWERS' FAVOURITE
 *      x ^ x = 0                a ^ b ^ a = b           (pair cancellation)
 *      x ^ 0 = x                XOR is associative + commutative.
 *
 * Worked problems in this file:
 *   1. LC 136  Single Number                            (XOR cancellation)
 *   2. LC 137  Single Number II                         (bit-by-bit mod 3)
 *   3. LC 260  Single Number III                        (XOR split by bit)
 *   4. LC 191  Number of 1 Bits                         (bitCount or n & (n-1) trick)
 *   5. LC 231  Power of Two                             (n & (n-1) == 0)
 *   6. LC 338  Counting Bits  (dp on n & (n-1))
 *   7. LC 78   Subsets — bitmask enumeration            (2^n masks)
 *   8. LC 371  Sum of Two Integers (no + or -)
 *   9. LC 190  Reverse Bits
 *  10. LC 201  Bitwise AND of Numbers Range             (common prefix)
 */
public class Module19_BitManipulation {

    // 1. LC 136 — single missing pair → XOR everything
    static int singleNumber(int[] nums) {
        int x = 0;
        for (int n : nums) x ^= n;
        return x;
    }

    // 2. LC 137 — every other number appears 3 times. For each bit position, count mod 3.
    static int singleNumberII(int[] nums) {
        int ones = 0, twos = 0;
        for (int n : nums) {
            ones = (ones ^ n) & ~twos;
            twos = (twos ^ n) & ~ones;
        }
        return ones;
    }

    // 3. LC 260 — two singletons among pairs.
    //    XOR-all gives a^b. Pick any set bit; partition by that bit.
    static int[] singleNumberIII(int[] nums) {
        int xor = 0;
        for (int n : nums) xor ^= n;
        int bit = xor & -xor;                 // any one differing bit
        int a = 0, b = 0;
        for (int n : nums) {
            if ((n & bit) == 0) a ^= n;
            else                b ^= n;
        }
        return new int[]{a, b};
    }

    // 4. LC 191 — popcount via n & (n-1)
    static int hammingWeight(int n) {
        int c = 0;
        while (n != 0) { n &= (n - 1); c++; }
        return c;
    }

    // 5. LC 231 — power of two iff exactly one set bit
    static boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }

    // 6. LC 338 — Counting Bits  (dp: bits[i] = bits[i >> 1] + (i & 1))
    static int[] countBits(int n) {
        int[] f = new int[n + 1];
        for (int i = 1; i <= n; i++) f[i] = f[i >> 1] + (i & 1);
        return f;
    }

    // 7. LC 78 — Subsets via bitmask enumeration  (each mask = a subset)
    static List<List<Integer>> subsetsBitmask(int[] nums) {
        int n = nums.length;
        List<List<Integer>> out = new ArrayList<>();
        for (int mask = 0; mask < (1 << n); mask++) {
            List<Integer> sub = new ArrayList<>();
            for (int i = 0; i < n; i++) if ((mask & (1 << i)) != 0) sub.add(nums[i]);
            out.add(sub);
        }
        return out;
    }

    // 8. LC 371 — Sum without + or -. Use XOR (sum w/o carry) and (a & b) << 1 (carry).
    static int getSum(int a, int b) {
        while (b != 0) {
            int carry = (a & b) << 1;
            a = a ^ b;
            b = carry;
        }
        return a;
    }

    // 9. LC 190 — reverse 32-bit unsigned. We use the manual loop for clarity;
    //    Integer.reverse(n) also works.
    static int reverseBits(int n) {
        int out = 0;
        for (int i = 0; i < 32; i++) {
            out = (out << 1) | (n & 1);
            n >>>= 1;
        }
        return out;
    }

    // 10. LC 201 — AND of all numbers in [m, n] = common left-prefix of m and n.
    static int rangeBitwiseAnd(int m, int n) {
        int shift = 0;
        while (m != n) { m >>= 1; n >>= 1; shift++; }
        return m << shift;
    }

    public static void main(String[] args) {
        System.out.println("singleNumber           = " + singleNumber(new int[]{4, 1, 2, 1, 2}));         // 4
        System.out.println("singleNumberII         = " + singleNumberII(new int[]{2, 2, 3, 2}));          // 3
        System.out.println("singleNumberIII        = " + Arrays.toString(singleNumberIII(new int[]{1, 2, 1, 3, 2, 5}))); // [3,5]
        System.out.println("hammingWeight(11)      = " + hammingWeight(11));                              // 3
        System.out.println("isPowerOfTwo(16)       = " + isPowerOfTwo(16));                                // true
        System.out.println("countBits(5)           = " + Arrays.toString(countBits(5)));                   // [0,1,1,2,1,2]
        System.out.println("subsetsBitmask([1,2])  = " + subsetsBitmask(new int[]{1, 2}));
        System.out.println("getSum(2,3)            = " + getSum(2, 3));                                    // 5
        System.out.println("reverseBits(43261596)  = " + reverseBits(43261596));                            // 964176192
        System.out.println("rangeBitwiseAnd(5,7)   = " + rangeBitwiseAnd(5, 7));                            // 4
    }

    /*
     * PRACTICE SET
     *   • LC 268   Missing Number                       (XOR variant)
     *   • LC 461   Hamming Distance
     *   • LC 477   Total Hamming Distance                (bit-by-bit counting)
     *   • LC 1009  Complement of Base 10 Integer
     *   • LC 762   Prime Number of Set Bits
     *   • LC 1356  Sort Integers by Number of 1 Bits
     *   • LC 2275  Largest Combination With Bitwise AND > 0
     *   • LC 1310  XOR Queries of a Subarray            (prefix XOR)
     *   • LC 1734  Decode XORed Permutation
     *   • Common: GCD via Stein's algorithm using bit shifts.
     */
}
