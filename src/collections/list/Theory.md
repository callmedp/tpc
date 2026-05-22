# Collections — List: Theory

---

## Q1. What is the `List` interface?

`List<E>` is an **ordered** collection (sequence) that:
- Maintains insertion order
- Allows **duplicates**
- Allows positional access via index (`get(i)`, `set(i, e)`, `add(i, e)`, `remove(i)`)
- Allows `null` (depends on impl)

### Key methods (in addition to Collection)
| Method | Description |
|---|---|
| `E get(int index)` | Read at index |
| `E set(int index, E e)` | Replace at index, returns old |
| `void add(int index, E e)` | Insert at index, shifts right |
| `E remove(int index)` | Remove at index, returns removed |
| `int indexOf(Object o)` | First index of o, or -1 |
| `int lastIndexOf(Object o)` | Last index of o, or -1 |
| `List<E> subList(int from, int to)` | View of [from, to) — changes reflect back! |
| `ListIterator<E> listIterator()` | Bidirectional iterator |
| `void sort(Comparator)` | In-place sort |
| `static List.of(...)` | Java 9 immutable list |
| `static List.copyOf(coll)` | Java 10 immutable copy |

---

## Q2. ArrayList vs LinkedList — when to use which?

| Aspect | `ArrayList` | `LinkedList` |
|---|---|---|
| Backing | Dynamic array (`Object[]`) | Doubly linked list of nodes |
| `get(i)` | **O(1)** | O(n) — must walk |
| `add(e)` at end | O(1) amortized | O(1) |
| `add(i, e)` middle | O(n) — shift right | O(n) — walk + O(1) link |
| `add` at head | O(n) — shift all | **O(1)** |
| `remove(i)` | O(n) — shift left | O(n) — walk |
| Memory | Contiguous, less per element | Extra `prev`/`next` pointers per node |
| Cache locality | **Excellent** | Poor (pointer chasing) |
| Implements | `List`, `RandomAccess` | `List`, `Deque` |
| Best for | Random access, read-heavy | Frequent inserts/deletes at ends, queue/deque use |

**Practical rule of thumb:** Almost always pick `ArrayList`. `LinkedList` shines only when used as a `Deque` for frequent head/tail operations — and even then `ArrayDeque` is usually faster.

### ArrayList internals
- Default initial capacity = 10 (lazy: empty array until first add).
- Growth: `newCap = oldCap + (oldCap >> 1)` ⇒ **1.5x**.
- `Arrays.copyOf(...)` is used to copy on growth.
- `trimToSize()` shrinks the backing array to current size.

### LinkedList internals
- Doubly linked: each node has `item`, `prev`, `next`.
- Maintains `first` and `last` references → O(1) head/tail ops.
- Implements both `List` and `Deque`.

---

## Q3. Why does `ArrayList` grow by 1.5x (not 2x)?

- Geometric growth gives **amortized O(1)** for `add()`.
- 1.5x balances **memory waste** vs **reallocation frequency**:
  - 2x can waste up to 50% of allocated memory.
  - 1.5x wastes less, while still amortizing.
- Java engineers chose 1.5x as a pragmatic trade-off (matches C++ MSVC `std::vector`).

---

## Q4. What is `RandomAccess` marker interface?

`java.util.RandomAccess` is a **marker** interface (no methods). It signals "this list supports fast (constant-time) positional access."

- Implemented by: `ArrayList`, `Vector`, `Stack`, `CopyOnWriteArrayList`.
- NOT by: `LinkedList`.

Library code can check it to choose between two iteration strategies:

```java
if (list instanceof RandomAccess)
    for (int i = 0; i < list.size(); i++) doSomething(list.get(i)); // fast
else
    for (E e : list) doSomething(e); // safer for LinkedList
```

`Collections.binarySearch` and `Collections.sort` consult this flag.

---

## Q5. What is the `subList()` trap?

`subList(from, to)` returns a **live view** backed by the original list. Structural modifications via the view affect the parent and vice versa:

```java
List<Integer> list = new ArrayList<>(List.of(1, 2, 3, 4, 5));
List<Integer> view = list.subList(1, 4);   // [2, 3, 4]
view.clear();                              // list becomes [1, 5]
```

**Trap:** if you structurally modify the **parent** while a subList view exists, any subsequent operation on the view throws `ConcurrentModificationException`.

```java
List<Integer> sub = list.subList(0, 2);
list.add(99);          // modifies parent
sub.size();            // CME
```

---

## Q6. What is `Vector` and `Stack`? Why avoid them?

- `Vector` (Java 1.0) — a dynamic array like `ArrayList`, but **every method is `synchronized`**. Coarse-grained locking → poor performance.
- `Stack` extends `Vector` — adds `push/pop/peek`. Also synchronized; carries `Vector`'s baggage and exposes a confusing `List` API (you can `add(0, x)` to the "bottom").

### Prefer
- For single-threaded list: `ArrayList`.
- For stack: `ArrayDeque` (`push`, `pop`, `peek`).
- For thread-safe list: `Collections.synchronizedList(new ArrayList<>())` or `CopyOnWriteArrayList`.

### Vector vs ArrayList specifics
| | ArrayList | Vector |
|---|---|---|
| Sync | No | Yes (synchronized methods) |
| Growth | 1.5x | 2x (default; configurable via `capacityIncrement`) |
| Legacy | No | Yes (since JDK 1.0) |
| Iterator | Fail-fast | Fail-fast (but with lock during iter) |

---

## Q7. What is `CopyOnWriteArrayList`?

A **thread-safe** `List` from `java.util.concurrent`.

- **Mutation copies the entire backing array.** Mutation methods (`add`, `set`, `remove`) acquire a lock, allocate a new array with the change, then volatile-publish it.
- **Reads (`get`, iteration) are lock-free** and operate on the snapshot at the time the iterator was created.
- Iterators are **fail-safe** — never throw CME; do NOT support `remove`, `set`, `add`.

### When to use
- Read-heavy, write-rare scenarios (event listener lists, config snapshots).
- When iteration must never throw CME even under concurrent writes.

### When NOT to use
- Write-heavy use — every mutation is O(n).
- Large lists with frequent mutation — high memory churn (GC pressure).

```java
List<String> listeners = new CopyOnWriteArrayList<>();
listeners.add("a");
for (String s : listeners) {
    listeners.add("b"); // safe — no CME; new element NOT in this iteration
}
```

---

## Q8. How to make an `ArrayList` thread-safe? Three options.

```java
// 1) Collections.synchronizedList — wraps with a synchronized facade
List<String> sync = Collections.synchronizedList(new ArrayList<>());
// CAVEAT: iteration must be manually synchronized:
synchronized (sync) {
    for (String s : sync) { ... }
}

// 2) CopyOnWriteArrayList — read-mostly
List<String> cow = new CopyOnWriteArrayList<>();

// 3) External lock (ReentrantLock / synchronized block)
```

---

## Q9. List immutability — three flavors.

```java
// 1) Truly immutable (Java 9+)
List<Integer> immutable = List.of(1, 2, 3);          // can't add/remove/set

// 2) Defensive copy + unmodifiable wrapper
List<Integer> unmod = Collections.unmodifiableList(new ArrayList<>(src));
// Note: if you keep a reference to the underlying list, you can still mutate it!

// 3) Immutable copy (Java 10+)
List<Integer> copy = List.copyOf(someList);          // copies + unmodifiable
```

`List.of()` and `List.copyOf()` reject `null` elements (NPE).

---

## Q10. How to convert between Array and List?

```java
// Array → List
String[] arr = {"a", "b", "c"};

List<String> fixed = Arrays.asList(arr);            // FIXED-SIZE view, backed by arr
List<String> resizable = new ArrayList<>(Arrays.asList(arr));
List<String> immutable = List.of(arr);              // Java 9+

// List → Array
List<String> list = List.of("a", "b", "c");
String[] a1 = list.toArray(new String[0]);          // preferred (Java 6+)
String[] a2 = list.toArray(String[]::new);          // Java 11+
Object[] a3 = list.toArray();                       // Object[], not String[]!
```

**Trap:** `Arrays.asList(int[])` returns `List<int[]>` of size 1, NOT `List<Integer>` of size n. Use `Integer[]` or `Arrays.stream(int[]).boxed()`.

---

## Q11. Why is `for (int i = 0; i < list.size(); i++)` faster than for-each on ArrayList?

Marginally faster because:
- For-each uses an `Iterator` → method calls (`hasNext`, `next`) and bounds checks.
- Indexed loop on `ArrayList` does direct array access via `elementData[i]`.
- JIT can usually inline both, so in practice the difference is negligible.

**Note:** On `LinkedList`, indexed loop is O(n²) (each `get(i)` walks the list). Use for-each / iterator.

---

## Q12. What are common ArrayList interview pitfalls?

1. **Removing while iterating with for-each** → CME. Use `Iterator.remove()` or `removeIf`.
2. **`subList` + parent modification** → CME on subList ops.
3. **`Arrays.asList()` returns a fixed-size list**, not a regular `ArrayList`. `add`/`remove` throw `UnsupportedOperationException`.
4. **`toArray()` returns `Object[]`** unless typed overload is used.
5. **`Iterator.remove()` can only be called once per `next()`**.
6. **`equals` and `hashCode` for List** compare by element order and content. Two Lists are equal iff same size, same elements, same order — across any List implementations.
