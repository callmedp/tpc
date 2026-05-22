# collections.dsa — Theory

Q&A for the `collections.dsa` package: a course-style cheatsheet of every Java collection / utility method used in DSA interviews at top product companies (Google, Microsoft, Amazon, Meta, Apple).

---

## 1. Which Java collection methods are most commonly used in DSA interviews at top product companies?

The cheatsheet is laid out as **15 modules**, each a self-contained runnable file under `src/collections/dsa/`. Treat it like a course — start at Module 1 and progress.

### Course outline

| # | Module | What you learn | Top patterns / problems |
|---|--------|----------------|--------------------------|
| 1 | `Module01_ArraysFoundation` | `Arrays.sort`, `copyOf`, `copyOfRange`, `fill`, `setAll`, `binarySearch`, `equals`, `deepEquals`, `hashCode`, `deepHashCode`, `toString`, `deepToString`, `stream`, `asList`, `parallelSort` | LC 88, 169, 215, 268, 287 |
| 2 | `Module02_NumbersAndCharacters` | `Math.max/min/abs/pow/sqrt/log/floor/ceil/round/floorMod/floorDiv/addExact`; `Integer.MAX_VALUE/parseInt/toString(radix)/toBinaryString/bitCount/highestOneBit/lowestOneBit/numberOfLeading-Trailing-Zeros/compare/signum/reverse`; bit tricks (`n & (n-1)`, `n & -n`); `Character.isDigit/isLetter/isLetterOrDigit/isWhitespace/isAlphabetic/toLowerCase/getNumericValue/digit` | LC 191, 231, 268, 371, 7 |
| 3 | `Module03_Lists` | `ArrayList`: `add(idx)`, `set`, `get`, `remove(int)` vs `remove(Object)`, `indexOf/lastIndexOf`, `subList` (view trap), `replaceAll`, `removeIf`, `toArray(new T[0])`; `Iterator` & `ListIterator`; `LinkedList` as Deque; complexity table | LC 1, 15, 56, 88, 27 |
| 4 | `Module04_StringsAndStringBuilder` | `String`: `charAt/substring/indexOf/contains/startsWith/endsWith/equals(IgnoreCase)/compareTo/toLowerCase/strip/replace/replaceAll/split/repeat/format/join/valueOf/chars/codePoints/toCharArray`; `StringBuilder`: `append/insert/delete/deleteCharAt/replace/setCharAt/reverse/setLength`; palindrome & anagram idioms | LC 125, 242, 49, 5, 8, 14, 28, 387 |
| 5 | `Module05_HashMap` | `put/get/getOrDefault/containsKey/containsValue/remove/size`; **atomic compound ops**: `putIfAbsent/computeIfAbsent/computeIfPresent/compute/merge`; `keySet/values/entrySet/forEach`; `Map.of/copyOf/entry`; four canonical patterns: frequency, seen-set, bucket grouping, prefix-sum cache | LC 1, 49, 242, 560, 387, 3, 438 |
| 6 | `Module06_HashSet` | `add/remove/contains/size`; set algebra: `addAll/retainAll/removeAll`; `removeIf`; `LinkedHashSet` (order-preserving dedup) | LC 217, 349, 350, 128, 202, 141 |
| 7 | `Module07_TreeMapTreeSet` | `TreeMap`: `firstKey/lastKey/floorKey/ceilingKey/lowerKey/higherKey` + their `Entry` variants; range views (`headMap/tailMap/subMap`); `pollFirstEntry/pollLastEntry`; `descendingMap/descendingKeySet`; `TreeSet` mirrors | LC 729, 731, 855, 220, 658, 1146 |
| 8 | `Module08_LinkedHashMap_LRU` | Insertion-order vs access-order constructor; `removeEldestEntry` for O(1) LRU cache | LC 146, 460, 1429 |
| 9 | `Module09_Stack` | `ArrayDeque` as Stack: `push/pop/peek`; matching, monotonic stack, RPN evaluation, DFS-iterative, Min Stack | LC 20, 155, 150, 224, 496, 739, 84, 394 |
| 10 | `Module10_QueueAndDeque` | `offer/poll/peek/add/remove/element`; deque: `offerFirst/offerLast/pollFirst/pollLast/peekFirst/peekLast`; BFS template, monotonic-deque sliding-window max, multi-source BFS, 0-1 BFS | LC 102, 199, 200, 994, 1091, 239, 542, 752 |
| 11 | `Module11_PriorityQueue` | `offer/poll/peek/size`; min-heap vs max-heap with `Comparator.reverseOrder()` and `Comparator.comparingInt`; Top-K, Top-K-frequent, merge K sorted, Dijkstra, two-heap median, meeting rooms II | LC 215, 347, 23, 295, 253, 621, 743, 973, 1167 |
| 12 | `Module12_Comparator` | `Comparator.comparing/comparingInt/comparingLong/comparingDouble`; `thenComparing`, `reversed`, `nullsFirst/nullsLast`, `naturalOrder/reverseOrder`; the `(a-b)` overflow trap; "Largest Number" pattern | LC 56, 179, 215, 252, 451, 853, 1366 |
| 13 | `Module13_CollectionsUtility` | `Collections.sort/reverse/shuffle/max/min/frequency/binarySearch/swap/rotate/fill/addAll/disjoint/nCopies/emptyList/singletonList/unmodifiableList/synchronizedList/reverseOrder` | LC 169, 78, 189, 75, 31 |
| 14 | `Module14_StreamsForDSA` | `Arrays.stream`, `IntStream.range/rangeClosed`, `map/filter/sorted/distinct/reduce/count/sum/max/anyMatch`, `boxed`, `Collectors.toList/toSet/toMap/groupingBy/counting/partitioningBy/joining/mapping` | LC 49 (one-liner), frequency maps |
| 15 | `Module15_Capstone` | Eight classic LeetCode problems, each picking the right collection from prior modules: Two Sum, Group Anagrams, Longest Substring No Repeat, Kth Largest, Sliding Window Max, Valid Parens, My Calendar I, LRU Cache | LC 1, 49, 3, 215, 239, 20, 729, 146 |

### Pattern → tool quick reference

| Pattern | Reach for |
|---|---|
| Frequency / counting | `HashMap.merge` / `getOrDefault` |
| Two-sum, seen-set | `HashMap` or `HashSet` |
| Dedup, set algebra | `HashSet` (`addAll/retainAll/removeAll`) |
| Top-K largest | `PriorityQueue` (min-heap of size K) |
| Top-K frequent | `HashMap` + `PriorityQueue` |
| Sliding-window max | `ArrayDeque` (monotonic deque) |
| Next greater element | `ArrayDeque` (monotonic stack) |
| Valid parentheses / DFS-iter | `ArrayDeque` as Stack |
| BFS / level-order / shortest path (unweighted) | `ArrayDeque` as Queue |
| Floor/Ceiling lookups | `TreeMap` / `TreeSet` |
| Calendar / interval overlap | `TreeMap.floorEntry` + `ceilingEntry` |
| LRU cache | `LinkedHashMap` (access-order) + `removeEldestEntry` |
| Group anagrams | `HashMap<sortedKey, List>` + `computeIfAbsent` |
| Custom sort | `Comparator.comparing.thenComparing` |
| Build long string | `StringBuilder` (never `+=` in a loop) |
| Bit tricks | `Integer.bitCount`, `n & (n-1)`, `n & -n` |
| Safe modulo for negatives | `Math.floorMod` |

### How to use this package

1. Open `Module01_ArraysFoundation.java`. Read the header comment, scroll through the body — every method is annotated with its purpose. Run it (`java collections.dsa.Module01_ArraysFoundation`) and read the output.
2. Do the **Practice problems** listed at the bottom of each module on LeetCode before moving on.
3. Move to the next module. Each module names its prereqs and reuses earlier patterns.
4. `Module15_Capstone` puts everything together — it's a deliberate test of "did I pick the right collection?" intuition.

---
