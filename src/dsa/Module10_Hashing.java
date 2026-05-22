package dsa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 10 — HASHING PATTERNS                                            │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * "Can I solve this with a HashMap / HashSet?" should be one of your first
 * thoughts on ANY array / string problem. Most of these problems trade O(n²)
 * for O(n) at the cost of O(n) extra space.
 *
 * FIVE PATTERNS YOU MUST RECOGNIZE
 *   A. SEEN-SET / INDEX LOOKUP        Two Sum, distinct k, longest substring
 *   B. FREQUENCY COUNTING             anagram check, first unique char
 *   C. BUCKET GROUPING                group anagrams, canonical key
 *   D. PREFIX-SUM CACHE               subarray sum = k  (Module 3 also covers)
 *   E. SET MEMBERSHIP O(1)            longest consecutive, contains duplicate
 *
 * Worked problems in this file:
 *   1. LC 1     Two Sum                                  (pattern A)
 *   2. LC 49    Group Anagrams                           (pattern C)
 *   3. LC 242   Valid Anagram                            (pattern B)
 *   4. LC 128   Longest Consecutive Sequence             (pattern E)
 *   5. LC 387   First Unique Character in a String       (pattern B)
 *   6. LC 219   Contains Duplicate II  (k-distance)      (pattern A)
 *   7. LC 36    Valid Sudoku                             (multiple sets)
 *   8. LC 290   Word Pattern                             (bijection — 2 maps)
 */
public class Module10_Hashing {

    // ─────────────────────────────────────────────────────────────────────────
    // 1. LC 1 — Two Sum
    // ─────────────────────────────────────────────────────────────────────────
    static int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> seen = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            Integer j = seen.get(target - nums[i]);
            if (j != null) return new int[]{j, i};
            seen.put(nums[i], i);
        }
        return new int[]{-1, -1};
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. LC 49 — Group Anagrams
    //    Canonical key = sorted chars. Alternative: char-count signature.
    // ─────────────────────────────────────────────────────────────────────────
    static List<List<String>> groupAnagrams(String[] strs) {
        Map<String, List<String>> bucket = new HashMap<>();
        for (String s : strs) {
            char[] c = s.toCharArray(); Arrays.sort(c);
            bucket.computeIfAbsent(new String(c), k -> new ArrayList<>()).add(s);
        }
        return new ArrayList<>(bucket.values());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. LC 242 — Valid Anagram (frequency compare)
    // ─────────────────────────────────────────────────────────────────────────
    static boolean isAnagram(String s, String t) {
        if (s.length() != t.length()) return false;
        int[] cnt = new int[26];
        for (int i = 0; i < s.length(); i++) { cnt[s.charAt(i) - 'a']++; cnt[t.charAt(i) - 'a']--; }
        for (int c : cnt) if (c != 0) return false;
        return true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. LC 128 — Longest Consecutive Sequence (O(n) with a Set)
    //    Only START a streak from a number with no predecessor in the set.
    // ─────────────────────────────────────────────────────────────────────────
    static int longestConsecutive(int[] nums) {
        Set<Integer> set = new HashSet<>();
        for (int n : nums) set.add(n);
        int best = 0;
        for (int n : set) {
            if (!set.contains(n - 1)) {
                int cur = n, len = 1;
                while (set.contains(cur + 1)) { cur++; len++; }
                best = Math.max(best, len);
            }
        }
        return best;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. LC 387 — First Unique Character in a String
    // ─────────────────────────────────────────────────────────────────────────
    static int firstUniqChar(String s) {
        int[] cnt = new int[26];
        for (int i = 0; i < s.length(); i++) cnt[s.charAt(i) - 'a']++;
        for (int i = 0; i < s.length(); i++) if (cnt[s.charAt(i) - 'a'] == 1) return i;
        return -1;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. LC 219 — Contains Duplicate II  (|i - j| ≤ k)
    // ─────────────────────────────────────────────────────────────────────────
    static boolean containsNearbyDuplicate(int[] nums, int k) {
        Map<Integer, Integer> last = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            Integer j = last.get(nums[i]);
            if (j != null && i - j <= k) return true;
            last.put(nums[i], i);
        }
        return false;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. LC 36 — Valid Sudoku (rows / cols / 3×3 boxes — 27 sets)
    // ─────────────────────────────────────────────────────────────────────────
    static boolean isValidSudoku(char[][] b) {
        Set<String> seen = new HashSet<>();
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++) {
                char ch = b[r][c];
                if (ch == '.') continue;
                String row = ch + "r" + r;
                String col = ch + "c" + c;
                String box = ch + "b" + (r / 3) + (c / 3);
                if (!seen.add(row) || !seen.add(col) || !seen.add(box)) return false;
            }
        return true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 8. LC 290 — Word Pattern (bijection between pattern chars and words)
    //    Use two maps, OR a single map with stored words & putIfAbsent index trick.
    // ─────────────────────────────────────────────────────────────────────────
    static boolean wordPattern(String pattern, String s) {
        String[] words = s.split(" ");
        if (words.length != pattern.length()) return false;
        Map<Character, String> p2w = new HashMap<>();
        Map<String, Character> w2p = new HashMap<>();
        for (int i = 0; i < pattern.length(); i++) {
            char p = pattern.charAt(i);
            String w = words[i];
            if (p2w.containsKey(p) && !p2w.get(p).equals(w)) return false;
            if (w2p.containsKey(w) && w2p.get(w) != p)       return false;
            p2w.put(p, w);
            w2p.put(w, p);
        }
        return true;
    }

    public static void main(String[] args) {
        System.out.println("twoSum                 = " + Arrays.toString(twoSum(new int[]{2, 7, 11, 15}, 9)));
        System.out.println("groupAnagrams          = " + groupAnagrams(new String[]{"eat", "tea", "tan", "ate", "nat", "bat"}));
        System.out.println("isAnagram              = " + isAnagram("anagram", "nagaram"));
        System.out.println("longestConsecutive     = " + longestConsecutive(new int[]{100, 4, 200, 1, 3, 2}));
        System.out.println("firstUniqChar          = " + firstUniqChar("leetcode"));
        System.out.println("containsNearbyDup      = " + containsNearbyDuplicate(new int[]{1, 2, 3, 1}, 3));
        char[][] board = {
                {'5','3','.','.','7','.','.','.','.'},
                {'6','.','.','1','9','5','.','.','.'},
                {'.','9','8','.','.','.','.','6','.'},
                {'8','.','.','.','6','.','.','.','3'},
                {'4','.','.','8','.','3','.','.','1'},
                {'7','.','.','.','2','.','.','.','6'},
                {'.','6','.','.','.','.','2','8','.'},
                {'.','.','.','4','1','9','.','.','5'},
                {'.','.','.','.','8','.','.','7','9'}};
        System.out.println("isValidSudoku          = " + isValidSudoku(board));
        System.out.println("wordPattern(abba)      = " + wordPattern("abba", "dog cat cat dog"));
    }

    /*
     * PRACTICE SET
     *   • LC 217   Contains Duplicate
     *   • LC 350   Intersection of Two Arrays II
     *   • LC 454   4Sum II                              (HashMap of pair sums)
     *   • LC 560   Subarray Sum Equals K               (prefix + map)
     *   • LC 1010  Pairs of Songs Sum Divisible by 60
     *   • LC 1396  Design Underground System           (multi-map design)
     *   • LC 1207  Unique Number of Occurrences
     *   • LC 2342  Max Sum of a Pair With Equal Sum of Digits
     */
}
