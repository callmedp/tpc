# Collections — Overview: Theory

---

## Q1. What is the Java Collections Framework?

The **Java Collections Framework (JCF)** is a unified architecture in `java.util` for storing, retrieving, and manipulating groups of objects. It provides:

- **Interfaces** — abstract data types (`List`, `Set`, `Queue`, `Map`, etc.)
- **Implementations** — concrete classes (`ArrayList`, `HashMap`, `TreeSet`, etc.)
- **Algorithms** — static methods in `Collections` and `Arrays` utility classes (sort, search, shuffle, etc.)
- **Infrastructure** — `Iterator`, `Iterable`, `Comparable`, `Comparator`

### Why it matters
- Reduces programming effort (don't reinvent data structures)
- Increases performance (highly optimized implementations)
- Provides interoperability (different APIs all speak Collection)
- Promotes software reuse

---

## Q2. Draw the Collections Framework hierarchy.

```
                 Iterable<T>            (root of iteration)
                     |
                Collection<E>
                /     |      \
             List    Queue    Set
              |       |        |
       ArrayList   Deque    SortedSet
       LinkedList  PQueue    TreeSet
       Vector      ArrayDQ   HashSet
       Stack                  LinkedHashSet


                  Map<K,V>            (NOT a Collection — separate hierarchy)
                  /    |     \
              HashMap  SortedMap  Hashtable
                       TreeMap     Properties
              LinkedHashMap
              ConcurrentHashMap
              WeakHashMap
              IdentityHashMap
              EnumMap
```

### Key observations
- `Map` is **NOT** a `Collection`. It's a separate root interface — a Map stores key-value pairs while a Collection stores single elements.
- `Iterable` is the super-interface of `Collection` and enables the enhanced `for-each` loop.
- `Stack` extends `Vector` (legacy — prefer `ArrayDeque`).
- `Hashtable` and `Vector` are legacy/synchronized (avoid in new code).

---

## Q3. What is the `Collection` interface? List its core methods.

`Collection<E>` is the root interface for all collections except maps.

| Method | Purpose |
|---|---|
| `boolean add(E e)` | Add element |
| `boolean addAll(Collection<? extends E> c)` | Add all elements |
| `boolean remove(Object o)` | Remove single instance |
| `boolean removeAll(Collection<?> c)` | Remove all elements present in c |
| `boolean retainAll(Collection<?> c)` | Keep only elements in c (intersection) |
| `boolean contains(Object o)` | Check membership |
| `boolean containsAll(Collection<?> c)` | Check subset |
| `int size()` | Count |
| `boolean isEmpty()` | Empty check |
| `void clear()` | Remove all |
| `Iterator<E> iterator()` | Get iterator |
| `Object[] toArray()` | Convert to array |
| `<T> T[] toArray(T[] a)` | Convert to typed array |
| `Stream<E> stream()` | Java 8 stream |
| `default boolean removeIf(Predicate)` | Java 8 conditional remove |

---

## Q4. What is `Iterable` and how does the enhanced `for-each` loop work?

`Iterable<T>` is the root for "anything that can be iterated". It has one method:

```java
Iterator<T> iterator();
```

The `for-each` loop is **syntactic sugar** over an `Iterator`:

```java
for (String s : list) { ... }

// compiled to:
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    String s = it.next();
    ...
}
```

Any class that implements `Iterable` can be used in `for-each`.

---

## Q5. What is the difference between `Iterator` and `ListIterator`?

| Feature | `Iterator` | `ListIterator` |
|---|---|---|
| Applies to | All Collections | Lists only |
| Direction | Forward only | Bidirectional (`next` + `previous`) |
| Add element during iteration | No | Yes (`add()`) |
| Replace element | No | Yes (`set()`) |
| Get index | No | Yes (`nextIndex()`, `previousIndex()`) |
| Remove | Yes (`remove()`) | Yes (`remove()`) |

```java
List<Integer> list = new ArrayList<>(List.of(1,2,3));
ListIterator<Integer> it = list.listIterator();
while (it.hasNext()) {
    int v = it.next();
    if (v == 2) it.set(20);   // replace
    if (v == 3) it.add(99);   // insert after current
}
```

---

## Q6. What is fail-fast vs fail-safe iteration?

**Fail-fast** iterators throw `ConcurrentModificationException` (CME) if the underlying collection is structurally modified during iteration (except via the iterator's own `remove()`/`add()`).

- Implemented in: `ArrayList`, `HashMap`, `HashSet`, `LinkedList`, `TreeMap`, `TreeSet`, etc.
- Mechanism: each collection has a `modCount` field. The iterator captures it on creation as `expectedModCount`; on each `next()` it checks `if (modCount != expectedModCount) throw new CME`.
- Note: this is **best-effort** — not guaranteed for concurrent modifications across threads.

**Fail-safe** iterators do NOT throw CME. They iterate over a **snapshot or copy** of the collection.

- Implemented in: `CopyOnWriteArrayList`, `CopyOnWriteArraySet`, `ConcurrentHashMap`.
- Trade-off: changes made during iteration are NOT visible to the iterator (stale view).

```java
// Fail-fast — throws CME
List<Integer> list = new ArrayList<>(List.of(1,2,3));
for (int v : list) { list.add(4); } // CME

// Fail-safe — no exception
List<Integer> safe = new CopyOnWriteArrayList<>(List.of(1,2,3));
for (int v : safe) { safe.add(4); } // OK (but new 4s NOT seen)
```

---

## Q7. What's the difference between `Collection` and `Collections`?

- `Collection` — the **interface** (singular) at the root of the framework.
- `Collections` — a **utility class** (`java.util.Collections`) with static helper methods like `sort`, `reverse`, `shuffle`, `min`, `max`, `unmodifiableList`, `synchronizedList`, `emptyList`, `singletonList`.

Similarly, `Arrays` is a utility class for arrays (`Arrays.sort`, `Arrays.asList`, etc.).

---

## Q8. What are generic wildcards `?`, `? extends T`, `? super T`?

Used in method parameters and return types for flexibility:

- `List<?>` — list of unknown type. Can read as `Object`, cannot add (except `null`).
- `List<? extends Number>` — list of `Number` or any subtype. Can read as `Number`, **cannot add**.
- `List<? super Integer>` — list of `Integer` or any supertype. **Can add** `Integer`, can read only as `Object`.

**PECS** — **P**roducer **E**xtends, **C**onsumer **S**uper.
- If you only **read** (produce values) from a generic structure, use `? extends`.
- If you only **write** (consume values) into it, use `? super`.

---

## Q9. What is the difference between `Arrays.asList()` and `List.of()`?

| Feature | `Arrays.asList(...)` | `List.of(...)` (Java 9+) |
|---|---|---|
| Returns | Fixed-size backed by array | Truly immutable |
| `add` / `remove` | UnsupportedOperationException | UnsupportedOperationException |
| `set(i, v)` | **Allowed** (writes to backing array) | UnsupportedOperationException |
| `null` elements | Allowed | **Not allowed** (NPE) |
| Implementation | `Arrays$ArrayList` (inner class, NOT `java.util.ArrayList`) | `ImmutableCollections$ListN` |

```java
List<Integer> a = Arrays.asList(1, 2, 3);
a.set(0, 99);       // OK
a.add(4);           // UnsupportedOperationException

List<Integer> b = List.of(1, 2, 3);
b.set(0, 99);       // UnsupportedOperationException
```

---

## Q10. What are the time complexities of common operations?

| Operation | ArrayList | LinkedList | HashMap | TreeMap | HashSet | TreeSet |
|---|---|---|---|---|---|---|
| `add` / `put` | O(1) amortized | O(1) head/tail | O(1) avg, O(log n) since Java 8 if treeified | O(log n) | O(1) avg | O(log n) |
| `remove` by index | O(n) | O(n) | — | — | — | — |
| `remove` by value | O(n) | O(n) | O(1) avg | O(log n) | O(1) avg | O(log n) |
| `get` / `contains` | O(1) / O(n) | O(n) / O(n) | O(1) avg | O(log n) | O(1) avg | O(log n) |
| Iteration | O(n) | O(n) | O(n + capacity) | O(n) | O(n + capacity) | O(n) |