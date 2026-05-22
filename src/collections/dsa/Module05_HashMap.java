package collections.dsa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  MODULE 5 — HASHMAP (the workhorse)                                      │
 * │  Prereq:  Modules 1–4                                                    │
 * │  Goal:    every HashMap method + the FOUR canonical DSA patterns         │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * HashMap is the most-used collection in DSA — period.
 * Master these four patterns and you can solve ~30% of LeetCode "Easy" and
 * a big chunk of "Medium" problems instantly.
 *
 *   PATTERN A — Frequency counting       (sliding window, anagrams, top-K)
 *   PATTERN B — Seen-set / Index lookup  (two-sum, longest substring)
 *   PATTERN C — Bucket grouping          (group anagrams, group by length)
 *   PATTERN D — Prefix-sum cache         (subarray sum equals K)
 *
 * What you'll learn:
 *   • put / get / getOrDefault / containsKey / containsValue / remove / size
 *   • putIfAbsent / computeIfAbsent / computeIfPresent / compute / merge
 *   • keySet / values / entrySet / forEach
 *   • replace / replaceAll
 *   • Map.of / Map.copyOf / Map.entry / Map.Entry.comparingByKey/Value
 */
public class Module05_HashMap {

    public static void main(String[] args) {

        // ─────────────────────────────────────────────────────────────────────
        // 1. The basics
        // ─────────────────────────────────────────────────────────────────────
        Map<String, Integer> m = new HashMap<>();
        m.put("a", 1);
        m.put("b", 2);
        m.get("a");                          // 1
        m.get("missing");                    // null
        m.getOrDefault("missing", 0);        // 0   — safer read
        m.containsKey("a");
        m.containsValue(2);
        m.remove("a");
        m.size();   m.isEmpty();   m.clear();
        m.put("a", 1); m.put("b", 2); m.put("c", 3);

        // ─────────────────────────────────────────────────────────────────────
        // 2. Atomic compound ops — the methods that make HashMap a superweapon
        // ─────────────────────────────────────────────────────────────────────
        m.putIfAbsent("a", 99);              // no-op if 'a' present
        m.computeIfAbsent("d", k -> 4);      // insert only if missing — common for grouping
        m.computeIfPresent("a", (k, v) -> v + 10);
        m.compute("a", (k, v) -> v == null ? 1 : v + 1);
        m.merge("a", 1, Integer::sum);       // CLEANEST increment-or-init
        m.replace("a", 100);                 // only if key exists
        m.replaceAll((k, v) -> v * 2);

        // ─────────────────────────────────────────────────────────────────────
        // 3. Iteration patterns
        // ─────────────────────────────────────────────────────────────────────
        for (String k : m.keySet())         { /* keys   */ }
        for (Integer v : m.values())        { /* values */ }
        for (Map.Entry<String, Integer> e : m.entrySet()) {
            e.getKey(); e.getValue();
        }
        m.forEach((k, v) -> { /* lambda */ });

        // ─────────────────────────────────────────────────────────────────────
        // 4. Immutable factories (Java 9)
        // ─────────────────────────────────────────────────────────────────────
        Map<String, Integer> tiny = Map.of("x", 1, "y", 2);              // up to 10 pairs
        Map<String, Integer> many = Map.ofEntries(
                Map.entry("x", 1),
                Map.entry("y", 2),
                Map.entry("z", 3));
        Map<String, Integer> copy = Map.copyOf(m);

        // ─────────────────────────────────────────────────────────────────────
        // ╔════════════════════════════════════════════════════════════════╗
        // ║  PATTERN A — Frequency counting (anagram, top-K, sliding win)  ║
        // ╚════════════════════════════════════════════════════════════════╝
        String s = "aabbbcccc";
        Map<Character, Integer> freq = new HashMap<>();
        for (char c : s.toCharArray()) {
            freq.merge(c, 1, Integer::sum);                   // cleanest
            // freq.put(c, freq.getOrDefault(c, 0) + 1);      // alt
        }
        // Valid Anagram (LC 242)
        boolean anagram = freqOf("listen").equals(freqOf("silent"));

        // ─────────────────────────────────────────────────────────────────────
        // ╔════════════════════════════════════════════════════════════════╗
        // ║  PATTERN B — Seen / Index lookup  (Two-Sum LC 1)                ║
        // ╚════════════════════════════════════════════════════════════════╝
        int[] nums = {2, 7, 11, 15};
        int target = 9;
        int[] answer = null;
        Map<Integer, Integer> seen = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            if (seen.containsKey(target - nums[i])) {
                answer = new int[]{seen.get(target - nums[i]), i};
                break;
            }
            seen.put(nums[i], i);
        }

        // ─────────────────────────────────────────────────────────────────────
        // ╔════════════════════════════════════════════════════════════════╗
        // ║  PATTERN C — Bucket grouping  (Group Anagrams LC 49)           ║
        // ╚════════════════════════════════════════════════════════════════╝
        Map<String, List<String>> buckets = new HashMap<>();
        for (String w : List.of("eat", "tea", "tan", "ate", "nat", "bat")) {
            char[] key = w.toCharArray(); Arrays.sort(key);
            buckets.computeIfAbsent(new String(key), k -> new ArrayList<>()).add(w);
        }

        // ─────────────────────────────────────────────────────────────────────
        // ╔════════════════════════════════════════════════════════════════╗
        // ║  PATTERN D — Prefix-sum cache  (Subarray Sum Equals K, LC 560) ║
        // ╚════════════════════════════════════════════════════════════════╝
        int[] arr = {1, 1, 1};
        int k = 2;
        Map<Integer, Integer> prefixCount = new HashMap<>();
        prefixCount.put(0, 1);                  // empty prefix has sum 0, once
        int sum = 0, count = 0;
        for (int x : arr) {
            sum += x;
            count += prefixCount.getOrDefault(sum - k, 0);
            prefixCount.merge(sum, 1, Integer::sum);
        }

        // ─────────────────────────────────────────────────────────────────────
        // 5. Showcase output
        // ─────────────────────────────────────────────────────────────────────
        System.out.println("m         = " + m);
        System.out.println("tiny      = " + tiny);
        System.out.println("freq      = " + freq);
        System.out.println("anagram   = " + anagram);
        System.out.println("twoSum    = " + Arrays.toString(answer));
        System.out.println("buckets   = " + buckets);
        System.out.println("subarrays = " + count + "  (expect 2)");

        // ─────────────────────────────────────────────────────────────────────
        // Practice problems:
        //   • LC 1     Two Sum                       — Pattern B
        //   • LC 49    Group Anagrams                — Pattern C
        //   • LC 242   Valid Anagram                 — Pattern A
        //   • LC 560   Subarray Sum Equals K         — Pattern D
        //   • LC 387   First Unique Character        — Pattern A
        //   • LC 3     Longest Substring No Repeat   — Patterns A + B (sliding window)
        //   • LC 438   Find All Anagrams in a String — Pattern A
        //   • LC 128   Longest Consecutive Sequence  — HashSet (Module 6)
        // ─────────────────────────────────────────────────────────────────────
    }

    private static Map<Character, Integer> freqOf(String s) {
        Map<Character, Integer> f = new HashMap<>();
        for (char c : s.toCharArray()) f.merge(c, 1, Integer::sum);
        return f;
    }
}