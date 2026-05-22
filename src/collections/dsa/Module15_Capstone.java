package collections.dsa;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 15 — CAPSTONE: 8 CLASSIC PROBLEMS, ALL TOOLS IN PLAY              │
 * │  Prereq:  Modules 1–14                                                   │
 * │  Goal:    end-to-end practice picking the RIGHT collection per problem   │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 *   This module is the graduation lap. Each problem chooses ONE collection
 *   intentionally — note the pattern → tool mapping in your head.
 *
 *     P1.  Two Sum                              → HashMap        (Module 5, Pattern B)
 *     P2.  Group Anagrams                       → HashMap+List   (Module 5, Pattern C)
 *     P3.  Longest Substring No Repeat          → HashMap window (Modules 4 + 5)
 *     P4.  Kth Largest Element                  → PriorityQueue  (Module 11, Pattern 1)
 *     P5.  Sliding Window Maximum               → ArrayDeque     (Module 10, Pattern 2)
 *     P6.  Valid Parentheses                    → ArrayDeque     (Module 9, Pattern 1)
 *     P7.  My Calendar I                        → TreeMap        (Module 7)
 *     P8.  LRU Cache                            → LinkedHashMap  (Module 8)
 */
public class Module15_Capstone {

    public static void main(String[] args) {

        System.out.println("P1 twoSum            = " + Arrays.toString(twoSum(new int[]{2, 7, 11, 15}, 9)));
        System.out.println("P2 groupAnagrams     = " + groupAnagrams(new String[]{"eat", "tea", "tan", "ate", "nat", "bat"}));
        System.out.println("P3 lengthLongest     = " + lengthOfLongestSubstring("abcabcbb"));
        System.out.println("P4 kthLargest(k=2)   = " + kthLargest(new int[]{3, 2, 1, 5, 6, 4}, 2));
        System.out.println("P5 slidingMax        = " + Arrays.toString(maxSlidingWindow(new int[]{1, 3, -1, -3, 5, 3, 6, 7}, 3)));
        System.out.println("P6 validParens       = " + isValidParens("({[]})"));
        System.out.println("P7 calendar bookings = " + calendarDemo());
        System.out.println("P8 LRU state         = " + lruDemo());
    }

    // ── P1. Two Sum  (LC 1) ──────────────────────────────────────────────────
    static int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> seen = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            Integer j = seen.get(target - nums[i]);
            if (j != null) return new int[]{j, i};
            seen.put(nums[i], i);
        }
        return new int[0];
    }

    // ── P2. Group Anagrams  (LC 49) ──────────────────────────────────────────
    static List<List<String>> groupAnagrams(String[] strs) {
        Map<String, List<String>> g = new HashMap<>();
        for (String w : strs) {
            char[] c = w.toCharArray(); Arrays.sort(c);
            g.computeIfAbsent(new String(c), k -> new ArrayList<>()).add(w);
        }
        return new ArrayList<>(g.values());
    }

    // ── P3. Longest Substring Without Repeating Characters  (LC 3) ───────────
    //    Sliding window with a HashMap of char -> last index.
    static int lengthOfLongestSubstring(String s) {
        Map<Character, Integer> lastIdx = new HashMap<>();
        int best = 0, left = 0;
        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            if (lastIdx.containsKey(c) && lastIdx.get(c) >= left) {
                left = lastIdx.get(c) + 1;             // jump past the duplicate
            }
            lastIdx.put(c, right);
            best = Math.max(best, right - left + 1);
        }
        return best;
    }

    // ── P4. Kth Largest Element  (LC 215) ────────────────────────────────────
    static int kthLargest(int[] nums, int k) {
        PriorityQueue<Integer> min = new PriorityQueue<>();
        for (int n : nums) {
            min.offer(n);
            if (min.size() > k) min.poll();
        }
        return min.peek();
    }

    // ── P5. Sliding Window Maximum  (LC 239) ─────────────────────────────────
    static int[] maxSlidingWindow(int[] nums, int k) {
        Deque<Integer> dq = new ArrayDeque<>();                 // indices, vals strictly decreasing
        int[] out = new int[nums.length - k + 1];
        for (int i = 0; i < nums.length; i++) {
            while (!dq.isEmpty() && dq.peekFirst() <= i - k) dq.pollFirst();
            while (!dq.isEmpty() && nums[dq.peekLast()] < nums[i]) dq.pollLast();
            dq.offerLast(i);
            if (i >= k - 1) out[i - k + 1] = nums[dq.peekFirst()];
        }
        return out;
    }

    // ── P6. Valid Parentheses  (LC 20) ───────────────────────────────────────
    static boolean isValidParens(String s) {
        Deque<Character> st = new ArrayDeque<>();
        Map<Character, Character> pair = Map.of(')', '(', ']', '[', '}', '{');
        for (char c : s.toCharArray()) {
            if (pair.containsValue(c)) st.push(c);
            else if (st.isEmpty() || st.pop() != pair.get(c)) return false;
        }
        return st.isEmpty();
    }

    // ── P7. My Calendar I  (LC 729) ──────────────────────────────────────────
    static TreeMap<Integer, Integer> calendarDemo() {
        TreeMap<Integer, Integer> cal = new TreeMap<>();
        bookCal(cal, 10, 20);
        bookCal(cal, 30, 40);
        bookCal(cal, 15, 25);          // overlap with [10,20) → rejected silently
        return cal;
    }
    private static boolean bookCal(TreeMap<Integer, Integer> cal, int start, int end) {
        var prev = cal.floorEntry(start);
        var next = cal.ceilingEntry(start);
        if (prev != null && prev.getValue() > start) return false;
        if (next != null && next.getKey()   < end)   return false;
        cal.put(start, end);
        return true;
    }

    // ── P8. LRU Cache  (LC 146) ──────────────────────────────────────────────
    static Map<Integer, Integer> lruDemo() {
        int cap = 3;
        Map<Integer, Integer> lru = new LinkedHashMap<>(cap, 0.75f, true) {
            @Override protected boolean removeEldestEntry(Map.Entry<Integer, Integer> e) {
                return size() > cap;
            }
        };
        lru.put(1, 10); lru.put(2, 20); lru.put(3, 30);
        lru.get(1);                    // touch 1
        lru.put(4, 40);                // evicts 2 (least recently used)
        return lru;
    }
}