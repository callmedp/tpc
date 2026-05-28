# Questions

## multithreading.basics
1. What is multithreading?
2. What is Runnable?
3. What is ExecutorService and how is it different from Runnable?
4. What is Future?
5. What does Thread.join() do?
6. What are the different types of ExecutorService and their use cases?
7. What is shutdown() and what is awaitTermination()?
8. What is Thread.sleep()?
9. How does synchronized prevent race conditions with multiple threads?
10. Is synchronized always used with static?
11. Give real-life code examples for instance-level and class-level synchronization.
12. What is Callable and how is it different from Runnable?
13. Real-world code example showing the difference between Callable and Runnable.
14. How is CompletableFuture different from Future?
15. Real-world code covering all CompletableFuture methods: supplyAsync, runAsync, thenApply, thenAccept, thenRun, thenCompose, thenCombine, exceptionally, handle, whenComplete, complete, completeExceptionally, allOf, anyOf.
16. Does CompletableFuture spawn a new thread for callbacks?
17. CompletableFuture chains cause callback hell — is there an async/await equivalent in Java?
18. What is the difference between scheduleAtFixedRate and scheduleAtFixedDelay? Real-world code for all ScheduledExecutorService methods.
19. Write a real-life use case for the Fork/Join framework using RecursiveTask and RecursiveAction.
20. How do you achieve thread safety in collections? Explain the difference between Synchronized collections and Concurrent collections with real-world code examples.

## collections.overview
1. What is the Java Collections Framework?
2. Draw the Collections Framework hierarchy.
3. What is the `Collection` interface? List its core methods.
4. What is `Iterable` and how does the enhanced `for-each` loop work?
5. What is the difference between `Iterator` and `ListIterator`?
6. What is fail-fast vs fail-safe iteration?
7. What's the difference between `Collection` and `Collections`?
8. What are generic wildcards `?`, `? extends T`, `? super T`?
9. What is the difference between `Arrays.asList()` and `List.of()`?
10. What are the time complexities of common operations across collections?

## collections.list
1. What is the `List` interface?
2. ArrayList vs LinkedList — when to use which?
3. Why does `ArrayList` grow by 1.5x (not 2x)?
4. What is the `RandomAccess` marker interface?
5. What is the `subList()` trap?
6. What is `Vector` and `Stack`? Why avoid them?
7. What is `CopyOnWriteArrayList`?
8. How to make an `ArrayList` thread-safe? Three options.
9. List immutability — three flavors.
10. How to convert between Array and List?
11. Why is indexed `for` faster than for-each on ArrayList?
12. Common ArrayList interview pitfalls.

## collections.set
1. What is the `Set` interface? Key properties.
2. Compare `HashSet`, `LinkedHashSet`, and `TreeSet`.
3. How does `HashSet` work internally?
4. Why must you override `equals` AND `hashCode` for set elements?
5. What is `TreeSet` — when to use it?
6. What is `LinkedHashSet`?
7. What is `EnumSet`? Why is it special?
8. What is `CopyOnWriteArraySet`?
9. How to make a Set thread-safe?
10. Common Set interview questions / gotchas.

## collections.queue
1. What is the `Queue` interface?
2. What is the `Deque` interface?
3. Compare `ArrayDeque`, `LinkedList`, `PriorityQueue`.
4. How does `ArrayDeque` work? Why prefer it over `Stack`?
5. How does `PriorityQueue` work?
6. Heap-based "Top K largest" pattern.
7. What are `BlockingQueue` implementations?
8. Why does `BlockingQueue` reject `null`?
9. What is `SynchronousQueue`? When to use it?
10. Common Queue interview pitfalls.

## collections.map
1. What is the `Map` interface? Why is it not a `Collection`?
2. How does `HashMap` work internally?
3. Why must `HashMap` keys override `equals` and `hashCode`?
4. Does HashMap allow null keys/values? (across all Map impls)
5. Why is `Hashtable` deprecated in practice?
6. How is `ConcurrentHashMap` different from `Hashtable`?
7. What is `LinkedHashMap`?
8. What is `TreeMap`?
9. What is `WeakHashMap`?
10. What is `IdentityHashMap`?
11. What is `EnumMap`?
12. What is `Properties`?
13. Java 8 default methods on `Map`.
14. Iteration patterns over Map.
15. How would you implement an LRU cache?
16. Quick comparison of all Map implementations.
17. Common Map interview gotchas.

## collections.sorting
1. What is `Comparable<T>`?
2. What is `Comparator<T>`?
3. `Comparable` vs `Comparator` — when to use which?
4. Why `Integer.compare(a, b)` instead of `a - b`?
5. Java 8 `Comparator` factories and combinators.
6. Sorting APIs in Java (List/Arrays/Stream).
7. What is **stable** sorting? Why does it matter?
8. `Collections.sort(list)` vs `list.sort(null)`?
9. What is "consistent with equals"?
10. Common sorting interview gotchas.

## collections.utilities
1. What is the `Collections` utility class?
2. What is the `Arrays` utility class?
3. `Collections.emptyList()` vs `new ArrayList<>()` vs `List.of()`.
4. `unmodifiableList` vs `List.copyOf` vs `List.of`.
5. Common interview gotchas for `Collections`/`Arrays`.

## collections.dsa
1. Which Java collection methods are most commonly used in DSA interviews at top product companies (Google, Microsoft, Amazon, Meta)?
   - Course-style cheatsheet broken into 15 progressive modules under `src/collections/dsa/`.
2. Why does `concMap.merge("COUNT-" + tid, 1, Integer::sum)` fail with "incompatible types: int cannot be converted to String"? (Map<K,V>.merge type rules)

## dsa
1. Implement every DSA pattern asked at top product companies (Google, Microsoft, Amazon, Meta, Apple), module by module.
   - 28-module course under `src/dsa/`, each module a runnable Java file with template + 3–10 worked LC problems + practice set. Index at `src/dsa/Theory.md`.
   - Phases: Array/String foundations → Linear data structures → Trees → Graphs → Recursion/Bits → DP (8 modules) → Greedy.

## kafka.basics
1. What is Apache Kafka and what problem does it solve?
2. What are the core components of Kafka? (Producer, Consumer, Broker, Topic, Partition, ZooKeeper/KRaft)
3. What is a Kafka Topic and a Partition? How does ordering work?
4. What is a Kafka Offset?
5. What is a Consumer Group? How does partition assignment work?
6. What is Replication and the ISR (In-Sync Replicas)?
7. What are Producer acknowledgments: acks=0, acks=1, acks=all?
8. What are Delivery Semantics: at-most-once, at-least-once, exactly-once?
9. How does Kafka store data on disk? (Log segments, sequential I/O, page cache)
10. What are Kafka's retention policies? (time-based, size-based, compaction)
11. What happens during Consumer Rebalancing? What strategies exist?
12. What are the key Producer configuration parameters and tuning?
13. What are the key Consumer configuration parameters and tuning?
14. What is Consumer Lag? How do you monitor it?
15. Kafka vs traditional message queues (RabbitMQ) — comparison.
16. What are Kafka's internal topics? (__consumer_offsets, __transaction_state)
17. ZooKeeper vs KRaft mode — what changed and why?
18. What are Kafka's compression options? When to use each?
19. What is Log Compaction? How does it work in detail?
20. What is Kafka Streams? KStream, KTable, windowing types.
21. What is Kafka Connect? Source vs Sink connectors.
22. What is Schema Evolution with Avro + Schema Registry?
23. Kafka High-Availability patterns — partition sizing, MirrorMaker 2, rack awareness.
24. How does Exactly-Once Semantics (EOS) work end-to-end?
25. Common Kafka interview gotchas.

## indexing.deep-dive
1. What is a database index? Core trade-offs.
2. How does a B-Tree index work? How is B+ Tree different?
3. What is a Clustered vs Non-Clustered index?
4. What is a Covering Index? What is an index-only scan?
5. What is a Composite Index and the Left-Prefix Rule?
6. What is Index Cardinality and Selectivity?
7. How does a Hash Index work? When to use it?
8. What is a Bitmap Index? When is it used?
9. What is a Full-Text Index? How does an Inverted Index work?
10. What is a Partial Index?
11. What is an Expression / Functional Index?
12. PostgreSQL indexing internals: heap, HOT updates, MVCC, visibility map, index types.
13. MySQL InnoDB indexing internals: clustered PK, secondary index double-lookup, UUID PK problem.
14. How to read EXPLAIN / EXPLAIN ANALYZE output?
15. What is an LSM Tree? How does it differ from B-Tree?
16. How does indexing work in NoSQL? (MongoDB, Elasticsearch, Cassandra)
17. OLTP vs OLAP indexing strategies — column stores and zone maps.
18. How does the query optimizer choose which index to use?
19. What is index maintenance overhead? Index bloat and fragmentation.
20. Common indexing mistakes and gotchas.