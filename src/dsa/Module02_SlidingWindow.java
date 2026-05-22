package dsa;

import java.util.HashMap;
import java.util.Map;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 2 — SLIDING WINDOW                                               │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * WHEN TO RECOGNIZE
 *   • "longest / shortest / max / min CONTIGUOUS sub-array / substring with …"
 *   • A constraint over the window (sum ≥ k, ≤ k distinct chars, all chars
 *     in target, no repeats) that you can MAINTAIN incrementally as the
 *     window slides.
 *
 * TWO FLAVOURS
 *   A. FIXED window of size k
 *        for r in 0..n-1:
 *            add nums[r] to window
 *            if r >= k:  remove nums[r - k]
 *            if r >= k - 1: record answer
 *
 *   B. VARIABLE window — expand right; shrink left while invalid
 *        l = 0
 *        for r in 0..n-1:
 *            add nums[r] to window
 *            while (window invalid):  remove nums[l]; l++
 *            record answer with window [l..r]
 *
 *   "Invalid" is the WHOLE problem. Define it precisely.
 *
 * COMPLEXITY  — O(n) (each index enters & leaves the window at most once).
 *
 * Worked problems in this file:
 *   1. LC 643  Maximum Average Subarray I             (fixed k)
 *   2. LC 567  Permutation in String                  (fixed k, char counts)
 *   3. LC 3    Longest Substring Without Repeats      (variable)
 *   4. LC 209  Minimum Size Subarray Sum              (variable; sum ≥ target)
 *   5. LC 76   Minimum Window Substring               (variable; "have ≥ need")
 *   6. LC 424  Longest Repeating Character Replacement (variable; max-freq trick)
 *   7. LC 1004 Max Consecutive Ones III               (variable; flip ≤ k zeros)
 */
public class Module02_SlidingWindow {

    // ─────────────────────────────────────────────────────────────────────────
    // 1. LC 643 — Maximum Average Subarray I (fixed window)
    // ─────────────────────────────────────────────────────────────────────────
    static double maxAverage(int[] nums, int k) {
        int sum = 0;
        for (int i = 0; i < k; i++) sum += nums[i];
        int best = sum;
        for (int r = k; r < nums.length; r++) {
            sum += nums[r] - nums[r - k];
            best = Math.max(best, sum);
        }
        return best / (double) k;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. LC 567 — Permutation in String (fixed window over s2 of size |s1|)
    //    Maintain two int[26]; window is valid if char counts match.
    // ─────────────────────────────────────────────────────────────────────────
    static boolean checkInclusion(String s1, String s2) {
        if (s1.length() > s2.length()) return false;
        int[] need = new int[26], have = new int[26];
        for (char c : s1.toCharArray()) need[c - 'a']++;
        int k = s1.length();
        for (int r = 0; r < s2.length(); r++) {
            have[s2.charAt(r) - 'a']++;
            if (r >= k) have[s2.charAt(r - k) - 'a']--;
            if (r >= k - 1 && java.util.Arrays.equals(need, have)) return true;
        }
        return false;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. LC 3 — Longest Substring Without Repeating Characters (variable)
    //    Shrink while the new char appears twice.
    // ─────────────────────────────────────────────────────────────────────────
    static int lengthOfLongestSubstring(String s) {
        int[] count = new int[128];
        int best = 0, l = 0;
        for (int r = 0; r < s.length(); r++) {
            count[s.charAt(r)]++;
            while (count[s.charAt(r)] > 1) count[s.charAt(l++)]--;
            best = Math.max(best, r - l + 1);
        }
        return best;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. LC 209 — Minimum Size Subarray Sum (variable; shrink while sum ≥ target)
    // ─────────────────────────────────────────────────────────────────────────
    static int minSubArrayLen(int target, int[] nums) {
        int best = Integer.MAX_VALUE, l = 0, sum = 0;
        for (int r = 0; r < nums.length; r++) {
            sum += nums[r];
            while (sum >= target) {
                best = Math.min(best, r - l + 1);
                sum -= nums[l++];
            }
        }
        return best == Integer.MAX_VALUE ? 0 : best;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. LC 76 — Minimum Window Substring
    //    Track `have` (matched chars) and `need` (required chars). Shrink
    //    while the window still satisfies all requirements.
    // ─────────────────────────────────────────────────────────────────────────
    static String minWindow(String s, String t) {
        Map<Character, Integer> need = new HashMap<>();
        for (char c : t.toCharArray()) need.merge(c, 1, Integer::sum);
        int required = need.size();
        Map<Character, Integer> win = new HashMap<>();
        int have = 0, l = 0, bestL = 0, bestLen = Integer.MAX_VALUE;
        for (int r = 0; r < s.length(); r++) {
            char c = s.charAt(r);
            win.merge(c, 1, Integer::sum);
            if (need.containsKey(c) && win.get(c).intValue() == need.get(c).intValue()) have++;
            while (have == required) {
                if (r - l + 1 < bestLen) { bestLen = r - l + 1; bestL = l; }
                char lc = s.charAt(l);
                win.merge(lc, -1, Integer::sum);
                if (need.containsKey(lc) && win.get(lc) < need.get(lc)) have--;
                l++;
            }
        }
        return bestLen == Integer.MAX_VALUE ? "" : s.substring(bestL, bestL + bestLen);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. LC 424 — Longest Repeating Character Replacement
    //    The window is valid if  windowLen - maxFreq <= k  (≤ k chars to flip).
    //    Trick: we never need to RE-COMPUTE maxFreq when shrinking — keeping a
    //    stale upper bound is fine because the answer never shrinks.
    // ─────────────────────────────────────────────────────────────────────────
    static int characterReplacement(String s, int k) {
        int[] count = new int[26];
        int l = 0, maxFreq = 0, best = 0;
        for (int r = 0; r < s.length(); r++) {
            count[s.charAt(r) - 'A']++;
            maxFreq = Math.max(maxFreq, count[s.charAt(r) - 'A']);
            while (r - l + 1 - maxFreq > k) {
                count[s.charAt(l) - 'A']--;
                l++;
            }
            best = Math.max(best, r - l + 1);
        }
        return best;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. LC 1004 — Max Consecutive Ones III (flip ≤ k zeros)
    //    Window is valid if zeros in window ≤ k.
    // ─────────────────────────────────────────────────────────────────────────
    static int longestOnes(int[] nums, int k) {
        int l = 0, zeros = 0, best = 0;
        for (int r = 0; r < nums.length; r++) {
            if (nums[r] == 0) zeros++;
            while (zeros > k) {
                if (nums[l] == 0) zeros--;
                l++;
            }
            best = Math.max(best, r - l + 1);
        }
        return best;
    }

    public static void main(String[] args) {
        System.out.println("maxAverage([1,12,-5,-6,50,3],4)  = " + maxAverage(new int[]{1, 12, -5, -6, 50, 3}, 4));
        System.out.println("checkInclusion(\"ab\",\"eidbaooo\")  = " + checkInclusion("ab", "eidbaooo"));
        System.out.println("lengthOfLongestSubstring(\"abcabcbb\") = " + lengthOfLongestSubstring("abcabcbb"));
        System.out.println("minSubArrayLen(7,[2,3,1,2,4,3])  = " + minSubArrayLen(7, new int[]{2, 3, 1, 2, 4, 3}));
        System.out.println("minWindow(\"ADOBECODEBANC\",\"ABC\") = " + minWindow("ADOBECODEBANC", "ABC"));
        System.out.println("characterReplacement(\"AABABBA\",1) = " + characterReplacement("AABABBA", 1));
        System.out.println("longestOnes([1,1,1,0,0,0,1,1,1,1,0],2) = " + longestOnes(new int[]{1, 1, 1, 0, 0, 0, 1, 1, 1, 1, 0}, 2));
    }

    /*
     * PRACTICE SET
     *   • LC 438  Find All Anagrams in a String         (fixed window, char counts)
     *   • LC 1456 Max Vowels in Substring of Length K   (fixed)
     *   • LC 2090 K Radius Subarray Averages            (fixed)
     *   • LC 159  Longest Substring with At Most 2 Distinct
     *   • LC 340  Longest Substring with At Most K Distinct
     *   • LC 904  Fruit Into Baskets                    (== ≤ 2 distinct)
     *   • LC 239  Sliding Window Maximum                (monotonic deque — Module 8)
     *   • LC 30   Substring with Concatenation of All Words
     *   • LC 992  Subarrays with K Different Integers   (atMost(K) - atMost(K-1))
     */
}
