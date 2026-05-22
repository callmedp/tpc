# Collections — Queue & Deque: Theory

---

## Q1. What is the `Queue` interface?

`Queue<E>` is a collection designed for **holding elements before processing** — typically FIFO (first-in-first-out).

### Two flavors of each operation
| Operation | Throws exception | Returns special value |
|---|---|---|
| Insert | `add(e)` (IllegalStateException on capacity overflow) | `offer(e)` (returns `false`) |
| Remove | `remove()` (NoSuchElementException if empty) | `poll()` (returns `null`) |
| Examine head | `element()` (NoSuchElementException if empty) | `peek()` (returns `null`) |

The "returns special value" forms are preferred for bounded queues because they don't throw on capacity boundaries.

---

## Q2. What is the `Deque` interface?

`Deque<E>` ("double-ended queue") supports insert/remove at both ends. It can be used as:

- **Queue (FIFO)** — `offer` (== `offerLast`), `poll` (== `pollFirst`)
- **Stack (LIFO)** — `push` (== `addFirst`), `pop` (== `removeFirst`), `peek` (== `peekFirst`)

### Methods
| Position | Throws | Returns special |
|---|---|---|
| First | `addFirst`, `removeFirst`, `getFirst` | `offerFirst`, `pollFirst`, `peekFirst` |
| Last  | `addLast`, `removeLast`, `getLast` | `offerLast`, `pollLast`, `peekLast` |

**Always prefer `ArrayDeque` over `Stack` and `LinkedList` for stack/queue use.**

---

## Q3. Compare `ArrayDeque`, `LinkedList`, `PriorityQueue`.

| Feature | `ArrayDeque` | `LinkedList` | `PriorityQueue` |
|---|---|---|---|
| Backing | Circular array | Doubly linked list | Binary heap (array) |
| Order | Insertion / LIFO | Insertion / LIFO | Priority (natural / comparator) |
| `offer`/`poll` | **O(1) amortized** | O(1) | O(log n) |
| `peek` | O(1) | O(1) | O(1) |
| `contains` | O(n) | O(n) | O(n) |
| Allows null | **No** | Yes | **No** |
| Iteration order | Insertion | Insertion | **Heap order — NOT sorted** |
| Implements | Deque | Deque + List | Queue |
| Thread safe | No | No | No |
| Use for | Stack/Deque | Rarely best | Top-K, scheduling |

---

## Q4. How does `ArrayDeque` work? Why prefer it over `Stack`?

`ArrayDeque` uses a **circular array** with `head` and `tail` indices wrapping modulo capacity. Insertion at either end is O(1) amortized (doubles on resize).

### Why prefer over `Stack`
- `Stack` is synchronized (slow) and extends `Vector` (legacy mess).
- `Stack` exposes `List` methods that don't belong on a stack.
- `ArrayDeque` is faster, unsynchronized, has a clean stack/deque API.

### Why prefer over `LinkedList` as a Queue/Deque
- No per-node allocation → less GC, better cache locality.
- Faster in benchmarks for queue/stack workloads.

```java
Deque<Integer> stack = new ArrayDeque<>();
stack.push(1); stack.push(2); stack.push(3);
stack.pop();    // 3
stack.peek();   // 2
```

---

## Q5. How does `PriorityQueue` work?

`PriorityQueue` is a **min-heap** backed by an array (no nodes).

- Default: natural order min-heap (head = smallest).
- For max-heap: `new PriorityQueue<>(Comparator.reverseOrder())`.
- `offer` and `poll` are O(log n).
- `peek` is O(1).
- **Iteration order is NOT sorted** — heap layout, not in-order. To get sorted output, `poll` until empty.
- Does NOT allow `null`.
- NOT thread-safe — use `PriorityBlockingQueue`.

### Internals
- Stored in a `Object[]` interpreted as a binary heap:
  - children of index `i` are at `2i+1` and `2i+2`
  - parent of `i` is `(i-1)/2`
- `offer`: append at end, "sift up" until heap property is restored.
- `poll`: take root, move last element to root, "sift down".

### Classic use cases
- Top-K elements (use a min-heap of size K).
- Dijkstra / A* search.
- Task schedulers prioritized by deadline.

---

## Q6. Heap-based "Top K largest" pattern.

```java
PriorityQueue<Integer> minHeap = new PriorityQueue<>();
int K = 3;
for (int x : data) {
    minHeap.offer(x);
    if (minHeap.size() > K) minHeap.poll();   // evict smallest
}
// minHeap now contains the K largest, smallest of them at head
```

---

## Q7. What are `BlockingQueue` implementations?

`BlockingQueue<E>` (`java.util.concurrent`) extends `Queue` and adds **blocking** insert/remove operations — used in producer-consumer pipelines.

### Operations matrix
| | Throws | Special value | Blocks | Times out |
|---|---|---|---|---|
| Insert | `add(e)` | `offer(e)` | `put(e)` | `offer(e, timeout, unit)` |
| Remove | `remove()` | `poll()` | `take()` | `poll(timeout, unit)` |
| Examine | `element()` | `peek()` | — | — |

### Implementations
| Class | Backing | Bounded | Order | Notable |
|---|---|---|---|---|
| `ArrayBlockingQueue` | Array | **Bounded** (fixed at construction) | FIFO | Single lock, optional fairness |
| `LinkedBlockingQueue` | Linked nodes | Optionally bounded (default `Integer.MAX_VALUE`) | FIFO | Two locks (put/take) → higher throughput |
| `PriorityBlockingQueue` | Heap | Unbounded | Priority | `put` never blocks; `take` blocks if empty |
| `DelayQueue<T extends Delayed>` | Heap | Unbounded | By delay | Elements only become takeable when their delay expires |
| `SynchronousQueue` | None | Capacity 0 | — | Direct hand-off — each `put` waits for a matching `take` |
| `LinkedTransferQueue` | Linked | Unbounded | FIFO | `transfer(e)` blocks until consumer takes it |
| `LinkedBlockingDeque` | Linked | Optionally bounded | FIFO/LIFO | Double-ended blocking |

### Classic producer-consumer
```java
BlockingQueue<Task> q = new ArrayBlockingQueue<>(100);
// producer
q.put(task);          // blocks if full
// consumer
Task t = q.take();    // blocks if empty
```

---

## Q8. Why does `BlockingQueue` reject `null`?

Because `null` is used as the **sentinel** by `poll()`/`peek()` to mean "no element available". Storing `null` would create ambiguity between "no element" and "the element is null".

This rule applies to most modern queue implementations (`ArrayDeque`, `PriorityQueue`, all `BlockingQueue`s) but NOT to `LinkedList` (legacy — does allow null).

---

## Q9. What is `SynchronousQueue`? When to use it?

A `SynchronousQueue` has **zero capacity** — every `put` must wait for a matching `take` (and vice versa). It's a direct hand-off channel.

### Use cases
- Pipelines where you want strict 1-to-1 hand-off without buffering.
- The default work queue in `Executors.newCachedThreadPool()` — new tasks immediately hand off to a free thread; if none is free, a new thread is created.

---

## Q10. Common Queue interview pitfalls.

1. **Iterating a `PriorityQueue` does not produce sorted order.** Must `poll()` repeatedly.
2. **`null` rejected** by `ArrayDeque`, `PriorityQueue`, all `BlockingQueue`s.
3. **`Queue.add` vs `offer`**: prefer `offer` for bounded queues to avoid exceptions.
4. **`Stack`/`Vector`** are legacy — use `ArrayDeque`.
5. **`PriorityQueue.remove(Object)`** is O(n) because it must scan the heap.
6. **`BlockingQueue.take()` blocks indefinitely** — handle interruption.