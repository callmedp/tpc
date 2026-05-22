# dsa — DSA Patterns Curriculum for Top-Product Interviews

A 28-module course covering every pattern asked in DSA interviews at Google, Microsoft, Amazon, Meta, Apple, etc. Each module under `src/dsa/Module##_Name.java` is **self-contained, runnable** (`java dsa.Module##_Name`), and follows the same structure:

1. **Header** — when to recognise the pattern, the template, complexity
2. **3–10 worked LeetCode problems** with idiomatic Java solutions
3. **Practice set** — LC numbers grouped by difficulty pattern

## Course progression

### Phase 1 — Array & String Foundations
| # | Module | Pattern |
|---|---|---|
| 1 | [TwoPointers](Module01_TwoPointers.java) | Opposite-end, same-direction, fast/slow. LC 167, 11, 15, 283, 26, 141, 876 |
| 2 | [SlidingWindow](Module02_SlidingWindow.java) | Fixed & variable window. LC 643, 567, 3, 209, 76, 424, 1004 |
| 3 | [PrefixSum](Module03_PrefixSum.java) | 1-D / 2-D prefix sum, prefix+HashMap, difference array. LC 303, 304, 560, 974, 525, 1109, 1248 |
| 4 | [CyclicSort](Module04_CyclicSort.java) | Index-as-key for 1..n / 0..n. LC 268, 448, 442, 287, 41, 645 |
| 5 | [BinarySearch](Module05_BinarySearch.java) | Lower-bound template, binary search on answer, rotated array. LC 704, 35, 34, 33, 153, 875, 1011, 410 |

### Phase 2 — Linear Data Structures
| # | Module | Pattern |
|---|---|---|
| 6 | [Intervals](Module06_Intervals.java) | Merge, insert, sweep line, greedy-by-end. LC 56, 57, 252, 253, 435, 452, 1288 |
| 7 | [LinkedList](Module07_LinkedList.java) | Dummy head, reverse-in-place, fast/slow, in-group reverse. LC 206, 21, 19, 142, 92, 25, 234, 143 |
| 8 | [MonotonicStack](Module08_MonotonicStack.java) | NGE, daily temps, largest rectangle, rain water, sliding-max deque, stock span. LC 496, 503, 739, 84, 42, 239, 901 |
| 9 | [HeapTopK](Module09_HeapTopK.java) | Min-heap size K, top-K frequent, merge K, streaming median, scheduler. LC 215, 347, 973, 23, 295, 621, 1167 |
| 10 | [Hashing](Module10_Hashing.java) | Seen-set, frequency, bucket, set membership, bijection. LC 1, 49, 242, 128, 387, 219, 36, 290 |

### Phase 3 — Trees
| # | Module | Pattern |
|---|---|---|
| 11 | [BinaryTree](Module11_BinaryTree.java) | DFS (pre/in/post), BFS level-order, post-order combining, LCA, serde. LC 94, 102, 104, 110, 226, 543, 236, 297 |
| 12 | [BST](Module12_BST.java) | Search / insert / delete, validate, kth smallest, BST LCA, sorted→BST. LC 700, 701, 450, 98, 230, 235, 108, 173 |
| 13 | [Trie](Module13_Trie.java) | Insert / search / startsWith, wildcard, multi-word board, binary trie (XOR). LC 208, 211, 648, 677, 212, 421 |

### Phase 4 — Graphs
| # | Module | Pattern |
|---|---|---|
| 14 | [GraphTraversal](Module14_GraphTraversal.java) | DFS islands, DFS area, BFS clone, 3-colour DFS cycle detection, multi-source BFS, 8-neighbour BFS, adjacency-matrix DFS. LC 200, 695, 133, 207, 994, 1091, 547 |
| 15 | [TopologicalSort](Module15_TopologicalSort.java) | Kahn's BFS, alien dictionary, MHT trim, reverse-topo, levelled BFS. LC 207, 210, 269, 310, 802, 1136 |
| 16 | [UnionFind](Module16_UnionFind.java) | Path compression + union by rank; provinces, islands, redundant edge, equations, account merge, online islands. LC 547, 200, 684, 990, 721, 305, 1319 |
| 17 | [ShortestPath](Module17_ShortestPath.java) | Dijkstra, Bellman-Ford (K stops), Dijkstra on grid, Floyd-Warshall. LC 743, 787, 1631, 1091, 2812 |

### Phase 5 — Recursion Family
| # | Module | Pattern |
|---|---|---|
| 18 | [Backtracking](Module18_Backtracking.java) | Choose-explore-unchoose. Subsets / permutations / combinations / palindrome partition / N-Queens. LC 78, 90, 46, 47, 39, 40, 22, 79, 131, 51 |
| 19 | [BitManipulation](Module19_BitManipulation.java) | XOR, popcount, bit tricks, bitmask subsets, sum without `+`, bit-reverse. LC 136, 137, 260, 191, 231, 338, 78, 371, 190, 201 |

### Phase 6 — Dynamic Programming (8 modules)
| # | Module | Pattern |
|---|---|---|
| 20 | [DPLinear](Module20_DPLinear.java) | 1-D state. Fib, house robber, LIS (n² and n log n), coin change, decode ways, Kadane. LC 70, 198, 213, 300, 322, 91, 53, 152 |
| 21 | [DPGrid](Module21_DPGrid.java) | 2-D grid traversal DP. Unique paths, min path sum, maximal square, dungeon game (back-DP). LC 62, 63, 64, 221, 174, 931, 120 |
| 22 | [DPKnapsack](Module22_DPKnapsack.java) | 0/1 vs unbounded knapsack; subset-sum; count vs max vs min variants. LC 416, 494, 474, 322, 518, 377 |
| 23 | [DPStrings](Module23_DPStrings.java) | LCS, edit distance, palindrome substr/subseq, regex, wildcard, word break. LC 1143, 72, 583, 5, 516, 647, 10, 44, 139 |
| 24 | [DPInterval](Module24_DPInterval.java) | dp[i][j] over ranges; burst balloons, merge stones, cut stick, scramble string, strange printer, matrix chain. LC 312, 1000, 1547, 87, 664 |
| 25 | [DPBitmask](Module25_DPBitmask.java) | 2ⁿ × n state machines. Partition K subsets, beautiful arrangement, visit-all-nodes, sufficient team, Held-Karp TSP. LC 698, 526, 847, 1125 |
| 26 | [DPTrees](Module26_DPTrees.java) | Post-order tuple returns. Max path sum, diameter, House Robber III, tree cameras, zigzag, univalued subtrees. LC 124, 543, 337, 968, 1372, 250 |
| 27 | [DPStocks](Module27_DPStocks.java) | State-machine DP. The 6-line solution for every Buy-Sell variant. LC 121, 122, 123, 188, 309, 714 |

### Phase 7 — Greedy & Other
| # | Module | Pattern |
|---|---|---|
| 28 | [Greedy](Module28_Greedy.java) | Exchange argument, sort + invariant. Jump game, gas station, partition labels, intervals, two-city, IPO, lemonade. LC 55, 45, 134, 763, 435, 1029, 860, 502, 678 |

---

## Pattern recognition cheatsheet

| Problem phrasing | Reach for |
|---|---|
| "Find pair / triplet with target sum on a sorted array" | Two Pointers (M1) |
| "Longest / shortest contiguous substring with constraint X" | Sliding Window (M2) |
| "Subarray sum / count equals K" | Prefix Sum + HashMap (M3) |
| "Array of 1..n, find missing / duplicate" | Cyclic Sort (M4) |
| "Smallest answer such that predicate(answer) is true" | Binary Search on Answer (M5) |
| "Min meeting rooms / non-overlapping intervals" | Intervals + heap-of-ends (M6) |
| "Reverse / rearrange a linked list" | Dummy node + two pointers (M7) |
| "For each element, next greater / smaller" | Monotonic Stack (M8) |
| "Top K / Kth largest / streaming median" | PriorityQueue (M9) |
| "Two Sum / frequency / group anagrams" | HashMap (M10) |
| "Tree path sum / diameter / LCA" | DFS post-order returning tuple (M11, M26) |
| "BST kth / range / LCA" | In-order traversal (M12) |
| "Prefix search across many words" | Trie (M13) |
| "Count connected components / spread / islands" | DFS / BFS (M14) or Union-Find (M16) |
| "Order of tasks with prereqs" | Topological Sort (M15) |
| "Shortest path with non-negative weights" | Dijkstra (M17) |
| "All subsets / permutations / N-Queens" | Backtracking (M18) |
| "Find single number / parity tricks" | Bit Manipulation (M19) |
| "1-D recurrence on index i" | Linear DP (M20) |
| "Grid path count / min sum" | Grid DP (M21) |
| "Subset sums to S / count ways with coins" | Knapsack DP (M22) |
| "LCS / edit distance / palindrome substr" | String DP (M23) |
| "Merge / split / range partition" | Interval DP (M24) |
| "n ≤ 20 with permute-and-pick" | Bitmask DP (M25) |
| "Buy / sell / cooldown / fee" | State-machine DP (M27) |
| "Maximise / minimise with no DP feel" | Greedy + proof (M28) |

## How to use this course

1. Run each module: `cd src && javac -d /tmp/cls dsa/Module01_TwoPointers.java && java -cp /tmp/cls dsa.Module01_TwoPointers`
2. Read the header to internalise the pattern recognition signals.
3. Trace through each worked problem on paper without looking, then re-derive the code.
4. Solve the **Practice set** at the bottom of each module on LeetCode before moving on. Aim for 3–5 problems per pattern minimum.
5. After Phase 4 (graphs), revisit any earlier modules whose patterns you couldn't recall — repetition cements them.

## Related package
- `collections.dsa/` is the companion course on Java *collection methods* used during interviews. This package teaches *algorithm patterns*; that package teaches *the language toolkit*.
