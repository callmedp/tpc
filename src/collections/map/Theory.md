# Collections — Map: Theory

> Maps are the #1 interview topic in Java collections. HashMap internals come up almost every interview.

---

## Q1. What is the `Map` interface? Why is it not a `Collection`?

`Map<K, V>` stores **key → value** associations. Keys are unique; each key maps to exactly one value.

It is **not a `Collection`** because the abstraction is fundamentally different: a Collection stores single elements, a Map stores pairs. However, you can view a Map as three Collections via:
- `keySet()` — `Set<K>` of keys
- `values()` — `Collection<V>` of values
- `entrySet()` — `Set<Map.Entry<K,V>>` of pairs

### Core methods
| Method | Description |
|---|---|
| `V put(K, V)` | Insert/replace; returns old value or null |
| `V get(Object)` | Look up; returns null if absent |
| `V remove(Object)` | Remove; returns old value or null |
| `boolean containsKey(Object)` | Key exists |
| `boolean containsValue(Object)` | Value exists (O(n)) |
| `int size()` / `boolean isEmpty()` / `void clear()` | — |
| `Set<K> keySet()` | View of keys (live) |
| `Collection<V> values()` | View of values (live) |
| `Set<Map.Entry<K,V>> entrySet()` | View of entries (live) |
| `default V getOrDefault(k, def)` | Java 8 |
| `default V putIfAbsent(k, v)` | Java 8 |
| `default V computeIfAbsent(k, fn)` | Java 8 — compute and insert if absent |
| `default V computeIfPresent(k, bifn)` | Java 8 |
| `default V compute(k, bifn)` | Java 8 |
| `default V merge(k, v, bifn)` | Java 8 — insert v, or combine with existing via bifn |
| `default void forEach(BiConsumer)` | Java 8 |
| `default void replaceAll(BiFunction)` | Java 8 |

---

## Q2. How does `HashMap` work internally?

`HashMap` is the most-asked Java interview topic. Know it cold.

### Data structure
- Backing: `Node<K,V>[] table` (array of buckets), initial capacity 16.
- Each bucket holds either:
  - a **linked list** of `Node<K,V>` (since Java 1.2)
  - or a **red-black tree** of `TreeNode<K,V>` (since Java 8) once a bucket has ≥ 8 nodes AND the table has ≥ 64 capacity.

### `put(K key, V value)` step by step
1. Compute `hash = key.hashCode() ^ (hashCode >>> 16)` (spreads high bits to low bits to reduce collisions in small tables).
2. `index = (capacity - 1) & hash` (cheap modulo because capacity is power of 2).
3. If `table[index] == null` → place a new Node.
4. Else, walk the bucket's chain. If a node with `hash == h && (k == key || k.equals(key))` exists, **update** its value and return the old one.
5. Otherwise append a new node.
6. After appending, if bucket length ≥ `TREEIFY_THRESHOLD` (8) and table length ≥ `MIN_TREEIFY_CAPACITY` (64), **treeify** the bucket. If table is smaller, **resize** instead.
7. `++size`. If `size > capacity * loadFactor` (default 0.75) → **resize** (double capacity, rehash all entries).

### `get(K key)`
Same hash computation → bucket → walk chain or tree → return value if found.

### Time complexity
- Average: O(1) for put/get/remove.
- Worst case before Java 8: O(n) (all keys collide into one bucket).
- Worst case from Java 8 onward: O(log n) (treeified bucket).

### Why power-of-2 capacity?
- Lets `index = (cap - 1) & hash` replace modulo with a single bitwise AND — much faster.

### Why load factor 0.75?
- Bell-curve-ish trade-off between space and time. Lower means more memory but fewer collisions; higher means denser table but more chain walks. 0.75 is empirically a good default.

### When does resize happen?
- When `size > threshold` (= `capacity * loadFactor`).
- Or during the "treeify but table is too small" path.
- Resize **doubles** the capacity. Each entry moves to either its old bucket index or `oldIndex + oldCapacity` (because the new top bit of `hash & (newCap-1)` is the only new bit).

### Treeification thresholds (Java 8+)
| Constant | Value | Meaning |
|---|---|---|
| `TREEIFY_THRESHOLD` | 8 | Bucket length above which chain → tree |
| `UNTREEIFY_THRESHOLD` | 6 | Tree size below which tree → chain (on resize) |
| `MIN_TREEIFY_CAPACITY` | 64 | Min table capacity before treeification (else resize) |

Why 8? Probabilistically, with a good hash function and load factor 0.75, the chance of a bucket reaching length 8 is < 1 in 10 million. So treeification kicks in only when hashing is bad (e.g., adversarial keys).

---

## Q3. Why must `HashMap` keys override `equals` and `hashCode`?

Because lookup is:
1. Hash the key → bucket index.
2. Walk the bucket; find an existing node where `node.hash == h && (node.key == key || node.key.equals(key))`.

If two equal keys produce different hashes, they land in different buckets → "duplicate" keys.
If two unequal keys share a hash, no problem — they coexist in the same bucket.

### Equals/hashCode contract recap
1. Equal objects MUST have equal hash codes.
2. Unequal objects SHOULD have different hash codes (collisions allowed but degrade performance).
3. `hashCode()` consistent across calls as long as state used in equals doesn't change.

### Mutability rule
Don't mutate fields used in `hashCode`/`equals` after putting the key in a map. The entry will be "lost" — `get` returns null, `containsKey` returns false, but iteration still yields the old entry.

---

## Q4. HashMap allows null keys/values?

| Map | null key | null value |
|---|---|---|
| `HashMap` | **1 allowed** | Yes |
| `LinkedHashMap` | **1 allowed** | Yes |
| `TreeMap` | No (natural order) | Yes |
| `Hashtable` | **No** (NPE) | **No** (NPE) |
| `ConcurrentHashMap` | **No** (NPE) | **No** (NPE) |

The null key in `HashMap` is always stored at index 0 (special case: `hash` of null is 0).

---

## Q5. Why is `Hashtable` deprecated in practice?

- Every method synchronized → coarse contention.
- Inherits from old `Dictionary` class.
- Allows neither null keys nor null values.
- For thread safety, use `ConcurrentHashMap`. For single-threaded, use `HashMap`.

---

## Q6. How is `ConcurrentHashMap` different from `Hashtable`?

| Feature | `Hashtable` | `ConcurrentHashMap` (Java 8+) |
|---|---|---|
| Locking | Synchronized whole table | CAS + per-bin (bucket) `synchronized` |
| Read | Locked | **Lock-free** (volatile reads) |
| Write throughput | Low (one writer at a time) | High (multiple bins can be written concurrently) |
| Iterator | Fail-fast (CME) | **Weakly consistent** — never throws CME; may reflect concurrent updates or not |
| Null keys/values | Disallowed | Disallowed |
| `size()` | Exact, locked | Estimate (counters, lock-free) — exact via `mappingCount()` |

### Why ConcurrentHashMap disallows null
With null values, `map.get(k) == null` is ambiguous: "key absent" or "key present with null value"? Single-threaded callers can disambiguate with `containsKey`, but in a concurrent map the state could change between the two calls. So null is forbidden by contract.

### Internals (Java 8 rewrite)
- Pre-Java 8: striped locking with `Segment` (16 segments).
- Java 8+: **no segments**. Same `Node[]` layout as HashMap. Mutation locks the first node of the bucket using `synchronized`. Reads use volatile loads.
- Treeification just like `HashMap`.
- Atomic ops: `putIfAbsent`, `compute`, `merge`, `computeIfAbsent` are all atomic per-key.

---

## Q7. What is `LinkedHashMap`?

`LinkedHashMap` extends `HashMap` and adds a **doubly linked list** across all entries.

- Default iteration: **insertion order**.
- Optionally **access order** via constructor flag — entries move to the tail when accessed (via `get`/`put`).
- O(1) put/get/remove like HashMap, with slight overhead for list maintenance.

### Famous use case: LRU cache
```java
new LinkedHashMap<K,V>(16, 0.75f, true) {       // access-order
    @Override
    protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
        return size() > MAX_ENTRIES;
    }
};
```

`removeEldestEntry` is called after every `put`/`putAll`. Return true to evict the head (least recently used in access-order, oldest insertion in insertion-order).

---

## Q8. What is `TreeMap`?

`TreeMap` is a `NavigableMap` backed by a **red-black tree**. All ops O(log n).

### Navigation
- `firstKey()`, `lastKey()`, `firstEntry()`, `lastEntry()`
- `floorKey(k)`, `ceilingKey(k)`, `lowerKey(k)`, `higherKey(k)`
- `pollFirstEntry()`, `pollLastEntry()`
- `headMap(to)`, `tailMap(from)`, `subMap(from, to)` — live views
- `descendingMap()`

### Caveats
- Keys must be `Comparable` OR a `Comparator` provided.
- **Key equality is `compareTo == 0`**, not `equals` — same trap as `TreeSet`.
- Does not allow `null` keys with natural ordering.

---

## Q9. What is `WeakHashMap`?

A `WeakHashMap` holds **weak references** to its keys. When a key has no other strong references, the GC may reclaim it, and the entry disappears from the map on the next access (cleaned via a `ReferenceQueue`).

### Use case
- Caches keyed on objects whose lifecycle you don't control (e.g., per-instance metadata).

### Caveat
- Values hold **strong** references. If a value strongly references its key, the key is never collectible. Use `WeakHashMap<K, WeakReference<V>>` if needed.
- Not thread-safe.

---

## Q10. What is `IdentityHashMap`?

Uses `==` (reference equality) and `System.identityHashCode(k)` instead of `equals`/`hashCode`. Two distinct String objects with the same content are different keys.

### Use cases
- Graph traversal / cycle detection (treat each object as identity).
- Serialization frameworks tracking "seen" objects.
- Topology / object graph metadata.

---

## Q11. What is `EnumMap`?

Highly specialized `Map` whose keys are an enum type. Backed by an array indexed by `ordinal()`.

- Extremely fast and compact.
- Maintains natural enum order (declaration order) during iteration.
- Rejects null keys.

```java
EnumMap<Day, Integer> hours = new EnumMap<>(Day.class);
hours.put(Day.MON, 8);
```

---

## Q12. What is `Properties`?

A legacy `Hashtable<Object, Object>` subclass intended for string key/value config (Java property files). Avoid for new code — use a typed `Map<String, String>`.

---

## Q13. Java 8 default methods on `Map`.

```java
// getOrDefault — avoids contains+get pattern
int count = map.getOrDefault(key, 0);

// putIfAbsent — atomic on ConcurrentHashMap
map.putIfAbsent(key, value);

// computeIfAbsent — lazy init pattern
map.computeIfAbsent(key, k -> new ArrayList<>()).add(item);

// computeIfPresent — update only if existing
map.computeIfPresent(key, (k, v) -> v + 1);

// compute — full control (insert / update / remove if null returned)
map.compute(key, (k, v) -> v == null ? 1 : v + 1);

// merge — counter pattern, atomic on CHM
map.merge(key, 1, Integer::sum);

// forEach
map.forEach((k, v) -> System.out.println(k + "=" + v));

// replaceAll
map.replaceAll((k, v) -> v.toUpperCase());
```

`computeIfAbsent` + `merge` are the workhorses of group-by and counter patterns.

---

## Q14. Iteration patterns over Map.

```java
// 1) entrySet — preferred; one lookup per pair
for (Map.Entry<K,V> e : map.entrySet()) { e.getKey(); e.getValue(); }

// 2) keySet then get — two lookups (worse)
for (K k : map.keySet()) { V v = map.get(k); }

// 3) values only
for (V v : map.values()) { ... }

// 4) Java 8 forEach
map.forEach((k, v) -> { ... });

// 5) Stream
map.entrySet().stream().filter(...).forEach(...);
```

---

## Q15. How would you implement an LRU cache?

```java
class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;
    LRUCache(int capacity) {
        super(capacity, 0.75f, true);   // access-order = true
        this.capacity = capacity;
    }
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }
}
```

For concurrent LRU, consider Caffeine, Guava `Cache`, or `ConcurrentLinkedHashMap`.

---

## Q16. Quick comparison: all Map implementations.

| Class | Order | Thread safe | null K | null V | Notes |
|---|---|---|---|---|---|
| `HashMap` | None | No | 1 | Yes | Default |
| `LinkedHashMap` | Insertion / Access | No | 1 | Yes | LRU |
| `TreeMap` | Sorted | No | No (natural) | Yes | NavigableMap |
| `Hashtable` | None | Yes (sync methods) | No | No | Legacy |
| `ConcurrentHashMap` | None | Yes (CAS + sync per bin) | No | No | Modern concurrent |
| `WeakHashMap` | None | No | 1 | Yes | GC reclaims keys |
| `IdentityHashMap` | None | No | 1 | Yes | `==`, not `equals` |
| `EnumMap` | Enum order | No | No | Yes | Array-backed, super fast |

---

## Q17. Common Map interview gotchas

1. **Wrong `hashCode`** → keys "lost" or "duplicated".
2. **Mutating keys after insertion** corrupts the map.
3. **`putIfAbsent(k, expensive())` still evaluates `expensive()`** even if k present; use `computeIfAbsent`.
4. **`computeIfAbsent` is not idempotent under concurrency on HashMap**, but is **atomic** on `ConcurrentHashMap`.
5. **`size()` on `ConcurrentHashMap` is approximate** during concurrent mutation (use `mappingCount`).
6. **`TreeMap.compareTo` and `equals` mismatch** breaks Map contract.
7. **Removing during iteration** — only via `iterator.remove()` (entrySet iterator); `map.remove` mid-iteration → CME (HashMap/TreeMap/LinkedHashMap).
8. **`HashMap` is NOT thread-safe** — under concurrent put without synchronization, you can get infinite loops (pre-Java 8) or data corruption (Java 8+).