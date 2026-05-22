# Collections — Utilities: `Collections` & `Arrays`

The `Collections` and `Arrays` classes provide static helper methods. They are not collections themselves; they operate on them.

---

## Q1. What is the `Collections` utility class?

`java.util.Collections` provides static algorithms and factories for working with `Collection`/`List`/`Map`/`Set`. Highlights:

### Sorting & searching
| Method | Description |
|---|---|
| `sort(List)` / `sort(List, Comparator)` | TimSort, stable, O(n log n) |
| `binarySearch(List, key)` | Requires sorted list; O(log n) on RandomAccess lists |
| `reverse(List)` | In-place reverse |
| `shuffle(List)` / `shuffle(List, Random)` | Fisher-Yates |
| `swap(List, i, j)` | — |
| `rotate(List, distance)` | Cyclic shift |
| `fill(List, value)` | Replace all elements |
| `copy(dest, src)` | Copy src into dest (dest must be ≥ src) |
| `min(coll)` / `max(coll)` | Natural order |
| `min(coll, comp)` / `max(coll, comp)` | Custom order |
| `frequency(coll, o)` | Count occurrences |
| `disjoint(c1, c2)` | True if no common element |

### Wrappers
| Method | Returns |
|---|---|
| `unmodifiableList/Set/Map/Collection(c)` | Read-only view |
| `synchronizedList/Set/Map(c)` | Thread-safe wrapper |
| `checkedList/Set/Map(c, type)` | Adds runtime type-check on insertion |
| `emptyList/Set/Map()` | Singleton empty immutable instance |
| `singletonList(e)` / `singleton(e)` / `singletonMap(k,v)` | One-element immutable |
| `nCopies(n, e)` | Immutable list of n copies |
| `newSetFromMap(map)` | Set backed by given Map |
| `asLifoQueue(deque)` | Wraps a Deque to expose it as a Queue with LIFO order |

### Empty / singleton — why use them?
- Memory: shared singletons avoid object allocation.
- API: clean return for "no results" (avoid returning null).

### Caveats
- `unmodifiableList(list)` is a **view** — if you keep a reference to the underlying mutable list and mutate it, the "unmodifiable" view changes too. For true safety, use `List.copyOf` (Java 10+).
- `synchronizedList` requires manual synchronization for iteration:
  ```java
  List<String> sync = Collections.synchronizedList(new ArrayList<>());
  synchronized (sync) { for (String s : sync) { ... } }
  ```

---

## Q2. What is the `Arrays` utility class?

`java.util.Arrays` provides static helpers for arrays. Highlights:

| Method | Description |
|---|---|
| `Arrays.asList(arr)` | Fixed-size List view backed by array |
| `Arrays.stream(arr)` | Stream over the array |
| `Arrays.sort(arr)` | Dual-Pivot Quicksort (primitives) / TimSort (objects) |
| `Arrays.sort(arr, from, to)` | Range sort |
| `Arrays.sort(arr, Comparator)` | Custom comparator (objects only) |
| `Arrays.parallelSort(arr)` | Fork/Join parallel sort |
| `Arrays.binarySearch(arr, key)` | Requires sorted |
| `Arrays.fill(arr, v)` | Replace all |
| `Arrays.fill(arr, from, to, v)` | Range fill |
| `Arrays.copyOf(arr, newLen)` | New array of given length, padded with default/null |
| `Arrays.copyOfRange(arr, from, to)` | Slice |
| `Arrays.equals(a, b)` | Shallow equality |
| `Arrays.deepEquals(a, b)` | Recursive for nested arrays |
| `Arrays.hashCode(arr)` / `deepHashCode(arr)` | Hash codes |
| `Arrays.toString(arr)` / `deepToString(arr)` | Printable |
| `Arrays.setAll(arr, i -> ...)` | Functional fill (Java 8) |
| `Arrays.parallelPrefix(arr, op)` | Cumulative reduction |

### Conversions
```java
// int[] → List<Integer> — Arrays.asList(int[]) gives List<int[]> of size 1!
int[] ints = {1,2,3};
List<Integer> list = Arrays.stream(ints).boxed().toList();   // Java 16+

// Integer[] → List<Integer> — works
Integer[] boxed = {1,2,3};
List<Integer> ok = Arrays.asList(boxed);

// List<Integer> → int[]
int[] arr = ok.stream().mapToInt(Integer::intValue).toArray();
```

---

## Q3. `Collections.emptyList()` vs `new ArrayList<>()` vs `List.of()`.

| | Mutable | Allocates | Type |
|---|---|---|---|
| `new ArrayList<>()` | Yes | Yes (each call) | `ArrayList` |
| `Collections.emptyList()` | No | No (singleton) | `EmptyList` (legacy unmodifiable) |
| `List.of()` | No | No (singleton; Java 9+) | `ImmutableCollections$ListN` |

Use `List.of()` for new code. Use the singletons for "no result" returns to avoid allocation.

---

## Q4. `unmodifiableList` vs `List.copyOf` vs `List.of`.

```java
List<Integer> src = new ArrayList<>(List.of(1, 2, 3));

// 1) View — mutations to src reflect in v1
List<Integer> v1 = Collections.unmodifiableList(src);
src.add(4);
System.out.println(v1);   // [1, 2, 3, 4] — yikes!

// 2) Defensive copy + unmodifiable — independent of src
List<Integer> v2 = List.copyOf(src);   // Java 10+

// 3) Literal immutable
List<Integer> v3 = List.of(1, 2, 3);
```

---

## Q5. Common interview gotchas

1. **`Arrays.asList(int[])`** returns `List<int[]>` of size 1, not `List<Integer>`.
2. **`Arrays.asList(...)` is fixed-size** — `add`/`remove` throw `UnsupportedOperationException`. `set` works.
3. **`Collections.unmodifiableList` is a view**, not a deep copy — caller can still mutate via the original reference.
4. **`Collections.synchronizedList` requires manual sync during iteration.**
5. **`Collections.binarySearch` on an unsorted list returns garbage** — must sort first.
6. **`Arrays.equals` is shallow** for `Object[][]` — use `deepEquals`.