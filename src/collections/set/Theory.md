# Collections — Set: Theory

---

## Q1. What is the `Set` interface? Key properties.

`Set<E>` is a `Collection` that contains **no duplicate elements**. Mathematically, it models a set.

- No duplicates (`add` returns `false` if already present)
- Order: depends on implementation (none / insertion / sorted)
- At most one `null` element (in most impls)
- Same `Collection` methods — no positional access (`get(i)` does NOT exist)

### Sub-interfaces
- `SortedSet` — adds ordering methods (`first`, `last`, `headSet`, `tailSet`, `subSet`)
- `NavigableSet` (Java 6) — adds navigation (`floor`, `ceiling`, `lower`, `higher`, `pollFirst`, `pollLast`, `descendingSet`)

---

## Q2. Compare `HashSet`, `LinkedHashSet`, and `TreeSet`.

| Feature | `HashSet` | `LinkedHashSet` | `TreeSet` |
|---|---|---|---|
| Backing | `HashMap` | `LinkedHashMap` | `TreeMap` (red-black tree) |
| Order | None (insertion-independent) | Insertion order | Sorted (natural / Comparator) |
| `add`/`contains`/`remove` | O(1) avg | O(1) avg | O(log n) |
| Allows `null` | Yes (1) | Yes (1) | **No** (NPE on natural ordering); depends on Comparator |
| Iteration cost | O(capacity + size) | O(size) | O(size) |
| Memory | Lowest | +prev/next pointers | Tree nodes (higher) |
| Thread safe | No | No | No |

### When to use which
- **`HashSet`** — default; fastest for membership testing.
- **`LinkedHashSet`** — predictable iteration order matters (e.g., deduplicating while preserving original order).
- **`TreeSet`** — need sorted iteration, range queries, or floor/ceiling.

---

## Q3. How does `HashSet` work internally?

`HashSet<E>` is backed by a `HashMap<E, Object>`. Each element is a key whose value is a dummy `PRESENT` object.

```java
public boolean add(E e) {
    return map.put(e, PRESENT) == null;   // null means key was absent
}
public boolean contains(Object o) {
    return map.containsKey(o);
}
```

So `HashSet`'s performance, null handling, capacity, load factor, and treeification all inherit from `HashMap` — see the Map Theory for the internal details.

---

## Q4. Why must you override `equals` AND `hashCode` for set elements?

Hash-based collections use `hashCode()` to find the right bucket and `equals()` to detect a match inside the bucket.

- If you override only `equals`: two equal objects may have different hashes → land in different buckets → set contains "duplicates".
- If you override only `hashCode`: two unequal objects may share a bucket; `contains` returns false even though `hashCode` matched.

### Contract
1. Equal objects MUST have equal hash codes.
2. Unequal objects SHOULD (not must) have different hash codes — collisions allowed.
3. `hashCode()` MUST be consistent across calls if the object's state doesn't change.
4. `equals` must be reflexive, symmetric, transitive, consistent.

### Mutability hazard
Mutating a field used in `hashCode`/`equals` after the object is in a `HashSet` corrupts the set — the element becomes "lost" (its hash bucket is wrong). **Treat set elements as effectively immutable.**

---

## Q5. What is `TreeSet` — when to use it?

`TreeSet` is a `NavigableSet` backed by a **red-black tree** (self-balancing BST). All operations are O(log n).

### Powerful navigation methods
| Method | Description |
|---|---|
| `first()` / `last()` | smallest / largest |
| `floor(e)` | largest ≤ e |
| `ceiling(e)` | smallest ≥ e |
| `lower(e)` | largest < e |
| `higher(e)` | smallest > e |
| `pollFirst()` / `pollLast()` | remove and return |
| `headSet(to)` | < to |
| `tailSet(from)` | ≥ from |
| `subSet(from, to)` | [from, to) |
| `descendingSet()` | reverse view |

### Caveats
- Element type must be `Comparable` OR you must supply a `Comparator`.
- **Equality is defined by `compareTo` returning 0**, NOT by `equals`! This can break the Set contract if `compareTo` is inconsistent with `equals`. Example: `TreeSet<BigDecimal>` treats `1.0` and `1.00` as equal; `HashSet<BigDecimal>` does not.
- Does NOT accept `null` with natural ordering.

---

## Q6. What is `LinkedHashSet`?

`LinkedHashSet` extends `HashSet` but uses a `LinkedHashMap` underneath, which maintains a doubly linked list across all entries in insertion order.

- Same O(1) avg ops as `HashSet`, with slight overhead from the linked list.
- Iteration is O(size), not O(capacity) — faster than `HashSet` on sparse sets.

### Use case
Deduplicate while preserving order:
```java
List<String> deduped = new ArrayList<>(new LinkedHashSet<>(original));
```

---

## Q7. What is `EnumSet`? Why is it special?

`EnumSet<E extends Enum<E>>` is a highly optimized `Set` implementation for enum types.

- **Bitvector internally** (one `long` if ≤ 64 constants — `RegularEnumSet`; a `long[]` otherwise — `JumboEnumSet`).
- Extremely fast and compact.
- All operations are essentially bitwise → much faster than `HashSet<MyEnum>`.
- Cannot store `null`.

### Factories (no public constructor)
```java
EnumSet<Day> weekend = EnumSet.of(Day.SAT, Day.SUN);
EnumSet<Day> all     = EnumSet.allOf(Day.class);
EnumSet<Day> none    = EnumSet.noneOf(Day.class);
EnumSet<Day> weekdays= EnumSet.complementOf(weekend);
EnumSet<Day> range   = EnumSet.range(Day.MON, Day.FRI);
```

---

## Q8. What is `CopyOnWriteArraySet`?

A thread-safe `Set` backed by a `CopyOnWriteArrayList`. Every mutation copies the backing array; `add` is O(n) because it scans for an existing element.

- Use when: read-heavy and few elements (e.g., listener registry).
- Avoid when: large, write-heavy sets — every `add` is O(n) and copies the whole array.

---

## Q9. How to make a Set thread-safe?

```java
// 1) Synchronized wrapper — coarse lock, manual sync for iteration
Set<String> sync = Collections.synchronizedSet(new HashSet<>());

// 2) CopyOnWriteArraySet — fail-safe iteration, read-mostly
Set<String> cow = new CopyOnWriteArraySet<>();

// 3) ConcurrentHashMap.newKeySet() — backed by CHM, full concurrency
Set<String> chmSet = ConcurrentHashMap.newKeySet();
```

**`ConcurrentHashMap.newKeySet()`** is the modern choice for concurrent sets — non-blocking reads, fine-grained writes, no snapshot cost.

---

## Q10. Common Set interview questions / gotchas.

1. **Adding a mutable object that changes its hash after insertion** → object is "lost" in the set.
2. **`TreeSet` with inconsistent `compareTo` vs `equals`** → violates Set contract.
3. **`HashSet` does not preserve insertion order** even if it appears to for small inputs.
4. **`null` rules**: `HashSet`/`LinkedHashSet` allow one null; `TreeSet` rejects null with natural ordering; `ConcurrentHashMap.newKeySet()` rejects null.
5. **Set algebra:** `addAll` = union, `retainAll` = intersection, `removeAll` = difference.
6. **Equality between Set implementations:** two Sets are equal iff same size and same elements, regardless of impl (HashSet equals LinkedHashSet equals TreeSet if elements match).