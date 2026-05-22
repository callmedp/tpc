# Collections — Sorting: Comparable & Comparator

---

## Q1. What is `Comparable<T>`?

`Comparable<T>` defines the **natural ordering** of a class. A class implements it to say "instances of me can be sorted by default."

```java
public interface Comparable<T> {
    int compareTo(T other);
}
```

### Contract
- Returns negative if `this < other`, zero if equal, positive if `this > other`.
- Must be reflexive: `x.compareTo(x) == 0`.
- Must be antisymmetric: `sgn(x.compareTo(y)) == -sgn(y.compareTo(x))`.
- Must be transitive: `x>y && y>z ⇒ x>z`.
- **Strongly recommended**: consistent with `equals` (`x.compareTo(y) == 0` iff `x.equals(y)`). Otherwise `TreeSet`/`TreeMap` will violate the Set/Map contract.

### Example
```java
class Employee implements Comparable<Employee> {
    String name; int salary;
    @Override public int compareTo(Employee o) {
        return Integer.compare(this.salary, o.salary);   // ascending by salary
    }
}
Collections.sort(employees);   // uses Comparable
```

---

## Q2. What is `Comparator<T>`?

`Comparator<T>` is an **external** ordering — used when:
- The class isn't `Comparable`.
- You want a different order than the natural one.
- You want multiple orderings.

```java
@FunctionalInterface
public interface Comparator<T> {
    int compare(T a, T b);
}
```

### Use
```java
Collections.sort(employees, (a, b) -> a.name.compareTo(b.name));        // by name
employees.sort(Comparator.comparing(Employee::getName));                 // factory
```

---

## Q3. `Comparable` vs `Comparator` — when to use which?

| Aspect | `Comparable` | `Comparator` |
|---|---|---|
| Where defined | Inside the class | Outside (lambda / class) |
| Modifies class? | Yes | No |
| Single ordering | One natural | Many possible |
| Method | `compareTo(other)` | `compare(a, b)` |
| Default for | `Collections.sort(list)`, `TreeSet`, `Arrays.sort` | passed explicitly |
| Functional interface | No (has equals contract) | Yes |

**Rule of thumb:** Use `Comparable` for "the obvious ordering" (e.g., name, id, timestamp). Use `Comparator` for ad-hoc or multiple orderings.

---

## Q4. Why `Integer.compare(a, b)` instead of `a - b`?

`a - b` overflows on extreme inputs:
```java
Integer.MIN_VALUE - 1   // overflows to Integer.MAX_VALUE  → wrong sign
```
`Integer.compare(a, b)` (and `Long.compare`, `Double.compare`) handles this safely.

Same for `Comparator.comparingInt(...)` — use the typed factories.

---

## Q5. Java 8 `Comparator` factories and combinators.

```java
// Single-key
Comparator<Emp> byName  = Comparator.comparing(Emp::getName);
Comparator<Emp> bySal   = Comparator.comparingInt(Emp::getSalary);
Comparator<Emp> byHire  = Comparator.comparing(Emp::getHireDate);

// Reverse
Comparator<Emp> bySalDesc = byName.reversed();
Comparator<Emp> natDesc   = Comparator.reverseOrder();

// Multi-key (thenComparing)
Comparator<Emp> byDeptThenSalDesc =
    Comparator.comparing(Emp::getDept)
              .thenComparing(Emp::getSalary, Comparator.reverseOrder());

// Null handling
Comparator<String> nullsFirst = Comparator.nullsFirst(Comparator.naturalOrder());
Comparator<String> nullsLast  = Comparator.nullsLast(Comparator.naturalOrder());

// Natural / reverse
Comparator.<Integer>naturalOrder();
Comparator.<Integer>reverseOrder();
```

---

## Q6. Sorting APIs in Java.

```java
// In-place on List (preferred, Java 8+)
list.sort(comparator);
list.sort(null);                       // natural order

// Static utility (works on List, returns void)
Collections.sort(list);                // natural
Collections.sort(list, comparator);

// Array sorting
Arrays.sort(arr);                      // dual-pivot quicksort for primitives
Arrays.sort(objArr);                   // TimSort for objects, stable
Arrays.sort(arr, fromIdx, toIdx);      // range sort
Arrays.parallelSort(arr);              // Fork/Join, parallel

// Stream
list.stream().sorted().collect(Collectors.toList());
list.stream().sorted(comparator).collect(Collectors.toList());
```

### Algorithms used
- **Primitives**: Dual-Pivot Quicksort (Vladimir Yaroslavskiy) — fast, NOT stable, average O(n log n).
- **Objects**: TimSort — adaptive merge sort, **stable**, O(n log n) worst-case, O(n) on already-sorted input.

---

## Q7. What is **stable** sorting? Why does it matter?

A sort is **stable** if elements with equal keys retain their original relative order.

```java
// Sort employees by department, but preserve original order within a department
emps.sort(Comparator.comparing(Emp::getDept));
```

Stable sorts let you sort by multiple keys via successive passes (sort by least-significant key first, then by more significant). With `Comparator.thenComparing`, you usually don't need this trick.

- `Arrays.sort(Object[])` — stable (TimSort).
- `Arrays.sort(int[])` — NOT stable (dual-pivot quicksort, but stability is moot for primitives — equal primitives are indistinguishable).
- `Collections.sort` — stable.
- `Stream.sorted()` — stable.

---

## Q8. `Collections.sort(list)` vs `list.sort(null)`?

Both behave identically (sort by natural order), but `list.sort(null)` is preferred since Java 8 — it's a method on `List`, avoiding an external utility call.

`Collections.sort` internally copies to an array, sorts, then writes back. `list.sort(null)` on `ArrayList` works directly on the backing array.

---

## Q9. What is "consistent with equals"?

A `Comparable` or `Comparator` is **consistent with equals** if `compare(a, b) == 0` ⇔ `a.equals(b)`.

`TreeSet`/`TreeMap` use the **comparator/comparable** for equality, ignoring `equals`. Inconsistency causes Set/Map contract violation:

```java
SortedSet<BigDecimal> s = new TreeSet<>();
s.add(new BigDecimal("1.0"));
s.add(new BigDecimal("1.00"));     // SAME by compareTo, DIFFERENT by equals
System.out.println(s.size());      // 1 — violates Set contract for HashSet equivalence
```

If you must use such keys in a TreeMap/TreeSet, supply a Comparator that aligns with the equality you want.

---

## Q10. Common interview gotchas

1. **`a - b` overflow** — use `Integer.compare`.
2. **`null` in stream `sorted()`** — NPE; use `Comparator.nullsFirst/Last`.
3. **`compareTo` returning `int` from `Long.compare` overflow** — use `Long.compare`, not `(int)(a - b)`.
4. **Mutating sort key after insertion in `TreeSet`/`TreeMap`** — same hazard as mutating `equals/hashCode` keys in `HashMap`.
5. **Stable vs unstable for primitives** — irrelevant; primitives have no other state.
6. **`Comparator.comparing(Emp::getName)` calls `getName` repeatedly** — for expensive keys, sort by precomputed pairs or cache the key.